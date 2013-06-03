#define LOG_TAG "JN_INPUT_ADAPTER"

#ifdef BUILD_NDK
#else
//#include <utils/Log.h>
//#include "JNIHelp.h"
//#include "android_runtime/AndroidRuntime.h"
#endif
#include <assert.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <linux/input.h>
#include "global.h"
#include "InputAdapter.h"
#include "CallBackInterface.h"
#include "com_jnselectronics_ime_jni_InputAdapter.h"
#include <sys/syscall.h>



#define gettid()   syscall(__NR_gettid)


static int ovalue = 0;
static int getvalue = 0;
static bool waitForgetKey = false;
static bool waitForgetJoy = false;
static int joy_num = 0;


struct fields_t {
	jmethodID onInputAdapterKeyDown;
	jmethodID onInputAdapterKeyUp;
	jmethodID onInputAdapterJoystickChange;
};

static fields_t inputCallBackField;

typedef struct tagJNiGlobale_t {
	JNIEnv *env;
	JavaVM *jvm;
} stJniGlobal_t;

static stJniGlobal_t sg_jni_global;
static jclass g_com_jnselectronics_ime_jni_InputAdapter_class;

//消费者信号量
static pthread_cond_t keyCond;
static pthread_mutex_t keyMutex;
static pthread_cond_t joyCond;
static pthread_mutex_t joyMutex;
pthread_cond_t getDeviceCond;
pthread_mutex_t getDeviceMutex;


static void init_jni_global() {
	memset(&sg_jni_global, 0x00, sizeof(stJniGlobal_t));
}


#ifdef BUILD_NDK
class AndroidRuntime {
public:
	static JNIEnv* getJNIEnv() {
		if (NULL != sg_jni_global.jvm && NULL == sg_jni_global.env) {
			sg_jni_global.jvm->GetEnv((void **) &sg_jni_global.env,
					JNI_VERSION_1_4);
		}
		return sg_jni_global.env;
	}
};
#else
using namespace android;
#endif

struct RawKeyEvent {
	int scanCode;
	int value;
	int KeyCode;
	int deviceId;
};

struct JoyStickEvent {
	int x;
	int y;
	int z;
	int rz;
	int hat_x;
	int hat_y;
	int deviceId;
};

struct RawKeyEvent keyEvent;
struct JoyStickEvent joyStickEvent;
static void doOnKeyDown(int scanCode, int value, int deviceId)
{
	keyEvent.scanCode = scanCode;
	keyEvent.value = value;
	keyEvent.deviceId = deviceId;
}

static void doOnKeyUp(int scanCode, int value, int deviceId)
{
	keyEvent.scanCode = scanCode;
	keyEvent.value = value;
	keyEvent.deviceId = deviceId;
	//LOGE("[%s][%d] ==>  scancode = 0X%02X value = 0x%02x", __FUNCTION__, __LINE__, scanCode, value);
}

static void doOnJoystickDataChange(int scanCode, int value, int deviceId)
{

	joyStickEvent.deviceId = deviceId;
	switch(scanCode)
	{
	case ABS_X:
		joyStickEvent.x = value;
		break;
	case ABS_Y:
		joyStickEvent.y = value;
		break;
	case ABS_Z:
		joyStickEvent.z = value;
		break;
	case ABS_RZ:
		joyStickEvent.rz = value;
		break;
	case ABS_HAT0X:
		joyStickEvent.hat_x = value;
		break;
	case ABS_HAT0Y:
		joyStickEvent.hat_y = value;
		break;
	}
}
void delay(int i)
{
	while(i)
	{
		i--;
	}
}

class InputAdapterCallBack : public CallBackInterface {
public:
	int keyProcess(const RawEvent *rawEvent) {

		int scanCode = rawEvent->scanCode;
		int value = rawEvent->value;
		int deviceId = rawEvent->deviceId;

		while(waitForgetKey)
		{
			LOGE("[%s][%d] ==> waitForgetKey", __FUNCTION__, __LINE__);
		}
		pthread_mutex_lock(&keyMutex);
		if (value == 1) {
			doOnKeyDown(scanCode, value, deviceId);
		} else if(value == 0){
			doOnKeyUp(scanCode, value, deviceId);
		}
		//delay(1000);
		pthread_cond_signal(&keyCond);
		pthread_mutex_unlock(&keyMutex);

		return 1; 
	};

	int joystickProcess(const RawEvent *rawEvent) { 
		int scanCode = rawEvent->scanCode;
		int value = rawEvent->value;
		int deviceId = rawEvent->deviceId;

		while(waitForgetJoy)
		{
			LOGE("[%s][%d] ==> waitForgetJoy", __FUNCTION__, __LINE__);
		}
		pthread_mutex_lock(&joyMutex);
		if(rawEvent->type != 0)
			doOnJoystickDataChange(scanCode, value, deviceId);
		pthread_cond_signal(&joyCond);
		pthread_mutex_unlock(&joyMutex);
		LOGE("[%s][%d] ==> notify getjoy ", __FUNCTION__, __LINE__);
		//delay(1000);
		return 1; 
	};
};

#ifdef BUILD_NDK
static InputAdapter* mInputAdapter = NULL;
static InputAdapterCallBack* mInputAdapterCallBack;
#else
static sp<InputAdapter> mInputAdapter = NULL;
static sp<InputAdapterCallBack> mInputAdapterCallBack;
#endif

JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_init(JNIEnv *env, jclass clazz) {
	init_jni_global();
	env->GetJavaVM(&sg_jni_global.jvm);
	pthread_mutex_init(&keyMutex, NULL);
	pthread_cond_init(&keyCond, NULL);
	pthread_mutex_init(&joyMutex, NULL);
	pthread_cond_init(&joyCond, NULL);
	pthread_mutex_init(&getDeviceMutex, NULL);
	pthread_cond_init(&getDeviceCond, NULL);
	mInputAdapter = InputAdapter::create();
	mInputAdapterCallBack = new InputAdapterCallBack();
	mInputAdapter->getKeyManager()->registerCallBackInterface(mInputAdapterCallBack);
	mInputAdapter->getJoystick()->registerCallBackInterface(mInputAdapterCallBack);
	joyStickEvent.x = 0x7f;
	joyStickEvent.y = 0x7f;
	joyStickEvent.z = 0x7f;
	joyStickEvent.rz = 0x7f;
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_start(JNIEnv *env, jclass clazz) {
	if (mInputAdapter == NULL) {
		LOGE("[%s][%d] ==> mInputAdapter is NULL", __FUNCTION__, __LINE__);
		return JNI_FALSE;
	}
	LOGE("[%s][%d] ==> mInputAdapter is start", __FUNCTION__, __LINE__);
	mInputAdapter->start();
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_stop(JNIEnv *env, jclass clazz) {
	if (mInputAdapter == NULL) {
		LOGE("[%s][%d] ==> mInputAdapter is NULL", __FUNCTION__, __LINE__);
		return JNI_FALSE;
	}

	mInputAdapter->stop();
	return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getKey (JNIEnv *env, jclass clazz, jobject rawEvent) {
	jclass jclazz = env->FindClass("com/jnselectronics/ime/jni/RawEvent");
	if (jclazz == NULL) {
		LOGE("[%s][%d] ==> could't find the com/jnselectronic/ime/jni/RawEvent class", __FUNCTION__, __LINE__);
		return;
	}
	// 通知生产者生产
	pthread_mutex_lock(&keyMutex);
	waitForgetKey = false;
	pthread_cond_wait(&keyCond, &keyMutex);
	jfieldID scanCode = env->GetFieldID(jclazz, "scanCode", "I");
	jfieldID value = env->GetFieldID(jclazz, "value", "I");
	jfieldID deviceId = env->GetFieldID(jclazz, "deviceId", "I");
	env->SetIntField(rawEvent, scanCode, keyEvent.scanCode);
	env->SetIntField(rawEvent, value, keyEvent.value);
	env->SetIntField(rawEvent, deviceId, keyEvent.deviceId);
	waitForgetKey =  true;
	pthread_mutex_unlock(&keyMutex);



}

JNIEXPORT jboolean JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getJoyStick
(JNIEnv *env, jclass clazz, jobject joyEvent)
{
	//	LOGE("[%s][%d] ==> enter getjoy", __FUNCTION__, __LINE__);

	pthread_mutex_lock(&joyMutex);
	//	LOGE("[%s][%d] ==> getjoy wait", __FUNCTION__, __LINE__);
	waitForgetJoy = false;
	pthread_cond_wait(&joyCond, &joyMutex);
	if(mJoystickChanged)
	{
		mJoystickChanged = 0;
		jclass jclazz = env->FindClass("com/jnselectronics/ime/jni/JoyStickEvent");
		if (jclazz == NULL) {
			LOGE("[%s][%d] ==> could't find the com/jnselectronic/ime/jni/JoyStickEvent class", __FUNCTION__, __LINE__);
			return false;
		}
		jfieldID x = env->GetFieldID(jclazz, "x", "I");
		jfieldID y = env->GetFieldID(jclazz, "y", "I");
		jfieldID z = env->GetFieldID(jclazz, "z", "I");
		jfieldID rz = env->GetFieldID(jclazz, "rz", "I");
		jfieldID hat_x = env->GetFieldID(jclazz, "hat_x", "I");
		jfieldID hat_y = env->GetFieldID(jclazz, "hat_y", "I");
		jfieldID deviceId = env->GetFieldID(jclazz, "deviceId", "I");

		env->SetIntField(joyEvent, x, joyStickEvent.x);
		env->SetIntField(joyEvent, y, joyStickEvent.y);
		env->SetIntField(joyEvent, z, joyStickEvent.z);
		env->SetIntField(joyEvent, rz, joyStickEvent.rz);
		env->SetIntField(joyEvent, hat_x, joyStickEvent.hat_x);
		env->SetIntField(joyEvent, hat_y, joyStickEvent.hat_y);
		env->SetIntField(joyEvent, deviceId, joyStickEvent.deviceId);
		pthread_mutex_unlock(&joyMutex);
		waitForgetJoy = true;
		return true;
	}
	pthread_mutex_unlock(&joyMutex);
	waitForgetJoy = true;
	return false;
}
JNIEXPORT jobject JNICALL Java_com_jnselectronics_ime_jni_InputAdapter_getDeviceList
(JNIEnv * env, jclass clazz)
{
	jclass list_cls = env->FindClass("java/util/ArrayList");//获得ArrayList类引用
	jmethodID list_costruct = env->GetMethodID(list_cls , "<init>","()V"); //获得得构造函数Id
	jobject list_obj = env->NewObject(list_cls , list_costruct); //创建一个Arraylist集合对象
	//或得Arraylist类中的 add()方法ID，其方法原型为： boolean add(Object object) ;
	jmethodID list_add  = env->GetMethodID(list_cls,"add","(Ljava/lang/Object;)Z");
	//LOGE("[%s][%d] ==> get devicelist", __FUNCTION__, __LINE__);
	for(int i = 0 ; i < 32; i++)
	{

		if(devicelist[i] == NULL)
			return list_obj;
		jstring dvicename = env->NewStringUTF(devicelist[i]);
		env->CallBooleanMethod(list_obj , list_add , dvicename); //执行Arraylist类实例的add方法，添加一个String
	}
	return list_obj ;
}
