package com.jan_gruber.rawprocessor.util;

//specifies constants which are also used in LibTiff, which is used to export
//so there is no need for mapping in native code

//this codes can be found here: http://www.awaresystems.be/imaging/tiff/tifftags/compression.html
public class CompressionConstants {
    public static int COMPRESSION_NONE = 1;
    public static int COMPRESSION_CCITTRLE = 2;  //not implemented due to licensing issues
    public static int COMPRESSION_CCITT_T4 =3;
    public static int COMPRESSION_CCITTFAX3 =  3;
    public static int COMPRESSION_CCITT_T6=4;
    public static int COMPRESSION_CCITTFAX4 = 4;
    public static int COMPRESSION_LZW = 5; //not implemented due to licensing issues
    public static int COMPRESSION_OJPEG = 6;
    public static int COMPRESSION_JPEG = 7;
    public static int COMPRESSION_NEXT = 32766;
    public static int COMPRESSION_CCITTRLEW = 32771;
    public static int COMPRESSION_PACKBITS = 32773;
    public static int COMPRESSION_THUNDERSCAN = 32809;
    public static int COMPRESSION_IT8CTPAD = 32895;
    public static int COMPRESSION_IT8LW = 32896;
    public static int COMPRESSION_IT8MP = 32897;
    public static int COMPRESSION_IT8BL = 32898;
    public static int COMPRESSION_PIXARFILM = 32908;
    public static int COMPRESSION_PIXARLOG = 32909;
    public static int COMPRESSION_DEFLATE = 32946;
    public static int COMPRESSION_ADOBE_DEFLATE = 8;
    public static int COMPRESSION_DCS = 32947;
    public static int COMPRESSION_JBIG = 34661;
    public static int COMPRESSION_SGILOG = 34676;
    public static int COMPRESSION_SGILOG24 = 34677;
    public static int COMPRESSION_JP2000 = 34712;


}
