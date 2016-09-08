/* $Author: jgruber $ */

#include "convolution_operator.h"
#include <iostream>
#define _USE_MATH_DEFINES
#include <math.h>



//Source of Gauss formula:
// http://upload.wikimedia.org/math/9/5/e/95ecdbb16befd4fdb760fa26c83a4b5e.png
void ConvolutionOperator::CalcGaussKernel(float* kernel_data, int kernel_size,
		double sigma) {
	std::cout << "ConvolutionOperator::calcGaussKernel()" << std::endl;

	int radius = kernel_size / 2;
	float euler_term = (float) (1.0f / (2.0f * M_PI * sigma * sigma));
	float distance = 0;
	float sum = 0;
	for (int y = -radius; y <= radius; y++) {
		int offset = (y + radius) * kernel_size;
		for (int x = -radius; x <= radius; x++) {
			distance = (float) ((x * x + y * y) / (2 * sigma * sigma));
			kernel_data[(x + radius) + offset] = (float) ((float) euler_term
					* exp(-distance));

			sum += kernel_data[(x + radius) * (y + radius)];
		}
	}
	for (int i = 0; i < kernel_size * kernel_size; i++) {
		kernel_data[i] = kernel_data[i] * (1.f / sum);
	}

}

void ConvolutionOperator::ConvolveWith2dKernel(double* in_data,
		double* out_data, int width, int height, float* kernel,
		int kernel_width) {
	std::cout << "ConvolutionOperator::convolveWith2dKernel()" << std::endl;

	int radius = kernel_width / 2;

	int counter = 0;

	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			float value = 0;

			for (int dy = -radius; dy <= radius; dy++) {
				int tmp_index_y = y + dy;
				if (0 <= tmp_index_y && tmp_index_y < height) {
					int offset = tmp_index_y * width;
					int kernel_offset = kernel_width * (dy + radius) + radius;
					for (int dx = -radius; dx <= radius; dx++) {
						float f = kernel[dx + kernel_offset];
						if (f != 0) {
							int tmp_index_x = x + dx;
							if (0 <= tmp_index_x && tmp_index_x < width) {
								double pixel = in_data[tmp_index_x + offset];
								value += f * pixel;

							}
						}
					}
				}
			}
			out_data[counter++] = value;
		}
	}
}
void ConvolutionOperator::ConvolveWith2dKernel(uint16_t* in_data,
		uint16_t* out_data, int width, int height, float* kernel,
		int kernel_width) {
	std::cout << "ConvolutionOperator::convolveWith2dKernel()" << std::endl;

	int radius = kernel_width / 2;

	int counter = 0;

	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			float value = 0;

			for (int dy = -radius; dy <= radius; dy++) {
				int tmp_index_y = y + dy;
				if (0 <= tmp_index_y && tmp_index_y < height) {
					int offset = tmp_index_y * width;
					int kernel_offset = kernel_width * (dy + radius) + radius;
					for (int dx = -radius; dx <= radius; dx++) {
						float f = kernel[dx + kernel_offset];
						if (f != 0) {
							int tmp_index_x = x + dx;
							if (0 <= tmp_index_x && tmp_index_x < width) {
								double pixel = in_data[tmp_index_x + offset];
								value += f * pixel;

							}
						}
					}
				}
			}
			out_data[counter++] = value;
		}
	}
}


