package com.jan_gruber.rawprocessor.controller.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.jan_gruber.rawprocessor.controller.Controller;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.spinn3r.log5j.Logger;

public class PanoramaViewAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger();
    private Controller mController;
    private MainFrame f;

    public PanoramaViewAction(Controller mController, MainFrame f) {
	this.mController = mController;
	this.f = f;
	putValue(NAME, "Create shift panorama");
	this.putValue(MNEMONIC_KEY, (int) 'P');
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (f.getMode() != 1){
	    mController.removeLastAddedImageContainer();
	    try {
		Thread.sleep(50);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    f.setPanoramaView();
	}
	
    }

}
