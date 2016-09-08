package com.jan_gruber.rawprocessor.controller.actions.panorama;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;

import com.jan_gruber.rawprocessor.controller.PanoramaController;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.jan_gruber.rawprocessor.view.gui.panorama.ParamPanel;
import com.spinn3r.log5j.Logger;

public class FeatureDetectionAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger();
    private PanoramaController pController;
    private MainFrame f;
    private ParamPanel pv;
    
    public FeatureDetectionAction(PanoramaController pController, MainFrame f, ParamPanel pv) {
	this.pController = pController;
	this.f = f;
	this.pv=pv;
	
	putValue(NAME, "Detect Features");
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D,
		InputEvent.META_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (pController.getImageContainer() != null) {
	
	final JDialog progressDialog = new JDialog(f, "Feature Detection Dialog",
		true);
	JProgressBar progressBar = new JProgressBar(0, 500);
	progressBar.setIndeterminate(true);
	progressBar.setEnabled(false);
	progressDialog.add(BorderLayout.CENTER, progressBar);
	progressDialog.add(BorderLayout.NORTH, new JLabel("Detecting points of interest..."));
	progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	progressDialog.setSize(300, 75);
	progressDialog.setLocationRelativeTo(f);

	if(pv!=null)
	    System.err.println("!=null");
	else
	    System.err.println("NULLNULL");
	    
	Thread processorThread = new Thread() {
	    public void run() {
		pController.startFeatureDetection(pv.getFeatureDetectionParams());
		LOGGER.info("Handed over to PanoramaController");
		

		//close if all other events on the EventQueue are processed
		EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			progressDialog.setVisible(false);

		    }
		});
	    }
	};

	processorThread.setPriority(Thread.MAX_PRIORITY);
	processorThread.start();
	progressDialog.setVisible(true);

	} else
	    LOGGER.info("Error");

    }
}
