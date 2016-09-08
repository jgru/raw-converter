package com.jan_gruber.rawprocessor.controller.actions.processor;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.jan_gruber.rawprocessor.controller.ProcessorController;
import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingPipeline;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.SideBarPanel;
import com.spinn3r.log5j.Logger;

public class ProcessAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger();
    private ProcessorController pController;
    private SideBarPanel adjustments;
    
    public ProcessAction(ProcessorController pController, SideBarPanel adjustments) {
	this.pController = pController;
	this.adjustments= adjustments;
	
	putValue(NAME, "Process");
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
		InputEvent.META_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	//if (pController.getImageContainer() != null) {
	    ProcessingParameters pm= adjustments.getProcessingParameters();
	    
	    // create appropriate processor
	    pController.setupProcessor(pm);
	    //start processing
	    pController.startProcessing();

	    LOGGER.info("Handed over to ProcessorController");
	//} else
	  //  LOGGER.info("Error");

    }

}
