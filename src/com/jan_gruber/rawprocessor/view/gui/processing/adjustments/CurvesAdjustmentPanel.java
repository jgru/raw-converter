package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.util.LUTFactory;
import com.jan_gruber.rawprocessor.util.Spline;
import com.jan_gruber.rawprocessor.view.gui.ColorDefinitions;
import com.spinn3r.log5j.Logger;

public class CurvesAdjustmentPanel extends AdjustmentPanel {
    private static final Logger LOGGER = Logger.getLogger();
    final String name = "Curves";
    private CurvePanel mCurvePanel;
    private JPanel togglePanel;
    public final int ALL_CHANNELS = 0;
    public final int RED_CHANNEL = 1;
    private final int GREEN_CHANNEL = 2;
    private final int BLUE_CHANNEL = 3;

    public CurvesAdjustmentPanel(int paneLength) {
	super();
	opName = ProcessingParameters.LUT;
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	togglePanel = initToggleButtons();
	this.add(togglePanel);

	mCurvePanel = new CurvePanel(paneLength);
	this.add(mCurvePanel);
    }

    private void channelChanged(int channel) {
	mCurvePanel.channelChanged(channel);

    }

    private class CurvePanel extends JPanel implements MouseMotionListener,
	    MouseListener {
	private int paneLength;
	private int channelSelector = 0;

	public CurvePanel(int paneLength) {
	    this.paneLength = paneLength;
	    this.setPreferredSize(new Dimension(paneLength, paneLength));
	    this.setMaximumSize(new Dimension(paneLength, paneLength));
	    this.addMouseListener(this);
	    this.addMouseMotionListener(this);
	    initCurve();
	}

	public void channelChanged(int channel) {
	    channelSelector = channel;
	    switch (channelSelector) {
	    case ALL_CHANNELS:
		currentColor = Color.black;
		break;
	    case RED_CHANNEL:
		currentColor = Color.red;
		break;
	    case GREEN_CHANNEL:
		currentColor = Color.green;
		break;
	    case BLUE_CHANNEL:
		currentColor = Color.blue;
		break;
	    default:
		break;
	    }

	    repaint();
	}

	final int SIZE = 256;
	int border = 44;
	int pointSize = 6;

	ArrayList<ArrayList<Point>> pointCollections = new ArrayList<ArrayList<Point>>();
	Point p1; // Start Point
	Point p2; // End Point 
	Rectangle r2;

	private void initCurve() {
	    border = paneLength - SIZE;
	    r2 = new Rectangle(0, 0, paneLength - border, paneLength - border);
	    p1 = new Point(r2.x, 0);
	    p2 = new Point(r2.width, r2.height);
	    splines = new Spline[4];
	    for (int i = 0; i < 4; i++) {

		ArrayList<Point> points = new ArrayList<Point>();
		points.add(p1);
		points.add(new Point((int) r2.getCenterX(), (int) r2
			.getCenterY()));
		points.add(p2);
		pointCollections.add(points);
		splines[i] = computeCubicSpline(pointCollections.get(i));
	    }

	}

	public short[][] getLUT() {
	    if (channelSelector == ALL_CHANNELS) {
		Spline s = computeCubicSplineWith16BitValuation(pointCollections
			.get(0));
		short[][] combinedLUT = LUTFactory.create16BitRGB_LUT(s);

		return combinedLUT;
	    } else {
		Spline[] convertedSplines = new Spline[3];
		for (int i = 0; i < convertedSplines.length; i++) {
		    convertedSplines[i] = computeCubicSplineWith16BitValuation(pointCollections
			    .get(i + 1));
		}
		short[][] rgbLUT= LUTFactory.create16BitRGB_LUT(convertedSplines);
		return rgbLUT;
	    }

	}

	private Spline computeCubicSpline(ArrayList<Point> coords2) {
	    Collections.sort(coords2);
	    int[] tabX = new int[coords2.size()];
	    int[] tabY = new int[coords2.size()];
	    int i = 0;
	    for (Point2D p : coords2) {
		tabX[i] = (int) p.getX();
		tabY[i] = (int) p.getY();
		i++;
	    }
	    Spline mSpline = new Spline(tabX, tabY);
	    return mSpline;
	}

	private Spline computeCubicSplineWith16BitValuation(
		ArrayList<Point> coords2) {
	    Collections.sort(coords2);
	    //coords in a virtual 0-> 65535 coord system
	    ArrayList<Point> convertedCoords = new ArrayList<Point>();
	    for (Point p : coords2) {
		Point conP = new Point((p.getX() / SIZE)
			* LUTFactory.VALUATION_16_BIT, (p.getY() / SIZE)
			* LUTFactory.VALUATION_16_BIT);
		convertedCoords.add(conP);
	    }

	    return computeCubicSpline(convertedCoords);
	}

	Spline[] splines;

	private void addPoint(int x1, int y1) {
	    LOGGER.info("addPoint");
	    pointCollections.get(channelSelector).add(new Point(x1, y1));
	    splines[channelSelector] = computeCubicSpline(pointCollections
		    .get(channelSelector));
	}

	private Point checkForCollision(int x, int y) {
	    for (Point p : pointCollections.get(channelSelector)) {
		if (p.getBoundingBox().contains(x, y))
		    return p;
	    }

	    return null;
	}

	Color currentColor = Color.black;

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;

	    BufferedImage content = new BufferedImage(r2.height, r2.height,
		    BufferedImage.TYPE_4BYTE_ABGR);
	    Graphics2D g3 = (Graphics2D) content.getGraphics();
	    drawBackground2(g3, r2);

	    g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    g3.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

	    int padding = 25;
	    //draw mouse cursor lines
	    if (visualizeMouse) {
		g3.setColor(Color.BLUE);
		g3.drawLine(mouseX, r2.height, mouseX, mouseY);
		g3.drawLine(r2.width, mouseY, mouseX, mouseY);
		g3.drawString("" + mouseX, r2.width - padding, mouseY);
		g3.drawString("" + (256 - mouseY), mouseX - padding, r2.height);
	    }

	    //draw spline
	    if (channelSelector == ALL_CHANNELS) {
		drawCombinedSpline(g3);
		
		//draw the points of the selected spline
				Point[] tmpPoints = pointCollections.get(channelSelector)
					.toArray(
						new Point[pointCollections.get(channelSelector)
							.size()]);
				for (Point p : tmpPoints) {
				    g3.drawOval((int) (p.x) - pointSize / 2, r2.height
					    - (int) (p.y) - pointSize / 2, pointSize, pointSize);
				}

	    } else {
		drawSeparateRGBSplines(g3);

		//draw the selected spline again, so that it is on top
		g3.setColor(currentColor);
		int prevY = 0;
		int prevX = 0;
		for (int x = 0; x < r2.width; x += 1) {
		    int y = splines[channelSelector].getValue(x);
		    g3.drawLine((int) (prevY), r2.height - (int) (prevX),
			    (int) (x), r2.height - (int) (y));
		    prevX = y;
		    prevY = x;
		}
		//draw the points of the selected spline
		Point[] tmpPoints = pointCollections.get(channelSelector)
			.toArray(
				new Point[pointCollections.get(channelSelector)
					.size()]);
		for (Point p : tmpPoints) {
		    g3.drawOval((int) (p.x) - pointSize / 2, r2.height
			    - (int) (p.y) - pointSize / 2, pointSize, pointSize);
		}

	    }

	    g3.dispose();
	    g2.drawImage(content, border / 2, border / 2, null);
	}

