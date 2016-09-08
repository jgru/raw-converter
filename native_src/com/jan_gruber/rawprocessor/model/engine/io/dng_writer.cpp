/*
 * DngWriter.cpp
 *
 *  Created on: Jan 20, 2014
 *      Author: JanGruber
 */
#include "dng_writer.h"
#include "tiff_writer.h"
#include <tiffio.h>
#include <string>
#include <iostream>
#define GREEN_OFFSET 1
#define BLUE_OFFSET 2

const char* DngWriter::kSoftware = "RawStitcher";
const int DngWriter::kSamplesPerPixel = 1;
const float DngWriter::kBaselineExposure = 2.0;
const char* DngWriter::kDNG_Version = "\001\004\0\0";
const char* DngWriter::kDNG_BackwardVersion = "\001\0\0\0";

enum tiff_cfa_color {
	CFA_RED = 0, CFA_GREEN = 1, CFA_BLUE = 2,
};

enum cfa_pattern {
	CFA_BGGR = 0, CFA_GBRG, CFA_GRBG, CFA_RGGB, CFA_NUM_PATTERNS,
};

static const char cfa_patterns[4][CFA_NUM_PATTERNS] = {
	[CFA_BGGR] = {CFA_BLUE, CFA_GREEN, CFA_GREEN, CFA_RED},
	[CFA_GBRG] = {CFA_GREEN, CFA_BLUE, CFA_RED, CFA_GREEN},
	[CFA_GRBG] = {CFA_GREEN, CFA_RED, CFA_BLUE, CFA_GREEN},
	[CFA_RGGB] = {CFA_RED, CFA_GREEN, CFA_GREEN, CFA_BLUE},
};

DngWriter::DngWriter(int16_t* data, int width, int height, int num_bands,
		int bitDepth, int orientation, char* path) :
		TiffWriter(data, width, height, num_bands, orientation, path) {
	this->cfa_data = new uint16[width * height];
	ConvertToOneSample(cfa_data, data, width, height, num_bands);

	this->width = width;
	this->height = height;
	this->path = path;
	Init();
	this->is_init = true;

}
DngWriter::DngWriter(uint16_t* data, int width, int height, char* path) :
		TiffWriter(width, height, 1, 1, path) {
	//make a copy of the data: (avoid double deleting pointers, etc..)
	this->width = width;
	this->height = height;
	this->path = path;
	this->cfa_data = new uint16_t[width*height];
	CopyCfaData(this->cfa_data, data, width, height);

	Init();
	this->is_init = true;

}

DngWriter::~DngWriter() {
	std::cout << "DngWriter::DTOR" << std::endl;

	if (cfa_data){
		delete[] cfa_data;
	}

	std::cout << "DngWriter::DTOR->cfa deleted" << std::endl;
	if (color_matrix_1)
		delete[] color_matrix_1;
	std::cout << "DngWriter::DTOR-> cm1 deleted" << std::endl;
	if (wb_multipliers)
		delete[] wb_multipliers;
	std::cout << "DngWriter::DTOR-> wb deleted" << std::endl;
	if (black_level)
		delete[] black_level;
	std::cout << "DngWriter::DTOR-> bl deleted" << std::endl;

}
void DngWriter::Init() {
	std::cout << "DngWriter::init" << std::endl;

	calibration_illuminant_1 = 17; //standard illuminant A
	calibration_illuminant_2 = 21; //D65
	bit_depth = 16;

}

void DngWriter::ConvertToOneSample(uint16_t* cfa_data, int16_t* orig_data, int width, int height,
		int num_bands) {
	std::cout << "DngWriter::convertToOneSample" << std::endl;
	int c = 0;
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width * num_bands - num_bands; x += num_bands) {

			if (y % 2 == 0) {
				cfa_data[c++] = orig_data[x + y * width * num_bands] & 0xFFFF;
				x += num_bands;
				cfa_data[c++] = orig_data[x + y * width * num_bands + GREEN_OFFSET]
						& 0xFFFF;

			} else {
				cfa_data[c++] = orig_data[x + y * width * num_bands + GREEN_OFFSET]
						& 0xFFFF;
				x += num_bands;
				cfa_data[c++] = orig_data[x + y * width * num_bands + BLUE_OFFSET]
						& 0xFFFF;
			}
		}
	}
}

