#include "com_jan_gruber_rawprocessor_model_engine_panorama_PanoramaEditor.h"

#include "raw_image.h"
#include "dng_writer.h"
#include "feature_detector.h"
#include "panorama_stitcher.h"
#include <iostream>
#include <vector>
#include <stdio.h>

/* $Author: jgruber $ */

/** \file
 * Implementation of the native methods, that provide functionality to stitch partial images to a panoramic one.
 * These methods are called from java code.
 *
 *
 * This source file, its associated header file and other needed source files are compiled
 * in libPanoramaStitching.jnilib
 *
 */


/* All PURE C++ classes follow loosely the Google styleguide:
 * http://google-styleguide.googlecode.com/svn/trunk/cppguide.xml#General%5FNaming%5FRules
 */


/**
 * This method is
 */


JNIEXPORT void JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_panorama_PanoramaEditor_stitch
(JNIEnv * env, jobject mEditor,
		jobjectArray rawContainers, jobject mParams, jstring path) {


	jsize len= env->GetArrayLength(rawContainers);
	std::vector<RawImage*> images;

	for(int i=0; i<len;i++) {
		// jobject #i
		jobject tmp_obj= env->GetObjectArrayElement(rawContainers,i);
		//retrieve class definition of jobject
		jclass classId= env->GetObjectClass(tmp_obj);
		//retrieve methodID of getRawBuffer() of RawImageContainer.java
		jmethodID methodId= env->GetMethodID(classId, "getRawBuffer","()[S");
		//call object and receive return value
		jobject rawBuffer= env->CallObjectMethod(tmp_obj, methodId);
		//interpret the return value as a short[]
		jshortArray *arr = reinterpret_cast<jshortArray*>(&rawBuffer);
		//get buffer as native types
		jshort* pixelData= env->GetShortArrayElements(*arr,0);

		//get information about the size of received buffer
		jmethodID methodId_width= env->GetMethodID(classId, "getRasterWidth","()I");
		jint width= env->CallIntMethod(tmp_obj, methodId_width);

        jmethodID methodId_height= env->GetMethodID(classId, "getRasterHeight","()I");
		jint height= env->CallIntMethod(tmp_obj, methodId_height);

		jmethodID methodId_numBands= env->GetMethodID(classId, "getNumBands","()I");
		jint numBands= env->CallIntMethod(tmp_obj, methodId_numBands);


		//fill the vector
		images.push_back(new RawImage(pixelData, width, height, numBands));

		env->ReleaseShortArrayElements(*arr, pixelData, 0);
    }

	PanoramaStitcher::StitchingParams params= extractStitchingParameters(env, mParams);
	PanoramaStitcher* mStitcher= new PanoramaStitcher(params);
	mStitcher->Stitch(images);

	//free memory
	while(!images.empty()) {
		RawImage* img= images.back();
		images.pop_back();
		delete img;
	}

	RawImage* result= mStitcher->get_output();


	if(path==NULL) {
		//get class id of caller class
		jclass classId= env->GetObjectClass(mEditor);
		//get method ID of stitchingCallback(short[], int, int);
		jmethodID midCallback= env->GetMethodID(classId, "stitchingCallback","([SII)V");
		//call callback method and "deliver" result back
		env->CallVoidMethod(mEditor, midCallback, result->get_cfa_data(), result->get_width(), result->get_height());
	} else {
		//get first RawImageContainer to retrieve metadata later
		jobject mContainer= env->GetObjectArrayElement(rawContainers,0);
		//export result
		writeDng(env, mContainer,path, result);
	}

	delete mStitcher;
}

PanoramaStitcher::StitchingParams extractStitchingParameters(JNIEnv * env,
		jobject params) {
	jclass classId = env->GetObjectClass(params);

	jmethodID midAlpha = env->GetMethodID(classId, "getAlpha", "()D");
	jdouble alpha = env->CallDoubleMethod(params, midAlpha);

	jmethodID midSigma = env->GetMethodID(classId, "getSigma", "()D");
	jdouble sigma = env->CallDoubleMethod(params, midSigma);

    jmethodID midBoxSize = env->GetMethodID(classId, "getBoxSize", "()I");
	jint boxSize = env->CallIntMethod(params, midBoxSize);

    jmethodID midThreshold = env->GetMethodID(classId, "getThreshold", "()I");
	jint threshold = env->CallIntMethod(params, midThreshold);

	struct FeatureDetector::FeatureDetectionParams featureParams = { alpha,
			sigma, threshold, boxSize};

	jmethodID midSeamlineCode = env->GetMethodID(classId, "getSeamlineCode",
			"()I");
	jint seamlineCode = env->CallIntMethod(params, midSeamlineCode);
	jmethodID midFeatherSize = env->GetMethodID(classId, "getFeatherSizeX",
			"()I");
	jint featherSizeX = env->CallIntMethod(params, midFeatherSize);
	jmethodID midVisualize = env->GetMethodID(classId, "isVisualizeSeamline",
			"()Z");
	jboolean isVisualize = env->CallBooleanMethod(params, midVisualize);
	bool isVisualizeSeam = isVisualize;

	struct PanoramaStitcher::StitchingParams stitchingParams = { featureParams,
			featherSizeX, seamlineCode, isVisualizeSeam };

	return stitchingParams;
}

