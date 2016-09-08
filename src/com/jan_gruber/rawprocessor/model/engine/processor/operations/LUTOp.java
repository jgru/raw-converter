package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

public class LUTOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();
    private short[][] mLUT;

    public LUTOp(short[][] mLUT) {
	this.rank = 3;
	this.mLUT = mLUT;
    }

    @Override
    public void apply(RawImageContainer rawToProcess) {
	if (mLUT == null)
	    return;

	LOGGER.info("Apply LUT");

	WritableRaster raster = rawToProcess.getProcessedData();
	int pixelStride = raster.getNumBands();
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	for (int i = 0; i < db.getSize() - pixelStride; i += pixelStride) {
	    for(int j=0; j< pixelStride;j++){
	    int val = db.getElem(i+j) & 0xffff;
	    val = val > 0xffff - 1 ? 0xffff - 1 : val < 0 ? 0 : val;
	    db.setElem(i+j, (mLUT[j][val] & 0xffff));
	    
	    }
	}
    }
}
