/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include "panorama_stitcher.h"
#include "raw_image.h"
/* Header for class com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor */

#ifndef _Included_com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor
#define _Included_com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor
#ifdef __cplusplus
extern "C" {
#endif
    
    
    JNIEXPORT jintArray JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_panorama_PanoramaEditor_detectFeatures
    (JNIEnv *, jobject, jobject, jobject);
    
    
    JNIEXPORT void JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_panorama_PanoramaEditor_stitch
    (JNIEnv * env, jobject,
     jobjectArray, jobject, jstring);
    

    void writeDng(JNIEnv *, jobject, jstring, RawImage*);

    PanoramaStitcher::StitchingParams extractStitchingParameters(JNIEnv *, jobject);
    
#ifdef __cplusplus
}
#endif
#endif