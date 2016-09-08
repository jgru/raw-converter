package com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai;

import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.spinn3r.log5j.Logger;

public class ViewportController {
	private static final Logger LOGGER = Logger.getLogger();
	ArrayList<Viewport> associatedViewports = new ArrayList<Viewport>();
	MouseEventProcessor mMouseProcessor;
	KeyboardProcessor mKeyProcessor;
	AffineTransform mTransform;

	// vertical and horizontal panning of the viewports content
	int translationX=50; 
	int translationY=50;
	// ranges from 1 (==100%) to 0.1 (10%)
	double magnifyFactor = 1;
	double maxMagnifyFactor;
	// used to scale content to a higher density screen
	float mappingFactor = 1;

	public ViewportController() {
		handleHdpiScreens();
	}

	private void handleHdpiScreens() {
		if (Toolkit.getDefaultToolkit().getDesktopProperty(
				"apple.awt.contentScaleFactor") != null) {
			// Mac OS (returns 2.0 for hdpi display, 1.0 for others
			mappingFactor = (Float) Toolkit.getDefaultToolkit()
					.getDesktopProperty("apple.awt.contentScaleFactor");
		} else
			// other platform
			mappingFactor = 1;
		magnifyFactor /= mappingFactor;
		maxMagnifyFactor = magnifyFactor;
		magnifyFactor=0.15;
		transformViewports();
	}

	public void registerViewport(Viewport viewport) {
		this.associatedViewports.add(viewport);
		
		//get initial translation factors from registered viewport
		translationX=viewport.getInitTranslationFactors()[0];
		translationY=viewport.getInitTranslationFactors()[1];
		
		setupEventHandling(viewport);
		
		// performs an initial transform to scale for a potential hdpi display
		transformViewports();
	}

	private void setupEventHandling(Viewport mViewport) {
		mMouseProcessor = new MouseEventProcessor(mViewport);
		mViewport.addMouseListener(mMouseProcessor);
		mViewport.addMouseMotionListener(mMouseProcessor);
		mViewport.addMouseWheelListener(mMouseProcessor);
		mKeyProcessor = new KeyboardProcessor();
	}

	protected void translateViewports() {
		for (Viewport v : associatedViewports) {
			v.viewportTranslated(translationX, translationY);
		}
	}

	Thread transformerThread;
	Runnable transformerRunnable = new Runnable() {

		@Override
		public void run() {
			for (Viewport v : associatedViewports) {
				
			    v.viewportTransformed(magnifyFactor);
				v.firePropertyChange(BasePanel.INFO_UPDATE, 0,
						(int) (magnifyFactor * mappingFactor * 100));
			}
		}
	};

	protected void transformViewports() {
		magnifyFactor = magnifyFactor > maxMagnifyFactor ? maxMagnifyFactor
				: magnifyFactor < 0.1 ? 0.1 : magnifyFactor;
		
		if (transformerThread != null && transformerThread.isAlive())
			try {
				transformerThread.join();
			} catch (InterruptedException e) {
				LOGGER.error("Error while joining transformerThread");
				LOGGER.error(e);
			}

		transformerThread = new Thread(transformerRunnable);
		transformerThread.start();

	}

	public int getMagnificationInPercent() {
		return (int) (magnifyFactor * mappingFactor * 100);
	}

	private class MouseEventProcessor implements MouseInputListener,
			MouseWheelListener {
		Viewport vp;
		private final PropertyChangeSupport pcs = new PropertyChangeSupport(
				this);
		MouseInfo ml;
		int x = 0;
		int y = 0;

		MouseEventProcessor(Viewport vp) {
			super();
			this.vp = vp;
			ml = vp.getMouseLabel();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			vp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			ml.isVisible = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			ml.isVisible = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			x = e.getX() - translationX;
			y = e.getY() - translationY;

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			ml.isVisible = true;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			ml.isVisible = false;
			vp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			translationX = e.getX() - x;
			translationY = e.getY() - y;
			translateViewports();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			vp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			ml.x = e.getX();
			ml.y = e.getY();
			vp.repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			if (arg0.isMetaDown()) {
				translationY -= arg0.getWheelRotation() * 10;
				translateViewports();
			} else if (arg0.isAltDown()) {
				magnifyFactor += (arg0.getWheelRotation() * 0.05);
				transformViewports();
			}
		}
	}

	private class KeyboardProcessor {
		KeyboardFocusManager keyboardManager;
		ActionMap shortcutMap;

		public KeyboardProcessor() {
			shortcutMap = createActionMap();
			keyboardManager = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();

			keyboardManager.addKeyEventDispatcher(new KeyEventDispatcher() {

				@Override
				public boolean dispatchKeyEvent(KeyEvent e) {
					KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
					final Action a = shortcutMap.get(keyStroke);
					if (a != null) {
						final ActionEvent ae = new ActionEvent(e.getSource(), e
								.getID(), null);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								a.actionPerformed(ae);
							}
						});
						return true;
					}
					return false;
				}
			});

		}

		private ActionMap createActionMap() {
			ActionMap actionMap = new ActionMap();
			// TODO fix cmd + & cmd -
			KeyStroke zoomInShortCut = KeyStroke.getKeyStroke(
					KeyEvent.VK_PERIOD, KeyEvent.META_DOWN_MASK);
			actionMap.put(zoomInShortCut, new AbstractAction("Zoom In") {
				@Override
				public void actionPerformed(ActionEvent e) {
					magnifyFactor += 0.05;
					transformViewports();
					// ScrollPane sp = (ScrollPane)
					// mBasePanel.getSelectedComponent();
					// sp.mCanvas.setScale(sp.mCanvas.getScale() + 0.1);
				}
			});
			KeyStroke zoomToFitShortCut = KeyStroke.getKeyStroke(KeyEvent.VK_0,
					KeyEvent.META_DOWN_MASK);
			actionMap.put(zoomToFitShortCut, new AbstractAction("Zoom to fit") {

				@Override
				public void actionPerformed(ActionEvent e) {
					magnifyFactor = 0.3;
					transformViewports();
				}
			});
			KeyStroke zoomOutShortCut = KeyStroke.getKeyStroke(
					KeyEvent.VK_COMMA, KeyEvent.META_DOWN_MASK);
			actionMap.put(zoomOutShortCut, new AbstractAction("Zoom Out") {

				@Override
				public void actionPerformed(ActionEvent e) {
					magnifyFactor -= 0.05;
					transformViewports();
					// ScrollPane sp= (ScrollPane)
					// mBasePanel.getSelectedComponent();
					// sp.mCanvas.setScale(sp.mCanvas.getScale()-0.1);
					// mBottomInfoPanel.updateZoomLabel();
				}
			});
			// add more actions..
			return actionMap;

		}

	}
}
