package com.jan_gruber.rawprocessor.controller;

import com.jan_gruber.rawprocessor.controller.actions.processor.ProcessAction;
import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingPipeline;
import com.jan_gruber.rawprocessor.model.engine.processor.RawProcessor;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.SideBarPanel;
import com.spinn3r.log5j.Logger;

public class ProcessorController extends Controller {
    private static final Logger LOGGER = Logger.getLogger();
    protected RawProcessor mProcessor;
    protected ProcessingPipeline mPipeline;

    public ProcessorController() {
	super();

    }

    public ProcessAction getProcessAction(SideBarPanel ap) {
	return new ProcessAction(this, ap);
    }

    public RawProcessor getProcessor() {
	return mProcessor;
    }

    public void setProcessor(RawProcessor mProcessor) {
	this.mProcessor = mProcessor;
    }

    public ProcessingPipeline getPipeline() {
	return mPipeline;
    }

    public void setPipeline(ProcessingPipeline mPipeline) {
	this.mPipeline = mPipeline;
    }

    public void setupProcessor(ProcessingParameters pm) {
	LOGGER.info("setup processor");
	mProcessor = new RawProcessor();
	//mProcessor.addPropertyChangeListener(this);
	this.mPipeline = new ProcessingPipeline();
	this.mPipeline.buildPipeline(getImageContainer(),pm);
	// TODO proper pipeline build
	LOGGER.info("pipeline built");

    }

    public void startProcessing() {
	this.mProcessor.process(getImageContainer(), mPipeline);
    }

}
