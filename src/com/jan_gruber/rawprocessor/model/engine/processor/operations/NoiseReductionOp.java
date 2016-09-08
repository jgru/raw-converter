package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

public class NoiseReductionOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();
    private int boxSize;

    public NoiseReductionOp(int boxSize) {
	super();
	this.rank = 4;
	this.boxSize = boxSize;
    }

    @Override
    public void apply(RawImageContainer rawToProcess) {
	LOGGER.info("Apply Noise Reduction");
	WritableRaster raster = rawToProcess.getProcessedData();
	int width = raster.getWidth();
	int height = raster.getHeight();
	int pixelStride = raster.getNumBands();
	int scanline = pixelStride * width;
	int radius =  boxSize / 2;
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	short origPixelData[] = db.getData();

	//not "in situ"
	short outputPixelData[] = new short[origPixelData.length];

	int[] r = new int[boxSize * boxSize];
	short[] g = new short[r.length];
	short[] b = new short[r.length];
	int writeIndex = 0;

	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < scanline - pixelStride; x++) {
		int counter = 0;
		for (int dy = -radius; dy <= radius; dy++) {
		    int tmpIndexY = y + dy;
		    if (tmpIndexY >= 0 && tmpIndexY < height) {
			int offset = tmpIndexY * scanline;
			for (int dx = -radius; dx <= radius; dx++) {
			    int tmpIndexX = x + (dx * pixelStride);
			    if (tmpIndexX >= 0
				    && tmpIndexX < scanline) {
				r[counter] = db.getElem(offset + tmpIndexX);
				
				counter++;
			    }

			}

		    }
		}

		int medianIndex = doMedian(r);
		outputPixelData[y*scanline+x]= (short) (r[medianIndex] &0xffff);
	    }

	}
	System.arraycopy(outputPixelData, 0, origPixelData, 0,
		outputPixelData.length);

    }

    private int doMedian(int[] r) {
	int sum = 0;
	int minimumIndex = 0;
	int min = Integer.MAX_VALUE;

	for (int i = 0; i < r.length; i++) {
	    for (int j = 0; j < r.length; j++) {
		sum = sum + Math.abs(r[i]  - r[j] );

	    }
	    if (sum < min) {
		min = sum;
		minimumIndex = i;
	    }
	    sum = 0;
	}

	return minimumIndex;

    }

    private int doMedian(short[] r, short[] g, short[] b) {
	int sum = 0;
	int minimumIndex = 0;
	int min = Integer.MAX_VALUE;

	for (int i = 0; i < r.length; i++) {
	    for (int j = 0; j < r.length; j++) {
		sum = sum + Math.abs(r[i] & 0xffff - r[j] & 0xffff)
			+ Math.abs(g[i] & 0xffff - g[j] & 0xffff)
			+ Math.abs(b[i] & 0xffff - b[j] & 0xffff);

	    }
	    if (sum < min) {
		min = sum;
		minimumIndex = i;
	    }
	    sum = 0;
	}

	return minimumIndex;

    }
}
