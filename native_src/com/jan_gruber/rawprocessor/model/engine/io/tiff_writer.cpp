#include "tiff_writer.h"
#include <iostream>

//pulls declaration of malloc/free
#include <stdlib.h>

TiffWriter::TiffWriter(int16* data, int width, int height, int num_bands,
		int compression, int orientation, char* path) {
	//setup TiffWriter
	this->sixteen_bit_data = data;
	this->eight_bit_data =0;
	this->bit_depth = 16;
	this->width = width;
	this->height = height;
	this->num_bands = num_bands;
	this->compression = compression;
	this->orientation = orientation;
	this->path = path;

	this->is_init = true;

}
TiffWriter::TiffWriter(int16* data, int width, int height, int num_bands,
		int orientation, char* path) {
	std::cout << "tw constructor" << std::endl;
	//setup TiffWriter
	this->sixteen_bit_data = data;
	this->eight_bit_data = 0;
	this->bit_depth = 16;
	this->width = width;
	this->height = height;
	this->num_bands = num_bands;
	this->orientation = orientation;
	this->path = path;

	this->is_init = false;

}
TiffWriter::TiffWriter(int width, int height, int num_bands,
		int orientation, char* path) {
	std::cout << "TiffWriter::TiffWriter() constructor" << std::endl;
	//setup TiffWriter
	this->sixteen_bit_data = 0;
	this->eight_bit_data = 0;
	this->bit_depth = 16;
	this->width = width;
	this->height = height;
	this->num_bands = num_bands;
	this->orientation = orientation;
	this->compression=1;
	this->path = path;

	this->is_init = false;

}

TiffWriter::TiffWriter(int8* data, int width, int height, int num_bands,
		int orientation, int compression, char* path) {
	this->eight_bit_data = data;
	this->sixteen_bit_data = 0;
	this->bit_depth = 8;
	this->width = width;
	this->height = height;
	this->num_bands = num_bands;
	this->compression = compression;
	this->orientation = orientation;
	this->path = path;

	this->is_init = true;

}
TiffWriter::~TiffWriter() {
	//don't delete any pointers, because they come from the JNI and have to be released
	//in the JNICALL (one can't access them on this way (maybe they don't lie on the heap(???)) !!!)
}

bool TiffWriter::Write() {
	std::cout << "TiffWriter::Write" << std::endl;
	bool wasSuccessful = false;
	if (is_init) {
		//setup tiff
		TIFF *out = TIFFOpen(path, "w");
		uint32 scanline = num_bands * width;
		uint16 config = PLANARCONFIG_CONTIG;

		//write IFD
		TIFFSetField(out, TIFFTAG_IMAGELENGTH, height);
		TIFFSetField(out, TIFFTAG_IMAGEWIDTH, width);
		TIFFSetField(out, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
		TIFFSetField(out, TIFFTAG_SAMPLESPERPIXEL, num_bands);
		TIFFSetField(out, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_RGB);
		TIFFSetField(out, TIFFTAG_COMPRESSION, compression);
		TIFFSetField(out, TIFFTAG_BITSPERSAMPLE, bit_depth);
		TIFFSetField(out, TIFFTAG_ROWSPERSTRIP,
				TIFFDefaultStripSize(out, scanline));
		//write actual data
		if (bit_depth == 16)
			wasSuccessful = WriteData(out, sixteen_bit_data, scanline, height);
		else if (bit_depth == 8)
			wasSuccessful = WriteData(out, eight_bit_data, scanline, height);

		TIFFClose(out);
		return true;
	}
	return wasSuccessful;
}

bool TiffWriter::WriteData(TIFF* out, int16* data, int scanline, int height) {
	std::cout << "TiffWriter::WriteData" << std::endl;
	uint32 row, col, n;
	uint16 * scan_buf = new uint16[scanline];
	for (row = 0; row < height; row++) {

		for (col = 0; col < scanline; col++) {
			scan_buf[col] = data[col + row * scanline] & 0xFFFF;
		}

		if (TIFFWriteScanline(out, scan_buf, row) != 1) {
			printf("Unable to write a row.");
			return false;
		}
	}
	_TIFFfree(scan_buf);

	return true;
}

bool TiffWriter::WriteData(TIFF* out, uint16* data, int scanline, int height) {
	std::cout << "TiffWriter::writeData" << std::endl;
	uint32 row, col, n;
	uint16 * scan_buf = new uint16[scanline];
	for (row = 0; row < height; row++) {

		for (col = 0; col < scanline; col++) {
			scan_buf[col] = data[col + row * scanline];
		}

		if (TIFFWriteScanline(out, scan_buf, row) != 1) {
			printf("Unable to write a row.");
			return false;
		}
	}
	_TIFFfree(scan_buf);

	return true;
}
bool TiffWriter::WriteData(TIFF* out, int8* data, int scanline, int height) {
	std::cout << "TiffWriter::writeData" << std::endl;
	uint32 row, col, n;
	uint8 * scan_buf = new uint8[scanline];
	for (row = 0; row < height; row++) {

		for (col = 0; col < scanline; col++) {
			scan_buf[col] = data[col + row * scanline] & 0xFFFF;
		}

		if (TIFFWriteScanline(out, scan_buf, row) != 1) {
			printf("Unable to write a row.");
			return false;
		}
	}
	_TIFFfree(scan_buf);

	return true;
}
