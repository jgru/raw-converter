#include "panorama_stitcher.h"

//pulls declaration of std::sort(), min...
#include <algorithm>
//pulls declaration of std::abs() (neccessary for abs of doubles)
#include <cmath>
#include <iostream>
//pulls the declaration of std::numeric_limits<...>
#include <limits>
#include <math.h>
#include <stdlib.h>
//pulls declaration of uint16_t and so on
#include <stdint.h>
#include <tiffio.h>
#include <vector>

#include"convolution_operator.h"
#include "feature_detector.h"
#include "raw_image.h"

PanoramaStitcher::PanoramaStitcher() {
	this->detector_ = new FeatureDetector();
	this->out_image_ = 0;
	this->params_.feather_size = 10;
	this->params_.seamline_code = 1;

}
PanoramaStitcher::PanoramaStitcher(StitchingParams params) {
	this->params_ = params;
	this->detector_ = new FeatureDetector(params_.feature_params);
	this->out_image_ = 0;

}

PanoramaStitcher::~PanoramaStitcher() {
	if (detector_ != NULL)
		delete detector_;
	if (out_image_ != NULL)
		delete out_image_;

}
PanoramaStitcher::PanoramaStitcher(const PanoramaStitcher& that) {
	this->params_ = that.params_;

	if (that.detector_ != 0) {
		this->detector_ = new FeatureDetector(that.params_.feature_params);
	} else
		this->detector_ = 0;

	if (that.out_image_ != 0) {
		this->out_image_ = new RawImage(*that.out_image_);
	} else
		this->out_image_ = 0;
}
PanoramaStitcher& PanoramaStitcher::operator =(const PanoramaStitcher& that) {
	this->params_ = that.params_;
	this->detector_ = that.detector_;
	this->out_image_ = that.out_image_;
	return *this;
}


RawImage* PanoramaStitcher::get_output() {
	return this->out_image_;
}

void PanoramaStitcher::Stitch(std::vector<RawImage*> images) {
	if (images.size() > 0) {
		//initialize the required data structures
		std::vector<FeatureDetector::FeatureDescriptor> m_features;
		std::vector<FeatureDetector::FeatureDescriptor> o_features;
		int* cumulated_translation_ = new int[2];
		cumulated_translation_[0] = cumulated_translation_[1] = 0;
		int* current_translation_ = new int[2];
		current_translation_[0] = current_translation_[1] = 0;

		InitOutput(images[0]);

		//iterate over each partial image
		for (int i = 0; i < images.size() - 1; i++) {
			if (i < 1)
				m_features = detector_->RetrieveFeatures(images[i]);

			o_features = detector_->RetrieveFeatures(images[i + 1]);
			std::vector<PanoramaStitcher::Correspondance> corrs = matchFeatures(
					m_features, o_features);
			CalcHomography(current_translation_, corrs);
			cumulated_translation_[0] += current_translation_[0];
			cumulated_translation_[1] += current_translation_[1];

			std::cout << "ctrans " << cumulated_translation_[0] << std::endl;
			std::cout << "ctrans " << cumulated_translation_[1] << std::endl;

			//add another image
			CreateRawComposite(images[i + 1],
					cumulated_translation_);

			//swap the vectors
			std::vector<FeatureDetector::FeatureDescriptor>().swap(m_features);	//create empty vector and swap it-> deallocating the memory http://stackoverflow.com/questions/10464992/c-delete-vector-objects-free-memory
			m_features = o_features;
		}

		if (current_translation_ != NULL)
			delete[] current_translation_;
		if (cumulated_translation_ != NULL)
			delete[] cumulated_translation_;
	}
}

int PanoramaStitcher::CalcMSE(FeatureDetector::FeatureDescriptor f1,
		FeatureDetector::FeatureDescriptor f2) {
	int mse = 0;
	for (int i = 0; i < detector_->kDescriptorSize; i++) {	//FIXME
		mse += (abs(f1.patch[i] - f2.patch[i]));
	}
	return mse;
}

