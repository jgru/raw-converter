/* $Author: jgruber $ */

/** \file
 * The ConvolutionOperator class houses the functionality to convolve
 * one band per pixel- raster data.
 */

#ifndef ConvolutionOperator_h
#define ConvolutionOperator_h
//pulls declaration of uint16_t and so on
#include <stdint.h>
class ConvolutionOperator {
public:

/**
 Calculates a quadratic Gaussian kernel with the specified size and sigma and fills the result in kernel_data

 @param kernel_data The float array which will contain the result
 @param kernel_size The size in one dimension of the quadratic kernel
 @param sigma Trivially speaking specifies sigma the amount of blurring

 */
void CalcGaussKernel(float* kernel_data, int kernel_size, double sigma);

/**
 Convolves an input raster with a given, quadratic kernel and saves it to the given output raster

 @param in_data the input raster
 @param out_data the output raster, where the result is stored
 @param width the width of the rasters
 @param height the height of the rasters
 @param kernel the convolution kernel to apply
 @param kernel_size_x the dimension in one direction of the qudratic kernel
 */
void ConvolveWith2dKernel(double* in_data, double* out_data, int width,
		int height, float* kernel, int kernel_size_x);

void ConvolveWith2dKernel(uint16_t* in_data, uint16_t* out_data, int width,
		int height, float* kernel, int kernel_size_x);

};
#endif
