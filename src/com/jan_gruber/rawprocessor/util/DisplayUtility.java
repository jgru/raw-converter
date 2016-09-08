package com.jan_gruber.rawprocessor.util;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;

public class DisplayUtility {
	final GraphicsConfiguration gConfig;

	public DisplayUtility() {
		gConfig = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();

	}

	// returns the factor for mapping 72dpi content on 110dpi screen
	public double[] getMappingFactor() {
		return new double[] { 72d / getDPI(gConfig)[0], 72 / getDPI(gConfig)[1] };
	}

	public int[] getDPI(final GraphicsConfiguration gConfig) {
		final Graphics2D g2d = (Graphics2D) gConfig.createCompatibleImage(1, 1)
				.getGraphics();
		// The default AffineTransform maps coordinates onto the device such
		// that 72 user space coordinate units measure approximately 1 inch in
		// device space. The normalizing transform can be used to make this
		// mapping more exact.
		g2d.setTransform(gConfig.getDefaultTransform());
		g2d.transform(gConfig.getNormalizingTransform());
		final AffineTransform normedTransform = g2d.getTransform();
		g2d.dispose();

		// returns 110 on a retina MBP or 72 on a standard pixel density screen
		return new int[] { (int) (normedTransform.getScaleX() * 72),
				(int) (normedTransform.getScaleY() * 72) };
	}

}
