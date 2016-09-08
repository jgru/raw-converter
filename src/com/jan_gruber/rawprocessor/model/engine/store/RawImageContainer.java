package com.jan_gruber.rawprocessor.model.engine.store;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.media.jai.PlanarImage;
import javax.xml.transform.TransformerException;

import com.jan_gruber.rawprocessor.model.AbstractModel;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoPartialImage;

public abstract class RawImageContainer extends AbstractModel implements PanoPartialImage{
    public final static String PROCESSED = "PROCESSED";
    public final static String IMAGE_UPDATED = "UPDATED";

    protected String fileName;
    protected RawMetadata metadata;
    
    protected boolean isProcessed;
    protected int colorSpace;
    
    protected WritableRaster rawData;
    protected WritableRaster processedData;
    protected BufferedImage[] thumbnails;
    protected PlanarImage imgToDisplay;

    public abstract String getName();

    public abstract BufferedImage getThumbnail();

    public abstract PlanarImage getImgToDisplay();

    public abstract WritableRaster getRawData();
    public abstract short[] getRawBuffer();
    public abstract short[] getProcessedBuffer();
    public abstract int getRasterWidth();
    public abstract int getRasterHeight();
    public abstract int getNumBands();
    public abstract short getOrientation();

    public abstract String getMetadataAsXML() throws TransformerException,
	    IOException;

    public abstract byte[] getCfaPattern();

    public WritableRaster getProcessedData() {
	if (processedData != null)
	    return processedData;
	else
	    return rawData;
    }

    public void setProcessedData(WritableRaster processedData) {
	this.processedData = processedData;

    }

    public RawMetadata getMetadata() {
	return metadata;
    }
    public Integer getMeta(){
	return new Integer(0);
	
    }

    public void setMetadata(RawMetadata metadata) {
	this.metadata = metadata;
    }

    public boolean isProcessed() {
	return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
	this.isProcessed = isProcessed;
	if (isProcessed == true)
	    propertyChangeSupport.firePropertyChange(PROCESSED, null,
		    getImgToDisplay());
	else
	    propertyChangeSupport.firePropertyChange(IMAGE_UPDATED, fileName,
		    getImgToDisplay());
    }

    public int getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(int colorSpace) {
        this.colorSpace = colorSpace;
    }


}
