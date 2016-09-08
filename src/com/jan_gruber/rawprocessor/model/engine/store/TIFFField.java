package com.jan_gruber.rawprocessor.model.engine.store;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
//oriented at http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/TIFFField.html
// & https://bitbucket.org/tidalwave/jrawio-src/src/b5299fe1c439c89286ee6ab9038293344d5cc093/modules/codec/src/main/java/it/tidalwave/imageio/tiff/TIFFTag.java?at=2.0


import com.jan_gruber.rawprocessor.model.engine.io.TIFFTagParser;
import com.jan_gruber.rawprocessor.model.engine.io.TagDictionary;
import com.spinn3r.log5j.Logger;
//angelehnt an
//http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/TIFFField.html
public class TIFFField {
	private static final Logger LOGGER = Logger.getLogger();
	private int code;
	private int dataFormat;
	private long componentCount;
	private long offsetToTag;
	private String name;

	protected int[] intValue;
	protected String asciiValue;
	protected byte[] dataValues;
	// TODO rational value

	

	/** This array maps type codes to type descriptions. */
	private static final String[] typeLookUp = { "byte", "ascii", "short",
			"long", "rational", "signed byte", "undefined", "signed short",
			"signed long", "signed rational", "float", "double" };

	public TIFFField(int code, int dataFormat, long componentCount, Object data)
			throws IOException {
		this.offsetToTag=offsetToTag;
	    	this.code = code;
		this.name= TagDictionary.translate(code);
		this.dataFormat = dataFormat;
		this.componentCount = componentCount;

		switch (dataFormat) {
		case TIFFTagParser.TYPE_SIGNED_LONG:
		case TIFFTagParser.TYPE_LONG:
		case TIFFTagParser.TYPE_SIGNED_SHORT:
		case TIFFTagParser.TYPE_USHORT:
		case TIFFTagParser.TYPE_SIGNED_BYTE:
		case TIFFTagParser.TYPE_BYTE:
			intValue = (int[]) data;
			break;
		case TIFFTagParser.TYPE_ASCII:
			asciiValue = (String) data;
			break;
		case TIFFTagParser.TYPE_SIGNED_RATIONAL:
		case TIFFTagParser.TYPE_RATIONAL:
			// TODO
			break;
		case TIFFTagParser.TYPE_UNDEFINED:
			dataValues = (byte[]) data;
			break;
		case TIFFTagParser.TYPE_FLOAT:
			LOGGER.warn("TYPE_FLOAT is not implemented");
			break;
		case TIFFTagParser.TYPE_DOUBLE:
			LOGGER.warn("TYPE_DOUBLE is not implemented");
			break;
		default:
			LOGGER.warn("Type %d is unknown", code);
			break;
		}

	}

	public java.lang.Object getValue() {
		switch (dataFormat) {
		case TIFFTagParser.TYPE_SIGNED_BYTE:
		case TIFFTagParser.TYPE_BYTE:
			if (intValue.length == 1) {
				return new Byte((byte) intValue[0]);
			}

			byte[] bytes = new byte[intValue.length];

			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) intValue[i];
			}

			return bytes;

		case TIFFTagParser.TYPE_SIGNED_SHORT:
		case TIFFTagParser.TYPE_USHORT:
			if (intValue.length == 1) {
				return new Short((short) intValue[0]);
			}

			short[] shorts = new short[intValue.length];

			for (int i = 0; i < shorts.length; i++) {
				shorts[i] = (short) intValue[i];
			}

			return shorts;

		case TIFFTagParser.TYPE_SIGNED_LONG:
		case TIFFTagParser.TYPE_LONG:
			return (intValue.length == 1) ? new Integer(intValue[0])
					: (Object) intValue;

		case TIFFTagParser.TYPE_ASCII:
			return asciiValue;

		case TIFFTagParser.TYPE_SIGNED_RATIONAL:
		case TIFFTagParser.TYPE_RATIONAL:
			return null;

		case TIFFTagParser.TYPE_UNDEFINED:
			return dataValues;

		default:
			throw new RuntimeException("Unsupported type:" + dataFormat);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(int dataFormat) {
		this.dataFormat = dataFormat;
	}

	public long getComponentCount() {
		return componentCount;
	}

	public void setComponentCount(long componentCount) {
		this.componentCount = componentCount;
	}

	public long getOffsetToTag() {
	    return offsetToTag;
	}

	public void setOffsetToTag(long offsetToTag) {
	    this.offsetToTag = offsetToTag;
	}
}
