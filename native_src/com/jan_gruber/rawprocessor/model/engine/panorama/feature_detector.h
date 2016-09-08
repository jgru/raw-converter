/* $Author: jgruber $ */

/*
 * \file
 * This class houses the functions to detect features - more specifically
 * corners - by applying an implementation of the Harris corner detection.
 *
 */

#ifndef FeatureDetector_h
#define FeatureDetector_h

#include "raw_image.h"

//pulls declaration of uint16_t and so on
#include <stdint.h>
#include <vector>

class FeatureDetector {

public:
	/**
	 * This struct stores the parameters used by the corner detection algorithm
	 */
	struct FeatureDetectionParams {
		double alpha;
		double sigma;
		int threshold;
		int kernel_size;
	};

	FeatureDetector(FeatureDetectionParams params);
	FeatureDetector();
	FeatureDetector(double alpha, double sigma, int threshold, int kernel_size, int descriptor_size);

	/**
	 * This struct represents a detected Harris corner by its coordinates and
	 * its corner response value
	 */
	struct HarrisCorner {
		int x;
		int y;
		double cornerness_value; //corner response value
	};
	//constant patch size 5x5
	static const int kDescriptorSize=5*5;
	/**
	 * This struct represents the found features by the Harris corner
	 * and the belonging patch of image data
	 */
	struct FeatureDescriptor {
		HarrisCorner c;
		uint16_t patch[kDescriptorSize];
	};

	/**
	 * Detects the points of interest in the image by performing a Harris corner detection. Then descriptors are formed, which represent
	 * each feature.
	 *
	 * @param data A pointer to the grayscale data, which should be registered
	 * @param width The width of the raster data, which is about to be registered
	 * @param height The height of the raster data, which is about to be registered
	 *
	 * @return vector of the representations of the found features
	 */
	std::vector<FeatureDescriptor> RetrieveFeatures(uint16_t* data, int width,
			int height);

	/**
	 * Detects the points of interest in the image by performing a Harris corner detection. Then descriptors are formed, which represent
	 * each feature.
	 *
	 * @param data A pointer to the raw image container, which should be processed
	 *
	 * @return vector of the representations of the found features
	 */
	std::vector<FeatureDescriptor> RetrieveFeatures(RawImage *img);

	void set_alpha(double alpha);
	void set_sigma(double sigma);
	void set_threshold(int t);
	void set_kernel_size(double kernelSize);

private:
	/**
	 * The parameters, which define the behaviour of the feature detector
	 */
	FeatureDetectionParams feature_params_;
	static const float kDifferentiationKernel_[];
	static const int kDifferentiationKernelSize_ = 3;

	/**
	 * Calculates the cornerness value by taking the Gaussian smoothed I_x^2, I_y^2, I_x*I_y
	 * into account. See section 4.2.1.1 in the present thesis...
	 *
	 * @param a I_x^2
	 * @param b I_y^2
	 * @param c I_x*I_y
	 *
	 * @return corner response value q, whcih specifies
	 * the cornerness (the bigger the more distinct is the corner)
	 */
	double CalcCornerResponseFunction(double a, double b, double c,
			double alpha);

	/**
	 * Detects points of interest in the given grayscale image by implementing the
	 * harris corner detection. See section 4.2.1.1 in the present thesis for detailled explanation
	 * what this function is doing.
	 *
	 * @param data A pointer to the grayscale data, which should be processed
	 * @param width The width of the raster to process
	 * @param height The height of the raster to process
	 *
	 * @return a vector containing the detected HarrisCorners
	 */
	std::vector<HarrisCorner> DetectHarrisCorners(uint16_t* data, int width,
			int height);

	/**
	 * Forms a representation of the detected features by extracting a
	 * nxn-neighboorhood
	 *
	 * @param data A pointer to the raw image container, which was registered
	 * @param detectedCorners A vector containing the detected corners
	 *
	 * @return a vector containing the representations of the features
	 */
	std::vector<FeatureDescriptor> FormFeatureDescriptors(RawImage* img,
			std::vector<HarrisCorner> detectedCorners);
	std::vector<FeatureDescriptor> FormFeatureDescriptors(uint16_t* data,
			int width, int height, std::vector<HarrisCorner> detectedCorners);
};

#endif
