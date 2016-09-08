package com.jan_gruber.rawprocessor.model.engine.processor;

import com.jan_gruber.rawprocessor.model.AbstractModel;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.PipelineComponent;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

public class RawProcessor {
    private static final Logger LOGGER = Logger.getLogger();
    public static String PROCESSOR_STATE = "processedSuccessfully";
    boolean isInitialised;
    RawImageContainer mRaw;
    ProcessingPipeline mPipeline;
    Thread processorThread;
    
    Runnable processorRunnable = new Runnable() {

	@Override
	public void run() {
	    if (isInitialised) {
		// apply operations
		for (PipelineComponent pc : mPipeline.getPipelineComponents()) {
		    pc.apply(mRaw);
		}
		mRaw.setIsProcessed(true);
		LOGGER.info("Processed pipeline");
	    }
	}
    };

    public void process(RawImageContainer mRaw, ProcessingPipeline mPipeline) {
	LOGGER.info("process");
	if (mPipeline == null || mPipeline.isBuilt()) {
	    throw new IllegalArgumentException();
	}
	this.mRaw = mRaw;
	this.mPipeline = mPipeline;
	isInitialised = true;
	processorThread = new Thread(processorRunnable);
	processorThread.setPriority(Thread.MAX_PRIORITY);
	processorThread.start();


    }

}