std::vector<PanoramaStitcher::Correspondance> PanoramaStitcher::matchFeatures(
		std::vector<FeatureDetector::FeatureDescriptor> m_features,
		std::vector<FeatureDetector::FeatureDescriptor> o_features) {
	std::vector<PanoramaStitcher::Correspondance> corrs;

	//correspondance* corr = (correspondance*) malloc(
	//	m_length * sizeof(correspondance));
	std::cout << "correspondances allocated" << std::endl;

	for (int i = 0; i < m_features.size(); i++) {
		FeatureDetector::FeatureDescriptor tmp = m_features[i];
		//make an inital comparison
		FeatureDetector::FeatureDescriptor best_match = o_features[0];
		int mse = CalcMSE(tmp, best_match);

		//compare to every feature of the picture to stitch
		for (int j = 0; j < o_features.size(); j++) {
			int tmp_mse = PanoramaStitcher::CalcMSE(tmp, o_features[j]);
			//std::cout<<tmp_mse<<std::endl;
			if (tmp_mse < mse) {
				mse = tmp_mse;
				best_match = o_features[j];
			}
		}

		//save temporal feature and its the best match as correspondance
		//if they don't have the same coords=> exclude some features in the corners of the images
		if (tmp.c.x != best_match.c.x && tmp.c.y != best_match.c.y) {
			PanoramaStitcher::Correspondance corr = { tmp, best_match, mse };
			corrs.push_back(corr);
			//std::cout << "c.f1.x " << c.f1.x << "-" << " c.f2.x " << c.f2.x
			//		<< std::endl;
			//	std::cout << "c.f1.y " << c.f1.y << "-" << " c.f2.y " << c.f2.y
			//	<< std::endl;

			//std::cout << "corr " << i << "-" << " set; mse " << mse
			//		<< std::endl;
		}
	}

	return corrs;
}

void PanoramaStitcher::CalcHomography(int* translation,
		std::vector<PanoramaStitcher::Correspondance> corrs) {
	int tolerance = 2;
	int count = 40;
	std::vector<PanoramaStitcher::Correspondance> matching_corrs;

	int max_agreement = 0;

	for (int i = 0; i < count; i++) {

		std::cout << corrs[i].mse << " mse,  " << i << std::endl;
		int x_offset = (corrs[i].f1.c.x - corrs[i].f2.c.x);
		std::cout << i << " x-offset: " << (corrs[i].f1.c.x - corrs[i].f2.c.x)
				<< std::endl;
		int y_offset = (corrs[i].f1.c.y - corrs[i].f2.c.y);
		std::cout << i << " y-offset: " << (corrs[i].f1.c.y - corrs[i].f2.c.y)
				<< std::endl;
		std::vector<PanoramaStitcher::Correspondance> tmp_matches;
		tmp_matches.push_back(corrs[i]);

		for (int j = 0; j < count; j++) {
			if ((abs(x_offset - (corrs[j].f1.c.x - corrs[j].f2.c.x)) < tolerance)
					&& (abs(y_offset - (corrs[j].f1.c.y - corrs[j].f2.c.y))
							< tolerance)) {
				tmp_matches.push_back(corrs[j]);
			}
		}

		if (tmp_matches.size() > max_agreement) {
			matching_corrs.swap(tmp_matches);
			max_agreement = matching_corrs.size();
		}
	}

	//calculate the mean of the translations within the tolerance
	translation[0] = 0;
	translation[1] = 0;
	for (int i = 0; i < matching_corrs.size(); i++) {
		translation[0] += (matching_corrs[i].f1.c.x - matching_corrs[i].f2.c.x);
		translation[1] += (matching_corrs[i].f1.c.y - matching_corrs[i].f2.c.y);

	}

	translation[0] = translation[0] / (int) matching_corrs.size(); //result of vector.size() is unsigned-> cast to prevent rollover
	translation[1] = translation[1] / (int) matching_corrs.size();
	std::cout << "final x-offset: " << translation[0] << std::endl;
	std::cout << "final y-offset: " << translation[1] << std::endl;
}

