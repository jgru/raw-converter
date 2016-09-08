#include "raw_image.h"

#include <iostream>
//pulls declaration of malloc/free
#include <stdlib.h>

#define GREEN_OFFSET 1
#define BLUE_OFFSET 2
#include <tiffio.h>

RawImage::RawImage(int16_t* pixel_data, int width, int height,
		int pixel_stride) {

	this->width_ = width;
	this->height_ = height;

	this->green_plane_ = 0;
	//convert 3 banded interleaved raster data to a 1 sample per pixel raster
	cfa_data_ = new uint16_t[width * height];
	int scan = width * pixel_stride;

	for (int y = 0; y < height; y++) {
		for (int x = 0; x < scan - pixel_stride; x += pixel_stride) {

			if (y % 2 == 0) {
				cfa_data_[x / pixel_stride + y * width] =
						pixel_data[x + y * scan] & 0xFFFF;
				x += pixel_stride;
				cfa_data_[x / pixel_stride + y * width] = pixel_data[x + y * scan
						+ GREEN_OFFSET] & 0xFFFF;

			} else {
				cfa_data_[x / pixel_stride + y * width] = pixel_data[x + y * scan
						+ GREEN_OFFSET] & 0xFFFF;
				x += pixel_stride;
				cfa_data_[x / pixel_stride + y * width] = pixel_data[x + y * scan
						+ BLUE_OFFSET] & 0xFFFF;

			}
		}
	}
}

RawImage::RawImage(uint16_t* cfa_data, int width, int height) {
	this->width_ = width;
	this->height_ = height;
	this->cfa_data_ = cfa_data;
	this->green_plane_ = 0;
}
RawImage::RawImage(int width, int height) {
	this->width_ = width;
	this->height_ = height;
	this->cfa_data_ = new uint16_t[width_ * height_];
	this->green_plane_ = 0;
}

RawImage::~RawImage() {
	std::cout << "RawImage::DTOR" << std::endl;

	if (green_plane_)
		delete[] green_plane_;
	if (cfa_data_ != NULL)
		delete[] cfa_data_;
}
RawImage::RawImage(const RawImage& that) {
	this->width_ = that.width_;
	this->height_ = that.height_;
	if (that.green_plane_ != 0) {
		this->green_plane_ = new uint16_t[width_ * height_];
		std::copy(that.green_plane_, that.green_plane_ + width_ * height_,
				this->green_plane_);
	} else
		this->green_plane_ = 0;

	if (that.cfa_data_ != 0) {
		this->cfa_data_ = new uint16_t[width_ * height_];
		std::copy(that.cfa_data_, that.cfa_data_ + width_ * height_,
				this->cfa_data_);
	} else
		this->cfa_data_ = 0;

}
RawImage& RawImage::operator =(const RawImage& that) {
	this->width_ = that.width_;
	this->height_ = that.height_;
	uint16_t* local_cfa_data = 0;
	if (that.cfa_data_ != 0) {
		local_cfa_data = new uint16_t[width_ * height_];
		std::copy(that.cfa_data_, that.cfa_data_ + width_ * height_,
				this->cfa_data_);
	}
	delete[] this->cfa_data_;
	this->cfa_data_ = local_cfa_data;

	uint16_t* local_green_data = 0;
	if (that.green_plane_ != 0) {
		local_green_data = new uint16_t[width_ * height_];
		std::copy(that.green_plane_, that.green_plane_ + width_ * height_,
				this->green_plane_);
	}
	delete[] this->green_plane_;
	this->green_plane_ = local_green_data;
	return *this;
}


int RawImage::get_width() {
	return width_;
}
int RawImage::get_height() {
	return height_;
}
uint16_t* RawImage::get_cfa_data() {
	return cfa_data_;
}
void RawImage::set_cfa_data(uint16_t* cfaData, int width, int height) {
	std::cout << "RawImage::setCfaData" << std::endl;
	if (this->cfa_data_ != NULL)
		delete[] this->cfa_data_;

	this->cfa_data_ = cfaData;
	this->width_ = width;
	this->height_ = height;
}

uint16_t* RawImage::get_green_plane() {
	if (green_plane_ == 0) {
		green_plane_ = new uint16_t[width_ * height_];
		this->InterpolateGreenPlane(cfa_data_, green_plane_, width_, height_);
	}
	return green_plane_;
}


void RawImage::InterpolateGreenPlane(uint16_t* cfa_data,
		uint16_t* green_pixel_data, int width, int height) {
	int pixel_stride = 1;
	int scanLine = width_;
	const int kLeft = -pixel_stride;
	const int kTopLeft = -scanLine - pixel_stride;
	const int kTop = -scanLine;
	const int kTopRight = -scanLine + pixel_stride;
	const int kRight = pixel_stride;
	const int kBottomLeft = scanLine - pixel_stride;
	const int kBottom = scanLine;
	const int kBottomRight = scanLine + pixel_stride;

	//safety zone
	int brink = 1;

	for (int y = brink; y < height_ - brink; y++) {
		for (int x = brink; x < width_ - brink; x++) {
			int scanpos = x + y * scanLine;
			if (y % 2 == 0) {
				//RGRGRG scanline
				if (x % 2 == 0) {
					green_pixel_data[scanpos] = (cfa_data[scanpos + kLeft]
							+ cfa_data[scanpos + kRight]
							+ cfa_data[scanpos + kBottom]
							+ cfa_data[scanpos + kTop]) / 4;
				} else {
					green_pixel_data[scanpos] = (cfa_data[scanpos + kTopLeft]
							+ cfa_data[scanpos + kTopRight]
							+ cfa_data[scanpos + kBottomLeft]
							+ cfa_data[scanpos + kBottomRight]) / 4;
				}

			} else {
				//GBGBGB scanline
				if (x % 2 != 0) {
					green_pixel_data[scanpos] = (cfa_data[scanpos + kLeft]
							+ cfa_data[scanpos + kRight]
							+ cfa_data[scanpos + kBottom]
							+ cfa_data[scanpos + kTop]) / 4;
				} else {
					green_pixel_data[scanpos] = (cfa_data[scanpos + kTopLeft]
							+ cfa_data[scanpos + kTopRight]
							+ cfa_data[scanpos + kBottomLeft]
							+ cfa_data[scanpos + kBottomRight]) / 4;
				}
			}
		}
	}
	std::cout << "green plane successfully interpolated" << std::endl;
	//writeTiff(_greenPlane, _width, _height, 0);
}
