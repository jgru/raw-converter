package com.jan_gruber.rawprocessor.model.engine.io;

public class WriteParams {
    private String path;
    private String fileName;
    private String formatSuffix;
    private int bitDepth;
    private int compression;

    public WriteParams() {

    }

    public WriteParams(String pathToDir, String fileName, String formatSuffix,
	    int bitDepth, int compression) {
	this.path = pathToDir;
	this.fileName = fileName;
	this.formatSuffix = formatSuffix == null ? ".tiff" : formatSuffix;
	this.bitDepth = bitDepth;
	this.compression = compression;

	this.path = this.path + "/" + this.fileName + this.formatSuffix;
    }

    public String getPath() {
	return path;
    }

    public void setPath(String pathToDir) {
	this.path = pathToDir;
    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getFormat() {
	return formatSuffix;
    }

    public void setFormat(String format) {
	this.formatSuffix = format;
    }

    public int getBitDepth() {
	return bitDepth;
    }

    public void setBitDepth(int bitDepth) {
	this.bitDepth = bitDepth;
    }

    public int getCompression() {
	return compression;
    }

    public void setCompression(int compression) {
	this.compression = compression;
    }

}
