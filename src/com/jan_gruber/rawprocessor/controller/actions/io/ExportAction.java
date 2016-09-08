package com.jan_gruber.rawprocessor.controller.actions.io;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jan_gruber.rawprocessor.controller.IOController;
import com.jan_gruber.rawprocessor.model.engine.io.WriteParams;
import com.jan_gruber.rawprocessor.util.CompressionConstants;
import com.jan_gruber.rawprocessor.view.gui.FoldablePanelContainer;
import com.spinn3r.log5j.Logger;

public class ExportAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger();
    private IOController ioController;
    private JFrame frame;
    JFrame optionFrame;

    public ExportAction(IOController ioController, JFrame frame) {
	super();
	this.ioController = ioController;
	this.frame = frame;

	this.putValue(NAME, "Export Image");
	this.putValue(MNEMONIC_KEY, (int) 'E');
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	//get further user input
	optionFrame = new JFrame();
	optionFrame.getContentPane().add(createOptionPanel());
	optionFrame.setSize(new Dimension(250, 550));
	optionFrame.setLocationRelativeTo(frame);
	optionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	optionFrame.setVisible(true);

    }

    private String formatSuffix=".tiff";
    private int compression = 1;
    private int bitDepth = 16;

    private void kickOffExport() {
	if (ioController.getImageContainer() == null) {
	    JOptionPane
		    .showMessageDialog(
			    optionFrame,
			    "No file to save.\nPlease open a .cr2 image\nand try again",
			    "No File", JOptionPane.WARNING_MESSAGE);
	    return;
	}
	if (!new File(pathField.getText()).exists()) {
	    JOptionPane
		    .showMessageDialog(
			    optionFrame,
			    "Not a valid file path.\nPlease choose an EXISTING directory\nand try again",
			    "Unexisting File Path", JOptionPane.WARNING_MESSAGE);
	    return;
	}

	optionFrame.dispose();
	final WriteParams mParams = new WriteParams(pathField.getText(),
		fileNameField.getText(), formatSuffix, bitDepth, compression);

	final JDialog progressDialog = new JDialog(frame, "File IO Dialog",
		true);
	JProgressBar progressBar = new JProgressBar(0, 500);
	progressBar.setIndeterminate(true);
	progressDialog.add(BorderLayout.CENTER, progressBar);
	progressDialog.add(BorderLayout.NORTH, new JLabel("Writing "+ formatSuffix+"- File..."));
	progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	progressDialog.setSize(300, 75);
	progressDialog.setLocationRelativeTo(frame);

	Thread exportThread = new Thread() {
	    public void run() {
		ioController.exportImage(mParams);

		//close if all other events on the EventQueue are processed
		EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			progressDialog.setVisible(false);

		    }
		});
	    }
	};

	exportThread.setPriority(Thread.MAX_PRIORITY);
	exportThread.start();
	progressDialog.setVisible(true);

    }

    JTextField pathField;
    JTextField fileNameField;
    JRadioButton eightBitRB;
    JRadioButton sixteenBitRB;
    JRadioButton lzwRB;
    JRadioButton noneRB;
    JRadioButton deflateRB;

    private JPanel createOptionPanel() {

	JPanel optionPanel = new JPanel();
	RadioListener rl = new RadioListener();

	JPanel pathPanel = new JPanel();
	pathPanel.setPreferredSize(new Dimension(220, 100));//FIXME
	pathField = new JTextField("/path/to/file");
	pathField.setColumns(8);
	pathPanel.add(pathField);
	JButton chooseButton = new JButton("Choose");
	pathPanel.add(chooseButton);
	chooseButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		JFileChooser mFileChooser = new JFileChooser("/Users/");
		mFileChooser
			.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		mFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		int returnVal = mFileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    LOGGER.info("Chosen File: "
			    + mFileChooser.getSelectedFile().getName());

		    mFileChooser.getSelectedFile().getAbsolutePath();
		    pathField.setText(mFileChooser.getSelectedFile()
			    .getAbsolutePath());
		}
	    }
	});
	fileNameField = new JTextField("file_name");
	fileNameField.setColumns(12);
	pathPanel.add(fileNameField);
	FoldablePanelContainer fp = new FoldablePanelContainer();
	fp.addPanel(pathPanel, "FilePath");

	JPanel formatPanel = new JPanel();
	formatPanel.setPreferredSize(new Dimension(220, 130));//FIXME
	//formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
	JPanel suffixPanel = new JPanel();
	JLabel formatLabel = new JLabel("Format");
	suffixPanel.add(formatLabel);
	JComboBox formatCombo = new JComboBox(new String[] { ".tiff", ".dng" });
	formatCombo.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		formatSuffix = (String) e.getItem();
		LOGGER.info("fileType: "+ formatSuffix);
	    }
	});
	suffixPanel.add(formatCombo);
	formatCombo.setSelectedIndex(0);
	formatPanel.add(suffixPanel);
	JPanel bitPanel = new JPanel();
	bitPanel.setLayout(new BoxLayout(bitPanel, BoxLayout.Y_AXIS));
	bitPanel.setBorder(new TitledBorder("Bitdepth"));
	bitPanel.setPreferredSize(new Dimension(220, 100)); //FIXME
	ButtonGroup bitBG = new ButtonGroup();
	eightBitRB = new JRadioButton("8 Bits Per Channel");
	eightBitRB.addActionListener(rl);
	sixteenBitRB = new JRadioButton("16 Bits Per Channel");
	sixteenBitRB.addActionListener(rl);
	sixteenBitRB.setSelected(true);
	bitBG.add(eightBitRB);
	bitBG.add(sixteenBitRB);
	bitPanel.add(eightBitRB);
	bitPanel.add(sixteenBitRB);
	formatPanel.add(bitPanel);
	fp.addPanel(formatPanel, "Format");

	JPanel compressionPanel = new JPanel();
	compressionPanel.setLayout(new BoxLayout(compressionPanel,
		BoxLayout.Y_AXIS));
	compressionPanel.setBorder(new TitledBorder("Compression"));
	ButtonGroup compressionGroup = new ButtonGroup();
	noneRB = new JRadioButton("None");
	noneRB.addActionListener(rl);
	noneRB.setSelected(true);
	lzwRB = new JRadioButton("LZW");
	lzwRB.addActionListener(rl);
	deflateRB = new JRadioButton("Deflate");
	deflateRB.addActionListener(rl);
	compressionGroup.add(noneRB);
	compressionGroup.add(lzwRB);
	compressionGroup.add(deflateRB);
	compressionPanel.setPreferredSize(new Dimension(220, 100)); //FIXME
	compressionPanel.add(noneRB);
	compressionPanel.add(lzwRB);
	compressionPanel.add(deflateRB);
	fp.addPanel(compressionPanel, "Compression");

	optionPanel.add(fp.getComponent());
	JButton exportButton = new JButton("Save");
	exportButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		kickOffExport();
	    }
	});
	optionPanel.add(exportButton);
	return optionPanel;
    }

    class RadioListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    AbstractButton b = (AbstractButton) e.getSource();
	    if (b.equals(eightBitRB)) {
		 JOptionPane
		    .showMessageDialog(
			    optionFrame,
			    "Downsampling not implemented yet.\nPlease use 16 Bpc",
			    "BitDepth Warning", JOptionPane.WARNING_MESSAGE);
	    return;
	    } else if (b.equals(sixteenBitRB)) {
		bitDepth = 16;
	    } else if (b.equals(noneRB)) {
		compression = CompressionConstants.COMPRESSION_NONE;
	    } else if (b.equals(lzwRB)) {
		compression = CompressionConstants.COMPRESSION_LZW;
	    } else if (b.equals(deflateRB)) {
		compression = CompressionConstants.COMPRESSION_DEFLATE;

	    }

	}
    }

}
