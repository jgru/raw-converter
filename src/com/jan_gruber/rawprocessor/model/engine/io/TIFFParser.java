package com.jan_gruber.rawprocessor.model.engine.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.jan_gruber.rawprocessor.model.engine.store.TIFFField;
import com.spinn3r.log5j.Logger;


public class TIFFParser {
	private static final Logger LOGGER = Logger.getLogger();
	protected ImageInputStream mStream;
	protected TIFFTagParser mTagParser;

	public TIFFParser(ImageInputStream mStream) {
		this.mStream = mStream;
		this.mTagParser = new TIFFTagParser();
	}

	final int LITTLE_ENDIAN = 73; // hex 49
	final int BIG_ENDIAN = 77; // hex 4d

	protected boolean checkHeader(RAWImageInputStreamImpl rawStream)
			throws IOException {
		rawStream.seek(rawStream.getBaseOffset());

		int endianOne = rawStream.readByte();
		LOGGER.info("Endian One: "+ endianOne);
		int endianTwo = rawStream.readByte();
		LOGGER.info("Endian Two: "+ endianTwo);
		if (endianOne != endianTwo) {
			LOGGER.warn("Unvalid file");
		}
		boolean isLittleEndian = (endianOne == LITTLE_ENDIAN)
				&& (endianTwo == LITTLE_ENDIAN);

		// check magic number (42)
		int formatId = rawStream.readUnsignedShort();
		LOGGER.info("Format-Id: "+ formatId);
		
		return isLittleEndian;
	}

	protected void setByteOrder(RAWImageInputStreamImpl rawStream,
			boolean isLittleEndian) {
		if (isLittleEndian)
			rawStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		else
			rawStream.setByteOrder(ByteOrder.BIG_ENDIAN);

	}

	// detailed information:
	// http://www.media.mit.edu/pia/Research/deepview/exif.html
	protected long parseHeader(ImageInputStream mStream) throws IOException {
		mStream.seek(0);
		mStream.skipBytes(4);
		long offset = mStream.readUnsignedInt();
		LOGGER.info("Offset to first IFD: %d", offset);
		return offset;

	}

	protected ArrayList<IFD> retrieveIFDs(ImageInputStream mStream)
			throws IOException {
		int counter = 0;
		ArrayList<IFD> ifdList = new ArrayList<IFD>();
		// read the offset to the first IFD
		long offset = parseHeader(mStream);
		// add the primary IFD
		ifdList.add(retrieveIFD(mStream, offset, counter));

		while (ifdList.get(counter).offsetToNextIFD > 0) {
			ifdList.add(retrieveIFD(mStream,
					ifdList.get(counter).offsetToNextIFD, counter));
			
			counter++;
		}
		LOGGER.info("%d IFDs found", ifdList.size());

		return ifdList;
	}

	protected IFD retrieveIFD(ImageInputStream mStream, long offset, int id)
			throws IOException {
		LOGGER.info("retrieveIFD()");
		mStream.seek(offset);
		// read number of entries
		int entryCount = mStream.readUnsignedShort();
		IFD mIFD = new IFD(offset, entryCount, id);
		LOGGER.info("Number of entries: %d", entryCount);

		// read all entries
		while (entryCount > 0) {
			// add them to the IFD's list of tags-
			TIFFField tagToAdd = mTagParser.parseTIFFTag(mStream);
			mIFD.addTag(tagToAdd);
			
			LOGGER.info("Tag: " + tagToAdd.getCode() + " count: "+ tagToAdd.getComponentCount()+" datatype:"+ tagToAdd.getDataFormat());
			
			//LOGGER.info("mStream: " + mStream.getStreamPosition());
			checkForSubDirectories(mStream, mIFD, tagToAdd);
			entryCount--;
		}
		// read the offset to the next IFD
		long offsetToNextIFD = mStream.readUnsignedInt();
		LOGGER.info("Offset to next IFD: %d", offsetToNextIFD);
		if (offsetToNextIFD != 0)
			mIFD.setOffsetToNextIFD(offsetToNextIFD);

		mIFD.offsetToPixelData = mStream.getStreamPosition();
		return mIFD;
	}

	protected void checkForSubDirectories(ImageInputStream mStream2, IFD mIFD,
			TIFFField tagToAdd) throws IOException {

		if (tagToAdd.getCode() == 34665) {
			LOGGER.info("Subdirectory #%d found", tagToAdd.getCode());
			mIFD.setHasSubDirectory(true);
			mIFD.setHasExifDir(true);
			int offsetToSubDir = (Integer) tagToAdd.getValue();
			mIFD.setOffsetToSubDirectory(offsetToSubDir);
			mIFD.setSubDir(loadSubDirectory(mStream2, offsetToSubDir));
		}
		

	}