	private void drawSeparateRGBSplines(Graphics2D g) {
	    g.setColor(Color.red);
	    int prevY = 0;
	    int prevX = 0;
	    for (int x = 0; x < r2.width; x += 1) {
		int y = splines[1].getValue(x);

		g.drawLine((int) (prevY), r2.height - (int) (prevX), (int) (x),
			r2.height - (int) (y));
		prevX = y;
		prevY = x;
	    }

	    g.setColor(Color.green);
	    prevY = 0;
	    prevX = 0;
	    for (int x = 0; x < r2.width; x += 1) {
		int y = splines[2].getValue(x);

		g.drawLine((int) (prevY), r2.height - (int) (prevX), (int) (x),
			r2.height - (int) (y));
		prevX = y;
		prevY = x;
	    }
	    g.setColor(Color.blue);
	    prevY = 0;
	    prevX = 0;
	    for (int x = 0; x < r2.width; x += 1) {
		int y = splines[3].getValue(x);

		g.drawLine((int) (prevY), r2.height - (int) (prevX), (int) (x),
			r2.height - (int) (y));
		prevX = y;
		prevY = x;
	    }

	}

	private void drawCombinedSpline(Graphics2D g) {
	    g.setColor(currentColor);
	    int prevY = 0;
	    int prevX = 0;
	    for (int x = 0; x < r2.width; x += 1) {
		int y = splines[0].getValue(x);

		g.drawLine((int) (prevY), r2.height - (int) (prevX), (int) (x),
			r2.height - (int) (y));
		prevX = y;
		prevY = x;
	    }

	}

