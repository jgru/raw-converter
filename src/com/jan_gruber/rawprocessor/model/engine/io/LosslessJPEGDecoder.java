package com.jan_gruber.rawprocessor.model.engine.io;


import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.spinn3r.log5j.Logger;

//Quelle:
//https://bitbucket.org/tidalwave/jrawio-src/src/a1f0e7a0a141ee4b85a936449e06bab3e3e29e1b/modules/codec/src/main/java/it/tidalwave/imageio?at=default
/***********************************************************************************************************************
 * 
 * This class implements a 16-bit Lossless JPEG decoder. This kind of encoding
 * is widely used by some RAW formats which include blocks of encoded data.
 * 
 * @author fritz
 * @version $Id: LosslessJPEGDecoder.java,v af350c73f482 2009/09/10 12:30:05
 *          fabrizio $
 * 
 **********************************************************************************************************************/
public class LosslessJPEGDecoder {
	private final static String CLASS = LosslessJPEGDecoder.class.getName();
	private final static Logger logger = Logger.getLogger(CLASS);

	private final static int BYTE_MASK = 0xff;

	private final static int SHORT_MASK = 0xffff;

	/** The size of a bit sample. */
	private int bitsPerSample;

	/** The height of the encoded block. */
	private int height;

	/** The width of the encoded block. */
	private int width;

	/** The number of channels. */
	private int channelCount;

	/** The size of a row (it's the width * channelCount) */
	private int rowSize;

	private int[] vPredictors;

	private HuffmannDecoder[] decoders;

	private short[] rowBuffer;

	/*******************************************************************************************************************
	 * 
	 * Creates an empty decoder. Prior using, the decoder must be bound to an
	 * input stream by means of the {@link #reset(ImageInputStream)} method. The
	 * same instance can be reused more than once by resetting it multiple
	 * times.
	 * 
	 ******************************************************************************************************************/
	public LosslessJPEGDecoder() {
	}

	/*******************************************************************************************************************
	 * 
	 * Links the decoder to a given input stream. This method also parses the
	 * Lossless JPEG header.
	 * 
	 * @param iis
	 *            the input stream
	 * @throws IOException
	 *             if an I/O error occurs
	 * 
	 ******************************************************************************************************************/
	public void reset(final RAWImageInputStreamImpl iis, int bitsPerSample) throws IOException {
		this.bitsPerSample=bitsPerSample;
		final short magic = iis.readShort();

		if (magic != (short) 0xffd8) {
			throw new RuntimeException("Bad magic: "
					+ Integer.toHexString(magic & SHORT_MASK));
		}

		final HuffmannDecoder[] dcTables = new HuffmannDecoder[4];

		loop: for (;;) {	
			final short tag = iis.readShort();
			final int length = iis.readShort() - 2;

			logger.info(">>>> tag: %x length: %d", tag & SHORT_MASK, length);
			
			if (((tag & SHORT_MASK) <= 0xff00) || (length > 255)) {
				throw new RuntimeException("Bad tag:"
						+ Integer.toHexString(tag & SHORT_MASK));
			}

			switch (tag) {
			case (short) 0xffc3:
				bitsPerSample = iis.readByte() & BYTE_MASK;
				height = iis.readShort() & SHORT_MASK;
				width = iis.readShort() & SHORT_MASK;
				channelCount = iis.readByte() & BYTE_MASK;
				rowSize = width * channelCount;
				iis.skipBytes(length - 6);
				rowBuffer = new short[rowSize];
				vPredictors = new int[channelCount];

				for (int i = 0; i < channelCount; i++) {
					vPredictors[i] = 1 << (bitsPerSample - 1);
				}

				logger.info(
						"bitsPerSample: %d, height: %d, width: %d, rowSize: %d, channelCount: %d",
						bitsPerSample, height, width, rowSize, channelCount);
				break;

			case (short) 0xffc4:
				final byte[] data = new byte[length];
				iis.readFully(data);

				for (int scan = 0; (scan < length) && (data[scan] < 4);) {
					final int channel = data[scan++];
					int decoderLen = 16;

					for (int q = scan; q < scan + 16; q++) {
						decoderLen += data[q];
					}

					final byte[] temp = new byte[decoderLen];
					System.arraycopy(data, scan, temp, 0, temp.length);
					dcTables[channel] = HuffmannDecoder
							.createDecoderWithJpegHack(temp, 0);
					logger.info("Decoder[%d] = %s", channel, dcTables[channel]);
					scan += decoderLen;
				}

				break;

			case (short) 0xffda:
				final int channels = iis.readUnsignedByte();
				decoders = new HuffmannDecoder[channels];

				for (int i = 0; i < channels; i++) {
					final int index = iis.readUnsignedByte();
					final int dcac = iis.readUnsignedByte();
					final int dc = dcac >> 4;
					final int ac = dcac & 0xF;
					logger.info("Decoder index=%d, DC table=%d, AC table=%d",
							index, dc, ac);
					decoders[index - 1] = dcTables[dc];
				}

				iis.skipBytes(3);

				break loop;
			}
		}
	}

	/*******************************************************************************************************************
	 * 
	 * Loads and decodes another row of data from this encoder.
	 * 
	 * @param iis
	 *            the bit reader to read data from
	 * @throws IOException
	 *             if an I/O error occurs
	 * 
	 ******************************************************************************************************************/
	public short[] loadRow(RAWImageInputStreamImpl iis) throws IOException {
		int scan = 0;

		for (int x = 0; x < width; x++) {
			for (int c = 0; c < channelCount; c++) {
				final HuffmannDecoder decoder = decoders[c];
				final int bitCount = decoder.decode(iis);
				final int diff = decoder.readSignedBits(iis, bitCount);
				rowBuffer[scan] = (short) ((x == 0) ? (vPredictors[c] += diff)
						: (rowBuffer[scan - channelCount] + diff));
				scan++;
			}
		}

		return rowBuffer;
	}

	public int getHeight() {
		return height;
	}


	public int getWidth() {
		return width;
	}

	public int getRowSize() {
		return rowSize;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}


}