void WriteGrayTiff(uint32_t* mData, int width, int height, int i) {
	//setup tiff
	std::cout << "write tiff" << std::endl;
	TIFF *out;
	if (i == 0)
		out = TIFFOpen("/Users/JanGruber/diff_test.tiff", "w");
	else
		out = TIFFOpen("/Users/JanGruber/stitch_test_other_green_plane.tiff",
				"w");
	uint32 image_width = width;
	uint32 image_height = height;
	uint16 pixel_stride = 1;
	uint32 scanline = image_width;
	int32 comp = 1;
	int16 bps = 32;

	TIFFSetField(out, TIFFTAG_IMAGELENGTH, image_height);
	TIFFSetField(out, TIFFTAG_IMAGEWIDTH, image_width);
	//TIFFSetField(out, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
	TIFFSetField(out, TIFFTAG_SAMPLESPERPIXEL, pixel_stride);
	TIFFSetField(out, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_MINISBLACK);
	TIFFSetField(out, TIFFTAG_COMPRESSION, comp);
	TIFFSetField(out, TIFFTAG_BITSPERSAMPLE, bps);
	TIFFSetField(out, TIFFTAG_ROWSPERSTRIP,
			TIFFDefaultStripSize(out, scanline));

	std::cout << "setup ifd" << std::endl;
	uint32 * scan_buf = new uint32[scanline];
	uint32 row, col, n;
	for (row = 0; row < image_height; row++) {

		for (col = 0; col < scanline; col++) {
			scan_buf[col] = mData[col + row * scanline];

		}
		if (TIFFWriteScanline(out, scan_buf, row) != 1) {
			printf("Unable to write a row.");
			break;
		}
	}

	_TIFFfree(scan_buf);
	TIFFClose(out);

}
void PanoramaStitcher::InitOutput(RawImage* base_img) {
	int width = base_img->get_width();
	int height = base_img->get_height();
	out_image_ = new RawImage(width, height);

	uint16_t* in_buffer = base_img->get_cfa_data();
	uint16_t* out_buffer = out_image_->get_cfa_data();

	for (int y = 0; y < out_image_->get_height(); y++) {
		for (int x = 0; x < out_image_->get_width(); x++) {
			out_buffer[x + y * width] = in_buffer[x + y * width];
		}
	}
}

