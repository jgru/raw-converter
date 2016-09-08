package com.jan_gruber.rawprocessor.model.engine.io;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;

import com.jan_gruber.rawprocessor.model.engine.store.TIFFField;
import com.spinn3r.log5j.Logger;

public class TIFFTagParser {
    private static final Logger LOGGER = Logger.getLogger();

    public static final short TYPE_BYTE = 1;
    public static final short TYPE_ASCII = 2;
    public static final short TYPE_USHORT = 3;
    public static final short TYPE_LONG = 4;
    public static final short TYPE_RATIONAL = 5;
    public static final short TYPE_SIGNED_BYTE = 6;
    public static final short TYPE_UNDEFINED = 7;
    public static final short TYPE_SIGNED_SHORT = 8;
    public static final short TYPE_SIGNED_LONG = 9;
    public static final short TYPE_SIGNED_RATIONAL = 10;
    public static final short TYPE_FLOAT = 11;
    public static final short TYPE_DOUBLE = 12;

    public static final String[] typeLookUp = { "byte", "ascii", "short",
	    "long", "rational", "signed_byte", "undefined", "signed_short",
	    "signed_long", "signed_rational", "float", "double" };

    public TIFFField parseTIFFTag(ImageInputStream mStream) throws IOException {
	int code = mStream.readUnsignedShort();
	int dataFormat = mStream.readUnsignedShort();
	long componentCount = mStream.readUnsignedInt();

	return new TIFFField(code, dataFormat, componentCount, parseDataValues(
		mStream, dataFormat, componentCount));
    }

    public Object parseDataValues(ImageInputStream mStream, int dataFormat,
	    long valueCount) throws IOException {

	switch (dataFormat) {
	case TYPE_SIGNED_BYTE:
	case TYPE_BYTE:
	    return readBytes(mStream, valueCount);
	case TYPE_ASCII:
	    return readAscii(mStream, valueCount);
	case TYPE_SIGNED_SHORT:
	case TYPE_USHORT:
	    return readShort(mStream, valueCount);
	case TYPE_SIGNED_LONG:
	case TYPE_LONG:
	    return readInt(mStream, valueCount);
	case TYPE_SIGNED_RATIONAL:
	case TYPE_RATIONAL:
	    mStream.readUnsignedInt();
	    // TODO
	    break;
	case TYPE_UNDEFINED:
	    return readUndefinedValue(mStream, valueCount);
	case TYPE_FLOAT:
	    LOGGER.warn("TYPE_FLOAT is not implemented");
	    mStream.skipBytes(4);
	    break;
	case TYPE_DOUBLE:
	    LOGGER.warn("TYPE_DOUBLE is not implemented");
	    mStream.skipBytes(4);
	    break;
	default:
	    LOGGER.warn("Format %d is unknown", dataFormat);
	    break;
	}

	return null;

    }

    protected byte[] readUndefinedValue(ImageInputStream mStream,
	    long componentCount) throws IOException {
	byte[] buffer = new byte[(int) componentCount];
	if (componentCount <= 4) {
	    for (int i = 0; i < componentCount; i++) {
		buffer[i] = mStream.readByte();
	    }
	    mStream.skipBytes(4 - componentCount);
	    return buffer;
	} else {
	    long offsetToData = mStream.readUnsignedInt();
	    return readUndefinedValue(mStream, componentCount, offsetToData);
	}
    }

    protected byte[] readUndefinedValue(ImageInputStream mStream,
	    long componentCount, long offsetToData) throws IOException {
	byte[] buffer = new byte[(int) componentCount];
	mStream.mark();
	mStream.seek(offsetToData);
	mStream.read(buffer);
	mStream.reset();
	return buffer;
    }

    protected String readAscii(ImageInputStream mStream, long componentCount)
	    throws IOException {
	StringBuffer strBuf = new StringBuffer();
	boolean isReset = false;
	if (componentCount > 4) {
	    isReset = true;
	    long offsetToData = mStream.readUnsignedInt();
	    mStream.mark();
	    mStream.seek(offsetToData);
	}
	int i = (int) componentCount;
	while (i > 0) {
	    char tmpChar = (char) mStream.readByte();
	    if (tmpChar == 0)
		break;
	    strBuf.append(tmpChar);
	    i--;
	}
	if (isReset)
	    mStream.reset();
	else
	    mStream.skipBytes(4 - componentCount);
	return strBuf.toString();
    }

    protected int[] readBytes(ImageInputStream mStream, long componentCount)
	    throws IOException {

	if (componentCount <= 4) {
	    int[] buffer = new int[(int) componentCount];
	    for (int i = 0; i < componentCount; i++) {
		buffer[i] = mStream.readByte();
	    }
	    mStream.skipBytes(4 - componentCount);
	    return buffer;
	} else {
	    long offsetToData = mStream.readUnsignedInt();
	    return readBytes(mStream, componentCount, offsetToData);
	}
    }

    protected int[] readBytes(ImageInputStream mStream, long componentCount,
	    long offsetToData) throws IOException {
	int[] buffer = new int[(int) componentCount];
	mStream.mark();
	mStream.seek(offsetToData);
	for (int i = 0; i < componentCount; i++) {
	    buffer[i] = mStream.readByte();
	}
	mStream.reset();
	return buffer;
    }

    protected int[] readShort(ImageInputStream mStream, long componentCount)
	    throws IOException {
	if (componentCount <= 2) {
	    int[] buffer = new int[(int) componentCount];
	    for (int i = 0; i < componentCount; i++) {
		buffer[i] = mStream.readShort();
	    }
	    mStream.skipBytes(4 - componentCount * 2);
	    return buffer;
	} else {
	    long offsetToData = mStream.readUnsignedInt();
	    return readShort(mStream, componentCount, offsetToData);
	}
    }

    protected int[] readShort(ImageInputStream mStream, long valueCount,
	    long offsetToData) throws IOException {
	int[] buffer = new int[(int) valueCount];
	mStream.mark();
	mStream.seek(offsetToData);
	for (int i = 0; i < valueCount; i++) {
	    buffer[i] = mStream.readShort();
	}
	mStream.reset();
	return buffer;
    }

    protected int[] readInt(ImageInputStream mStream, long componentCount)
	    throws IOException {
	long value = mStream.readUnsignedInt();
	if (componentCount == 1)
	    return new int[] { (int) value };
	else
	    return readInt(mStream, componentCount, value);
    }

    protected int[] readInt(ImageInputStream mStream, long componentCount,
	    long offsetToData) throws IOException {
	int[] buffer = new int[(int) componentCount];
	mStream.mark();
	mStream.seek(offsetToData);
	for (int i = 0; i < componentCount; i++) {
	    buffer[i] = mStream.readInt();
	}
	mStream.reset();
	return buffer;

    }

}
