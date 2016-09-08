package com.jan_gruber.rawprocessor.main;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.BasicConfigurator;

import com.jan_gruber.rawprocessor.controller.MasterController;
import com.jan_gruber.rawprocessor.view.gui.ColorDefinitions;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.spinn3r.log5j.Logger;

public class CR2_Processor {
    private static final Logger LOGGER = Logger.getLogger();

    public static void main(String[] args) {
	BasicConfigurator.configure();
	LOGGER.info("starting program");

	// launch the GUI
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		setLookAndFeel();
		new MainFrame(new MasterController());
	    }
	});

    }

    private static void setLookAndFeel() {
	// set properties to for menu bar of Mac OS X
	System.setProperty("apple.laf.useScreenMenuBar", "true");

	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

	    //copy progress bar of System LAF
	    HashMap<Object, Object> progressDefaults = new HashMap<Object, Object>();
	    for (Map.Entry<Object, Object> entry : UIManager.getDefaults()
		    .entrySet()) {
		if (entry.getKey().getClass() == String.class
			&& ((String) entry.getKey()).startsWith("ProgressBar")) {
		    progressDefaults.put(entry.getKey(), entry.getValue());
		}
	    }

	    prepareLayout();

	    UIManager
		    .setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");

	    // copy back progress bar of metal Look and Feel
	    for (Map.Entry<Object, Object> entry : progressDefaults.entrySet()) {
		UIManager.getDefaults().put(entry.getKey(), entry.getValue());
	    }
	} catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InstantiationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (UnsupportedLookAndFeelException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private static void prepareLayout() {
	UIManager.put("nimbusBase", ColorDefinitions.baseColor);
	UIManager.put("control", ColorDefinitions.controlColor);
	UIManager.put("nimbusBlueGrey", ColorDefinitions.bgColor);
	//UIManager.put("Panel.background", ColorDefinitions.bgColor);
	UIManager.put("Label[Enabled].textForeground",
		ColorDefinitions.labelColor);
	//UIManager.put("foreground ", ColorDefinitions.labelColor);
	//UIManager.put("Label.foreground", ColorDefinitions.labelColor);
	//UIManager.put("foreground ", ColorDefinitions.labelColor);

	UIManager.put("FormattedTextField.background",
		ColorDefinitions.labelColor);

    }
}
