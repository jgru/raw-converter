package com.jan_gruber.rawprocessor.controller.actions.io;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jan_gruber.rawprocessor.controller.ProcessorController;
import com.jan_gruber.rawprocessor.controller.SubController;
import com.jan_gruber.rawprocessor.model.engine.io.CR2ImageReader;
import com.jan_gruber.rawprocessor.model.engine.io.RawImageReader;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.spinn3r.log5j.Logger;

public class FileOpenAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger();
    private SubController sController;
    private MainFrame f;

    public FileOpenAction(SubController subController, MainFrame f) {
	this.sController = subController;
	this.f = f;
	putValue(NAME, "Open File");
	this.putValue(MNEMONIC_KEY, (int) 'O');
    }

    boolean isProcessing;

    @Override
    public void actionPerformed(ActionEvent e) {
	//switch perspective
	if (sController.getMaster() instanceof ProcessorController) {
	    isProcessing = true;
	    if (f.getMode() != 0)
		f.setProcessingView();
	}
	//get further user input
	JFileChooser mFileChooser = new JFileChooser("/Users/");
	FileNameExtensionFilter filter = new FileNameExtensionFilter(
		"Canon Raw Files", "cr2");
	mFileChooser.setDialogType(JFileChooser.FILES_ONLY);
	mFileChooser.setFileFilter(filter);

	int returnVal = mFileChooser.showOpenDialog(null);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    LOGGER.info("Chosen File: "
		    + mFileChooser.getSelectedFile().getName());

	    String fileName = mFileChooser.getSelectedFile().getName();
	    if (checkForDuplicate(fileName))
		return;

	    fileName = mFileChooser.getSelectedFile().getAbsolutePath();

	    // Load the specified file.
	    File file = new File(fileName);
	    final RawImageReader mReader = new CR2ImageReader(file);
	    mReader.addPropertyChangeListener(sController);

	    final JDialog progressDialog = new JDialog(f, "File IO Dialog",
		    true);
	    JProgressBar progressBar = new JProgressBar(0, 500);
	    progressBar.setIndeterminate(true);
	    progressBar.setEnabled(false);
	    progressDialog.add(progressBar, BorderLayout.CENTER);
	    progressDialog.add(new JLabel("Decoding Raw File..."),
		    BorderLayout.NORTH);
	    
	    progressDialog
		    .setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	    progressDialog.setSize(300, 75);
	    progressDialog.setLocationRelativeTo(f);

	    Thread readerThread = new Thread() {
		public void run() {
		    mReader.readFile(!isProcessing);

		    //close if all other events on the EventQueue are processed
		    EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
			    progressDialog.setVisible(false);

			}
		    });

		}
	    };

	    readerThread.setPriority(Thread.MAX_PRIORITY);
	    readerThread.start();

	    progressDialog.setVisible(true);

	    LOGGER.info("File loaded successfully");
	} else
	    LOGGER.info("Error while opening file");

    }

    private boolean checkForDuplicate(String fileName) {
	if (sController.getMaster().contains(fileName)) {
	    JOptionPane.showMessageDialog(f,
		    "File already opened.\nPlease choose abother one",
		    "Duplicate Warning", JOptionPane.WARNING_MESSAGE);
	    return true;
	} else
	    return false;

    }

}
