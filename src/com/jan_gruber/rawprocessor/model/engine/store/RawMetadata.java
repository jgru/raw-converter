package com.jan_gruber.rawprocessor.model.engine.store;

public interface RawMetadata {
    public int getWidth();
    public int getHeight();
    public String getMake();
    public String getModel();
    public short getOrientation();
    public long getStripOffsetToRawData();
    public short[] getWhiteBalanceRGGBMultipliers();
    public float[] getNeutralWhiteBalanceMultipliers();
    public short[] getBlackLevelsPerChannel();
    public double[] getColorMatrix1();
    //only supported by a few models
    public short getWhiteLevel();
    public byte[] getCfaPattern();
    
}
