#ifndef __JOYSTICK_H__
#define __JOYSTICK_H__
       
#include "CallBackInterface.h"

#ifdef BUILD_NDK
class Joystick {
#else
namespace android {

class Joystick : public virtual RefBase {
#endif
public:
	CREATE_FUNC(Joystick);
	int init() {
		mCallBackInterface = NULL;
		return 1;
	}
	
#ifdef BUILD_NDK
	void registerCallBackInterface(CallBackInterface *callBackInterface) {
#else
	void registerCallBackInterface(sp<CallBackInterface> callBackInterface) {
#endif
		mCallBackInterface = callBackInterface;
		LOGE("[%s][%d] ==> register callbackinterface", __FUNCTION__, __LINE__);
	}
	
	int joystickProcess(const RawEvent *rawEvent) {
		if (mCallBackInterface == NULL) {
			LOGE("[%s][%d] ==> mCallBackInterface is NULL", __FUNCTION__, __LINE__);
			return -1;
		}
		
		mCallBackInterface->joystickProcess(rawEvent);
		return 1;
	}
	
private:
#ifdef BUILD_NDK
	CallBackInterface *mCallBackInterface;
#else
	sp<CallBackInterface> mCallBackInterface;
#endif
};

#ifdef BUILD_NDK
#else
};
#endif

#endif
