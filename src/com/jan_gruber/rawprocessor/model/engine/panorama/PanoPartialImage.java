package com.jan_gruber.rawprocessor.model.engine.panorama;

public interface PanoPartialImage {
    public boolean hasDetectedFeatures();

    public void setHasDetectedFeatures(boolean hasDetectedFeatures);

    public int[] getFeatureCoords();

    public void setFeatureCoords(int[] coords);
}
