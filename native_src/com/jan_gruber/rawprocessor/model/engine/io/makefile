#Source files.
SRC = com_jan_gruber_rawprocessor_model_engine_io_RawImageWriter.cpp TiffWriter.cpp DngWriter.cpp

OUT = ../../../../../../../libImageExport.jnilib

# include directories
INCLUDES = -I/System/Library/Frameworks/JavaVM.framework/Headers

# C++ compiler flags (-g -O2 -Wall)
CCFLAGS = 

# compiler
CC = g++

# library paths
LIBS = -lc -ltiff



$(OUT): 
	$(CC) $(CFlags) $(LIBS) $(INCLUDES) com_jan_gruber_rawprocessor_model_engine_io_RawImageWriter.cpp tiff_writer.cpp dng_writer.cpp -shared -o $(OUT)


