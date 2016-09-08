package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.beans.PropertyChangeEvent;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputListener;

import com.jan_gruber.rawprocessor.view.gui.ColorDefinitions;
import com.jan_gruber.rawprocessor.view.gui.UpdatableComponent;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai.Viewport;
import com.spinn3r.log5j.Logger;

/**
 * @author JanGruber
 * @10.10.2013
 * 
 *             Specifies a GUI element, which displays the histogram of a
 *             BufferedImage.
 */
public class HistogramPanel extends JPanel implements MouseMotionListener,
	MouseInputListener, UpdatableComponent {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger();
    Histogram histogram;
    int binToVisualize;
    TitledBorder tb;
    int bitDepth;
    Composite mAdditiveComposite;

    public HistogramPanel() {
	super();
	this.setPreferredSize(new Dimension(300, 120));
	this.setMaximumSize(new Dimension(300, 180));
	tb = new TitledBorder("Histogram");
	this.setBorder(tb);
	this.addMouseMotionListener(this);
	this.addMouseListener(this);

    }

    /**
     * Gets the currently displayed BufferedImage and induces all necessary
     * steps to determine and display its histogram
     */
    public void update(BasePanel mBasePanel) {

	Viewport vp = mBasePanel.getViewport(); //(Viewport) mBasePanel.getSelectedComponent();
	RenderedImage input = vp.getOrigImg();
	if (input != null) {
	    bitDepth = input.getSampleModel().getSampleSize(0);

	    ParameterBlock pb = new ParameterBlock();
	    pb.addSource(input);
	    pb.add(null); // The ROI.
	    pb.add(1); // Samplings.
	    pb.add(1);
	    pb.add(new int[] { 256 }); // Num. bins.
	    pb.add(new double[] { 0 }); // lowest 
	    pb.add(new double[] { Math.pow(2, bitDepth) }); // set highest value
	    PlanarImage tmp = JAI.create("histogram", pb);
	    histogram = (Histogram) tmp.getProperty("histogram");

	    //normalize histogram for drawing
	    Thread t = new Thread() {
		@Override
		public void run() {
		    LOGGER.info("normalize thread");
		    normalizeTonalValueDistribution();
		}
	    };
	    t.start();
	}

	tb.setTitle("Histogram - " + bitDepth + " Bits per Sample");
	repaint();

    }

    /**
     * normalizes the channel data to normed floats. Otherwise the histogram's
     * amplitude would be dependent on the image resolution
     */
    float[][] normalizedBins;
    int max = 0;

    private void normalizeTonalValueDistribution() {
	normalizedBins = new float[histogram.getNumBands()][];

	for (int i = 0; i < histogram.getBins().length; i++) {
	    normalizedBins[i] = new float[histogram.getBins()[i].length];
	    for (int j = 0; j < normalizedBins[i].length; j++) {
		int tmpVal = histogram.getBins()[i][j] & 0xFFFF;
		max = tmpVal > max ? tmpVal : max;
	    }
	    for (int j = 0; j < normalizedBins[i].length; j++) {
		normalizedBins[i][j] = (float) ((histogram.getBins()[i][j] & 0xFFFF) / (max * 1f));
	    }
	}
    }

    int lineLength = 0;
    int xOffset;
    int yOffset;
    Rectangle rect;

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;

	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);

	//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
	//	0.8f));

	rect = g2.getClipBounds();
	int width = rect.width;
	int height = rect.height;
	if (histogram != null) {
	    xOffset = rect.width - histogram.getNumBins(0);
	    yOffset = rect.height / 4;
	    Rectangle bounds = new Rectangle(xOffset / 2, yOffset / 2, width
		    - xOffset, height - yOffset);
	    // color for ARGBs
	    Color[] colorPalette = { Color.RED, Color.GREEN, Color.BLUE,
		    Color.WHITE };

	    g2.setColor(ColorDefinitions.labelColor);
	    String highCaption = "" + (int) histogram.getHighValue(0);
	    g2.drawString(highCaption, bounds.width - 15, height - 10);

	    String middleCaption = "" + (int) histogram.getHighValue(0) / 2;
	    g2.drawString(middleCaption, bounds.width / 2, height - 10);

	    String lowCaption = "" + (int) histogram.getLowValue(0);
	    g2.drawString(lowCaption, bounds.x + 2, height - 10);

	    int skip = normalizedBins[0].length / 8;
	    if (histogram != null) {
		Polygon[] poly = new Polygon[3];

		for (int i = 0; i < normalizedBins.length; i++) {
		    poly[i] = new Polygon();
		    poly[i].addPoint(bounds.x, bounds.height);

		    for (int j = 0; j < normalizedBins[i].length; j++) {
			// set appropriate color
			if (histogram.getNumBands() == 1)
			    g2.setColor(Color.BLACK);
			else
			    g2.setColor(colorPalette[i]);

			poly[i].addPoint(
				j + bounds.x,
				(int) ((int) bounds.height - normalizedBins[i][j]
					* (bounds.height - bounds.x)));

			// draw dividers on x-axis
			if (j % skip == 0 || j == normalizedBins[i].length - 1) {
			    if (j == 0 || j == normalizedBins[i].length / 2
				    || j == normalizedBins[i].length - 1)
				lineLength = bounds.height / 10;
			    else
				lineLength = bounds.height / 20;

			    g2.setColor(ColorDefinitions.labelColor);
			    g2.drawLine(j + bounds.x, bounds.height, j
				    + bounds.x, bounds.height + lineLength);
			}
		    }
		    poly[i].addPoint(normalizedBins[i].length - 1 + bounds.x,
			    bounds.height);
		    poly[i].addPoint(bounds.width, bounds.height);

		}
		
		//inspired by http://www.raditha.com/java/image/histogram.php
		Area red = new Area(poly[0]);
		Area green = new Area(poly[1]);
		Area blue = new Area(poly[2]);

		//draw rgb polygons
		g2.setColor(colorPalette[0]);
		g2.fill(red);

		g2.setColor(colorPalette[1]);
		g2.fill(green);

		g2.setColor(colorPalette[2]);
		g2.fill(blue);

		//retrieve intersection areas
		red.intersect(green);
		green.intersect(blue);
		blue.intersect(new Area(poly[0]));

		//draw intersection areas
		g2.setColor(new Color(255, 255, 0));
		g2.fill(red);

		g2.setColor(new Color(0, 255, 255));
		g2.fill(green);

		g2.setColor(new Color(255, 0, 255));
		g2.fill(blue);

		g2.setColor(Color.LIGHT_GRAY.darker());
		blue.intersect(new Area(poly[0]));
		g2.fill(blue);

		
		    g2.setColor(Color.white);
		    g2.drawLine(binToVisualize + bounds.x, bounds.height,
			    binToVisualize + bounds.x, bounds.y);
	    }

	}
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
	int tolerancy = 3;
	int tone = e.getX() - xOffset / 2;
	int lowVal = tone - tolerancy;
	int highVal = tone + tolerancy;
	firePropertyChange(BasePanel.VIEWPORT_UPDATE, lowVal, highVal);

	binToVisualize = e.getX() - xOffset / 2;
	repaint();
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
	firePropertyChange(BasePanel.VIEWPORT_END_UPDATE, true, false);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void updateComponent(PropertyChangeEvent e) {
	if (e.getPropertyName().equals(BasePanel.HISTOGRAM_UPDATE)) {
	    binToVisualize = (Integer) e.getNewValue();
	    if (bitDepth == 16)
		// scale down to 256
		binToVisualize = (int) (binToVisualize * 1d / 0xffff * 1d * 0xff);

	    repaint();
	} else if (e.getPropertyName().equals(BasePanel.STATE_UPDATE))
	    update((BasePanel) e.getNewValue());
    }

}
