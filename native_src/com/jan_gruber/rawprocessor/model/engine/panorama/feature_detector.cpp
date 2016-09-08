/* $Author: jgruber $ */

#include "feature_detector.h"

//pulls declaration of std::sort()
#include <algorithm>
#include <iostream>
//pulls declaration o sqrt()...
#include <math.h>
//pulls declaration of uint16_t and so on
#include <stdint.h>
//pulls declaration of abs()...
#include <stdlib.h>
#include <vector>


#include "convolution_operator.h"


bool Compare_corner_response(const FeatureDetector::HarrisCorner &a,
		const FeatureDetector::HarrisCorner &b) {
	return b.cornerness_value < a.cornerness_value;
}

const float FeatureDetector::kDifferentiationKernel_[] = { -1.0, 0.0, 1.0 };


FeatureDetector::FeatureDetector() {
	//init with default values
	this->feature_params_.alpha = 0.01;
	this->feature_params_.sigma = 2.6;
	this->feature_params_.threshold = 100000;
	this->feature_params_.kernel_size = 5;
}

FeatureDetector::FeatureDetector(FeatureDetectionParams feature_params) {
	this->feature_params_ = feature_params;
}
FeatureDetector::FeatureDetector(double alpha, double sigma, int threshold,
		int kernel_size, int descriptorSize) {
	this->feature_params_.alpha = alpha;
	this->feature_params_.sigma = sigma;
	this->feature_params_.threshold = threshold;
	this->feature_params_.kernel_size = kernel_size;
}


void FeatureDetector::set_alpha(double alpha) {
	this->feature_params_.alpha = alpha;
}
void FeatureDetector::set_sigma(double sigma) {
	this->feature_params_.sigma = sigma;
}
void FeatureDetector::set_threshold(int threshold) {
	this->feature_params_.threshold = threshold;
}
void FeatureDetector::set_kernel_size(double kernel_size) {
	this->feature_params_.kernel_size = kernel_size;
}

std::vector<FeatureDetector::FeatureDescriptor> FeatureDetector::RetrieveFeatures(
		RawImage *img) {
	return FormFeatureDescriptors(img,
			DetectHarrisCorners(img->get_green_plane(), img->get_width(),
					img->get_height()));
}
std::vector<FeatureDetector::FeatureDescriptor> FeatureDetector::RetrieveFeatures(
		uint16_t* data, int width, int height) {
	return FormFeatureDescriptors(data, width, height,
			DetectHarrisCorners(data, width, height));
}

double FeatureDetector::CalcCornerResponseFunction(double a, double b, double c,
		double alpha) {
	double q_result = ((a * b) - (c * c)) - (alpha * ((a + b) * (a + b)));

	return (q_result / (1<<20));//(1024 * 1024));
}

