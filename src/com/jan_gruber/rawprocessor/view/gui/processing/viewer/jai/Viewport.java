package com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jan_gruber.rawprocessor.view.gui.UpdatableComponent;

public abstract class Viewport extends JComponent implements
	ViewportControlListener, UpdatableComponent {

    protected MouseInfo mouseLabel;
    protected ViewportController mController;

    RenderedImage origImg;

    protected SampleModel sampleModel;
    protected ColorModel colorModel;
    protected int bitsPerChannel;
    protected int numBands;
    protected int imageWidth = 0, imageHeight = 0;

    BufferedImage frameBuffer;
    boolean isVisualizeTonalRange;
    boolean renderingFinished;

    Viewport() {

	mouseLabel = new MouseInfo();
	mController = new ViewportController();
	mController.registerViewport(this);

    }

    public Viewport(final RenderedImage origImg) {
	super();
	
	setImage(origImg);
	mouseLabel = new MouseInfo();
	mController = new ViewportController();
	mController.registerViewport(this);

    }

    public void setImage(RenderedImage img) {
	origImg = img;
	colorModel = img.getColorModel();
	sampleModel = img.getSampleModel();
	bitsPerChannel = sampleModel.getSampleSize(0);
	numBands = sampleModel.getNumBands();
	prepareImageForRendering(img);
    }

    public abstract void prepareImageForRendering(RenderedImage img);

    protected abstract void applyViewportTransformation(RenderedImage origImg,
	   double scaleFactor);

    protected abstract void visualizeTonalRange(BufferedImage frameBuf,
	    Graphics2D g);

    protected abstract void drawMouseLabel(Graphics2D g);

    public ViewportController getController() {
	return mController;
    }

    public void setmController(ViewportController mController) {
	this.mController = mController;
    }

    public MouseInfo getMouseLabel() {
	return mouseLabel;
    }

    public void setMouseLabel(MouseInfo ml) {
	this.mouseLabel = mouseLabel;
    }

    public RenderedImage getOrigImg() {
	return origImg;
    }

    public void setOrigImg(RenderedImage origImg) {
	this.origImg = origImg;
    }
}