void writeDng(JNIEnv * env, jobject mContainer, jstring path, RawImage* out) {
	//get the pixel data
	uint16_t* pixelData = out->get_cfa_data();
	int width = out->get_width();
	int height = out->get_height();
	//get filepath and name
	char* file_path = new char[300];
	const char* native_path = env->GetStringUTFChars(path, 0);
	strcpy(file_path, native_path);
	env->ReleaseStringUTFChars(path, native_path);

	//init DngWriter
	DngWriter* mWriter = new DngWriter(pixelData, width, height, file_path);

	jclass classId = env->GetObjectClass(mContainer);
	jmethodID midMetadata = env->GetMethodID(classId, "getMetadata",
			"()Lcom/jan_gruber/rawprocessor/model/engine/store/RawMetadata;");
	jobject metadata = (jobject) env->CallObjectMethod(mContainer, midMetadata);

	//get metadata information
	jclass metadataClassId = env->GetObjectClass(metadata);

	jmethodID midWbMultipliers = env->GetMethodID(metadataClassId,
			"getNeutralWhiteBalanceMultipliers", "()[F");
	jobject wbData = env->CallObjectMethod(metadata, midWbMultipliers);
	jfloatArray wb = reinterpret_cast<jfloatArray>(wbData);
	jsize wbLen = env->GetArrayLength(wb);
	jfloat* wbMultipliers = env->GetFloatArrayElements(wb, 0);
	float* nativeWbMultipliers = new float[wbLen];
	for (int i = 0; i < wbLen; i++) {
		nativeWbMultipliers[i] = static_cast<float>(wbMultipliers[i]);
	}

	jmethodID midColorMatrix1 = env->GetMethodID(metadataClassId,
			"getColorMatrix1", "()[D");
	jobject colorMatrix1Data = env->CallObjectMethod(metadata, midColorMatrix1);
	jdoubleArray cm = reinterpret_cast<jdoubleArray>(colorMatrix1Data);
	jdouble* colorMatrix1Double = env->GetDoubleArrayElements(cm, 0);
	jsize cm1Len = env->GetArrayLength(cm);
	float* colorMatrix1 = new float[cm1Len];
	for (int i = 0; i < cm1Len; i++) {
		colorMatrix1[i] = static_cast<float>(colorMatrix1Double[i]);
	}

	mWriter->SetColorInfo(colorMatrix1, nativeWbMultipliers);

	jmethodID midBlackLevel = env->GetMethodID(metadataClassId,
			"getBlackLevelsPerChannel", "()[S");
	jobject blackLevelData = env->CallObjectMethod(metadata, midBlackLevel);
	jshortArray bl = reinterpret_cast<jshortArray>(blackLevelData);
	jsize blLen = env->GetArrayLength(bl);
	jshort* blackLevelShorts = env->GetShortArrayElements(bl, 0);

	float* blackLevels = new float[blLen];
	for (int i = 0; i < blLen; i++) {
		blackLevels[i] = static_cast<float>(blackLevelShorts[i]);
	}

	mWriter->SetBlackLevel(blackLevels);

	jmethodID midMake = env->GetMethodID(metadataClassId, "getMake",
			"()Ljava/lang/String;");
	jobject makeObj = env->CallObjectMethod(metadata, midMake);
	jstring makeString = reinterpret_cast<jstring>(makeObj);
	jint strMakeLen = env->GetStringUTFLength(makeString);
	const char* constMake = env->GetStringUTFChars(makeString, 0);
	char* make = new char[strMakeLen];
	strcpy(make, constMake);
	env->ReleaseStringUTFChars(makeString, constMake);

	jmethodID midModel = env->GetMethodID(metadataClassId, "getModel",
			"()Ljava/lang/String;");
	jstring modelString = (jstring) env->CallObjectMethod(metadata, midModel);
	jint strModelLen = env->GetStringUTFLength(modelString);
	const char* constModel = env->GetStringUTFChars(modelString, 0);
	char* model = new char[strModelLen];
	strcpy(model, constModel);
	env->ReleaseStringUTFChars(modelString, constModel);

	char uniqueModel[strMakeLen + strModelLen];
	strcpy(uniqueModel, make);
	strcat(uniqueModel, model);

	mWriter->SetCameraInfo(make, model, uniqueModel);
	mWriter->Write();

	env->ReleaseFloatArrayElements(wb, wbMultipliers, 0);
	env->ReleaseDoubleArrayElements(cm, colorMatrix1Double, 0);
	env->ReleaseShortArrayElements(bl, blackLevelShorts, 0);

	delete mWriter;
}