std::vector<FeatureDetector::HarrisCorner> FeatureDetector::DetectHarrisCorners(
		uint16_t* pixel_data, int width, int height) {

	int radius = kDifferentiationKernelSize_ / 2;
	// initialize arrays to store A,B,C- Values
	double* x_diff = new double[width * height];
	double* y_diff = new double[width * height];
	double* xy_diff = new double[width * height];
	double xDiff, yDiff;

	// iterate through input pixels and calc structure matrix
	for (int v = 0; v < height; v++) {
		for (int u = 0; u < width; u++) {
			xDiff = 0;
			yDiff = 0;

			for (int dy = -radius; dy <= radius; dy++) {
				int tmpIndexY = dy + v;
				if (tmpIndexY > 0 && tmpIndexY < height) {
					int offset = tmpIndexY * width;
					yDiff += kDifferentiationKernel_[dy + radius]
							* pixel_data[u + offset];
				}
			}
			for (int dx = -radius; dx <= radius; dx++) {
				int tmpIndexX = dx + u;
				if (tmpIndexX > 0 && tmpIndexX < width) {
					xDiff += kDifferentiationKernel_[dx + radius]
							* pixel_data[(v * width) + tmpIndexX];
				}
			}

			// store values of structure matrix in float array
			x_diff[u + v * width] = xDiff * xDiff;
			y_diff[u + v * width] = yDiff * yDiff;
			xy_diff[u + v * width] = xDiff * yDiff;
		}
	}
	std::cout << "structure matrix filled" << std::endl;

	ConvolutionOperator convOp;

	float* gauss_kernel = new float[feature_params_.kernel_size
			* feature_params_.kernel_size];
	convOp.CalcGaussKernel(gauss_kernel, feature_params_.kernel_size,
			feature_params_.sigma);

	double* conv_x_diff = new double[width * height];
	convOp.ConvolveWith2dKernel(x_diff, conv_x_diff, width, height,
			gauss_kernel, feature_params_.kernel_size);
	delete[] x_diff;

	double* conv_y_diff = new double[width * height];
	convOp.ConvolveWith2dKernel(y_diff, conv_y_diff, width, height,
			gauss_kernel, feature_params_.kernel_size);
	delete[] y_diff;

	double* conv_xy_diff = new double[width * height];
	convOp.ConvolveWith2dKernel(xy_diff, conv_xy_diff, width, height,
			gauss_kernel, feature_params_.kernel_size);
	delete[] xy_diff;
	delete[] gauss_kernel;

	std::cout << "convolution performed" << std::endl;

	std::vector<HarrisCorner> detected_corners;
	std::cout << "Threshold: " << feature_params_.threshold << std::endl;
	double max = 0;

	// calc corner response function for every pixel - C(x,y)
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			double q = CalcCornerResponseFunction(conv_x_diff[x + (y * width)],
					conv_y_diff[x + (y * width)], conv_xy_diff[x + (y * width)],
					feature_params_.alpha);

			if (q > max)
				max = q;

			if (q > feature_params_.threshold) {
				struct HarrisCorner c = { x, y, q };
				detected_corners.push_back(c);
			}
		}
	}
	delete[] conv_x_diff;
	delete[] conv_y_diff;
	delete[] conv_xy_diff;

	std::cout << "max: " << max << std::endl;

	//sort detected corners by their corner response values
	std::sort(detected_corners.begin(), detected_corners.end(),
			&Compare_corner_response);
	std::cout << detected_corners.size() << std::endl;
	std::cout << " detected corners sorted" << std::endl;

	//perform non-maximum suppression
	int extinction_radius = 50;
	//for (int i = 0; i < 15; i++) {
	//	std::cout << "C" << i << " q: " << detected_corners[i].q << std::endl;
	//}

	for (int i = 0; i < detected_corners.size(); i++) {
		HarrisCorner c = detected_corners[i];

		for (int j = i + 1; j < detected_corners.size(); j++) {
			HarrisCorner o = detected_corners[j];
			if (abs(c.x - o.x) <= extinction_radius
					&& abs(c.y - o.y) <= extinction_radius) {
				detected_corners.erase(detected_corners.begin() + j);
			}
		}
	}

	std::cout << "non-maximum suppression performed" << std::endl;

	return detected_corners;
}

std::vector<FeatureDetector::FeatureDescriptor> FeatureDetector::FormFeatureDescriptors(
		RawImage* img, std::vector<HarrisCorner> detectedCorners) {
	return FormFeatureDescriptors(img->get_cfa_data(), img->get_width(),
			img->get_height(), detectedCorners);
}
std::vector<FeatureDetector::FeatureDescriptor> FeatureDetector::FormFeatureDescriptors(
		uint16_t* data, int width, int height,
		std::vector<HarrisCorner> detectedCorners) {
	int radius = static_cast<int>(sqrt(kDescriptorSize))/2;
	std::cout << "radius" << radius << std::endl;
	std::cout << "width" << width << std::endl;
	std::cout << "height" << height << std::endl;
	std::cout << "corners size" << detectedCorners.size() << std::endl;
	std::vector<FeatureDetector::FeatureDescriptor> featurePatches;

	for (int i = 0; i < detectedCorners.size(); i++) {

		struct FeatureDetector::FeatureDescriptor f = { detectedCorners[i], {
				0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF,
				0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF,
				0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF,
				0xFFFF } };

		int c = 0;
		for (int v = -radius; v <= radius; v++) {
			int tmp_y = v + f.c.y;
			int offset = tmp_y * width;
			if (tmp_y >= 0 && tmp_y < height) {
				for (int u = -radius; u <= radius; u++) {
					int tmp_x = f.c.x + u;
					if (tmp_x >= 0 && tmp_x < width) {
						f.patch[c] = data[tmp_x + offset];
					}
					c++;
				}
			}
		}

		featurePatches.push_back(f);

		//std::cout << "filled features" << std::endl;
	}
	return featurePatches;
}

