#ifndef __INPUT_ADAPTER_H__
#define __INPUT_ADAPTER_H__
      
#include "Joystick.h"
#include "EventHub.h"
#include "KeyManager.h"

#ifdef BUILD_NDK
class InputAdapter {
#else
namespace android {

class InputAdapter : public virtual RefBase {
#endif
public:
	CREATE_FUNC(InputAdapter);
	int init();
	void release();
	
#ifdef BUILD_NDK
	Joystick* getJoystick();
	KeyManager* getKeyManager();
	EventHub* getEventHub();
#else
	sp<Joystick> getJoystick();
	sp<KeyManager> getKeyManager();
	sp<EventHub> getEventHub();
#endif
	int start();
	int stop();
	void loopOnce();
	void processRawEventLocked(const RawEvent *eventBuffer);
	void dumpRawEvent(const RawEvent *event);
	
#ifdef BUILD_NDK

#else
	class InputAdapterThread : public Thread {
	public:

		InputAdapterThread(sp<InputAdapter> adapter);
		virtual ~InputAdapterThread();
		
	private:
		virtual bool threadLoop();
		sp<InputAdapter> mInputAdapter;
	};
	
	class InputAdapterNotifierThread : public Thread {
		public:
			InputAdapterNotifierThread(sp<InputAdapter> adapter);
			virtual ~InputAdapterNotifierThread();
			
		private:
			virtual bool threadLoop();
			sp<InputAdapter> mInputAdapter;
		};
#endif
	
private:
#ifdef BUILD_NDK
	Joystick* mJoystick;
	KeyManager* mKeyManager;
	EventHub* mEventHub;
#else
	sp<Joystick> mJoystick;
	sp<KeyManager> mKeyManager;
	sp<EventHub> mEventHub;
	sp<InputAdapterThread> mInputAdapterThread;
	sp<InputAdapterNotifierThread> mInputAdapterNotifierThread;
	Mutex mLock;
#endif
	
public:
#ifdef BUILD_NDK
	int thread_exit;
#endif
	static const int EVENT_BUFFER_SIZE = 256;
	RawEvent mEventBuffer[EVENT_BUFFER_SIZE];
};

#ifdef BUILD_NDK
#else
};
#endif

#endif