void PanoramaStitcher::CreateRawComposite(
		RawImage* img_to_add, int* trans) {
	double feather_size = static_cast<double>(params_.feather_size);
	bool is_visualize_seamline = params_.is_visualize_seamline;

	std::cout << "final x-offset: " << trans[0] << std::endl;
	std::cout << "final y-offset: " << trans[1] << std::endl;

//get the raster data of the partial images
	uint16_t* cfa_data = out_image_->get_cfa_data();
	int m_width = out_image_->get_width();
	int m_height = out_image_->get_height();

	uint16_t* other_cfa_data = img_to_add->get_cfa_data();
	int o_width = img_to_add->get_width();
	int o_height = img_to_add->get_height();

	int scan = 0;
	int out_scan = 0;
	int out_x = 0;
	int out_y = 0;

//specifies the offset of other_cfa_data
	int diff_x = trans[0];
	int diff_y = trans[1];

//correct cfa alignment
	if (diff_x % 2 != 0) {
		if (diff_x > 0)
			diff_x--;
		else {
			diff_x++;
		}
	}
	if (diff_y % 2 != 0) {
		if (diff_y > 0)
			diff_y--;
		else
			diff_y++;
	}

	/*  Preffered layouts:
	 *########################################################
	 *                          |
	 * x_diff>0 and y_diff>0:   |        x_diff>0 and y_diff<0:
	 *  ____________            |                ___________
	 * |  m         |           |               |       o   |
	 * |         ___|_______    |        _______|____       |
	 * |        |   |       |   |       |       |    |      |
	 * |________|___|       |   |       |       |____|______|
	 *          |        o  |   |       | m          |
	 *          |___________|   |       |____________|
	 *                          |
	 *                          |
	 *                          |
	 *                          |
	 *  layouts to avoid:       |
	 *#######################################################
	 *                          |
	 * x_diff<0 and y_diff<0:   |        x_diff<0 and y_diff>0:
	 *  ____________            |                ___________
	 * |  o         |           |               |       m   |
	 * |         ___|_______    |        _______|___        |
	 * |        |   |       |   |       |       |   |       |
	 * |________|___|       |   |       |       |___|_______|
	 *          |       m   |   |       | o         |
	 *          |___________|   |       |___________|
	 *							|
	 *
	 */
	bool is_swapped;
//swap the two images, if they form an unwanted layout
//this is neccessary to simplify seamline computation
	if (diff_x < 0 && diff_y < 0) {
		uint16_t* tmp_data = cfa_data;
		cfa_data = other_cfa_data;
		other_cfa_data = tmp_data;
		diff_x = abs(diff_x);
		diff_y = abs(diff_y);
		is_swapped = true;
	}

	else if (diff_x < 0 && diff_y > 0) {
		uint16_t* tmp_data = cfa_data;
		cfa_data = other_cfa_data;
		other_cfa_data = tmp_data;
		diff_x = abs(diff_x);
		diff_y = -1 * diff_y;
		is_swapped = true;
	}

	out_y = abs(diff_y);

//declare temporary pixels
	uint16_t m_pixel;
	uint16_t o_pixel;

//set up the output raster
	int out_width = m_width + abs(diff_x);
	int out_height = m_height + abs(diff_y);

	std::vector<uint16_t> vertical_path;

	if (params_.seamline_code == 0) {
		vertical_path = ComputeSimpleBoundaryCut(img_to_add, out_width,
				out_height, diff_x, diff_y, is_swapped);
	} else {
		vertical_path = ComputeMinErrorBoundaryCut(img_to_add, out_width,
				out_height, diff_x, diff_y, is_swapped);
	}

	uint16_t* out_data = new uint16_t[out_width * out_height];
	std::cout << "allocated out array" << std::endl;
	std::cout << "out_width: " << out_width << std::endl;
	std::cout << "out_height: " << out_height << std::endl;

//fill the output raster
	for (int y = 0; y < out_height; y++) {

		for (int x = 0; x < out_width; x++) {
			scan = x + y * out_width;
			m_pixel = 0;
			o_pixel = 0;

			//get the relevant pixel of each buffer, if there is one
			if (x - out_x >= 0 && x - out_x < m_width && y - out_y >= 0
					&& y - out_y < m_height) {
				double act_x = x - out_x;
				double act_y = y - out_y;
				m_pixel = cfa_data[x - out_x + (y - out_y) * m_width];
			}

			if (x - out_x - diff_x >= 0 && x - out_x - diff_x < o_width
					&& y - out_y - diff_y >= 0
					&& y - out_y - diff_y < o_height) {
				double act_x = x - out_x - diff_x;
				double act_y = y - out_y - diff_y;
				o_pixel = other_cfa_data[x - out_x - diff_x
						+ (y - out_y - diff_y) * o_width];
			}

			if (m_pixel == 0 || o_pixel == 0) {
				out_data[scan] = m_pixel + o_pixel;
			} else if (m_pixel != 0 && o_pixel != 0) {
				if (diff_y > 0) {
					if (x < vertical_path[y]) {

						if (x > vertical_path[y] - feather_size) {
							double alpha = (vertical_path[y] + feather_size - x)
									/ (feather_size * 2);
							out_data[scan] = alpha * m_pixel
									+ (1 - alpha) * o_pixel;
						} else
							out_data[scan] = m_pixel;

						//out_data[scan] = m_pixel;
					} else {

						if (x < vertical_path[y] + feather_size) {
							double alpha = abs(
									x - (vertical_path[y] - feather_size))
									/ (feather_size * 2);
							out_data[scan] = alpha * o_pixel
									+ (1 - alpha) * m_pixel;
						} else
							out_data[scan] = o_pixel;

						//out_data[scan] = o_pixel;
						if (is_visualize_seamline && x == vertical_path[y])
							out_data[scan] = o_pixel * 2;
					}

				} else {
					if (x < vertical_path[y]) {

						if (x > vertical_path[y] - feather_size) {
							double alpha = (vertical_path[y] + feather_size - x)
									/ (feather_size * 2);
							out_data[scan] = alpha * m_pixel
									+ (1 - alpha) * o_pixel;
						} else
							out_data[scan] = m_pixel;

						//out_data[scan] = m_pixel;
					} else {

						if (x < vertical_path[y] + feather_size) {
							double alpha = abs(
									x - (vertical_path[y] - feather_size))
									/ (feather_size * 2);
							out_data[scan] = alpha * o_pixel
									+ (1 - alpha) * m_pixel;
						} else
							out_data[scan] = o_pixel;

						//out_data[scan] = o_pixel;

						if (is_visualize_seamline && x == vertical_path[y]) {
							out_data[scan] = o_pixel * 2;
							std::cout << vertical_path[y] << std::endl;
						}
					}

				}
			}
		}
	}
	out_image_->set_cfa_data(out_data, out_width, out_height);

}

