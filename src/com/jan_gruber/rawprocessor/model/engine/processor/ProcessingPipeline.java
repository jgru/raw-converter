package com.jan_gruber.rawprocessor.model.engine.processor;

import java.util.ArrayList;
import java.util.Collections;

import com.jan_gruber.rawprocessor.model.engine.processor.operations.ColorConversionOp;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.ConvolutionOp;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.DemosaicingOp;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.LUTOp;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.NoiseReductionOp;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.PipelineComponent;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.PreConditioningOp;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;

public class ProcessingPipeline {
    boolean isBuilt;
    boolean isSorted;
    private ArrayList<PipelineComponent> pipelineComponents = new ArrayList<PipelineComponent>();

    public void buildPipeline(RawImageContainer mRaw,
	    ProcessingParameters mParameters) {
	for (String opName : mParameters.getOperationList()) {
	    if (opName.equals(ProcessingParameters.PRECONDITIONING)) {
		pipelineComponents.add(new PreConditioningOp(mParameters
			.isBlackLevelCompensation(), mParameters
			.isWhiteBalancing(), mParameters.isNormalization(),
			mParameters.getWbType()));
	    } else if (opName.equals(ProcessingParameters.DEMOSAICING)) {
		int demosaicingType = mParameters.getDemosaicType();
		pipelineComponents.add(new DemosaicingOp(demosaicingType));

	    } else if (opName.equals(ProcessingParameters.COLOR_CONVERT)) {
		boolean isGammaAdjust= mParameters.isGammaAdjustment();
		pipelineComponents.add(new ColorConversionOp(isGammaAdjust));
		

	    } else if (opName.equals(ProcessingParameters.NOISE_REDUCTION)) {
		int nrBoxSize = mParameters.getNoiseReductionBoxSize();
		pipelineComponents.add(new NoiseReductionOp(nrBoxSize));

	    } else if (opName.equals(ProcessingParameters.LUT)) {
		short[][] mLUT = mParameters.getLUT();
		pipelineComponents.add(new LUTOp(mLUT));

	    } else if (opName.equals(ProcessingParameters.CONVOLUTION)) {
		int convType = mParameters.getConvolutionType();
		int convBoxSize = mParameters.getConvolutionBoxSize();
		float[] kernelData = mParameters.getCustomConvolutionKernel();
		pipelineComponents.add(new ConvolutionOp(convType,
			convBoxSize, kernelData));

	    }

	    // TODO add other operations

	}
	Collections.sort(pipelineComponents);

    }

    public void addToPipeline(PipelineComponent component2Add) {
	pipelineComponents.add(component2Add);

    }

    public boolean isBuilt() {
	return isBuilt;
    }

    public void setIsBuilt(boolean isBuilt) {
	this.isBuilt = isBuilt;
    }

    public ArrayList<PipelineComponent> getPipelineComponents() {
	return pipelineComponents;
    }

    public void setPipelineComponents(
	    ArrayList<PipelineComponent> pipelineComponents) {
	this.pipelineComponents = pipelineComponents;
    }

}
