package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.util.HashMap;

public class MatrixLookUp {
    private static HashMap<String, double[]> matrixByCameraName = new HashMap<String, double[]>();
    static {
	//retrieved from ColorMatrx2 (0xC622) Tag after converting to DNGs
	matrixByCameraName.put("Canon EOS 20D", new double[] { 0.6599, -0.0537,
		-0.0891, -0.8071, 1.5783, 0.2424, -0.1983, 0.2234, 0.7462 });
	matrixByCameraName.put("Canon EOS 450D", new double[] { 0.5784,
		-0.0262, -0.0821, -0.7539, 1.5064, 0.2672, -0.1982, 0.2681,
		0.7427 });
	matrixByCameraName.put("Canon EOS 5D", new double[] { 0.6347, -0.0479,
		-0.0972, -0.8297, 1.5954, 0.2480, -0.1968, 0.2131, 0.7649 });
	matrixByCameraName.put("Canon EOS 5D Mark II", new double[] { 0.4716,
		0.0603, -0.0830, -0.7798, 1.5474, 0.2480, -0.1496, 0.1937,
		0.6651 });
	matrixByCameraName.put("Canon EOS 5D Mark III", new double[] { 0.6722,
		-0.0635, -0.0963, -0.4287, 1.246, 0.2028, -0.0908, 0.2162,
		0.5668 });
	matrixByCameraName.put("Canon EOS 40D", new double[] { 0.6071, -0.0747,
		-0.0856, -0.7653, 1.5365, 0.2441, -0.2025, 0.2553, 0.7315 });

	matrixByCameraName.put("Canon EOS 6D", new double[] { 0.7034, -0.0804,
		-0.1014, -0.4420, 1.2564, 0.2058, -0.0851, 0.1994, 0.5758 });

	//retrieved from: http://www.cybercom.net/~dcoffin/dcraw/dcraw.c
	matrixByCameraName.put("Canon EOS 7D", new double[] { 0.6844, -0.0996,
		-0.0856, -0.3876, 1.1761, 0.2396, -0.593, 0.1772, 0.6198 });
	matrixByCameraName.put("Canon EOS 10D", new double[] { 8197, -2000,
		-1118, -6714, 14335, 2592, -2536, 3178, 8266 });
	matrixByCameraName.put("Canon EOS 20Da", new double[] { 1.4155,
		-0.5065, -0.1382, -0.6550, 1.4633, 0.2039, -0.1623, 0.1824,
		0.6561 });
	matrixByCameraName.put("Canon EOS 30D", new double[] { 0.6257, -0.0303,
		-0.1000, -0.7880, 1.5621, 0.2396, -0.1714, 0.1904, 0.7046 });
	matrixByCameraName.put("Canon EOS 40D", new double[] { 0.6071, -0.747,
		-0.856, -0.7653, 1.5365, 0.2441, -0.2025, 0.2553, 0.7315 });
	matrixByCameraName.put("Canon EOS 50D", new double[] { 0.4920, 0.0616,
		-0.0593, -0.6493, 1.3964, 0.2784, -0.1774, 0.3178, 0.7005 });
	matrixByCameraName.put("Canon EOS 60D", new double[] { 0.6719, -0.0994,
		-0.0925, -0.4408, 1.2426, 0.2211, -0.0887, 0.2129, 0.6051 });
	matrixByCameraName.put("Canon EOS 100D", new double[] { 0.6602,
		-0.0841, -0.0939, -0.4472, 1.2458, 0.2247, -0.0975, 0.2039,
		0.6148 });
	matrixByCameraName.put("Canon EOS 300D", new double[] { 0.8197,
		-0.2000, -0.1118, -0.6714, 1.4335, 0.2592, -0.2536, 0.3178,
		0.8266 });
	matrixByCameraName.put("Canon EOS 350D", new double[] { 0.6018,
		-0.0617, -0.0965, -0.8645, 1.5881, 0.2975, -0.1530, 0.1719,
		0.7642 });
	matrixByCameraName.put("Canon EOS 400D", new double[] { 0.7054,
		-0.1501, -0.0990, -0.8156, 1.5544, 0.2812, -0.1278, 0.1414,
		0.7796 });
	matrixByCameraName.put("Canon EOS 450D", new double[] { 0.5784,
		-0.0262, -0.0821, -0.7539, 1.5064, 0.2672, -0.1982, 0.2681,
		0.7427 });
	matrixByCameraName.put("Canon EOS 500D", new double[] { 0.4763, 0.0712,
		-0.0646, -0.6821, 1.4399, 0.2640, -0.1921, 0.3276, 0.6561 });
	matrixByCameraName.put("Canon EOS 550D", new double[] { 0.6941,
		-0.1164, -0.0857, -0.3825, 1.1597, 0.2534, -0.0416, 0.1540,
		0.6039 });
	matrixByCameraName.put("Canon EOS 600D", new double[] { 0.6461,
		-0.0907, -0.0882, -0.4300, 1.2184, 0.2378, -0.0819, 0.1944,
		0.5931 });
	matrixByCameraName.put("Canon EOS 650D", new double[] { 0.6602,
		-0.0841, -0.0939, -0.4472, 1.2458, 0.2247, -0.0975, 0.2039,
		0.6148 });
	matrixByCameraName.put("Canon EOS 700D", new double[] { 0.6602,
		-0.0841, -0.0939, -0.4472, 1.2458, 0.2247, -0.0975, 0.2039,
		0.6148 });
	matrixByCameraName.put("Canon EOS 1000D", new double[] { 0.6771,
		-0.1139, -0.0977, -0.7818, 1.5123, 0.2928, -0.1244, 0.1437,
		0.7533 });
	matrixByCameraName.put("Canon EOS 1100D", new double[] { 0.6444,
		-0.0904, -0.0893, -0.4563, 1.2308, 0.2535, -0.0903, 0.2016,
		0.6728 });
	matrixByCameraName.put("Canon EOS M", new double[] { 0.6602, -0.0841,
		-0.0939, -0.4472, 1.2458, 2.247, -0.0975, 0.2039, 0.6148 });
	matrixByCameraName.put("Canon EOS-1Ds Mark III", new double[] { 0.5859,
		-0.0211, -0.0930, -0.8255, 1.6017, 0.2353, -0.1732, 0.1887,
		0.7448 });
	matrixByCameraName.put("Canon EOS-1Ds Mark II", new double[] { 0.6517,
		-0.0602, -0.0867, -0.8180, 1.5926, 0.2378, -0.1618, 0.1771,
		0.7633 });
	matrixByCameraName.put("Canon EOS-1D Mark IV", new double[] { 0.6014,
		-0.0220, -0.0795, -0.4109, 1.2014, 0.2361, -0.0561, 0.1824,
		0.5787 });
	matrixByCameraName.put("Canon EOS-1D Mark III", new double[] { 0.6291,
		-0.0540, -0.0976, -0.8350, 1.6145, 0.2311, -0.1714, 0.1858,
		0.7326 });
	matrixByCameraName.put("Canon EOS-1D Mark II N", new double[] { 0.6240,
		-0.0466, -0.0822, -0.8180, 1.5825, 0.2500, -0.1801, 0.1938,
		0.8042 });
	matrixByCameraName.put("Canon EOS-1D Mark II", new double[] { 0.6264,
		-0.0582, -0.0724, -0.8312, 1.5948, 0.2504, -0.1744, 0.1919,
		0.8664 });

    }

    public static double[] getMatrixByCameraName(String key) {
	if (matrixByCameraName.containsKey(key)) {
	    return matrixByCameraName.get(key);
	} else
	    return null;
    }
}
