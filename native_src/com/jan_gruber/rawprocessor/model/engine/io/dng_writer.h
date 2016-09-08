/*
 * DngWriter.h
 *
 *  Created on: Jan 20, 2014
 *      Author: JanGruber
 */

#ifndef DNGWRITER_H_
#define DNGWRITER_H_
#include <tiffio.h>
#include <string>
#include "tiff_writer.h"

class DngWriter: public TiffWriter {
public:
	DngWriter(int16_t* data, int width, int height, int num_bands, int bit_depth, int orientation,
			char* path);
	DngWriter(uint16_t* data, int width, int height, char* path);
	~DngWriter();

	void SetCameraInfo(char* make, char* model, char* unique_camera_model);
	void SetColorInfo(float* color_matrix_1,
			float* wb_multipliers);
	void SetBlackLevel(float* black_level_per_channel);

	bool Write();

protected:
	void Init();

	void ConvertToOneSample(uint16_t* cfa_data, int16_t* orig_data, int width, int height, int num_bands);
	void CopyCfaData(uint16_t* cfa_data, uint16_t* orig_data, int width, int height);
	uint16* cfa_data;




//IFD#0
	std::string make;
	std::string model;
	std::string unique_camera_model;

	static const char* kSoftware;

	static const char* kDNG_Version;
	 static const char* kDNG_BackwardVersion;

	float* color_matrix_1;
	float* color_matrix_2;
	float* wb_multipliers;
	float* analog_balance;
	int calibration_illuminant_1;
	int calibration_illuminant_2;

	//IFD#1
	static const int kSamplesPerPixel;
	static const float kBaselineExposure;
	char cfa_pattern;
	float* black_level;

};

#endif /* DNGWRITER_H_ */
