
# source files.
SRC = com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor.cpp convolution_operator.cpp dng_writer.cpp feature_detector.cpp panorama_stitcher.cpp raw_image.cpp tiff_writer.cpp

OUT = ../../../../../../../libPanoramaStitching.jnilib

# include directories
INCLUDES = -I/System/Library/Frameworks/JavaVM.framework/Headers 

# C++ compiler flags (-g -O2 -Wall)
CCFLAGS =

# compiler
CC = g++

# library paths
LIBS = -lc -ltiff



$(OUT): 
	$(CC) $(CFlags) $(LIBS) $(INCLUDES) com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor.cpp convolution_operator.cpp dng_writer.cpp feature_detector.cpp panorama_stitcher.cpp raw_image.cpp tiff_writer.cpp -shared -o $(OUT)

