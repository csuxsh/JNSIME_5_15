#ifndef __BLUETOOTH_EVENT_H__
#define __BLUETOOTH_EVENT_H__

#include <linux/input.h>
#include <sys/epoll.h>
#include <fcntl.h>
#include "define.h"
#include "com_jnselectronics_ime_jni_InputAdapter.h"


#ifdef BUILD_NDK
#include <android/log.h>

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)
#else
#include <utils/Log.h>
#endif

#ifdef BUILD_NDK
#else
namespace android {
#endif
/*
 * Input device classes.
 */
enum {
	/* The input device is a keyboard or has buttons. */
	INPUT_DEVICE_CLASS_KEYBOARD      = 0x00000001,

	/* The input device is an alpha-numeric keyboard (not just a dial pad). */
	INPUT_DEVICE_CLASS_ALPHAKEY      = 0x00000002,

	/* The input device is a touchscreen or a touchpad (either single-touch or multi-touch). */
	INPUT_DEVICE_CLASS_TOUCH         = 0x00000004,

	/* The input device is a cursor device such as a trackball or mouse. */
	INPUT_DEVICE_CLASS_CURSOR        = 0x00000008,

	/* The input device is a multi-touch touchscreen. */
	INPUT_DEVICE_CLASS_TOUCH_MT      = 0x00000010,

	/* The input device is a directional pad (implies keyboard, has DPAD keys). */
	INPUT_DEVICE_CLASS_DPAD          = 0x00000020,

	/* The input device is a gamepad (implies keyboard, has BUTTON keys). */
	INPUT_DEVICE_CLASS_GAMEPAD       = 0x00000040,

	/* The input device has switches. */
	INPUT_DEVICE_CLASS_SWITCH        = 0x00000080,

	/* The input device is a joystick (implies gamepad, has joystick absolute axes). */
	INPUT_DEVICE_CLASS_JOYSTICK      = 0x00000100,

	/* The input device is external (not built-in). */
	INPUT_DEVICE_CLASS_EXTERNAL      = 0x80000000,
};

struct RawEvent {
	//nsecs_t when;
	int32_t deviceId;
	int32_t type;
	int32_t scanCode;
	int32_t keyCode;
	int32_t value;
	uint32_t flags;
	uint8_t count;
};

enum TOUCH_TYPE {
	TOUCH_TYPE_SINGLE_POINTER = 0,
	TOUCH_TYPE_MT_POINTER,
	TOUCH_TYPE_RELEASE,
};

struct TouchEvent {
	int32_t x;
	int32_t y;
	int32_t width_major;
	int32_t touch_major;
	int32_t btn_touch;
	int32_t pointer;
	int32_t pressure;
	int32_t touchtype;
};

struct ExcludedDevices {
	char *name;
};

#ifdef BUILD_NDK
struct InputDeviceIdentifier {
	char name[256];
	char location[256];
	char uniqueId[256];
	uint16_t bus;
	uint16_t vendor;
	uint16_t product;
	uint16_t version;
};
#endif

#ifdef BUILD_NDK
#define INPUT_PROP_MAX                  0x1f

#define ABS_MT_SLOT             0x2f    /* MT slot being modified */
#define ABS_MT_TOUCH_MAJOR      0x30    /* Major axis of touching ellipse */
#define ABS_MT_TOUCH_MINOR      0x31    /* Minor axis (omit if circular) */
#define ABS_MT_WIDTH_MAJOR      0x32    /* Major axis of approaching ellipse */
#define ABS_MT_WIDTH_MINOR      0x33    /* Minor axis (omit if circular) */
#define ABS_MT_ORIENTATION      0x34    /* Ellipse orientation */
#define ABS_MT_POSITION_X       0x35    /* Center X ellipse position */
#define ABS_MT_POSITION_Y       0x36    /* Center Y ellipse position */
#define ABS_MT_TOOL_TYPE        0x37    /* Type of touching device */
#define ABS_MT_BLOB_ID          0x38    /* Group a set of packets as a blob */
#define ABS_MT_TRACKING_ID      0x39    /* Unique ID of initiated contact */
#define ABS_MT_PRESSURE         0x3a    /* Pressure on contact area */
#define ABS_MT_DISTANCE         0x3b    /* Contact hover distance */

#define EVIOCGPROP(len)         _IOC(_IOC_READ, 'E', 0x09, len)         /* get device properties */
#endif

class EventHub {
public:
	CREATE_FUNC(EventHub);
	int init();
	void release();

	int getEvents(int timeoutMillis, RawEvent *buffer, size_t bufferSize);

	struct Device {
		Device *next;
		int fd;
		int32_t id;
#ifdef BUILD_NDK
		char path[256];
#else
		String8 path;
#endif
#ifdef BUILD_NDK
		struct InputDeviceIdentifier *identifier;
#else
		InputDeviceIdentifier identifier;
#endif
		uint32_t classes;			
		uint8_t keyBitmask[(KEY_MAX + 1) / 8];
		uint8_t absBitmask[(ABS_MAX + 1) / 8];
		uint8_t relBitmask[(REL_MAX + 1) / 8];
		uint8_t swBitmask[(SW_MAX + 1) / 8];
		uint8_t ledBitmask[(LED_MAX + 1) / 8];
		uint8_t propBitmask[(INPUT_PROP_MAX + 1) / 8];

#ifdef BUILD_NDK
		Device(int fd, int32_t did, char* dpath, struct InputDeviceIdentifier *didentifier);
#else
		Device(int fd, int32_t did, String8& path, InputDeviceIdentifier& identifier);
#endif
		~Device();

		void close();
	};

	int openDeviceLocked(char *devicePath);
	void closeDeviceLocked(Device *device);
	Device* getDeviceByPathLocked(char* devicePath);
	Device* getDeviceByClassesLocked(uint32_t classes);
	int closeDeviceByPathLocked(char *devicePath);
	int scanInput();
	int readNotifyLocked();
	int isOpened(char *name);
	void setGetDeviceLock(pthread_cond_t *DevieCond, pthread_mutex_t *DevieMutex);
private:
	int readDevice(Device *device, RawEvent *buffer, int bufferSize);

#ifdef BUILD_NDK
	int mDevicesCount;
	int mDevicesIndex;
	struct Device *mDevices;
	struct Device *mDevicesPos;
#else
	KeyedVector<int32_t, Device *> mDevices;
#endif
	Device *mOpeningDevices;
	Device *mClosingDevices;

	int iNotifyFd;
	int mNextDeviceId;
	int mDeviceAdded;
	pthread_cond_t *mDeviceCond;
	pthread_mutex_t *mDeviceMutex;

#ifdef BUILD_NDK
	struct ExcludedDevices *mExcludedDevices;
#else
	Vector<String8> mExcludedDevices
#endif
};

#ifdef BUILD_NDK
#else
};
#endif

#endif
