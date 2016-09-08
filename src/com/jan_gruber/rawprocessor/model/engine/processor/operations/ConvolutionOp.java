package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.DataBufferUShort;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

public class ConvolutionOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();

    public static final int CUSTOM_CONVOLUTION = 0;
    public static final int HIGHPASS_SHARPENING = 1;
    public static final int UNSHARP_MASKING = 2;
    private int type;
    private int boxSizeX;
    private float[] kernelData;

    public ConvolutionOp(int type, int boxSizeX, float[] kernelData) {
	super();
	this.rank = 5;
	this.type = type;
	this.boxSizeX = boxSizeX;
	this.kernelData = kernelData;
    }

    @Override
    public void apply(RawImageContainer rawToProcess) {
	switch (type) {
	case 0:
	    //apply custom kernel
	    applyCustomConvolution(rawToProcess.getProcessedData());
	    break;
	case 1:
	    applyHighpassSharpening(rawToProcess.getProcessedData());
	    break;
	case 2:
	    //apply UnsharpMasking
	    applyUnsharpMask(rawToProcess.getProcessedData());
	    break;
	}

    }

    private void applyCustomConvolution(WritableRaster raster) {
	LOGGER.info("Apply Custom Convolution");
	if (kernelData == null)
	    throw new IllegalArgumentException("Custom kernel null");
	else
	    convolve16BitInterleavedRGBWith2dKernel(raster, new Kernel(
		    boxSizeX, boxSizeX, kernelData));
    }

    // kernel from: http://www.cyanogen.com/help/maximdl/High-Pass_Filtering.htm
    final float[] SIMPLE_HP_KERNEL = { 0,-1,0,-1,5,-1,0,-1,0 };
	    //{1,1,1,1,-8,1,1,1,1};
	//{ -1 / 9f, -1 / 9f, -1 / 9f, -1 / 9f, 2f,
	  //  -1 / 9f, -1 / 9f, -1 / 9f, -1 / 9f };

    // { 0f, -0.25f, 0f, -0.25f, 2f, -0.25f, 0f, -0.25f, 0f };

    private void applyHighpassSharpening(WritableRaster raster) {
	LOGGER.info("Apply Highpass- Sharpening");
	Kernel k = new Kernel(3, 3, SIMPLE_HP_KERNEL);
	convolve16BitInterleavedRGBWith2dKernel(raster, k);

    }

    private void applyUnsharpMask(WritableRaster raster) {
	LOGGER.info("Apply Unsharp Masking");
	// Variance==sigma-> blurring factor
	int weight = 3;
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	short[] pixelData = db.getData();

	Kernel gaussKernel = calcGaussKernel(boxSizeX, weight);
	convolve16BitInterleavedRGBWith2dKernelAndSubtractFromOrig(pixelData,
		raster.getWidth(), raster.getHeight(), raster.getNumBands(),
		gaussKernel);

    }

    private void convolve16BitInterleavedRGBWith2dKernel(WritableRaster raster,
	    Kernel mKernel) {
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	int width = raster.getWidth();
	int height = raster.getHeight();
	int pixelStride = raster.getNumBands();
	short[] convolvedPixelData = convolve16BitInterleavedRGBWith2dKernel(
		db.getData(), width, height, pixelStride, mKernel);
	System.arraycopy(convolvedPixelData, 0, db.getData(), 0,
		convolvedPixelData.length);
    }

    private short[] convolve16BitInterleavedRGBWith2dKernel(
	    short[] origPixelData, int width, int height, int pixelStride,
	    Kernel mKernel) {
	short[] outputPixelData = new short[origPixelData.length];
	int scanline = width * pixelStride;

	float[] kernel = mKernel.getKernelData(null);
	int kernelWidth = mKernel.getWidth();
	int radius = kernelWidth / 2;

	int counter = 0;

	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < scanline - pixelStride; x++) {
		float tmpVal = 0;

		for (int dy = -radius; dy <= radius; dy++) {
		    int tmpIndexY = y + dy;
		    if (0 <= tmpIndexY && tmpIndexY < height) {
			int offset = tmpIndexY * scanline;
			int kernelOffset = kernelWidth * (dy + radius) + radius;
			for (int dx = -radius; dx <= radius; dx++) {
			    float f = kernel[dx + kernelOffset];
			    if (f != 0) {
				int tmpIndexX = x + (dx * pixelStride);
				if (0 <= tmpIndexX && tmpIndexX < scanline) {
				    short pixel = origPixelData[tmpIndexX
					    + offset];
				    tmpVal += f * (pixel & 0xffff);

				}
			    }

			}

		    }
		}

		//clamping
		tmpVal = tmpVal > 0xffff ? 0xffff : tmpVal < 0 ? 0 : tmpVal;
		outputPixelData[x + y * scanline] = (short) tmpVal;
	    }

	}

	return outputPixelData;
    }

    private void convolve16BitInterleavedRGBWith2dKernelAndSubtractFromOrig(
	    short[] pixelDataToModify, int width, int height, int pixelStride,
	    Kernel mKernel) {
	short[] outputPixelData = new short[pixelDataToModify.length];
	int scanline = width * pixelStride;

	float[] kernel = mKernel.getKernelData(null);
	int kernelWidth = mKernel.getWidth();
	int radius = kernelWidth / 2;

	int counter = 0;

	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < scanline - pixelStride; x++) {
		float tmpVal = 0;

		for (int dy = -radius; dy <= radius; dy++) {
		    int tmpIndexY = y + dy;
		    if (0 <= tmpIndexY && tmpIndexY < height) {
			int offset = tmpIndexY * scanline;
			int kernelOffset = kernelWidth * (dy + radius) + radius;
			for (int dx = -radius; dx <= radius; dx++) {
			    float f = kernel[dx + kernelOffset];
			    if (f != 0) {
				int tmpIndexX = x + (dx * pixelStride);
				if (0 <= tmpIndexX && tmpIndexX < scanline) {
				    short pixel = pixelDataToModify[tmpIndexX
					    + offset];
				    tmpVal += f * (pixel & 0xffff);

				}
			    }

			}

		    }
		}

		//clamping
		tmpVal = tmpVal > 0xffff ? 0xffff : tmpVal < 0 ? 0 : tmpVal;
		pixelDataToModify[x + y * scanline] -= (short) tmpVal;
	    }

	}

    }

    private Kernel calcGaussKernel(int kernelSize, double sigma) {
	// source of gauss formula:
	// http://upload.wikimedia.org/math/9/5/e/95ecdbb16befd4fdb760fa26c83a4b5e.png

	float[] kernelData = new float[kernelSize * kernelSize];
	int radius = kernelSize / 2;
	float eulerTerm = (float) (1.0f / (2.0f * Math.PI * Math.pow(sigma, 2)));
	float distance = 0;
	float sum = 0;
	for (int y = -radius; y <= radius; y++) {
	    int offset = (y + radius) * kernelSize;
	    for (int x = -radius; x <= radius; x++) {
		distance = (float) ((x * x + y * y) / (2 * sigma * sigma));
		kernelData[(x + radius) + offset] = (float) ((float) eulerTerm * Math
			.exp(-distance));

		sum += kernelData[(x + radius) * (y + radius)];
	    }
	}
	for (int i = 0; i < kernelData.length; i++) {
	    kernelData[i] = kernelData[i] * (1.f / sum);
	}
	return new Kernel(kernelSize, kernelSize, kernelData);
    }

}
