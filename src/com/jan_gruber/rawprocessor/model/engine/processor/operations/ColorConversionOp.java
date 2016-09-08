package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.color.ColorSpace;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import Jama.Matrix;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;

import edu.umbc.cs.maple.utils.JamaUtils;

public class ColorConversionOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();
    private boolean isGammaAdjustment;

    // sRGB -> CIE XYZ 
    double[] RGB_TO_XYZ = { 0.412453, 0.357580, 0.180423, 0.212671, 0.715160,
	    0.072169, 0.019334, 0.119193, 0.950227 };
    /*
    // CIE XYZ -> ProPhoto
    double[] XYZ_TO_RGB =  { 0.7976749,  0.1351917,  0.0313534,
        0.2880402 , 0.7118741 , 0.0000857,
        0.0000000 , 0.0000000 , 0.8252100};
    */

    //D65 -> D50 (Bradford)
    final double[] D65_To_D50 = { 1.0478112, 0.0228866, -0.0501270, 0.0295424,
	    0.9904844, -0.0170491, -0.0092345, 0.0150436, 0.7521316 };

    public ColorConversionOp() {
	super();
	this.rank = 3;
	isGammaAdjustment = false;
    }

    public ColorConversionOp(boolean isGammaAdjustment) {
	super();
	this.rank = 3;
	this.isGammaAdjustment = isGammaAdjustment;
    }

    @Override
    public void apply(RawImageContainer rawToProcess) {
	double[] camRGBMatrix = { 1.0, 0, 0, 0, 1.0, 0, 0, 0, 1.0 };

	//check if it is already converted
	if (rawToProcess.getColorSpace() != ColorSpace.CS_LINEAR_RGB
		&& rawToProcess.getColorSpace() != ColorSpace.CS_sRGB)
	    camRGBMatrix = prepareCameraMatrix(MatrixLookUp
		    .getMatrixByCameraName(rawToProcess.getMetadata()
			    .getModel()));

	LOGGER.info("Apply color conversion");

	applyColorConversion(rawToProcess.getProcessedData(), camRGBMatrix);

	if (isGammaAdjustment)
	    rawToProcess.setColorSpace(ColorSpace.CS_sRGB);
	else
	    rawToProcess.setColorSpace(ColorSpace.CS_LINEAR_RGB);

    }

    private double[] prepareCameraMatrix(double[] xyzToCamMatrix) {
	Matrix RGBToXYZmatrix = new Matrix(RGB_TO_XYZ, 3);
	Matrix RGBToCamMatrix = new Matrix(xyzToCamMatrix, 3);
	Matrix conversionMatrixRGB = RGBToXYZmatrix.times(RGBToCamMatrix);
	//normalize with maximum column sum 
	for (int i = 0; i < conversionMatrixRGB.getRowDimension(); i++) {
	    double colSum = JamaUtils.colsum(conversionMatrixRGB, i);
	    conversionMatrixRGB.set(0, i, conversionMatrixRGB.get(0, i)
		    / colSum);
	    conversionMatrixRGB.set(1, i, conversionMatrixRGB.get(1, i)
		    / colSum);
	    conversionMatrixRGB.set(2, i, conversionMatrixRGB.get(2, i)
		    / colSum);

	}
	conversionMatrixRGB = conversionMatrixRGB.inverse();
	LOGGER.info(Arrays.toString(conversionMatrixRGB.getColumnPackedCopy()));

	double[] resultingMatrix = conversionMatrixRGB.getColumnPackedCopy();
	return resultingMatrix;

    }

    final double gamma = 1 / 2.2;

    private void applyColorConversion(WritableRaster raster,
	    double[] camRGBMatrix) {
	int pixelStride = raster.getNumBands();
	int scanline = raster.getWidth() * pixelStride;
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();

	//temporary storage of the converted tristimulus
	int[] convertedValues = new int[3];

	for (int y = 0; y < raster.getHeight(); y++) {
	    for (int x = 0; x < scanline; x += pixelStride) {
		int scanPos = y * scanline + x;

		//perform matrix multiplication (row times column)
		for (int b = 0; b < pixelStride; b++) {
		    double convertedVal = (camRGBMatrix[b * 3 + 0]
			    * ((db.getElem(scanPos) & 0xffff) / 65535.0)
			    + camRGBMatrix[b * 3 + 1]
			    * ((db.getElem(scanPos + 1) & 0xffff) / 65535.0) + camRGBMatrix[b * 3 + 2]
			    * ((db.getElem(scanPos + 2) & 0xffff) / 65635.0));

		    if (isGammaAdjustment)
			convertedVal = Math.pow(convertedVal, gamma);

		    int convertedInt = (int) (convertedVal * 0xffff);
		    convertedInt = convertedInt < 0 ? 0 : convertedInt;
		    convertedInt = convertedInt <= 0xffff ? convertedInt
			    : 0xffff;
		    convertedValues[b] = convertedInt;

		}
		//store result in the DataBuffer
		db.setElem(scanPos, convertedValues[0]);
		db.setElem(scanPos + 1, convertedValues[1]);
		db.setElem(scanPos + 2, convertedValues[2]);
		convertedValues[0] = convertedValues[1] = convertedValues[2] = 0;

	    }

	}

    }

    public double[] product(double[] m1, double[] m2) {
	double[] r = new double[9];

	r[2] = m1[0] * m2[2] + m1[1] * m2[5] + m1[2] * m2[8];
	r[5] = m1[3] * m2[2] + m1[4] * m2[5] + m1[5] * m2[8];
	r[8] = m1[6] * m2[2] + m1[7] * m2[5] + m1[8] * m2[8];

	r[1] = m1[0] * m2[1] + m1[1] * m2[4] + m1[2] * m2[7];
	r[4] = m1[3] * m2[1] + m1[4] * m2[4] + m1[5] * m2[7];
	r[7] = m1[6] * m2[1] + m1[7] * m2[4] + m1[8] * m2[7];

	r[0] = m1[0] * m2[0] + m1[1] * m2[3] + m1[2] * m2[6];
	r[3] = m1[3] * m2[0] + m1[4] * m2[3] + m1[5] * m2[6];
	r[6] = m1[6] * m2[0] + m1[7] * m2[3] + m1[8] * m2[6];

	return r;
    }

    public void normalizeRows(double[] m) {
	double colSum = m[0] + m[1] + m[2];
	System.err.println(colSum);
	m[0] /= colSum;
	m[1] /= colSum;
	m[2] /= colSum;

	colSum = m[3] + m[4] + m[5];
	System.err.println(colSum);
	m[3] /= colSum;
	m[4] /= colSum;
	m[5] /= colSum;

	colSum = m[6] + m[7] + m[8];
	System.err.println(colSum);
	m[6] /= colSum;
	m[7] /= colSum;
	m[8] /= colSum;
    }

    public double[] inverse(double[] m) {
	double cof0 = +(m[4] * m[8] - m[7] * m[5]);
	double cof1 = -(m[3] * m[8] - m[6] * m[5]);
	double cof2 = +(m[3] * m[7] - m[6] * m[4]);
	double cof3 = -(m[1] * m[8] - m[7] * m[2]);
	double cof4 = +(m[0] * m[8] - m[6] * m[2]);
	double cof5 = -(m[0] * m[7] - m[6] * m[1]);
	double cof6 = +(m[1] * m[5] - m[4] * m[2]);
	double cof7 = -(m[0] * m[5] - m[3] * m[2]);
	double cof8 = +(m[0] * m[4] - m[3] * m[1]);

	double det = m[0] * cof0 + m[1] * cof1 + m[2] * cof2;

	double nc[] = new double[9];
	nc[0] = cof0 / det;
	nc[1] = cof3 / det;
	nc[2] = cof6 / det;
	nc[3] = cof1 / det;
	nc[4] = cof4 / det;
	nc[5] = cof7 / det;
	nc[6] = cof2 / det;
	nc[7] = cof5 / det;
	nc[8] = cof8 / det;

	return nc;
    }

}
