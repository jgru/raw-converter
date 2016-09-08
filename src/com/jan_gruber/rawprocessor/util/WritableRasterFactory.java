package com.jan_gruber.rawprocessor.util;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

public class WritableRasterFactory {
    private static WritableRaster createCompatibleWritableRaster(
	    WritableRaster raster) {
	//FIXME error 
	
	final int width = raster.getWidth();
	final int height = raster.getHeight();
	final int numBands = raster.getNumBands();
	final SampleModel sm = raster.getSampleModel();
	final int dt = sm.getDataType();
	WritableRaster out = RasterFactory.createBandedRaster(dt, width,
		height, numBands, new Point(0, 0));

	return out;
    }

    public static WritableRaster create16BitGrayscaleRaster(int sensorWidth,
	    int sensorHeight) {
	ColorModel cm = new ComponentColorModel(
		ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 16 },
		false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
	WritableRaster raster = cm.createCompatibleWritableRaster(sensorWidth,
		sensorHeight);
	return raster;
    }

    public static WritableRaster create16BitRGBRaster(int sensorWidth,
	    int sensorHeight) {
	final ColorSpace colorSpace = ColorSpace
		.getInstance(ColorSpace.CS_LINEAR_RGB);
	final ColorModel colorModel = new ComponentColorModel(colorSpace,
		new int[] { 16, 16, 16 }, false, false, Transparency.OPAQUE,
		DataBuffer.TYPE_USHORT);

	WritableRaster raster = colorModel.createCompatibleWritableRaster(
		sensorWidth, sensorHeight);

	return raster;
    }

}
