package com.jan_gruber.rawprocessor.model.engine.panorama;

public class PanoramaParameters {
    public static final int SIMPLE_CUT = 0;
    public static final int MINIMUM_BOUNDARY_CUT = 0;
    public static final boolean IS_EXPORT = true; //FIXME to get data back to managed code

    private double alpha = 0.01;
    private double sigma = 2.6;
    private int boxSize = 3;
    private int threshold = 100000;
    private int descriptorSizeX = 10;
    private int featherSizeX = 10;
    private int seamlineCode = 1;
    private boolean isVisualizeSeam = false;

    public PanoramaParameters(double alpha, double sigma, int boxSize,
	    int threshold, int featherSizeX, int seamlineCode,
	    boolean isVisualizeSeam) {
	this.alpha = alpha;
	this.sigma = sigma;
	this.boxSize = boxSize;
	this.threshold = threshold;
	this.featherSizeX = featherSizeX;
	this.seamlineCode = seamlineCode;
	this.isVisualizeSeam = isVisualizeSeam;
    }

    public double getAlpha() {
	return alpha;
    }

    public void setAlpha(double alpha) {
	this.alpha = alpha;
    }

    public double getSigma() {
	return sigma;
    }

    public void setSigma(double sigma) {
	this.sigma = sigma;
    }

    public int getBoxSize() {
	return boxSize;
    }

    public void setBoxSize(int boxSize) {
	this.boxSize = boxSize;
    }

    public int getThreshold() {
	return threshold;
    }

    public void setThreshold(int threshold) {
	this.threshold = threshold;
    }

    public int getFeatherSizeX() {
	return featherSizeX;
    }

    public void setFeatherSizeX(int featherSizeX) {
	this.featherSizeX = featherSizeX;
    }

    public int getDescriptorSizeX() {
	return descriptorSizeX;
    }

    public void setDescriptorSizeX(int descriptorSizeX) {
	this.descriptorSizeX = descriptorSizeX;
    }

    public int getSeamlineCode() {
	return seamlineCode;
    }

    public void setSeamlineCode(int seamlineCode) {
	this.seamlineCode = seamlineCode;
    }

    public boolean isVisualizeSeamline() {
	return isVisualizeSeam;
    }

    public void setVisualizeSeamline(boolean isVisualizeSeamline) {
	this.isVisualizeSeam = isVisualizeSeamline;
    }
    public boolean isExport(){
	return IS_EXPORT;
    }
}
