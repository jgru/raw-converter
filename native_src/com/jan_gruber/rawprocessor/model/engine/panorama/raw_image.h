/* $Author: jgruber $ */

/*
 * \file
 * This class is stores the bare minimum information necessary to
 * handle raw partial images with regular Bayer-pattern, which are
 * about to get stitched together.
 *
 */

#ifndef RawImage_h
#define RawImage_h

//pulls declaration of uint16_t...
#include <stdint.h>

#define GREEN_OFFSET 1
#define BLUE_OFFSET 2

class RawImage {
public:
	RawImage(int16_t* cfa_data, int width, int height, int pixel_stride);
	RawImage(uint16_t* cfa_data, int width, int height);
	RawImage(int width, int height);
	~RawImage();
	//define copy and copy assignment constructor,
		//because memory is allocated by this class
		//explanation:http://stackoverflow.com/questions/4172722/what-is-the-rule-of-three
		RawImage(const RawImage& that);
		RawImage& operator=(const RawImage& that);

	/**
	 * Returns the pointer to the interpolated green plane of the cfa data.
	 *
	 * @return a pointer to interpolated green plane of the raw image data
	 */
	uint16_t* get_green_plane();

	/**
	 * Returns the raw one sample per pixel CFA data
	 *
	 * @return the pointer to the raw cfa data
	 */
	uint16_t* get_cfa_data();
	/**
	 * Receives new cfa_data and associated width and height. The old
	 * cfa_data_ gets deleted.
	 *
	 * @param cfa_data The pointer to the new cfa data to store
	 * @param width The width of the new raster
	 * @param height The height of the new raster
	 */
	void set_cfa_data(uint16_t* cfa_data, int width, int height);


	int get_width();
	int get_height();

private:
	int width_;
	int height_;
	uint16_t* cfa_data_;
	uint16_t* green_plane_;

	/**
	 * Interpolates the missing green samples bilinearly and stores them
	 * in memory pointed to by the second parameter (green_plane)
	 *
	 * @param cfa_data Pointer to the RGGB raw data
	 * @param green_plane Pointer to the memory, where the interpolated green samples are stored
	 * @param width The width of both rasters
	 * @param height The height of both rasters
	 */
	void InterpolateGreenPlane(uint16_t* cfa_data, uint16_t* green_data,
			int width, int height);

};
#endif