JNIEXPORT jintArray JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_panorama_PanoramaEditor_detectFeatures(
		JNIEnv * env, jobject obj, jobject container, jobject mParams) {

	//retrieve class definition of jobject
	jclass containerClassId = env->GetObjectClass(container);
	std::cout << "containerClassId" << std::endl;
	//retrieve methodID of getRawBuffer() of RawImageContainer.java
	jmethodID methodId = env->GetMethodID(containerClassId, "getRawBuffer",
			"()[S");
	std::cout << "method" << std::endl;
	//call object and receive return value
	jobject rawBuffer = env->CallObjectMethod(container, methodId);
	std::cout << "buffer" << std::endl;
	//interpret the return value as a short[]
	jshortArray *arr = reinterpret_cast<jshortArray*>(&rawBuffer);
	//get buffer as native types
	jshort* pixelData = env->GetShortArrayElements(*arr, 0);
	std::cout << "pixeldata" << std::endl;

	//get information about the size of received buffer
	jmethodID methodId_width = env->GetMethodID(containerClassId,
			"getRasterWidth", "()I");
	jint width = env->CallIntMethod(container, methodId_width);
	std::cout << "width2 " << width << "w orig " << std::endl;
	jmethodID methodId_height = env->GetMethodID(containerClassId,
			"getRasterHeight", "()I");
	jint height = env->CallIntMethod(container, methodId_height);
	std::cout << "height" << height << "h orig " << std::endl;
	jmethodID methodId_numBands = env->GetMethodID(containerClassId,
			"getNumBands", "()I");
	jint numBands = env->CallIntMethod(container, methodId_numBands);
	std::cout << "numbands" << std::endl;

	RawImage img(pixelData, width, height, numBands);

	jclass paramClassId = env->GetObjectClass(mParams);
	jmethodID midAlpha = env->GetMethodID(paramClassId, "getAlpha", "()D");
	jdouble alpha = env->CallDoubleMethod(mParams, midAlpha);
	double mAlpha = alpha;
	std::cout << "alpha" << mAlpha << std::endl;
	jmethodID midThreshold = env->GetMethodID(paramClassId, "getThreshold",
			"()I");
	jint threshold = env->CallIntMethod(mParams, midThreshold);
	int mThreshold = threshold;
	std::cout << "threshold" << mThreshold << std::endl;
	jmethodID midBoxSize = env->GetMethodID(paramClassId, "getBoxSize", "()I");
	jint boxSize = env->CallIntMethod(mParams, midBoxSize);
	int mBoxSize = boxSize;
	std::cout << "box" << mBoxSize << std::endl;
	jmethodID midSigma = env->GetMethodID(paramClassId, "getSigma", "()D");
	jdouble sigma = env->CallDoubleMethod(mParams, midSigma);
	double mSigma = sigma;
	std::cout << "sigma" << mSigma << std::endl;
		jmethodID midDescriptorSize = env->GetMethodID(paramClassId, "getDescriptorSizeX",
				"()I");
		jint descriptorSizeX = env->CallIntMethod(mParams, midDescriptorSize);

	FeatureDetector detector(mAlpha, mSigma, mThreshold, mBoxSize, descriptorSizeX);
	std::vector<FeatureDetector::FeatureDescriptor> v =
			detector.RetrieveFeatures(&img);

	int size = v.size() * 2;
	jintArray feature_coords;
	feature_coords = env->NewIntArray(size);
	std::cout << "jint array set up" << std::endl;
	int filler[size];
	int c = 0;
	for (int i = 0; i < size / 2; i++) {
		filler[c++] = v[i].c.x;
		filler[c++] = v[i].c.y;
	}

	env->SetIntArrayRegion(feature_coords, 0, size, filler);
	//free(filler);

	std::cout << "native task performed" << std::endl;

	return feature_coords;
}

