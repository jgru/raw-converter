package com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.spinn3r.log5j.Logger;

/**
 * This class performs as a canvas for large images. It houses the functionality
 * to render only the visible tiles.
 * 
 * @author JanGruber
 * 
 */
public class TilingViewport extends Viewport {
    private static final Logger LOGGER = Logger.getLogger();
    private static final long serialVersionUID = 1L;

    protected TiledImage displayImg;

    public TilingViewport(RenderedImage img) {
	super(img);
    }

    public void setImage(RenderedImage img) {
	origImg = img;
	colorModel = img.getColorModel();
	sampleModel = img.getSampleModel();
	bitsPerChannel = sampleModel.getSampleSize(0);
	numBands = sampleModel.getNumBands();
	if (mController != null)
	    mController.transformViewports();
	else
	    prepareImageForRendering(origImg);
    }

    @Override
    public void prepareImageForRendering(RenderedImage img) {
	displayImg = makeTiledImage(img);
	calcTileInfo(displayImg);
	renderingFinished = false;
	repaint();
    }

    @Override
    protected void applyViewportTransformation(RenderedImage origImg,
	    double scale) {
	if (origImg == null)
	    return;
	/*
	more info on downsampling with little artifacts on: 
	http://download.java.net/media/jai/javadoc/1.1.3/jai-apidocs/javax/media/jai/operator/SubsampleAverageDescriptor.html
	and
	https://blogs.warwick.ac.uk/mmannion/entry/using_subsample_averaging/
	*/
	ParameterBlock params = new ParameterBlock();
	params.addSource(origImg);
	params.add(scale);// x scale factor
	params.add(scale);// y scale factor
	params.add(0.0F);// x translate
	params.add(0.0F);// y translate

	Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
	map.put(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	map.put(RenderingHints.KEY_COLOR_RENDERING,
		RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	map.put(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	map.put(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);

	RenderingHints hints = new RenderingHints(map);

	RenderedOp alteredImage = JAI.create("SubsampleAverage", params, hints);
	prepareImageForRendering(alteredImage.createInstance());

    }

    private TiledImage makeTiledImage(RenderedImage img) {
	// FIXME black border due to tiling
	int tileWidth = img.getWidth() / 32;
	int tileHeight = img.getHeight() / 32;

	TiledImage tiledImg = new TiledImage(img, tileWidth, tileHeight);
	return tiledImg;
    }

    protected int maxTileIndexX, maxTileIndexY;
    protected int maxTileCordX, maxTileCordY;
    protected int minTileIndexX, minTileIndexY;
    protected int minTileCordX, minTileCordY;
    protected int tileGridXOffset, tileGridYOffset;
    protected int tileWidth, tileHeight;

    protected void calcTileInfo(TiledImage img) {
	imageWidth = img.getWidth();
	imageHeight = img.getHeight();
	tileWidth = img.getTileWidth();
	tileHeight = img.getTileHeight();
	maxTileIndexX = img.getMinTileX() + img.getNumXTiles() - 1;
	// LOGGER.info("Tiles in X: %d", maxTileIndexX);
	maxTileIndexY = img.getMinTileY() + img.getNumYTiles() - 1;
	// LOGGER.info("Tiles in Y: %d", maxTileIndexY);
	maxTileCordX = img.getMaxX();
	maxTileCordY = img.getMaxY();
	minTileIndexX = img.getMinTileX();
	minTileIndexY = img.getMinTileY();
	minTileCordX = img.getMinX();
	minTileCordY = img.getMinY();
	tileGridXOffset = img.getTileGridXOffset();
	tileGridYOffset = img.getTileGridYOffset();

    }

    @Override
    public void viewportTranslated(int translationX, int translationY) {
	this.translationX = translationX;
	this.translationY = translationY;
	renderingFinished = false;
	repaint();
    }

    @Override
    public void viewportTransformed(double scaleFactor) {
	applyViewportTransformation(origImg, scaleFactor);
    }

    int leftIndex;
    int topIndex;
    int rightIndex;
    int bottomIndex;
    int padding = 50;
    int translationX = padding;
    int translationY = padding;

    @Override
    public void paintComponent(Graphics gc) {
	Graphics2D g2 = (Graphics2D) gc;
	Rectangle rect = this.getBounds();

	if (!renderingFinished && displayImg != null) {
	    //FIXME speed of magnifying 8bit BufferedImage (AffineTransform)
	    if (bitsPerChannel == 8)
		frameBuffer = new BufferedImage(this.getWidth(),
			this.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    else {

		final ColorSpace colorSpace = ColorSpace
			.getInstance(ColorSpace.CS_LINEAR_RGB);
		final ColorModel colorModel = new ComponentColorModel(
			colorSpace, new int[] { 16, 16, 16 }, false, false,
			Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
		frameBuffer = new BufferedImage(colorModel,
			colorModel.createCompatibleWritableRaster(rect.width,
				rect.height), false, null);
	    }
	    Graphics2D g = frameBuffer.createGraphics();

	    Rectangle viewport = new Rectangle(0, 0, this.getWidth(),
		    this.getHeight());

	    // translate the viewport in the opposite directions
	    viewport.translate(-1 * translationX, -1 * translationY);

	    leftIndex = displayImg.XToTileX(viewport.x);
	    if (leftIndex < minTileIndexX)
		leftIndex = minTileIndexX;
	    if (leftIndex > maxTileIndexX)
		leftIndex = maxTileIndexX;

	    rightIndex = displayImg.XToTileX(viewport.x + viewport.width - 1);
	    if (rightIndex < minTileIndexX)
		rightIndex = minTileIndexX;
	    if (rightIndex > maxTileIndexX)
		rightIndex = maxTileIndexX;

	    topIndex = displayImg.YToTileY(viewport.y);
	    if (topIndex < minTileIndexY)
		topIndex = minTileIndexY;
	    if (topIndex > maxTileIndexY)
		topIndex = maxTileIndexY;

	    bottomIndex = displayImg.YToTileY(viewport.y + viewport.height - 1);
	    if (bottomIndex < minTileIndexY)
		bottomIndex = minTileIndexY;
	    if (bottomIndex > maxTileIndexY)
		bottomIndex = maxTileIndexY;

	    //set all pixels in the graphics of frameBuffer to a gray
	    g.setColor(this.getParent().getBackground());
	    g.fillRect(0, 0, rect.width, rect.height);

	    for (int tileY = topIndex; tileY <= bottomIndex; tileY++) {
		for (int tileX = leftIndex; tileX <= rightIndex; tileX++) {
		    WritableRaster tile = displayImg.getWritableTile(tileX,
			    tileY);
		    // necessary to set the minX & minY coords to 0,0;
		    // otherwise->
		    // error when creating the BufferedImage
		    WritableRaster translatedTile = (WritableRaster) tile
			    .createChild(tileX * tile.getWidth(),
				    tileY * tile.getHeight(), tile.getWidth(),
				    tile.getHeight(), 0, 0, null);
		    BufferedImage bufferedImageTile = new BufferedImage(
			    colorModel, translatedTile,
			    colorModel.isAlphaPremultiplied(), null);
		    AffineTransform atx = AffineTransform.getTranslateInstance(
			    tile.getMinX() + translationX, tile.getMinY()
				    + translationY);
		    g.drawRenderedImage(bufferedImageTile, atx);
		}
	    }
	    renderingFinished = true;

	    // draw newly calculated frame
	    g2.drawImage(frameBuffer, 0, 0, null);
	} else {

	    // draw frame from buffer
	    g2.drawImage(frameBuffer, 0, 0, null);
	    // visualize certain tonal range
	    visualizeTonalRange(frameBuffer, g2);
	}
	drawMouseLabel(g2);
    }

    @Override
    protected void visualizeTonalRange(BufferedImage fb, Graphics2D g) {
	if (isVisualizeTonalRange) {
	    Rectangle rect = g.getClipBounds();
	    Raster r = frameBuffer.getData();
	    DataBuffer db = r.getDataBuffer();
	    int scanline = rect.width * numBands;
	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		    0.6f));
	    g.setColor(Color.RED);

	    if (bitsPerChannel == 16 && numBands == 1) {
		for (int y = 0; y < rect.height; y++) {
		    for (int x = 0; x < rect.width; x++) {
			if ((db.getElem(y * rect.width + x) & 0xffff) > lowToneToVisualize
				&& (db.getElem(y * rect.width + x) & 0xffff) < highToneToVisualize)
			    g.drawOval(x, y, 2, 2);

		    }
		}
	    } else if (bitsPerChannel == 16 && numBands == 3) {
		/*
		for (int y = 0; y < rect.height; y++) {
		    for (int x = 1; x < scanline - numBands; x += numBands) {
			if ((db.getElem(y * scanline + x)) > lowToneToVisualize
				&& (db.getElem(y * scanline + x)) < highToneToVisualize)
			    g.drawOval(x, y, 2, 2);

		    }
		}
		*/
	    } else if (bitsPerChannel == 8 && numBands == 3) {
		int[] pixelData = frameBuffer.getRGB(0, 0, rect.width,
			rect.height, null, 0, rect.width);

		for (int y = 0; y < rect.height; y++) {
		    for (int x = 0; x < rect.width; x++) {
			if (((pixelData[y * rect.width + x] >> 8) & 0xFF) > lowToneToVisualize
				&& ((pixelData[y * rect.width + x] >> 8) & 0xff) < highToneToVisualize)
			    g.drawOval(x, y, 2, 2);

		    }
		}

	    }
	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	}
    }

    int lineOffset = 20;
    int mousePadding = 24;

    @Override
    protected void drawMouseLabel(Graphics2D g) {
	if (this.mouseLabel != null && mouseLabel.isVisible) {
	    // retrieve the hovered tile
	    int tileXX = displayImg.XToTileX(mouseLabel.x - 1 * translationX);
	    int tileYY = displayImg.YToTileY(mouseLabel.y - translationY);
	    WritableRaster tile = displayImg.getWritableTile(tileXX, tileYY);
	    if (tile != null) {
		// draw black background
		g.setColor(Color.black);
		g.setComposite(AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.4f));
		g.fillRoundRect(mouseLabel.x + mousePadding / 2,
			mouseLabel.y - 15, 130, lineOffset * 2, lineOffset / 2,
			lineOffset / 2);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		g.setColor(Color.white);

		// draw information
		int[] pixelValues = tile.getPixel(mouseLabel.x - translationX,
			mouseLabel.y - translationY,
			new int[displayImg.getNumBands()]);

		String s = ("x,y: " + (mouseLabel.x - 1 * translationX) + ", " + (mouseLabel.y - translationY));
		g.drawString(s, mouseLabel.x + mousePadding, mouseLabel.y);

		for (int i = 0; i < pixelValues.length; i++) {
		    g.drawString(
			    (int) ((pixelValues[i] & 0xffff)
				    / Math.pow(2, bitsPerChannel) * 100)
				    + "%; ", mouseLabel.x + mousePadding
				    + (int) (mousePadding * 1.5 * i),
			    mouseLabel.y + lineOffset);
		}
		// fire property change event, which is determined for the
		// histogram update
		this.firePropertyChange(BasePanel.HISTOGRAM_UPDATE, 0,
			pixelValues.length > 1 ? pixelValues[1]
				: pixelValues[0]);
	    }
	}

    }

    public RenderedImage getOrigImg() {
	return origImg;
    }

    public void setOrigImg(RenderedImage img) {
	this.origImg = img;
	setImage(img);
    }

    public int getTileWidth() {
	return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
	this.tileWidth = tileWidth;
	prepareImageForRendering(origImg);
    }

    public int getTileHeight() {
	return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
	this.tileHeight = tileHeight;
	prepareImageForRendering(origImg);
    }

    int lowToneToVisualize;
    int highToneToVisualize;

    @Override
    public void updateComponent(PropertyChangeEvent e) {
	if (e.getPropertyName().equals(BasePanel.VIEWPORT_UPDATE)) {
	    isVisualizeTonalRange = true;
	    lowToneToVisualize = (int) ((Integer) e.getOldValue()
		    * Math.pow(2, bitsPerChannel) / 256);
	    highToneToVisualize = (int) ((Integer) e.getNewValue()
		    * Math.pow(2, bitsPerChannel) / 256);
	    repaint();
	} else if (e.getPropertyName().equals(BasePanel.VIEWPORT_END_UPDATE)) {
	    isVisualizeTonalRange = (Boolean) e.getNewValue();
	}
    }

    @Override
    public int[] getInitTranslationFactors() {
	return new int[] { translationX, translationY };
    }
}
