/*
 * TiffWriter.h
 *
 *  Created on: Jan 20, 2014
 *      Author: JanGruber
 */

#ifndef TIFFWRITER_H_
#define TIFFWRITER_H_
#include <tiffio.h>

class TiffWriter{
public:
	TiffWriter(int16* data, int width, int height, int numBands, int compression, int orientation, char* path);
	TiffWriter(int16* data, int width, int height, int numBands, int orientation, char* path);
	TiffWriter(int8* data, int width, int height, int numBands, int compression,  int orientation, char* path);
	TiffWriter( int width, int height, int numBands, int orientation, char* path);
	~TiffWriter();
	bool Write();

protected:
	bool WriteData(TIFF* out, int16* data, int scanline, int height);
	bool WriteData(TIFF* out, uint16* data, int scanline, int height);
	bool WriteData(TIFF* out, int8* data, int scanline, int height);

	bool is_init;
	char* path;
	uint32 width;
	uint32 height;
	uint16 num_bands;
	uint16 bit_depth;
	int8* eight_bit_data;
	int16* sixteen_bit_data;
	uint16 compression;
	uint16 orientation;



};

#endif /* TIFFWRITER_H_ */
