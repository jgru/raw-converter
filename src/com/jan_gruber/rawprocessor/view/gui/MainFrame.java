package com.jan_gruber.rawprocessor.view.gui;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.plaf.MenuBarUI;

import com.jan_gruber.rawprocessor.controller.Controller;
import com.jan_gruber.rawprocessor.controller.IOController;
import com.jan_gruber.rawprocessor.controller.MasterController;
import com.jan_gruber.rawprocessor.view.AnimatedComponentFactory;
import com.jan_gruber.rawprocessor.view.gui.panorama.PanoramaView;
import com.jan_gruber.rawprocessor.view.gui.processing.ProcessingView;
import com.mrlonee.swing.animation.AnimatedPanel;
import com.mrlonee.swing.animation.AnimatedPanel.AnimationType;
import com.spinn3r.log5j.Logger;

public class MainFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class);
    MasterController mController;
    int mode = 0;

    JMenuBar menuBar;
    AnimatedPanel processingView;
    AnimatedPanel panoramaView;

    public MainFrame(MasterController mController) {
	super();
	this.mController = mController;
	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	menuBar = createWindowMenuBar(mController.getProcessorController());
	this.setJMenuBar(menuBar);

	processingView = AnimatedComponentFactory
		.createAnimatedPanel(setupProcessingView());
	processingView.setAnimationType(AnimationType.SlideAnimationFromLeft);
	this.getContentPane().add(processingView);

	setupShortCuts();

	this.setSize(1400, 800);
	setLocation(0, 0);
	this.setVisible(true);

    }

    private JComponent setupProcessingView() {
	return new ProcessingView(mController.getProcessorController());
    }

    private JComponent setupPanoramaView() {
	return new PanoramaView(mController.getPanController(), this);
    }

    public void setPanoramaView() {
	mode = 1;

	if (processingView != null) {
	    processingView.setVisible(false);
	}
	if (panoramaView == null) {
	    panoramaView = AnimatedComponentFactory
		    .createAnimatedPanel(setupPanoramaView());
	   panoramaView.setAnimationType(AnimationType.SlideAnimationFromRight);
	    this.getContentPane().add(panoramaView);
	}

	panoramaView.setVisible(true);

	invalidate();
	repaint();
    }

    public void setProcessingView() {
	mode = 0;

	if (panoramaView != null) {
	    panoramaView.setVisible(false);
	}
	processingView.setVisible(true);

	invalidate();
	repaint();
    }

    private JMenuBar createWindowMenuBar(Controller mController) {
	boolean isMac = checkOS();

	IOController ioController = mController.getIoController();

	JMenuBar topMenuBar = new JMenuBar();
	// set up file menu
	JMenu fileMenu = new JMenu("File");
	JMenuItem loadItem = new JMenuItem(ioController.getFileOpenAction(this));

	if (!isMac) {
	    loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
		    Event.CTRL_MASK));
	} else if (isMac) {
	    loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
		    Event.META_MASK));
	}
	fileMenu.add(loadItem);

	JMenuItem closeItem = new JMenuItem("Quit ImageProcessor");
	fileMenu.add(closeItem);
	if (!isMac) {
	    closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		    Event.CTRL_MASK));
	} else if (isMac) {
	    closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
		    Event.META_MASK));
	}

	topMenuBar.add(fileMenu);

	JMenu utilMenu = new JMenu("Utils");
	JMenuItem metadataItem = new JMenuItem(
		ioController.getDisplayMetadataAction(this));
	if (!isMac) {
	    metadataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
		    Event.CTRL_MASK));
	} else if (isMac) {
	    metadataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
		    Event.META_MASK));
	}
	utilMenu.add(metadataItem);
	topMenuBar.add(utilMenu);

	JMenu panoramaMenu = new JMenu("Panorama");
	JMenuItem shiftItem = new JMenuItem(
		mController.getPanoramaViewAction(this));
	if (!isMac) {
	    shiftItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		    Event.CTRL_MASK));
	} else if (isMac) {
	    shiftItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
		    Event.META_MASK));
	}
	panoramaMenu.add(shiftItem);
	topMenuBar.add(panoramaMenu);

	JMenu exportMenu = new JMenu("Export");
	JMenuItem exportImageItem = new JMenuItem(
		ioController.getExportAction(this));
	if (!isMac) {
	    exportImageItem.setAccelerator(KeyStroke.getKeyStroke(
		    KeyEvent.VK_E, Event.CTRL_MASK));
	} else if (isMac) {
	    exportImageItem.setAccelerator(KeyStroke.getKeyStroke(
		    KeyEvent.VK_E, Event.META_MASK));
	}
	exportMenu.add(exportImageItem);
	topMenuBar.add(exportMenu);

	if (System.getProperty("os.name").equals("Mac OS X"))
	    try {
		topMenuBar.setUI((MenuBarUI) Class.forName(
			"com.apple.laf.AquaMenuBarUI").newInstance());
	    } catch (InstantiationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	return topMenuBar;
    }

    private boolean checkOS() {
	boolean isMac = false;
	String vers = System.getProperty("os.name").toLowerCase();
	if (vers.indexOf("windows") != -1) {
	    isMac = false;
	} else if (vers.indexOf("mac") != -1) {
	    isMac = true;
	}
	return isMac;
    }

    ActionMap shortCutActionMap = new ActionMap();

    private void setupShortCuts() {

    }

    public int getMode() {
	return mode;
    }

    public void setMode(int mode) {
	this.mode = mode;
    }
}
