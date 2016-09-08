package com.jan_gruber.rawprocessor.model.engine.panorama;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.jai.PlanarImage;

import com.jan_gruber.rawprocessor.model.AbstractModel;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

public class PanoramaEditor extends AbstractModel {
    private static final Logger LOGGER = Logger.getLogger();
    public static final String FEATURE_UPDATE = "FEATURE_UPDATE";
    public static final String STITCHING_UPDATE = "STITCHING_UPDATE";

    static {
	System.loadLibrary("PanoramaStitching");
    }

    private boolean isPrepared;

    public void detectFeatures(ArrayList<RawImageContainer> mContainers,
	    PanoramaParameters fdp) {
	for (RawImageContainer container : mContainers) {
	    detectFeaturesAndVisualize(container, fdp);
	}
    }

    private void detectFeaturesAndVisualize(RawImageContainer container,
	    PanoramaParameters fdp) {
	if (!container.hasDetectedFeatures()) {
	    LOGGER.info("detect features in %s", container.getName());
	    int[] featureCoords = detectFeatures(container, fdp);

	    container.setFeatureCoords(featureCoords);
	    firePropertyChange(FEATURE_UPDATE, container.getName(),
		    visualizeFeatures(container, featureCoords));
	}
    }

    private PlanarImage visualizeFeatures(RawImageContainer con,
	    int[] featureCoords) {
	PlanarImage img = con.getImgToDisplay();
	Graphics2D g = (Graphics2D) img.getGraphics();
	g.setColor(Color.RED);
	g.setStroke(new BasicStroke(10));
	int circleRadius = 30;
	for (int i = 0; i < featureCoords.length - 2; i += 2) {
	    g.drawRect(featureCoords[i] - circleRadius / 2,
		    featureCoords[i + 1] - circleRadius / 2, circleRadius,
		    circleRadius);
	}
	g.dispose();
	return img;
    }

    public void stitchImages(ArrayList<RawImageContainer> images,
	    PanoramaParameters fdp, String path) {
	RawImageContainer[] containers = new RawImageContainer[images.size()];
	images.toArray(containers);

	if (path != null) {
	    //set output filepath with appropriate naming
	    String[] splittedName = images.get(0).getName().split("\\.");
	    path = path + "/" + splittedName[0] + "_pano" + ".dng";
	}

	stitch(containers, fdp, path);

	firePropertyChange(STITCHING_UPDATE, 0, 1);
    }

    /*
     * callback method called from native code
     */
    public void stitchingCallback(short[] pixelData, int width, int height) {
	LOGGER.info("Received stitching result");
	LOGGER.info(width);
	LOGGER.info(height);

	firePropertyChange(STITCHING_UPDATE, pixelData, new int[] { width,
		height });
    }

    private native int[] detectFeatures(RawImageContainer mContainer,
	    PanoramaParameters mParams);

    private native void stitch(RawImageContainer[] containers,
	    PanoramaParameters mParams, String path);

}