	protected IFD loadSubDirectory(ImageInputStream mStream, long offsetToSubDir)
			throws IOException {
		mStream.mark();
		IFD subDir = retrieveIFD(mStream, offsetToSubDir, (int) mStream.getStreamPosition());
		mStream.reset();
		return subDir;
	}

	protected BufferedImage[] retrieveThumbnails(ImageInputStream mStream,
			ArrayList<IFD> ifdList) {
		Iterator<IFD> it = ifdList.iterator();
		ArrayList<BufferedImage> thumbnailList = new ArrayList<BufferedImage>();
		while (it.hasNext()) {
			try {
				BufferedImage thumb = readThumbnail(mStream, it.next());
				if (thumb != null)
					thumbnailList.add(thumb);
			} catch (Exception e) {
				LOGGER.error("Error while loading thumbnails");
				e.printStackTrace();
			}

		}
		LOGGER.info("%d thumbnails found", thumbnailList.size());
		return thumbnailList.toArray(new BufferedImage[thumbnailList.size()]);
	}

	protected BufferedImage readThumbnail(ImageInputStream mStream, IFD mIFD)
			throws IOException {
		// indicates, that this IFD doesn't contain the tags used below
		if (mIFD.getTag(Integer.valueOf(513)) != null) {
			return loadJPEGThumbnail(mStream, mIFD);
		}

		try {
			// get the neccessary information from the tags in the IFD
			int imageWidth = (Short) mIFD.getTag(Integer.valueOf(256))
					.getValue();
			LOGGER.info("Width: %d", imageWidth);
			int imageHeight = (Short) mIFD.getTag(Integer.valueOf(257))
					.getValue();
			LOGGER.info("Height: %d", imageHeight);
			short[] bitsPerSample = (short[]) mIFD.getTag(Integer.valueOf(258))
					.getValue();
			LOGGER.info("Bits/Sample: " + bitsPerSample[1]);
			int compressionCode = (Short) mIFD.getTag(Integer.valueOf(259))
					.getValue();
			LOGGER.info("Compression Code: " + compressionCode);
			int stripOffset = (Integer) mIFD.getTag(Integer.valueOf(273))
					.getValue();
			LOGGER.info("Strip offset: " + stripOffset);
			int stripByte = (Integer) mIFD.getTag(Integer.valueOf(0x0117))
					.getValue();
			LOGGER.info("Strip byte: " + stripByte);

			if (compressionCode == 1)
				return loadUncompressedThumbnail(mStream, stripOffset,
						imageWidth, imageHeight, bitsPerSample[0]);
			else
				return loadJPEGThumbnail(mStream, stripOffset, imageWidth,
						imageHeight, bitsPerSample[0]);
		} catch (Exception e) {
			LOGGER.warn("No thumbnail found in IFD#%d", mIFD.getId());
			return null;
		}
	}

	// additional info:
	// http://turing.une.edu.au/~comp511/Tutorials/Week03/Tutorial03.html
	private BufferedImage loadUncompressedThumbnail(ImageInputStream iis,
			long offset, int imageWidth, int imageHeight, short bitsPerSample)
			throws IOException {
		final int pixelStride = 3;//FIXME read from metadata

		if (bitsPerSample == 16) {
			WritableRaster r = Raster.createInterleavedRaster(
					DataBuffer.TYPE_USHORT, imageWidth, imageHeight,
					pixelStride, null);

			iis.seek(offset);
			for (int i = 0; i < imageWidth * imageHeight * 3; i++) {
				r.getDataBuffer().setElem(i, iis.readUnsignedShort());
			}

			final ColorSpace colorSpace = ColorSpace
					.getInstance(ColorSpace.CS_LINEAR_RGB);
			final ColorModel colorModel = new ComponentColorModel(colorSpace,
					false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
			final Properties properties = new Properties();
			final BufferedImage bufferedImage = new BufferedImage(colorModel,
					r, false, properties);

			return bufferedImage;

		}
		LOGGER.warn("Not able to process %d BitsPerSample", bitsPerSample);
		return null;
	}

	private BufferedImage loadJPEGThumbnail(ImageInputStream stream,
			long offset, int imageWidth, int imageHeight, short bitsPerSample)
			throws IOException {

		LOGGER.info("loadJPEGThumbnail()");
		byte[] compressedPixelData = new byte[imageWidth * imageHeight * 3];
		stream.seek(offset);
		stream.read(compressedPixelData);

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(
				compressedPixelData));
		return image;
	}

	private BufferedImage loadJPEGThumbnail(ImageInputStream mStream, IFD mIFD)
			throws IOException {
		int thumbnailOffset = (Integer) mIFD.getTag(Integer.valueOf(513))
				.getValue();
		int thumbnailLength = (Integer) mIFD.getTag(Integer.valueOf(514))
				.getValue();
		byte[] buffer = new byte[thumbnailLength];
		mStream.seek(thumbnailOffset);
		mStream.readFully(buffer);

		return ImageIO.read(new ByteArrayInputStream(buffer));
		
	}
}
