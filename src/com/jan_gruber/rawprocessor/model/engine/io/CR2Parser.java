package com.jan_gruber.rawprocessor.model.engine.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.jan_gruber.rawprocessor.model.engine.store.CR2Metadata;
import com.jan_gruber.rawprocessor.model.engine.store.TIFFField;
import com.jan_gruber.rawprocessor.util.WritableRasterFactory;
import com.spinn3r.log5j.Logger;

public class CR2Parser extends TIFFParser {
    private static final Logger LOGGER = Logger.getLogger();

    public CR2Parser(ImageInputStream mStream) {
	super(mStream);
    }

    protected boolean checkCR2(ImageInputStream mStream) throws IOException {
	mStream.skipBytes(8);
	char c = (char) mStream.read();
	char r = (char) mStream.read();
	int majorVersion = mStream.read();
	LOGGER.info(c + " " + r + " " + majorVersion);
	mStream.seek(0);
	mStream.skipBytes(8);
	// check if byte 9 and 10 are 43 52 (ASCII CR) and byte 11=2
	if (mStream.read() == 0x43 && mStream.read() == 0x52
		&& mStream.readByte() == 2) {

	    return true;
	}
	return false;
    }

    // overrides the parent method from TIFFParser to deal with the makernote
    @Override
    protected void checkForSubDirectories(ImageInputStream mStream, IFD mIFD,
	    TIFFField tagToAdd) throws IOException {

	if (tagToAdd.getCode() == 34665) {
	    LOGGER.info("Subdirectory #%d found", tagToAdd.getCode());
	    mIFD.setHasSubDirectory(true);
	    mIFD.setHasExifDir(true);
	    int offsetToSubDir = (Integer) tagToAdd.getValue();
	    mIFD.setOffsetToSubDirectory(offsetToSubDir);
	    mIFD.setSubDir(loadSubDirectory(mStream, offsetToSubDir));
	} else if (tagToAdd.getCode() == 37500) {
	    LOGGER.info("Makernote Subdirectory #%d found", tagToAdd.getCode());
	    mStream.seek(mStream.getStreamPosition() - 12);
	    mStream.mark();
	    mStream.skipBytes(8);
	    long offsetToMakerNoteData = mStream.readUnsignedInt();
	    mStream.seek(offsetToMakerNoteData);
	    int entryCount = mStream.readUnsignedShort();
	    LOGGER.info(entryCount);

	    IFD makerNoteIFD = new IFD(offsetToMakerNoteData, entryCount);
	    mIFD.setHasSubDirectory(true);
	    mIFD.setHasMakernoteDir(true);
	    mIFD.setSubDir(makerNoteIFD);

	    while (entryCount > 0) {
		try {
		    TIFFField tag = mTagParser.parseTIFFTag(mStream);
		    LOGGER.info("Entry: " + entryCount + "; Code: "
			    + tag.getCode() + "; Type: " + tag.getDataFormat());
		    makerNoteIFD.addTag(tag);

		} catch (EOFException e) {
		    LOGGER.error(e);
		}
		entryCount--;
	    }
	    mStream.reset();
	    mStream.skipBytes(12);
	}
    }

    protected WritableRaster parseRawData(ImageInputStream mStream,
	    CR2Metadata metadata, boolean cropMaskedPixels) throws IOException {

	return decompressRawData((RAWImageInputStreamImpl) mStream, metadata,
		cropMaskedPixels);

    }

