package com.jan_gruber.rawprocessor.controller.actions.io;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.xml.transform.TransformerException;

import com.jan_gruber.rawprocessor.controller.IOController;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.MetadataTextPane;

public class DisplayMetadataAction extends AbstractAction {
    JFrame mFrame;
    IOController ioController;

    public DisplayMetadataAction(IOController ioController, JFrame mFrame) {
	super();
	this.ioController = ioController;
	this.mFrame = mFrame;

	this.putValue(NAME, "Display Metadata");
	this.putValue(MNEMONIC_KEY, (int) 'M');
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	JFrame metadataFrame = new JFrame();
	MetadataTextPane mtp = null;

	
	    try {
		mtp = new MetadataTextPane(ioController);
		 JScrollPane jsp = new JScrollPane(mtp);
		    metadataFrame.getContentPane().add(jsp);

		    metadataFrame.setLocation(Toolkit.getDefaultToolkit()
			    .getScreenSize().width * 3 / 4, mFrame
			    .getLocationOnScreen().y);
		    metadataFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    metadataFrame.setSize(new Dimension((int) (mFrame.getWidth()/4), 750));
		    metadataFrame.setVisible(true);
	    } catch (NullPointerException e1) {
		e1.printStackTrace();
		JOptionPane
		.showMessageDialog(
			mFrame,
			"No file selected!\nOpen a file first (Cmd+O)\n and try again (Cmd+M)!",
			"Error while trying to load metadata",
			JOptionPane.WARNING_MESSAGE);
	    } catch (TransformerException e1) {
		e1.printStackTrace();
		JOptionPane
		.showMessageDialog(
			mFrame,
			"Unfortunately there was a error,\nwhen loading the metadata.\nPlease try again!",
			"Error while trying to load metadata",
			JOptionPane.WARNING_MESSAGE);
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		JOptionPane
		.showMessageDialog(
			mFrame,
			"No file selected!\nOpen a file first (Cmd+O)\n and try again (Cmd+M)!",
			"Error while trying to load metadata",
			JOptionPane.WARNING_MESSAGE);
	    }
	   

	
    }}