	private void drawBackground2(Graphics2D g, Rectangle r) {
	    //draw white background
	    g.setColor(ColorDefinitions.labelColor);
	    g.fillRect(r.x, r.y, r.width, r.height);
	    //draw lines to structure area
	    g.setColor(Color.DARK_GRAY);
	    g.drawLine(r.x, r.y, r.x, r.height);
	    g.drawLine(r.x + (r.width) / 4, r.y, r.x + (r.width) / 4, r.height);
	    g.drawLine(r.x + ((r.width) / 2), r.y, r.x + ((r.width) / 2),
		    r.height);
	    g.drawLine(r.x + ((r.width) / 4) * 3, r.y, r.x + ((r.width) / 4)
		    * 3, r.height);

	    g.drawLine(r.x, r.y, r.width, r.y);
	    g.drawLine(r.x, r.y + (r.height) / 2, r.width, r.y
		    + ((r.height) / 2));
	    g.drawLine(r.x, r.y + r.height / 4, r.width, r.y + r.height / 4);
	    g.drawLine(r.x, r.y + (r.height) * 3 / 4, r.width, r.y
		    + ((r.height) * 3 / 4));

	}

	private class Point extends Point2D.Double implements Comparable<Point> {
	    int pointSize = 15;
	    Rectangle2D boundingBox;

	    public Point(double x, double y) {
		super(x, y);
		boundingBox = new Rectangle2D.Double();
		boundingBox.setFrame(x - pointSize / 2, y - pointSize / 2,
			pointSize, pointSize);
	    }

	    public void setX(double x) {
		this.x = x;
		updateBoundingBox();
	    }

	    public void setY(double y) {
		this.y = y;
	    }

	    private void updateBoundingBox() {
		boundingBox.setFrame(this.x - pointSize / 2, this.y - pointSize
			/ 2, pointSize, pointSize);
	    }

	    public Rectangle2D getBoundingBox() {
		return boundingBox;
	    }

	    @Override
	    public int compareTo(Point o) {
		return (int) (this.x - o.x);
	    }
	}

	int x;
	int y;

	int mouseX;
	int mouseY;
	boolean visualizeMouse;

	@Override
	public void mouseDragged(MouseEvent e) {
	    if (tmpPoint != null) {
		tmpPoint.setX(e.getX() - border / 2);
		tmpPoint.setY(r2.height - e.getY() + border / 2);

		if (!r2.contains(tmpPoint)) {
		    pointCollections.get(channelSelector).remove(tmpPoint);
		    return;
		}
		repaint();
	    }

	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    visualizeMouse = true;
	    mouseX = e.getX() - border / 2;
	    mouseY = e.getY() - border / 2;
	    repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	    //mousePressed handles click events too
	}

	int origX;
	int origY;
	Point tmpPoint;

	@Override
	public void mousePressed(MouseEvent e) {
	    origX = e.getX() - border / 2;
	    origY = r2.height - e.getY() + border / 2;
	    Point p = checkForCollision(origX, origY);
	    if (p != null)
		tmpPoint = p;
	    else {
		addPoint(origX, origY);
	    }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    if (tmpPoint != null) {
		splines[channelSelector] = computeCubicSpline(pointCollections
			.get(channelSelector));
		tmpPoint = null;
	    }
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	    mouseX = e.getX() - border / 2;
	    mouseY = e.getY() - border / 2;
	    repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    visualizeMouse = false;
	    mouseX = r2.width;
	    mouseY = r2.height;
	    repaint();

	}
    }

    ArrayList<JToggleButton> mToggles = new ArrayList<JToggleButton>();
    JToggleButton rgbToggle;
    JToggleButton rToggle;
    JToggleButton gToggle;
    JToggleButton bToggle;

    private JPanel initToggleButtons() {
	JPanel selectionPanel = new JPanel();
	selectionPanel
		.setLayout(new BoxLayout(selectionPanel, BoxLayout.X_AXIS));
	ChannelChangeListener mChangeListener = new ChannelChangeListener();

	rgbToggle = new JToggleButton("RGB", true);
	rgbToggle.addActionListener(mChangeListener);
	selectionPanel.add(rgbToggle);
	mToggles.add(ALL_CHANNELS, rgbToggle);

	rToggle = new JToggleButton("Red", false);
	rToggle.addActionListener(mChangeListener);
	selectionPanel.add(rToggle);
	mToggles.add(RED_CHANNEL, rToggle);

	gToggle = new JToggleButton("Green", false);
	gToggle.addActionListener(mChangeListener);
	selectionPanel.add(gToggle);
	mToggles.add(GREEN_CHANNEL, gToggle);

	bToggle = new JToggleButton("Blue", false);
	bToggle.addActionListener(mChangeListener);
	selectionPanel.add(bToggle);
	mToggles.add(BLUE_CHANNEL, bToggle);

	return selectionPanel;
    }

    class ChannelChangeListener implements ActionListener{


	@Override
	public void actionPerformed(ActionEvent e) {
	    JToggleButton b = (JToggleButton) e.getSource();
	    channelChanged(mToggles.indexOf(b));
	    if (b.isSelected()) {
		for (JToggleButton tmp : mToggles) {
		    if (tmp.equals(b)) {
			continue;
		    } else
			tmp.setSelected(false);
		}
	    }	    
	}

    }

    public String getName() {
	return name;
    }

    public short[][] getLUT() {
	return mCurvePanel.getLUT();
    }

}
