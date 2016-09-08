package com.jan_gruber.rawprocessor.util;

public class LUTFactory {
    public static final int VALUATION_16_BIT = (1 << 16) - 1;
    public static final int CHANNEL_COUNT = 3;

    public static short[][] create16BitRGB_LUT(Spline s) {
	short[][] mLUT = new short[CHANNEL_COUNT][VALUATION_16_BIT];

	int[] tmp = s.getValues(0, VALUATION_16_BIT);
	for (int i = 0; i < VALUATION_16_BIT; i++) {
	    mLUT[0][i] = (short) tmp[i];
	    mLUT[1][i] = (short) tmp[i];
	    mLUT[2][i] = (short) tmp[i];
	}

	return mLUT;
    }

    public static short[][] create16BitRGB_LUT(Spline[] convertedSplines) {
	short[][] mLUT = new short[CHANNEL_COUNT][VALUATION_16_BIT];
	int[][] splineValues = new int[CHANNEL_COUNT][];

	for (int j = 0; j < CHANNEL_COUNT; j++) {
	    splineValues[j] = convertedSplines[j]
		    .getValues(0, VALUATION_16_BIT);
	}

	for (int i = 0; i < VALUATION_16_BIT; i++) {
	    for (int k = 0; k < CHANNEL_COUNT; k++) {
		mLUT[k][i] = (short) splineValues[k][i];
	    }
	}

	return mLUT;
    }
}