void DngWriter::CopyCfaData(uint16_t* cfa_data, uint16_t* orig_data, int width, int height){
	for(int y=0; y<height;y++){
		for(int x=0; x<width;x++){
			cfa_data[x+y*width]= orig_data[x+y*width];

		}
	}

}
void DngWriter::SetCameraInfo(char* make, char* model,
		char* uniqueCameraModel) {
	this->make = make;
	this->model = model;
	this->unique_camera_model = uniqueCameraModel;

}
void DngWriter::SetColorInfo(float* color_matrix_1, float* wb_multipliers) {
	this->color_matrix_1 = color_matrix_1;
	this->wb_multipliers = wb_multipliers;

}
void DngWriter::SetBlackLevel(float* black_level_per_channel) {
	this->black_level = black_level_per_channel;
}

bool DngWriter::Write() {
	std::cout << "DngWriter::write()" << std::endl;

	bool is_successful = false;
	if (is_init) {
		long suboffset = 0;
		//setup tiff
		TIFF *tif = TIFFOpen(path, "w");
		uint16 config = PLANARCONFIG_CONTIG;

		TIFFSetField(tif, TIFFTAG_SUBFILETYPE, 1);
		TIFFSetField(tif, TIFFTAG_IMAGEWIDTH, (width >> 4));
		TIFFSetField(tif, TIFFTAG_IMAGELENGTH, (height >> 4));
		TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, 8);
		TIFFSetField(tif, TIFFTAG_COMPRESSION, COMPRESSION_NONE);
		TIFFSetField(tif, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_RGB);
		TIFFSetField(tif, TIFFTAG_MAKE, make.c_str());
		TIFFSetField(tif, TIFFTAG_MODEL, model.c_str());
		TIFFSetField(tif, TIFFTAG_ORIENTATION, ORIENTATION_TOPLEFT);
		TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, 3);
		TIFFSetField(tif, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
		TIFFSetField(tif, TIFFTAG_SOFTWARE, DngWriter::kSoftware);
		//TIFFSetField (tif, TIFFTAG_DATETIME, datetime);

		//point to "Raw IFD"
		TIFFSetField(tif, TIFFTAG_SUBIFD, 1, &suboffset);
		TIFFSetField(tif, TIFFTAG_DNGVERSION, DngWriter::kDNG_Version);
		TIFFSetField(tif, TIFFTAG_DNGBACKWARDVERSION,
				DngWriter::kDNG_BackwardVersion);
		TIFFSetField(tif, TIFFTAG_UNIQUECAMERAMODEL, "Canon EOS 5D Mark III");
		TIFFSetField(tif, TIFFTAG_COLORMATRIX1, 9, color_matrix_1);
		//TIFFSetField(tif, TIFFTAG_COLORMATRIX2, 9, colorMatrix2);
		TIFFSetField(tif, TIFFTAG_ASSHOTNEUTRAL, 3, wb_multipliers);
		TIFFSetField(tif, TIFFTAG_BASELINEEXPOSURE, kBaselineExposure);
		TIFFSetField(tif, TIFFTAG_ANALOGBALANCE, 3,
				(float[3] ) { 1.0, 1.0, 1.0 });
		TIFFSetField(tif, TIFFTAG_CALIBRATIONILLUMINANT1,
				calibration_illuminant_1);
		TIFFSetField(tif, TIFFTAG_CALIBRATIONILLUMINANT2,
				calibration_illuminant_2);
		//write black thumbnail
		uint16 * scan_buf = new uint16[width];
		for (int row = 0; row < height >> 4; row++)
			TIFFWriteScanline(tif, scan_buf, row);

		TIFFWriteDirectory(tif);
		//specify as Main IFD
		TIFFSetField(tif, TIFFTAG_SUBFILETYPE, 0);
		//TIFFSetField(tif, TIFFTAG_ORIENTATION, ORIENTATION_TOPLEFT);
		TIFFSetField(tif, TIFFTAG_IMAGEWIDTH, width);
		TIFFSetField(tif, TIFFTAG_IMAGELENGTH, height);
		TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, bit_depth);
		TIFFSetField(tif, TIFFTAG_COMPRESSION, COMPRESSION_NONE);
		TIFFSetField(tif, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_CFA);
		TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, 1);
		TIFFSetField(tif, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
		TIFFSetField(tif, TIFFTAG_CFAREPEATPATTERNDIM, (short[] ) { 2, 2 });
		TIFFSetField(tif, TIFFTAG_CFAPATTERN, cfa_patterns[3]);
		TIFFSetField(tif, TIFFTAG_BLACKLEVELREPEATDIM, (short[] ) { 2, 2 });
		TIFFSetField(tif, TIFFTAG_BLACKLEVEL, 4, black_level);

		is_successful = TiffWriter::WriteData(tif, cfa_data, width, height);

		TIFFClose(tif);
	}
	return is_successful;
}

