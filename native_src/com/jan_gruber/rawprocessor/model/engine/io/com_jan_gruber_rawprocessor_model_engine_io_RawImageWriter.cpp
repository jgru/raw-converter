#include "com_jan_gruber_rawprocessor_model_engine_io_RawImageWriter.h"
#include "tiff_writer.h"
#include "dng_writer.h"
#include <iostream>
#include <tiffio.h>
#include <string.h>

JNIEXPORT void JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_io_RawImageWriter_writeDng
(JNIEnv * env, jobject obj, jobject mContainer, jobject metadata, jstring path) {
	std::cout<<"JNICALL writeDng"<<std::endl;

	//get the pixel data
	jclass classId= env->GetObjectClass(mContainer);
	jmethodID methodId= env->GetMethodID(classId, "getProcessedBuffer","()[S");
	jobject buffer= env->CallObjectMethod(mContainer, methodId);
	jshortArray arr = reinterpret_cast<jshortArray>(buffer);
	jshort* pixelData= env->GetShortArrayElements(arr,0);

	//get additional information about the image data
	jmethodID methodId_width= env->GetMethodID(classId, "getRasterWidth","()I");
	jint width= env->CallIntMethod(mContainer, methodId_width);
	jmethodID methodId_height= env->GetMethodID(classId, "getRasterHeight","()I");
	jint height= env->CallIntMethod(mContainer, methodId_height);
	jmethodID methodId_numBands= env->GetMethodID(classId, "getNumBands","()I");
	jint numBands= env->CallIntMethod(mContainer, methodId_numBands);

	jmethodID midOrientation= env->GetMethodID(classId, "getOrientation", "()S");
	jshort orientation = env->CallIntMethod(mContainer, midOrientation);

	//get filepath and name
	char* file_path= new char[300];
	const char* native_path= env->GetStringUTFChars(path,0);
	strcpy(file_path, native_path);
	env->ReleaseStringUTFChars(path, native_path);

	//init DngWriter
	DngWriter* mWriter= new DngWriter (pixelData, width, height, numBands, 16, orientation, file_path);

	//get metadata information
	jclass metadataClassId= env->GetObjectClass(metadata);

	jmethodID midWbMultipliers= env->GetMethodID(metadataClassId, "getNeutralWhiteBalanceMultipliers", "()[F");
	jobject wbData= env->CallObjectMethod(metadata, midWbMultipliers);
	jfloatArray wb= reinterpret_cast<jfloatArray>(wbData);
	jsize wbLen= env->GetArrayLength(wb);
	jfloat* wbMultipliers= env->GetFloatArrayElements(wb,0);
	//create this duplicate array to avoid "pointer beeing freed was not allocated"-error, which is caused,
	//when trying to delete a pointer from the heap, which is actually on the stack
	float* nativeWbMultipliers= new float[wbLen];
	for(int i=0; i<wbLen;i++) {
		nativeWbMultipliers[i]= static_cast<float>(wbMultipliers[i]);
	}

	jmethodID midColorMatrix1= env->GetMethodID(metadataClassId, "getColorMatrix1", "()[D");
	jobject colorMatrix1Data= env->CallObjectMethod(metadata, midColorMatrix1);
	jdoubleArray cm= reinterpret_cast<jdoubleArray>(colorMatrix1Data);
	jdouble* colorMatrix1Double= env->GetDoubleArrayElements(cm,0);
	jsize cm1Len= env->GetArrayLength(cm);
	float* colorMatrix1= new float [cm1Len];
	for(int i=0; i<cm1Len;i++) {
		colorMatrix1[i]= static_cast<float>(colorMatrix1Double[i]);
	}

	mWriter->SetColorInfo(colorMatrix1, nativeWbMultipliers);

	jmethodID midBlackLevel= env->GetMethodID(metadataClassId, "getBlackLevelsPerChannel", "()[S");
	jobject blackLevelData= env->CallObjectMethod(metadata, midBlackLevel);
	jshortArray bl= reinterpret_cast<jshortArray>(blackLevelData);
	jsize blLen= env->GetArrayLength(bl);
	jshort* blackLevelShorts= env->GetShortArrayElements(bl,0);

	float* blackLevels= new float[blLen];
	for(int i=0; i<blLen;i++) {
		blackLevels[i]= static_cast<float>(blackLevelShorts[i]);
	}
	mWriter->SetBlackLevel(blackLevels);

	jmethodID midMake= env->GetMethodID(metadataClassId, "getMake", "()Ljava/lang/String;");
	jobject makeObj = env->CallObjectMethod(metadata, midMake);
	jstring makeString= reinterpret_cast<jstring>(makeObj);
	jint strMakeLen = env->GetStringUTFLength(makeString);
	const char* constMake= env->GetStringUTFChars(makeString,0);
	char* make= new char[strMakeLen];
	strcpy(make, constMake);
	env->ReleaseStringUTFChars(makeString, constMake);

	jmethodID midModel= env->GetMethodID(metadataClassId, "getModel","()Ljava/lang/String;");
	jstring modelString =(jstring) env->CallObjectMethod(metadata, midModel);
	jint strModelLen = env->GetStringUTFLength(modelString);
	const char* constModel= env->GetStringUTFChars(modelString,0);
	char* model= new char[strModelLen];
	strcpy(model, constModel);
	env->ReleaseStringUTFChars(modelString, constModel);

	char uniqueModel [strMakeLen+ strModelLen];
	strcpy(uniqueModel, make);
	strcat(uniqueModel, model);

	mWriter->SetCameraInfo(make, model, uniqueModel);
	mWriter->Write();

	env->ReleaseFloatArrayElements(wb, wbMultipliers, 0);
	env->ReleaseDoubleArrayElements(cm, colorMatrix1Double, 0);
	env->ReleaseShortArrayElements(bl, blackLevelShorts, 0);
	env->ReleaseShortArrayElements(arr, pixelData, 0);
	delete mWriter;
}

