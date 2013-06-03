#define LOG_TAG "INPUT_ADAPTER"

#include "InputAdapter.h"
#include "debug.h"
#include "global.h"


#ifdef BUILD_NDK
#include "pthread.h"
#endif
#include "errno.h"

#ifdef BUILD_NDK
#else
namespace android {
#endif
//-----InputAdapter-----


void InputAdapter::release() {
	mEventHub->release();
}

int InputAdapter::init() {
	mJoystick = Joystick::create();
	mKeyManager = KeyManager::create();
	mEventHub = EventHub::create();

#ifdef BUILD_NDK
	thread_exit = 0;
#else
	mInputAdapterThread = new InputAdapterThread(this);
	mInputAdapterNotifierThread = new InputAdapterNotifierThread(this);
	LOGE("[%s][%d] ==> init finished", __FUNCTION__, __LINE__);
#endif
	return 1;
}

void *getEventThread(void *p) {
	InputAdapter *mInputAdapter = (InputAdapter*) p;
	if (mInputAdapter == NULL) {
		LOGE("[%s][%d] ==> mInputAdapter is NULL", __FUNCTION__, __LINE__);
		return NULL;
	}
	while (0 == mInputAdapter->thread_exit) {
		memset(mInputAdapter->mEventBuffer, 0, mInputAdapter->EVENT_BUFFER_SIZE);
		mInputAdapter->getEventHub()->getEvents(0, mInputAdapter->mEventBuffer, mInputAdapter->EVENT_BUFFER_SIZE);
		mInputAdapter->processRawEventLocked(mInputAdapter->mEventBuffer);
	//	LOGE("[%s][%d] ==> getevent thread is NULL", __FUNCTION__, __LINE__);

	}
	return NULL;
}

void *monitorNotoifierThread(void *p) {
	InputAdapter *mInputAdapter = (InputAdapter*) p;
	if (mInputAdapter == NULL) {
		LOGE("[%s][%d] ==> mInputAdapter is NULL", __FUNCTION__, __LINE__);
		return NULL;
	}
	while(0 == mInputAdapter->thread_exit) {
		//	LOGE("[%s][%d] ==> readNotifyLocked is run", __FUNCTION__, __LINE__);
		mInputAdapter->getEventHub()->readNotifyLocked();
	}
	return NULL;
}

int InputAdapter::start() {
	mEventHub->scanInput();
#ifdef BUILD_NDK
	pthread_t pid;

	int ret = pthread_create(&pid, NULL, getEventThread, this);
	if (ret) {
		LOGE("[%s][%d] ==> create getEventThread error (%s)", __FUNCTION__, __LINE__, strerror(errno));
		return -1;
	}
	ret = pthread_create(&pid, NULL, monitorNotoifierThread, this);
	if (ret) {
		LOGE("[%s][%d] ==> create monitorNotifierThread error (%s)", __FUNCTION__, __LINE__, strerror(errno));
		return -1;
	}

	return 1;
#else
	int ret = mInputAdapterThread->run("InputAdapterThread", PRIORITY_URGENT_DISPLAY);
	if (ret) {
		LOGE("[%s][%d] ==> Could not start InputAdapterThread thread due to error %d.", __FUNCTION__, __LINE__, ret);
		mInputAdapterThread->requestExit();
		return -1;
	}
	ret = mInputAdapterNotifierThread->run("InputAdapterNotifierThread", PRIORITY_URGENT_DISPLAY);
	if (ret) {
		LOGE("[%s][%d] ==> Could not start InputAdapterNotifierThread thread due to error %d.", __FUNCTION__, __LINE__, ret);
		mInputAdapterNotifierThread->requestExit();
		return -1;
	}

	return OK;
#endif
}
int InputAdapter::stop() {
#ifdef BUILD_NDK
	thread_exit = 1;
	return 1;
#else
	int result = mInputAdapterThread->requestExitAndWait();
	if (result) {
		LOGE("[%s][%d] ==> Could not stop InputReader thread due to error %d.", __FUNCTION__, __LINE__, result);
	}

	return OK;
#endif
}

#ifdef BUILD_NDK
Joystick* InputAdapter::getJoystick() {
	return mJoystick;
}

KeyManager* InputAdapter::getKeyManager() {
	return mKeyManager;
}

EventHub* InputAdapter::getEventHub() {
	return mEventHub;
}
#else
sp<Joystick> InputAdapter::getJoystick() {
	return mJoystick;
}

sp<KeyManager> InputAdapter::getKeyManager() {
	return mKeyManager;
}

sp<EventHub> InputAdapter::getEventHub() {
	return mEventHub;
}
#endif

void InputAdapter::loopOnce() {
#ifdef BUILD_NDK
#else
	AutoMutex _l(mLock);
	memset(mEventBuffer, 0, EVENT_BUFFER_SIZE);
	mEventHub->getEvents(0, mEventBuffer, EVENT_BUFFER_SIZE);
	processRawEventLocked(mEventBuffer);
#endif
}

void InputAdapter::dumpRawEvent(const RawEvent *event) {
	LOGE("[%s][%d] ==> event.type = 0x%02x, event.scancode = 0x%02x event.value = 0x%02x event.deviceid = %d",
			__FUNCTION__, __LINE__, event->type, event->scanCode, event->value, event->deviceId);
}

void InputAdapter::processRawEventLocked(const RawEvent *eventBuffer) {
	for (size_t i = 0; i < eventBuffer->count; i ++) {
#if DEBUG_SWITCH
		dumpRawEvent(eventBuffer);
#endif
		static int per_EV = 0;
		switch (eventBuffer->type) {
		case EV_KEY:
			//	LOGE("[%s][%d] ==> processKeys", __FUNCTION__, __LINE__);
			mKeyManager->processKeys(eventBuffer);
			break;
		case EV_ABS:
			//	LOGE("[%s][%d] ==> processJoyStick", __FUNCTION__, __LINE__);
			mJoystick->joystickProcess(eventBuffer);
			mJoystickChanged = 1;
			mJoystick->joystickProcess(eventBuffer);
			LOGE("[%s][%d] ==> joystickchanged ok", __FUNCTION__, __LINE__);
			break;
		case EV_SYN:
			if(per_EV == EV_ABS)
			{

			}
			break;
		}
		per_EV =  eventBuffer->type ;
		eventBuffer ++;
	}
}


#ifdef BUILD_NDK

#else
//---- InputAdapterThread-----
InputAdapter::InputAdapterThread::InputAdapterThread(sp<InputAdapter> adapter):
					Thread(true), mInputAdapter(adapter) {

}

InputAdapter::InputAdapterThread::~InputAdapterThread() {

}

bool InputAdapter::InputAdapterThread::threadLoop() {
	mInputAdapter->loopOnce();
	return true;
}

//---- InputAdapterNotifierThread-----
InputAdapter::InputAdapterNotifierThread::InputAdapterNotifierThread(sp<InputAdapter> adapter):
					Thread(true), mInputAdapter(adapter) {

}

InputAdapter::InputAdapterNotifierThread::~InputAdapterNotifierThread() {

}

bool InputAdapter::InputAdapterNotifierThread::threadLoop() {
	LOGE("[%s][%d] ==> readNotifyLocked", __FUNCTION__, __LINE__);
	mInputAdapter->getEventHub()->readNotifyLocked();
	return true;
}

};
#endif
