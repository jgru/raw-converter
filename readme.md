#RawConverter
---
This is an implementation of a raw processing pipeline to process .cr2-Files (raw files from Canon DSLRs) and render them viewable. the processing can be done step by step with custom parameters to fully understand each processing step. The following steps are implemented:
- Cropping of border pixels
- Black level subtraction and normalization
- Demosaicing
- Color space transformation
- Application of LUTs, which are generated from a curves panel
- Noise reduction
- Compensation sharpening
- Export as .tiff file

In addition to that there is functionality to flat stitch raw files, which were captured via a shift lens (TS- or PC-lenses), and export them as .dngs. So it's possible to preserve the 'raw' nature of the files, altough they were already stitched together, which has certain benefits. Stitching is done in native code, which is called with the use of the Java Native Interface. The raw processing pipeline is implemented in pure Java and follows the MVC architecture.

 

##Attention

RawProcessor needs a patched Version of LibTiff 4.0.3, which is used to write .tif and .dng files. For more info see:
http://www.cybercom.net/~dcoffin/dcraw/libtiff.patch
https://github.com/ncsuarc/tiffutils/blob/master/libtiff-4.0.3.patch



The patch should look like this: 
Add support for CFA pattern tags in TIFF images.  This allows creating
DNG images with a custom bayer pattern.
---
 libtiff/tif_dirinfo.c |    2 ++
 libtiff/tiff.h        |    3 +++
 2 files changed, 5 insertions(+)

diff --git a/libtiff/tif_dirinfo.c b/libtiff/tif_dirinfo.c
index d319931..3432925 100644
--- a/libtiff/tif_dirinfo.c
+++ b/libtiff/tif_dirinfo.c
@@ -128,6 +128,8 @@ tiffFields[] = {
 	{ TIFFTAG_PIXAR_FOVCOT, 1, 1, TIFF_FLOAT, 0, TIFF_SETGET_FLOAT, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 1, 0, "FieldOfViewCotangent", NULL },
 	{ TIFFTAG_PIXAR_MATRIX_WORLDTOSCREEN, 16, 16, TIFF_FLOAT, 0, TIFF_SETGET_C0_FLOAT, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 1, 0, "MatrixWorldToScreen", NULL },
 	{ TIFFTAG_PIXAR_MATRIX_WORLDTOCAMERA, 16, 16, TIFF_FLOAT, 0, TIFF_SETGET_C0_FLOAT, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 1, 0, "MatrixWorldToCamera", NULL },
+	{ TIFFTAG_CFAREPEATPATTERNDIM, 2, 2, TIFF_SHORT, 0, TIFF_SETGET_C0_UINT16, TIFF_SETGET_UNDEFINED,	FIELD_CUSTOM, 0,	0,	"CFARepeatPatternDim", NULL },
+	{ TIFFTAG_CFAPATTERN,	4, 4,	TIFF_BYTE, 0, TIFF_SETGET_C0_UINT8, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 0,	0,	"CFAPattern" , NULL},
 	{ TIFFTAG_COPYRIGHT, -1, -1, TIFF_ASCII, 0, TIFF_SETGET_ASCII, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 1, 0, "Copyright", NULL },
 	/* end Pixar tags */
 	{ TIFFTAG_RICHTIFFIPTC, -3, -3, TIFF_LONG, 0, TIFF_SETGET_C32_UINT32, TIFF_SETGET_UNDEFINED, FIELD_CUSTOM, 0, 1, "RichTIFFIPTC", NULL },
diff --git a/libtiff/tiff.h b/libtiff/tiff.h
index 19b4e79..fedcdb8 100644
--- a/libtiff/tiff.h
+++ b/libtiff/tiff.h
@@ -201,6 +201,7 @@ typedef enum {
 #define	    PHOTOMETRIC_CIELAB		8	/* !1976 CIE L*a*b* */
 #define	    PHOTOMETRIC_ICCLAB		9	/* ICC L*a*b* [Adobe TIFF Technote 4] */
 #define	    PHOTOMETRIC_ITULAB		10	/* ITU L*a*b* */
+#define	    PHOTOMETRIC_CFA		32803	/* color filter array */
 #define     PHOTOMETRIC_LOGL		32844	/* CIE Log2(L) */
 #define     PHOTOMETRIC_LOGLUV		32845	/* CIE Log2(L) (u',v') */
 #define	TIFFTAG_THRESHHOLDING		263	/* +thresholding used on data */
@@ -402,6 +403,8 @@ typedef enum {
 #define TIFFTAG_PIXAR_MATRIX_WORLDTOCAMERA 33306
 /* tag 33405 is a private tag registered to Eastman Kodak */
 #define TIFFTAG_WRITERSERIALNUMBER      33405   /* device serial number */
+#define TIFFTAG_CFAREPEATPATTERNDIM	33421	/* dimensions of CFA pattern */
+#define TIFFTAG_CFAPATTERN		33422	/* color filter array pattern */
 /* tag 33432 is listed in the 6.0 spec w/ unknown ownership */
 #define	TIFFTAG_COPYRIGHT		33432	/* copyright string */
 /* IPTC TAG from RichTIFF specifications */
-- 