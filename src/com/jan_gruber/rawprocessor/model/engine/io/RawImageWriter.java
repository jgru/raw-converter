package com.jan_gruber.rawprocessor.model.engine.io;

import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.model.engine.store.RawMetadata;
import com.spinn3r.log5j.Logger;

public class RawImageWriter {
    private static final Logger LOGGER = Logger.getLogger();
    private RawImageContainer mRawToExport;
    private WriteParams mParams;
    private final int RGB_CHANNEL_COUNT = 3;

    static {
	System.loadLibrary("ImageExport");
    }

    public RawImageWriter(RawImageContainer mRawToExport, WriteParams mParams) {
	this.mRawToExport = mRawToExport;
	this.mParams = mParams;

	LOGGER.info("BitDepth: " + mParams.getBitDepth());
	LOGGER.info("Compression: " + mParams.getCompression());
	LOGGER.info("Path: " + mParams.getPath());
	LOGGER.info("FileNamge: " + mParams.getFileName());
	LOGGER.info("Format: " + mParams.getFormat());
    }

    public void export() {
	String formatSuffix = mParams.getFormat();
	if (formatSuffix.equals(".tiff")) {
	    try {
		exportTiff(mRawToExport, mParams.getCompression(),
			mParams.getPath());
	    } catch (Exception e) {
		LOGGER.error("Error while linking or in native code, while writing tiff");
		LOGGER.error(e);
	    }
	}
	if (formatSuffix.equals(".dng")) {
	    try {
		writeDng(mRawToExport, mRawToExport.getMetadata(),
			mParams.getPath());
	    } catch (Exception e) {
		LOGGER.error("Error while linking or in native code, while writing dng");
		LOGGER.error(e);
	    }
	}
    }

    private native void writeDng(RawImageContainer mContainer,
	    RawMetadata metadata, String filePath);

    private native void exportTiff(RawImageContainer img, int compression,
	    String filePath);
    //    private native void writeTiff(String filePath, short[] pixelData,
    //	    int bitDepth, int numBands, int width, int height, int compression);

}
