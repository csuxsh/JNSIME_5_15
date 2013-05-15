#include "com_jnselectronics_ime_jni_ScreenShot.h"
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <linux/fb.h>
#include "android/log.h"

#define LOG_TAG "screenshot"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG, __VA_ARGS__)



#ifdef __cplusplus
extern "C" {
#endif

extern int save_bmp(char * path);

/*
 * Class:     com_jnselectronics_ime_jni_ScreenShot
 * Method:    getScreenShot
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_ScreenShot_getScreenShot
(JNIEnv *env, jclass jclazz)
{
	//if(take_screenshot(fb_in, png))
	if(save_bmp("/mnt/sdcard/jnsinput/tmp.bmp"))
		return JNI_TRUE;
	return JNI_FALSE;
}


#ifdef __cplusplus
}
#endif
