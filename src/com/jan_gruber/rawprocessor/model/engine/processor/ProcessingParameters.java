package com.jan_gruber.rawprocessor.model.engine.processor;

import java.util.ArrayList;
import java.util.HashMap;

public class ProcessingParameters {
    private ArrayList<String> operationList;

    //OperationNames
    public static final String PRECONDITIONING = "PreConditioning ";
    public static final String WHITEBALANCING = "WhiteBalancing ";
    public static final String DEMOSAICING = "Demosaicing ";
    public static final String LUT = "LUTAdjustment ";
    public static final String COLOR_CONVERT = "ColorConversion ";
    public static final String CONVOLUTION = "Convolution ";
    public static final String NOISE_REDUCTION = "NoiseReduction ";
    public static final ArrayList<String> opNameList = new ArrayList<String>();
    static {
	opNameList.add(PRECONDITIONING);
	opNameList.add(DEMOSAICING);
	opNameList.add(LUT);
	opNameList.add(COLOR_CONVERT);
	opNameList.add(CONVOLUTION);
	opNameList.add(NOISE_REDUCTION);
    }
    private int demosaicType;
    private boolean isNormalization;
    private boolean isBlackLevelCompensation;
    private boolean isWhiteBalancing;
    private int wbType;
    private boolean isGammaAdjustment;
    private int noiseReductionBoxSize;
    private int convolutionBoxSize;
    private int convolutionType;
    private float[] customConvolutionKernel;
    private short[][] mLUT;

    public ProcessingParameters() {
	operationList = new ArrayList<String>();

    }

    public void addOperation(String opName) {
	if (opNameList.contains(opName)) {
	    if (operationList.contains(opName))
		return;
	    else
		this.operationList.add(opName);
	} else
	    throw new IllegalArgumentException();
    }

    public ArrayList<String> getOperationList() {
	return operationList;
    }

    public void setOperationList(ArrayList<String> operationList) {
	this.operationList = operationList;
    }

    public int getDemosaicType() {
	return demosaicType;
    }

    public void setDemosaicType(int demosaicType) {
	this.demosaicType = demosaicType;
    }

    public int getNoiseReductionBoxSize() {
	return noiseReductionBoxSize;
    }

    public void setNoiseReductionBoxSize(int noiseReductionBoxSize) {
	this.noiseReductionBoxSize = noiseReductionBoxSize;
    }

    public boolean isBlackLevelCompensation() {
	return isBlackLevelCompensation;
    }

    public void setBlackLevelCompensation(boolean isBlackLevelCompensation) {
	this.isBlackLevelCompensation = isBlackLevelCompensation;
    }

    public boolean isWhiteBalancing() {
	return isWhiteBalancing;
    }

    public void setWhiteBalancing(boolean isWhiteBalancing) {
	this.isWhiteBalancing = isWhiteBalancing;
    }

    public int getWbType() {
	return wbType;
    }

    public void setWbType(int wbType) {
	this.wbType = wbType;
    }

    public boolean isNormalization() {
	return isNormalization;
    }

    public void setNormalization(boolean isNormalization) {
	this.isNormalization = isNormalization;
    }

    public short[][] getLUT() {
	return mLUT;
    }

    public void setLUT(short[][] mLUT) {
	this.mLUT = mLUT;
    }

    public int getConvolutionBoxSize() {
        return convolutionBoxSize;
    }

    public void setConvolutionBoxSize(int convolutionBoxSize) {
        this.convolutionBoxSize = convolutionBoxSize;
    }

    public int getConvolutionType() {
        return convolutionType;
    }

    public void setConvolutionType(int convolutionType) {
        this.convolutionType = convolutionType;
    }

    public float[] getCustomConvolutionKernel() {
        return customConvolutionKernel;
    }

    public void setCustomConvolutionKernel(float[] customConvolutionKernel) {
        this.customConvolutionKernel = customConvolutionKernel;
    }

    public boolean isGammaAdjustment() {
        return isGammaAdjustment;
    }

    public void setGammaAdjustment(boolean isGammaAdjustment) {
        this.isGammaAdjustment = isGammaAdjustment;
    }

}