std::vector<uint16_t> PanoramaStitcher::ComputeMinErrorBoundaryCut(
		RawImage* img_to_add, int out_width, int out_height, int diff_x,
		int diff_y, bool is_swapped) {
	uint16_t* m_data;
	uint16_t* o_data;
	int m_width;
	int m_height;
	int o_width;
	int o_height;

	//swap here as well
	if (is_swapped) {
		o_data = out_image_->get_green_plane();
		o_width = out_image_->get_width();
		o_height = out_image_->get_height();

		m_data = img_to_add->get_green_plane();
		m_width = img_to_add->get_width();
		m_height = img_to_add->get_height();
	} else {
		m_data = out_image_->get_green_plane();
		m_width = out_image_->get_width();
		m_height = out_image_->get_height();

		o_data = img_to_add->get_green_plane();
		o_width = img_to_add->get_width();
		o_height = img_to_add->get_height();

	}

	int out_y = abs(diff_y);
	int out_x = 0;

	//setup the difference raster
	uint32_t* diff_x_data = new uint32_t[out_width * out_height];
	int min_y = abs(diff_y); // < 0 ? 0 : diff_y;
	//compute the error surface/difference raster
	for (int y = 0; y < out_height; y++) {
		for (int x = 0; x < out_width; x++) {
			int scan = x + y * out_width;
			int m_pixel = 0;
			int o_pixel = 0;
			//get the relevant pixel of each raster
			if (x - out_x >= 0 && x - out_x < m_width && y - out_y >= 0
					&& y - out_y < m_height) {
				m_pixel = m_data[x - out_x + (y - out_y) * m_width];
			}

			if (x - out_x - diff_x >= 0 && x - out_x - diff_x < o_width
					&& y - out_y - diff_y >= 0
					&& y - out_y - diff_y < o_height) {
				o_pixel = o_data[x - out_x - diff_x
						+ (y - out_y - diff_y) * o_width];
			}

			if (x < diff_x || x >= m_width - 2) {
				diff_x_data[scan] = 0xFFFFFFFF;
			} else {
				//prevent rollover
				uint64_t diff_value = abs(m_pixel - o_pixel);
				diff_x_data[scan] = diff_value > 0xFFFFFFFF ? 0xFFFFFFFF : diff_value;

				//calc E(x,y) = e(x,y) + min(E(x-1, y-1), E(x,y-1), E(x+1,y-1))
				if (y > out_y+3) {
					if (x < 1)
						diff_value =
								diff_value
										+ std::min(
												diff_x_data[x
														+ (y - 1) * out_width],
												diff_x_data[x + 1
														+ (y - 1) * out_width]);
					else if (x > 0 && x < out_width - 1)
						diff_value =
								diff_value
										+ std::min(
												diff_x_data[x - 1
														+ (y - 1) * out_width],
												std::min(
														diff_x_data[x
																+ (y - 1)
																		* out_width],
														diff_x_data[x + 1
																+ (y - 1)
																		* out_width]));
					else
						diff_value =
								diff_value
										+ std::min(
												diff_x_data[x - 1
														+ (y - 1) * out_width],
												diff_x_data[x
														+ (y - 1) * out_width]);

					diff_x_data[scan] =
							diff_value > 0xFFFFFFFF ? 0xFFFFFFFF : diff_value;

				}
			}
		}
	}
	std::cout << "calculated E(x,y)" << std::endl;

	//for debug purposes
	//WriteGrayTiff(diff_x_data, out_width, out_height, 0);

	//datastructure to store the x-coordinates of the vertical
	// minimum cost path for each scanline
	std::vector<uint16_t> vertical_path(out_height);

	//form vertical minimum error boundary cut
	//search the minimum value in the last row and trace
	//the minimum error path up to the top
	int max_value = std::numeric_limits<int>::max(); //the maximum value, that can possibly emerge
	int search_offset = 35;
	//the overlap
	int max_y =
			diff_y > 0 ? m_height - search_offset : o_height - search_offset;
	for (int y = out_height - 1; y >= min_y; y--) {
		int min = max_value;
		if (y == out_height - 1) {
			//exclude the absolute borders
			for (int x = diff_x; x < m_width; //maybe change to feathersize again
					x++) {
				int scan = x + y * out_width;
				if (diff_x_data[scan] < min) {
					min = diff_x_data[scan];
					vertical_path[y] = x;
				}
			}

		} else
			for (int x = vertical_path[y + 1] - 1;
					x <= vertical_path[y + 1] + 1; x++) {
				int scan = x + y * out_width;
				if (diff_x_data[scan] < min && x > diff_x) {
					min = diff_x_data[scan];
					vertical_path[y] = x;
				}
			}
	}
	std::cout << "calculated vertical path" << std::endl;

	return vertical_path;
}

