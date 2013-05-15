#include "jni.h"
#define BUILD_NDK
#ifdef BUILD_NDK
#include "android/log.h"
#include <pthread.h>
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)
#endif
#ifndef _Included_com_jnselectronics_ime_jni_InputAdapter
#define _Included_com_jnselectronics_ime_jni_InputAdapter

#ifdef __cplusplus
extern "C" {
#endif

extern  pthread_cond_t getDeviceCond;
extern  pthread_mutex_t getDeviceMutex;

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    init
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_init
  (JNIEnv *, jclass);

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    start
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_start
  (JNIEnv *, jclass);

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    stop
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_stop
  (JNIEnv *, jclass);

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    getKey
 * Signature: (Lcom/jnselectronics/ime/jni/RawEvent;)V
 */
JNIEXPORT void JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getKey
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    getJoyStick
 * Signature: (Lcom/jnselectronics/ime/jni/RawEvent;)V
 */
JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getJoyStick
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_jnselectronics_ime_jni_InputAdapter
 * Method:    getDeviceList
 * Signature: ()Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getDeviceList
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif

