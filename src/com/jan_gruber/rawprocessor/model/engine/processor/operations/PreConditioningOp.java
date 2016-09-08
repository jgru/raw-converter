package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.util.WritableRasterFactory;
import com.spinn3r.log5j.Logger;

public class PreConditioningOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();

    public static final int WB_AS_SHOT = 0;
    public static final int WB_AUTO = 1;
    public static final int WB_MEASURED = 2;
    public static final int WB_DAYLIGHT = 3;
    public static final int WB_SHADE = 4;
    public static final int WB_CLOUDY = 5;
    public static final int WB_TUNGSTEN = 6;

    int wbType;

    boolean isNormalization;
    boolean isBlackLevelCompensation;
    boolean isWhiteBalancing;

    double[] normalizationFactors;
    short[] blackLevelPerChannel;
    short[] wbRGGBLevels;

    //offset in databuffer
    int redOffset = 0;
    int greenOffset = 1;
    int blueOffset = 2;

    public PreConditioningOp(boolean isBlackLevelCompensation, boolean isWB,
	    boolean isNormalization, int wbType) {
	super();
	this.rank = 0;
	this.isBlackLevelCompensation = isBlackLevelCompensation;
	this.isWhiteBalancing = isWB;
	this.isNormalization = isNormalization;
	this.wbType = wbType;
    }

    @Override
    public void apply(RawImageContainer mRaw) {
	LOGGER.info("Precondition sensor data");
	WritableRaster raster = mRaw.getRawData();
	DataBufferUShort origDb = (DataBufferUShort) raster.getDataBuffer();
	int pixelStride = raster.getNumBands();
	int scanline = raster.getWidth() * pixelStride;

	WritableRaster processedData = WritableRasterFactory
		.create16BitRGBRaster(raster.getWidth(), raster.getHeight());
	mRaw.setProcessedData(processedData);
	DataBufferUShort procDb = (DataBufferUShort) processedData
		.getDataBuffer();
	//copy data from orig raw raster to raster that will be processed
	System.arraycopy(origDb.getData(), 0, procDb.getData(), 0,
		procDb.getData().length);

	preparePreconditioningAdjustments(mRaw, processedData);

	for (int y = 0; y < raster.getHeight(); y += 1) {
	    for (int x = 0; x < scanline - pixelStride; x += pixelStride) {
		int scanPos = x + y * scanline;

		int tmpR = procDb.getElem(scanPos);
		int tmpG = procDb.getElem(scanPos + greenOffset);
		int tmpB = procDb.getElem(scanPos + blueOffset);

		if (tmpG == 0 && tmpB == 0) {
		    //red pixel
		    procDb.setElem(
			    x + y * scanline,
			    applyPreConditioning(procDb.getElem(scanPos),
				    blackLevelPerChannel[0],
				    normalizationFactors[0], wbRGGBLevels[0]));

		} else if (tmpR == 0 && tmpB == 0) {
		    //green pixel 
		    //check the neighboring pixels red sample
		    if (procDb.getElem(scanPos + pixelStride) == 0) {
			//green in blue row neighboring red is 0
			procDb.setElem(
				x + y * scanline + greenOffset,
				applyPreConditioning(
					procDb.getElem(scanPos + greenOffset),
					blackLevelPerChannel[1],
					normalizationFactors[1],
					wbRGGBLevels[1]));
		    } else {
			//green in red row
			procDb.setElem(
				x + y * scanline + greenOffset,
				applyPreConditioning(
					procDb.getElem(scanPos + greenOffset),
					blackLevelPerChannel[1],
					normalizationFactors[1],
					wbRGGBLevels[2]));
		    }
		} else if (tmpG == 0 && tmpR == 0) {
		    //blue pixel
		    procDb.setElem(
			    x + y * scanline + blueOffset,
			    applyPreConditioning(
				    procDb.getElem(scanPos + blueOffset),
				    blackLevelPerChannel[2],
				    normalizationFactors[2], wbRGGBLevels[3]));
		}
	    }
	}

    }

    private void preparePreconditioningAdjustments(RawImageContainer mRaw,
	    WritableRaster raster) {
	//check, which correction adjustment to apply
	if (isBlackLevelCompensation) {
	    if (mRaw.getMetadata().getBlackLevelsPerChannel() != null)
		blackLevelPerChannel = mRaw.getMetadata()
			.getBlackLevelsPerChannel();
	    else
		blackLevelPerChannel = calcBlackLevel(raster);
	    LOGGER.info("BlackLevelPerChannel  "
		    + Arrays.toString(blackLevelPerChannel));
	} else
	    blackLevelPerChannel = new short[] { 0, 0, 0 };

	if (isNormalization) {
	    sensorMax = mRaw.getMetadata().getWhiteLevel();
	    LOGGER.info("WhiteLevel: " + sensorMax);
	    normalizationFactors = calcNormalization(raster);
	    LOGGER.info("NormalizationFactorPerChannel  "
		    + Arrays.toString(normalizationFactors));
	} else
	    normalizationFactors = new double[] { 1, 1, 1 };

	if (isWhiteBalancing) {
	    wbRGGBLevels = new short[4];

	    short[] completeWBData = mRaw.getMetadata()
		    .getWhiteBalanceRGGBMultipliers();
	    int c = 0;
	    for (int i = wbType * 4; i < wbType * 4 + 4; i++) {
		wbRGGBLevels[c] = completeWBData[i];
		c++;
	    }

	    LOGGER.info("WhiteBalance RGGB multipliers  "
		    + Arrays.toString(wbRGGBLevels));
	} else
	    wbRGGBLevels = new short[] { 2048, 1200, 1200, 2048 };

    }

    private final double VALUATION_MAX = (1 << 16) - 1; //maximum value of an unsigned short 2^16 - 1 (65535)
    private double sensorMax = (1 << 14) - 1; //maximum value of 12 Bit A/D-Converter, 2^12 - 1 

    private double[] calcNormalization(WritableRaster raster) {
	short[] blackLevels;

	if (blackLevelPerChannel != null)
	    blackLevels = blackLevelPerChannel;
	else
	    blackLevels = calcBlackLevel(raster);
	double[] normalizationFactors = new double[3];
	for (int i = 0; i < normalizationFactors.length; i++) {
	    normalizationFactors[i] = VALUATION_MAX
		    / (sensorMax - blackLevels[i]);
	}

	return normalizationFactors;
    }

    /*
     * Even if encoded using 14bits, the "real black" may not be recorded as RGB = ( 0, 0, 0) and the white as ( 16384, 16384, 16384 ), 
     * because of the sensor physical characteristics (2^14 == 16384). For example for the 5D Mark II, the black level is ( 1023, 1023, 1023 ) 
     * and the white level ( 15600, 15600, 15600 ). Src:http://lclevy.free.fr/cr2/#key_info 5.2
     *      */

    /**
     * This method calculates the black level of a given CFA- Array raster for
     * each channel (R,G1,G2,B) in a 2x2 block. The black level is compute by
     * measuring the masked pixels at the edge of the sensor. This method picks
     * the four corners and calculates the average.
     * 
     * @param raster
     * @return blackLevelPerChannel, an int array containing the blacklevel for
     *         each channel
     */
    protected short[] calcBlackLevel(WritableRaster raster) {
	//FIXME calculation not correct!!!
	DataBuffer db = raster.getDataBuffer();
	int scanline = raster.getNumBands() * raster.getWidth();
	int numBands = raster.getNumBands();

	short[] blackLevelPerColorFilter = new short[4];
	final int corners = 4;
	//ignore the first two columns, because they sometimes have a bright stripe.
	int scanPos = 2 * scanline + 12;

	int innerPadding = 0;

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		blackLevelPerColorFilter[i * 2 + j] += (db.getElem(scanPos
			+ innerPadding + i * scanline + j * numBands + (i + j))); //+i is the band offset
	    }
	}
	short[] blackLevelPerChannel = {
		blackLevelPerColorFilter[0],
		(short) ((blackLevelPerColorFilter[1] + blackLevelPerColorFilter[2]) / 2),
		blackLevelPerColorFilter[3] };

	LOGGER.info("Calced BlackLevel per Channel "
		+ Arrays.toString(blackLevelPerChannel));

	return blackLevelPerChannel;
    }

    /**
     * applies the white balance multiplier for the specific channel, performs a
     * blacklevel subtraction and normalizes the valuation
     * 
     * @param multiplier
     *            scale factor
     * @param value
     *            the sample to scale
     * @return
     */
    private final static int SCALE = 1024; //scaling necessary, because wb multiplier is around 2000

    protected short applyPreConditioning(int value, int blackLevel,
	    double normalizationFactor, int multiplier) {
	int tmp = value;
	tmp = tmp - blackLevel;
	tmp = (int) (tmp * ((multiplier * normalizationFactor) / SCALE));

	if (tmp > 0xffff)
	    tmp = 0xffff;
	else if (tmp < 0)
	    tmp = 0;

	return (short) tmp;
    }

}