std::vector<uint16_t> PanoramaStitcher::ComputeSimpleBoundaryCut(
		RawImage* img_to_add, int out_width, int out_height, int diff_x,
		int diff_y, bool is_swapped) {
	/*uint16_t* m_data = out_image_->get_green_plane();
	 int m_width = out_image_->get_width();
	 int m_height = out_image_->get_height();

	 uint16_t* o_data = img_to_add->get_green_plane();
	 int o_width = img_to_add->get_width();
	 int o_height = img_to_add->get_height();*/
	uint16_t* m_data;
	uint16_t* o_data;
	int m_width;
	int m_height;
	int o_width;
	int o_height;

	//swap here as well
	if (is_swapped) {
		o_data = out_image_->get_green_plane();
		o_width = out_image_->get_width();
		o_height = out_image_->get_height();

		m_data = img_to_add->get_green_plane();
		m_width = img_to_add->get_width();
		m_height = img_to_add->get_height();
	} else {
		m_data = out_image_->get_green_plane();
		m_width = out_image_->get_width();
		m_height = out_image_->get_height();

		o_data = img_to_add->get_green_plane();
		o_width = img_to_add->get_width();
		o_height = img_to_add->get_height();

	}

	int out_y = abs(diff_y);
	int out_x = 0;

	//setup the difference raster
	uint32_t* diff_data = new uint32_t[out_width * out_height];

	//compute the difference raster
	for (int y = 0; y < out_height; y++) {
		for (int x = 0; x < out_width; x++) {
			int scan = x + y * out_width;
			int m_pixel = 0;
			int o_pixel = 0;

			//get the relevant pixel of each raster
			if (x - out_x >= 0 && x - out_x < m_width && y - out_y >= 0
					&& y - out_y < m_height) {
				m_pixel = m_data[x - out_x + (y - out_y) * m_width];
			}

			if (x - out_x - diff_x >= 0 && x - out_x - diff_x < o_width
					&& y - out_y - diff_y >= 0
					&& y - out_y - diff_y < o_height) {
				o_pixel = o_data[x - out_x - diff_x
						+ (y - out_y - diff_y) * o_width];
			}
			//calc absolute difference
			diff_data[scan] = abs(m_pixel - o_pixel);
		}
	}
    //for debug purposes
	//WriteGrayTiff(diff_data, out_width, out_height, 0);
    
    
	//set identifier of seamline direction
	//(either diagonal top right to bottom left, or top left to bottom right)
	bool isRightToLeftSeam;
	if (diff_x > 0 && diff_y > 0)
		isRightToLeftSeam = true;
	else
		isRightToLeftSeam = false;

	int x_pos;
	if (isRightToLeftSeam)
		x_pos = m_width - 5;	//-5 neccessary?
	else
		x_pos = diff_x + 5;

	std::cout << x_pos << " x_pos" << std::endl;
//the overlap
	int max_y = diff_y > 0 ? m_height - 1 : o_height - 1;
	int min_y = diff_y; // < 0 ? 0 : diff_y;

	std::vector<uint16_t> vertical_path(out_height);
	vertical_path[min_y] = x_pos;

	for (int y = min_y; y < max_y; y++) {
		int curr_pos = vertical_path[y];
		std::cout << curr_pos << " curr_pos" << std::endl;

		int scan = curr_pos + y * out_width;

		if (isRightToLeftSeam) {
			/* Top right to bottom left seamline
			 * ->check, where to place the cut in this scanline by seeking the minimum of the left,
			 *the bottom-left and the bottom neighboor of the current pixel
			 *in the difference image buffer
			 *
			 *	_________________________
			 *	|           |###########|
			 *	|    x-1    |   x_pos   |    <---- current scanline
			 *	|           |###########|
			 *	-------------------------
			 *	|           |           |
			 *	|           |   x; y+1  |    <---- next scanline
			 *	|           |           |
			 *	|___________|___________|
			 *
			 */

			if (diff_data[scan - 1] < diff_data[scan + out_width]
					&& diff_data[scan - 1] < diff_data[scan - 1 + out_width]) {
				vertical_path[y] = --curr_pos;
				vertical_path[y + 1] = vertical_path[y];
			} else
				vertical_path[y + 1] = curr_pos;

		} else {
			/*Top left to bottom right seamline
			 *	_________________________
			 *	|###########|           |
			 *	| curr_pos  |   x+1;y   |    <---- current scanline
			 *	|###########|           |
			 *	-------------------------
			 *	|           |           |
			 *	|   x; y+1  |           |    <---- next scanline
			 *	|           |           |
			 *	|_______________________|
			 *
			 */

			if (diff_data[scan + 1] < diff_data[scan + 1 + out_width]
					&& diff_data[scan + 1] < diff_data[scan + out_width]) {
				vertical_path[y] = ++curr_pos;
				vertical_path[y + 1] = vertical_path[y];
			} else
				vertical_path[y + 1] = curr_pos;
		}

	}
	std::cout << "calculated vertical path" << std::endl;

	delete[] diff_data;
	return vertical_path;
}

