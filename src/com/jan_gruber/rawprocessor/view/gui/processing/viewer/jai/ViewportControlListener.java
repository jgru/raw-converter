package com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai;

import java.awt.geom.AffineTransform;

import javax.tools.JavaCompiler;

public interface ViewportControlListener {
	public void viewportTranslated(int translationX, int translationY);

	public void viewportTransformed(double scaleFactor);
	
	public int[] getInitTranslationFactors();
}
