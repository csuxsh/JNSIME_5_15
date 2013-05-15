#ifndef __KEY_MANAGER_H__
#define __KEY_MANAGER_H__
         
#include "CallBackInterface.h"
#include "EventHub.h"

#ifdef BUILD_NDK
class KeyManager {
#else
namespace android {

class KeyManager : public virtual RefBase  {
#endif
public:
	CREATE_FUNC(KeyManager);
	int init() {
		mCallBackInterface = NULL;
		return 1;
	}
	
	int processKeys(const RawEvent *rawEvent) {
		if (mCallBackInterface == NULL) {
			LOGE("[%s][%d] ==> mCallBackInterface = NULL", __FUNCTION__, __LINE__);
			return -1;
		}
		LOGE("[%s][%d] ==> keyProcess", __FUNCTION__, __LINE__);
		mCallBackInterface->keyProcess(rawEvent);
		return 0;
	}
	
#ifdef BUILD_NDK
	void registerCallBackInterface(CallBackInterface *callBackInterface) {
#else
	void registerCallBackInterface(sp<CallBackInterface> callBackInterface) {
#endif
		mCallBackInterface = callBackInterface;
		LOGE("[%s][%d] ==> register callbackinterface", __FUNCTION__, __LINE__);
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
