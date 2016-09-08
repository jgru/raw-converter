created by JanGruber

Compiling libTiffWriting.jnilib
requires patched LibTiff 4.0.3
####################################################################
To patch LibTiff 4.0.3
-> if on MacOSX use Homebrew (brew remove libtiff) to make sure, that all old lib tiff dirs are removed
-> download Libtiff 4.0.3 -> patch it with supplied patch (same dir)
-> cd /path/to/libtiff
-> patch -p1 < /path/to/patch

->./configure
->make
-> sudo make install 

####################################################################

to compile if requirements are met:
1. option: make


2.option
-> cd /path/to/native_src/
->  g++ -o libTiffWriting.jnilib -lc -ltiff -shared \-I/System/Library/Frameworks/JavaVM.framework/Headers model_engine_io_RawImageWriter.cpp

