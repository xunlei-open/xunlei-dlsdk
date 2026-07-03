#pragma once
#include <jni.h>
#include "xl_dl_sdk.h"

#ifdef  __cplusplus
extern "C" {
#endif

// JNI 函数声明
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_init(JNIEnv *env, jclass clazz, jobject joInitParam);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_uninit(JNIEnv *env, jclass clazz);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_login(JNIEnv *env, jclass clazz, jstring loginToken, jobject sessionIdGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getUnfinishedTask(JNIEnv *env, jclass clazz, jlongArray joTaskIdArray, jobject countGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getFinishedTask(JNIEnv *env, jclass clazz, jlongArray joTaskIdArray, jobject countGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_createP2spTask(JNIEnv *env, jclass clazz, jobject joCreateInfo, jobject taskIdGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_startTask(JNIEnv *env, jclass clazz, jlong taskId);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_stopTask(JNIEnv *env, jclass clazz, jlong taskId);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_deleteTask(JNIEnv *env, jclass clazz, jlong taskId, int deleteFile);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getTaskState(JNIEnv *env, jclass clazz, jlong taskId, jobject joState);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_getTaskInfo(JNIEnv* env, jclass clazz, jlong taskId, jstring infoName, jobject infoGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setConcurrentTaskCount(JNIEnv *env, jclass clazz, jint count);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setDownloadSpeedLimit(JNIEnv *env, jclass clazz, jint speed);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setUploadSwitch(JNIEnv *env, jclass clazz, jint uploadSwitch);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setUploadSpeedLimit(JNIEnv *env, jclass clazz, jint speed);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_version(JNIEnv *env, jclass clazz, jobject versionGetter);
JNIEXPORT jint JNICALL Java_com_xunlei_open_dlsdk_XLDownloadAPI_setDownloadUrlAcceleration(JNIEnv *env, jclass clazz, jboolean enable);

}