    final static int BUFFER_SIZE = 128 * 1024;

  
    // see F.Giudici's it.tidalwave.imageio.cr2.CR2RasterReader.class
    protected WritableRaster decompressRawData(
	    RAWImageInputStreamImpl mStream, CR2Metadata metadata,
	    boolean cropMaskedPixels) throws IOException {
	// offset to start of compressed raw data
	long offset = metadata.getStripOffset();
	int bitsPerSample = 14; // src: http://lclevy.free.fr/cr2/#sraw
	byte[] cfaPattern = metadata.getCfaPattern();

	final ByteOrder actualByteOrder = mStream.getByteOrder();
	// raw data is always big endian, even if the file is actually II
	mStream.setByteOrder(ByteOrder.BIG_ENDIAN);
	mStream.seek(offset);

	// init decoders
	LosslessJPEGDecoder jpegDecoder = new LosslessJPEGDecoder();
	jpegDecoder.reset(mStream, bitsPerSample);

	// get sensor measure from makernote or from startOfFrame 0xFFC3 (jpegDecoder.getRowSize();jpegDecoder.getHeight();)
	int sensorWidth = metadata.getSensorWidth();
	int sensorHeight = metadata.getSensorHeight();

	// attach the BitReader to the ImageInputStream for faster reading
	mStream.selectBitReader(-1, BUFFER_SIZE);
	mStream.setSkipZeroAfterFF(true);

	// create raster to store decompressed samples
	WritableRaster raster = WritableRasterFactory.create16BitRGBRaster(
		sensorWidth, sensorHeight);
	DataBufferUShort dataBuffer = (DataBufferUShort) raster.getDataBuffer();
	int numBands = raster.getNumBands();
	final int scanStride = sensorWidth * numBands;

	// retrieve slice information, length=3 or null
	if (metadata.getCr2slices() != null) {
	    LOGGER.info("CR2Slices[0]: %d", metadata.getCr2slices()[0]);
	    int sliceWidth = metadata.getCr2slices()[1];
	    LOGGER.info("sliceWidth: %d", sliceWidth);
	    int lastSliceWidth = metadata.getCr2slices()[2];
	    LOGGER.info("lastSliceWidth: %d", lastSliceWidth);
	    int regularSliceCount = (sensorWidth - lastSliceWidth) / sliceWidth;
	    LOGGER.info("regularSliceCount: %d", regularSliceCount);
	  
	    int cfaCorrector= metadata.getModel().equals("Canon EOS 5D Mark II")? 1: sensorHeight%2;
	    
	    
	    for (int y = 0; y < sensorHeight; y++) {
		// decode sensorWidth- samples
		short[] rowBuffer = jpegDecoder.loadRow(mStream);

		for (int x = 0; x < rowBuffer.length; x++) {
		    int transformedX = x;
		    int transformedY = y;
		    int scanPos = y * sensorWidth + x;
		    final int tileIndex = scanPos / (sliceWidth * sensorHeight);

		    if (tileIndex < regularSliceCount) {
			transformedX = scanPos % sliceWidth + tileIndex
				* sliceWidth;
			transformedY = (scanPos / sliceWidth) % sensorHeight;
		    } else {
			scanPos -= regularSliceCount * sliceWidth
				* sensorHeight;
			transformedX = scanPos % lastSliceWidth
				+ regularSliceCount * sliceWidth;
			transformedY = scanPos / lastSliceWidth;
		    }

		    transformedY -= cfaCorrector;
		    if ((transformedX>=0) && (transformedX < sensorWidth) && (transformedY >= 0)
			    && (transformedY < sensorHeight)) {
			int cfaIndex = (transformedY % 2) * 2 + transformedX
				% 2;
			dataBuffer.setElem(transformedY * scanStride
				+ transformedX * numBands
				+ cfaPattern[cfaIndex], rowBuffer[x]);
		    }
		}
	    }
	} else {
	    // no mapping necessary
	    for (int y = 0; y < sensorHeight; y++) {
		short[] rowBuffer = jpegDecoder.loadRow(mStream);
		for (int x = 0; x < sensorWidth; x++) {
		    int cfaIndex = (y % 2) * 2 + x % 2;
		    dataBuffer.setElem(y * scanStride + x * numBands
			    + cfaPattern[cfaIndex], rowBuffer[x]);
		}
	    }
	}
	mStream.setByteOrder(actualByteOrder);

	int cropLeft = 0;
	int cropRight = sensorWidth;
	int cropTop = 0;
	int cropBottom = sensorHeight;
	int outScanline = sensorWidth * 3;
	if (cropMaskedPixels) {
	    return cropRaster(raster, metadata);
	}
	return raster;
    }

    private WritableRaster cropRaster(WritableRaster raster,
	    CR2Metadata metadata) {
	int cropLeft = metadata.getLeftCrop();
	int cropRight = metadata.getRightCrop();
	System.err.println("cropR: "+ cropRight);
	System.err.println("sensor: "+ metadata.getSensorWidth());
	
	int cropTop = metadata.getTopCrop();
	int cropBottom = metadata.getBottomCrop();
	

	DataBufferUShort dataBuffer = (DataBufferUShort) raster.getDataBuffer();
	short[] pixelData = dataBuffer.getData();
	int scanStride = raster.getWidth() * raster.getNumBands();

	WritableRaster croppedRaster = WritableRasterFactory
		.create16BitRGBRaster(cropRight - cropLeft, cropBottom
			- cropTop);
	DataBufferUShort croppedDataBuffer = (DataBufferUShort) croppedRaster
		.getDataBuffer();
	short[] croppedPixelData = croppedDataBuffer.getData();
	int cropScan = croppedRaster.getWidth() * croppedRaster.getNumBands();

	for (int v = 0; v < croppedRaster.getHeight(); v++) {
	    for (int u = 0; u < cropScan; u++) {
		croppedPixelData[u + v * cropScan] = pixelData[(u + cropLeft * 3)
			+ (v + cropTop) * scanStride];

	    }
	}
	return croppedRaster;
    }
}
