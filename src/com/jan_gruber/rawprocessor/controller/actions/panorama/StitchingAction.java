package com.jan_gruber.rawprocessor.controller.actions.panorama;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;

import com.jan_gruber.rawprocessor.controller.PanoramaController;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaParameters;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.jan_gruber.rawprocessor.view.gui.panorama.PanoramaView;
import com.jan_gruber.rawprocessor.view.gui.panorama.ParamPanel;
import com.spinn3r.log5j.Logger;

public class StitchingAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger();
    private PanoramaController pController;
    private MainFrame f;
    private ParamPanel pp;

    public StitchingAction(PanoramaController pController, MainFrame f,
	    ParamPanel pp) {
	this.pController = pController;
	this.f = f;
	this.pp = pp;

	putValue(NAME, "Stitch Images");
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
		InputEvent.META_DOWN_MASK));
    }

    private String path;

    @Override
    public void actionPerformed(ActionEvent e) {
	if (pController.getImageCount() > 1) {
	    final PanoramaParameters fdp = pp
		    .getFeatureDetectionParams();

	    if (fdp.isExport()) {
		path = showFileChooser();
	    }

	    if (fdp.isExport() && path == null)
		return;

	    final JDialog progressDialog = initProgressDialog();

	    Thread stitchingThread = new Thread() {
		public void run() {
		    pController.startStitching(fdp, path);
		    LOGGER.info("Handed over to PanoramaController");

		    //close if all other events on the EventQueue are processed
		    EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
			    progressDialog.setVisible(false);
			    showSuccessDialog();
			    
			}

		    });
		}
	    };

	    stitchingThread.setPriority(Thread.MAX_PRIORITY);
	    stitchingThread.start();
	    progressDialog.setVisible(true);

	} else {
	    JOptionPane
		    .showMessageDialog(
			    f,
			    "Only one or no file opened.\nPlease open at least 2 images",
			    "Stitching Warning", JOptionPane.WARNING_MESSAGE);
	    LOGGER.info("At least 2 images neccessary to stich");
	}
    }

    private JDialog initProgressDialog() {
	JDialog progressDialog = new JDialog(f, "Stitching Dialog", true);
	JProgressBar progressBar = new JProgressBar(0, 500);
	progressBar.setIndeterminate(true);
	progressBar.setEnabled(false);
	progressDialog.add(progressBar, BorderLayout.CENTER);
	progressDialog.add(new JLabel(
		"Stitching selected images..."), BorderLayout.NORTH);

	progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	progressDialog.setSize(300, 75);
	progressDialog.setLocationRelativeTo(f);
	return progressDialog;
    }

    private String showFileChooser() {
	JFileChooser mFileChooser = new JFileChooser("/Users/");
	mFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	mFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	int returnVal = mFileChooser.showOpenDialog(null);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    LOGGER.info("Chosen File: "
		    + mFileChooser.getSelectedFile().getName());

	    return mFileChooser.getSelectedFile().getAbsolutePath();
	}
	return null;
    }
    private void showSuccessDialog() {
	JOptionPane
	    .showMessageDialog(
		    f,"Successful exported to "+ path,
		    "Finished", JOptionPane.NO_OPTION);
	    
	}
}