JNIEXPORT void JNICALL Java_com_jan_1gruber_rawprocessor_model_engine_io_RawImageWriter_exportTiff
(JNIEnv * env, jobject obj, jobject mContainer, jint compression, jstring path) {
	//get data
	jclass classId= env->GetObjectClass(mContainer);
	jmethodID methodId= env->GetMethodID(classId, "getProcessedBuffer","()[S");
	jobject buffer= env->CallObjectMethod(mContainer, methodId);
	jshortArray arr = reinterpret_cast<jshortArray>(buffer);
	jshort* pixelData= env->GetShortArrayElements(arr,0);
	short* nativePixelData= pixelData;

	//get additional information
	jmethodID methodId_width= env->GetMethodID(classId, "getRasterWidth","()I");
	jint width= env->CallIntMethod(mContainer, methodId_width);
	jmethodID methodId_height= env->GetMethodID(classId, "getRasterHeight","()I");
	jint height= env->CallIntMethod(mContainer, methodId_height);
	jmethodID methodId_numBands= env->GetMethodID(classId, "getNumBands","()I");
	jint numBands= env->CallIntMethod(mContainer, methodId_numBands);

	jmethodID midOrientation= env->GetMethodID(classId, "getOrientation", "()S");
	jshort orientation = env->CallIntMethod(mContainer, midOrientation);

	//get filepath and name
	char file_path[300];
	const char* native_path= env->GetStringUTFChars(path,0);
	strcpy(file_path, native_path);
	env->ReleaseStringUTFChars(path, native_path);

	//setup TiffWriter
	TiffWriter mWriter(nativePixelData, width, height, numBands, compression, orientation, file_path);
	mWriter.Write();

	//free dynamically allocated memory
	env->ReleaseShortArrayElements(arr, pixelData, 0);

}
