#define LOG_TAG "EVENT_HUB"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/inotify.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <dirent.h>
#include <string.h>
#include "EventHub.h"
#include "debug.h"
#include "global.h"


#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))
#define VID 0x05ac
#define PID 0x3212
/* this macro computes the number of bytes needed to represent a bit array of the specified size */
#define sizeof_bit_array(bits)  ((bits + 7) / 8)

#define ID_NUM 3
#define DEVICE_NUM 5


const int deviceID[ID_NUM] =
{
	0x05ac3212, // Bordcaom BT
	0x05ac0220,
	0xa7253324  //Bordcaom 2.4G
};
const char* deviceName[DEVICE_NUM] =
{
		"JNS JNS 2.4G Wireless Device",
		"SmartGamePad1234",
		"Callstel Gaming-Controller",
		"2.4G  Wireless  ProV2.0 2.4G  Wireless  ProV2.0",
		"BT HID"
};


#ifdef BUILD_NDK
#else
namespace android {
#endif
uint32_t getAbsAxisUsage(int32_t axis, uint32_t deviceClasses) {
	// Touch devices get dibs on touch-related axes.
	if (deviceClasses & INPUT_DEVICE_CLASS_TOUCH) {
		switch (axis) {
		case ABS_X:
		case ABS_Y:
		case ABS_PRESSURE:
		case ABS_TOOL_WIDTH:
		case ABS_DISTANCE:
		case ABS_TILT_X:
		case ABS_TILT_Y:
		case ABS_MT_SLOT:
		case ABS_MT_TOUCH_MAJOR:
		case ABS_MT_TOUCH_MINOR:
		case ABS_MT_WIDTH_MAJOR:
		case ABS_MT_WIDTH_MINOR:
		case ABS_MT_ORIENTATION:
		case ABS_MT_POSITION_X:
		case ABS_MT_POSITION_Y:
		case ABS_MT_TOOL_TYPE:
		case ABS_MT_BLOB_ID:
		case ABS_MT_TRACKING_ID:
		case ABS_MT_PRESSURE:
		case ABS_MT_DISTANCE:
			return INPUT_DEVICE_CLASS_TOUCH;
		}
	}

	// Joystick devices get the rest.
	return deviceClasses & INPUT_DEVICE_CLASS_JOYSTICK;
}

static bool containNonZeroByte(const uint8_t *array, uint32_t startIndex,
		uint32_t endIndex) {
	const uint8_t *end = array + endIndex;
	array += startIndex;
	while (array != end) {
		if (*(array++) != 0) {
			return true;
		}
	}
	return false;
}

// --- EventHub::Device ---
#ifdef BUILD_NDK
EventHub::Device::Device(int dfd, int32_t did, char* dpath, struct InputDeviceIdentifier *didentifier) {
	next = NULL;
	fd = dfd;
	id = did;
	//identifier = didentifier;
	identifier = (InputDeviceIdentifier *)malloc(sizeof(InputDeviceIdentifier));
	if(identifier == NULL)
		LOGE("[%s][%d] ==> DEVICE CREATE FAILED", __FUNCTION__, __LINE__);
	memcpy(identifier, didentifier, sizeof(InputDeviceIdentifier));
	classes = 0;
	memcpy(path, dpath, strlen(dpath));
	path[strlen(dpath)] = '\0';
#else
	EventHub::Device::Device(int fd, cst int32_t id, String8& path,
			InputDeviceIdentifier& identifier) :
					next(NULL), fd(fd), id(id), path(path), identifier(identifier), classes(
							0) {
#endif
		memset(keyBitmask, 0, sizeof(keyBitmask));
		memset(absBitmask, 0, sizeof(absBitmask));
		memset(relBitmask, 0, sizeof(relBitmask));
		memset(swBitmask, 0, sizeof(swBitmask));
		memset(ledBitmask, 0, sizeof(ledBitmask));
		memset(propBitmask, 0, sizeof(propBitmask));
	}

	EventHub::Device::~Device() {
		//close();
		free(identifier);
		LOGE("[%s][%d] ==> delete fd (%d)", __FUNCTION__, __LINE__, fd);
	}

	void EventHub::Device::close() {
		if (fd >= 0) {
			LOGE("[%s][%d] ==> close fd (%d)", __FUNCTION__, __LINE__, fd);
			::close (fd);
			fd = -1;
		}
	}

	void EventHub::release() {
#ifdef BUILD_NDK
		struct Device *p = mDevices;
		while (p) {
			struct Device *device = p;
			p = p->next;
			delete device;
		}
#else
		for (size_t i = 0; i < mDevices.size(); i ++) {
			delete mDevices.valueAt(i);
		}
#endif
		delete this;
	}

	int EventHub::init() {
		mNextDeviceId = 1;
		mDeviceAdded = 0;
#ifdef BUILD_NDK
		mDevices = NULL;
#endif
		mDeviceCond = (pthread_cond_t *)malloc(sizeof(pthread_cond_t));
		mDeviceMutex = (pthread_mutex_t *)malloc(sizeof(pthread_mutex_t));
		pthread_mutex_init(mDeviceMutex, NULL);
		pthread_cond_init(mDeviceCond, NULL);
		iNotifyFd = inotify_init();
		int ret = inotify_add_watch(iNotifyFd, DEVICE_PATH, IN_CREATE | IN_DELETE);
		if (ret < 0) {
			LOGE("[%s][%d] ==> can't register inotify for %s errno = %d (%s)",
					__FUNCTION__, __LINE__, DEVICE_PATH, errno, strerror(errno));
			return 0;
		}
		return 1;
	}

	int EventHub::scanInput() {
		int fd = -1;
		const char *dirname = "/dev/input";
		char devname[PATH_MAX];
		char *filename;
		DIR *dir;
		struct dirent *de;

		dir = opendir(dirname);
		if (dir == NULL)
			return -1;
		strcpy(devname, dirname);
		filename = devname + strlen(devname);
		*filename++ = '/';
		while ((de = readdir(dir))) {
			if (de->d_name[0] == '.'
					&& (de->d_name[1] == '\0'
							|| (de->d_name[1] == '.' && de->d_name[2] == '\0')))
				continue;
			strcpy(filename, de->d_name);
			openDeviceLocked(devname);
		}
		closedir(dir);
		struct Device *tmp = mDevices;
		for(int i = 0; tmp != NULL; i++)
		{
			devicelist[i] = tmp->identifier->name;
			LOGE("[%s][%d] ==> device name %s", __FUNCTION__, __LINE__, devicelist[i]);
			tmp = tmp->next;
		}
		return fd;
	}

	int EventHub::isOpened(char *name) {

		return 0;
	}

	int EventHub::openDeviceLocked(char *devicePath) {
		int deviceValid = 0;
		char buffer[256];
		int i=0;
		int j = 0;

		//String8 string(name);
#if DEBUG_SWITCH
		LOGE("[%s][%d] ==> opening device %s", __FUNCTION__, __LINE__, devicePath);
#endif
		WaitPermission:
		int fd = open(devicePath, O_RDWR);
		if (fd < 0) {
			//	LOGE("[%s][%d] ==> could not open %s errno = %d (%s)", __FUNCTION__,
			//			__LINE__, devicePath, errno, strerror(errno));
			if(errno == 13)
			{
				goto WaitPermission;
			}
			return -1;
		}

#ifdef BUILD_NDK
		struct InputDeviceIdentifier identifier;
#else
		InputDeviceIdentifier identifier;
#endif
		//get device name
		if (ioctl(fd, EVIOCGNAME(sizeof(buffer) - 1), &buffer) < 1) {

		} else {
			buffer[sizeof(buffer) - 1] = '\0';
#ifdef BUILD_NDK
			memcpy(identifier.name, buffer, sizeof(buffer));
#else
			identifier.name.setTo(buffer);
#endif
			//if (isOpened(identifier.name.string())) return 1;
		}

		// check to see if the device is on our ecluded list
		for(i = 0; i < DEVICE_NUM; i++)
		{
			if (0 == strcmp(identifier.name, deviceName[i])) {

				break;
			}
		}

		// get device driver version
		int driverVersion;
		if (ioctl(fd, EVIOCGVERSION, &driverVersion)) {
			LOGE("[%s][%d] ==> could not get driver version for %s errno = %d (%s)",
					__FUNCTION__, __LINE__, devicePath, errno, strerror(errno));
			close(fd);
			return -1;
		}

		// get device identifier
		struct input_id inputId = {0};
		if (ioctl(fd, EVIOCGID, &inputId)) {
			LOGE(
					"[%s][%d] ==> could not get device input id for %s errno = %d (%s)",
					__FUNCTION__, __LINE__, devicePath, errno, strerror(errno));
			close(fd);
			return -1;
		}

		identifier.bus = inputId.bustype;
		identifier.product = inputId.product;
		identifier.vendor = inputId.vendor;
		identifier.version = inputId.version;

		if(i == DEVICE_NUM)
		{
			LOGE("[%s][%d] ==> name= %s pid= %d, vid = %d", __FUNCTION__,
									__LINE__, identifier.name, identifier.product,identifier.vendor);


			for(j = 0;  j < ID_NUM; j++)
			{

				LOGE("[%s][%d] ==> name= %s vpid= %d, did = %d", __FUNCTION__,
																					__LINE__, identifier.name, ((identifier.vendor << 16) | identifier.product),deviceID[j]);
				if(((identifier.vendor << 16) | identifier.product) == deviceID[j])
				{

						break;
				}
			}
			if(j == ID_NUM)
			{
				close(fd);
				LOGE("[%s][%d] ==> ignoring event id %s driver %s", __FUNCTION__,
						__LINE__, devicePath, identifier.name);
				return -1;
			}
		}

		// get device physical location
		if (ioctl(fd, EVIOCGPHYS(sizeof(buffer) - 1), &buffer) < 1) {

		} else {
			buffer[sizeof(buffer) - 1] = '\0';
#ifdef BUILD_NDK
			memcpy(identifier.location, buffer, sizeof(buffer));
#else
			identifier.location.setTo(buffer);
#endif
		}

		// get device unique id
		if (ioctl(fd, EVIOCGUNIQ(sizeof(buffer) - 1), &buffer) < 1) {

		} else {
			buffer[sizeof(buffer) - 1] = '\0';
#ifdef BUILD_NDK
			memcpy(identifier.uniqueId, buffer, sizeof(buffer));
#else
			identifier.uniqueId.setTo(buffer);
#endif
		}

		// make file descriptor non-blocking for use with poll()

		if (fcntl(fd, F_SETFL, O_NONBLOCK)) {
			LOGE(
					"[%s][%d] ==> making device file descriptor non-blockin erron = %d (%s)",
					__FUNCTION__, __LINE__, errno, strerror(errno));
			close(fd);
			return -1;
		}

		int32_t deviceId = mNextDeviceId++;
#ifdef BUILD_NDK
		Device *device = new Device(fd, deviceId, devicePath, &identifier);
#else
		Device *device = new Device(fd, deviceId, String8(devicePath), identifier);
#endif

		ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(device->keyBitmask)),
				device->keyBitmask);
		ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(device->absBitmask)),
				device->absBitmask);
		ioctl(fd, EVIOCGBIT(EV_REL, sizeof(device->relBitmask)),
				device->relBitmask);
		ioctl(fd, EVIOCGBIT(EV_SW, sizeof(device->swBitmask)), device->swBitmask);
		ioctl(fd, EVIOCGBIT(EV_LED, sizeof(device->ledBitmask)),
				device->ledBitmask);
		ioctl(fd, EVIOCGPROP(sizeof(device->propBitmask)), device->propBitmask);

		bool haveKeyBoardKeys = containNonZeroByte(device->keyBitmask, 0,
				sizeof_bit_array(BTN_MISC))
						|| containNonZeroByte(device->keyBitmask, sizeof_bit_array(KEY_OK),
								sizeof_bit_array(KEY_MAX + 1));
		bool haveGamepadButtons = containNonZeroByte(device->keyBitmask,
				sizeof_bit_array(BTN_MISC), sizeof_bit_array(BTN_MOUSE));
		if (haveKeyBoardKeys || haveGamepadButtons) {
			device->classes |= INPUT_DEVICE_CLASS_KEYBOARD;
			deviceValid = 1;
		}

		//is this a modern multi-touch driver
		if (test_bit(ABS_MT_POSITION_X, device->absBitmask)
				&& test_bit(ABS_MT_POSITION_Y, device->absBitmask)) {
			if (test_bit(BTN_TOUCH, device->keyBitmask) || !haveGamepadButtons) {
				device->classes |= INPUT_DEVICE_CLASS_TOUCH
						| INPUT_DEVICE_CLASS_TOUCH_MT;
				deviceValid = 1;
			}
		} else if (test_bit(BTN_TOUCH, device->keyBitmask)
				&& test_bit(ABS_X, device->absBitmask)
				&& test_bit(ABS_Y, device->absBitmask)) { //is this an old style single-touch driver
			device->classes |= INPUT_DEVICE_CLASS_TOUCH;
			deviceValid = 1;
		}

		//see if this device is a joystick
		if (haveGamepadButtons) {
			uint32_t assumedClasses = device->classes | INPUT_DEVICE_CLASS_JOYSTICK;
			for (int i = 0; i <= ABS_MAX; i++) {
				if (test_bit(i, device->absBitmask)
						&& (getAbsAxisUsage(i, assumedClasses)
								& INPUT_DEVICE_CLASS_JOYSTICK)) {
					device->classes = assumedClasses;
					deviceValid = 1;
				}
			}
		}
		/*
	if (deviceValid == 0) {
		delete device;
		return 0;
	}
		 */
#ifdef BUILD_NDK
		LOGE("[%s][%d] ==> new device id = %d fd = %d path = %s name = %s class = 0X%0x",
				__FUNCTION__, __LINE__, deviceId, device->fd, device->path,
				device->identifier->name, device->classes);
#else
		LOGE("[%s][%d] ==> new device id = %d fd = %d path = %s name = %s class = 0X%0x",
				__FUNCTION__, __LINE__, deviceId, fd, devicePath,
				device->identifier.name.string(), device->classes);
#endif
#ifdef BUILD_NDK
		if (mDevices == NULL){
			mDevices = device;
			mDevices->next = NULL;
		} else {
			struct Device *p = mDevices;
			while (p->next) {
				p = p->next;
			}
			p->next = device;
			device->next = NULL;
			//	p = p->next;
			//	p = device;

			//p->next = NULL;
		}

#else
		mDevices.add(deviceId, device);
#endif
		mDeviceAdded = 1;
		pthread_mutex_lock(mDeviceMutex);
		pthread_cond_signal(mDeviceCond);
		pthread_mutex_unlock(mDeviceMutex);
		return 0;
	}

	int EventHub::getEvents(int timeoutMillis, RawEvent *buffer, size_t bufferSize) {
		int ret = 0;
		int max_fd = 0;
		fd_set input;
		struct timeval timeout;
		int32_t ms = 500;

#ifdef BUILD_NDK
		struct Device *p = mDevices;

		if (p == NULL) {
			LOGE("[%s][%d] ==> getlock", __FUNCTION__,
					__LINE__);
			pthread_mutex_lock(mDeviceMutex);
			LOGE("[%s][%d] ==> suspendse", __FUNCTION__,
					__LINE__);
			pthread_cond_wait(mDeviceCond, mDeviceMutex);
			pthread_mutex_unlock(mDeviceMutex);
			LOGE("[%s][%d] ==> resume", __FUNCTION__,
					__LINE__);
			return -1;
		}

#else
		LOGE("[%s][%d] ==> mDevices.size()=%d", __FUNCTION__,
				__LINE__,mDevices.size());
		if (mDevices.size() == 0) {
			//	usleep(1000);

			//return 0;
		}
#endif

#ifdef BUILD_NDK
		//if (1 == mDeviceAdded) {
		FD_ZERO(&input);

		timeout.tv_sec  = ms / 1000;
		timeout.tv_usec = (ms % 1000) * 1000;
		do {
			max_fd = (p->fd > max_fd) ? p->fd : max_fd;
			FD_SET(p->fd, &input);
			p = p->next;
		} while (p);
		max_fd += 1;
		//	mDeviceAdded = 0;
		//}
#else
		for (int i = 0; i < mDevices.size(); i++) {
			Device *device = mDevices.valueAt(i);
			max_fd = device->fd > max_fd ? device->fd : max_fd;
			FD_SET(device->fd, &input);
		}
#endif

		max_fd += 1;
		ret = select(max_fd, &input, NULL, NULL, &timeout);
		if (ret < 0)
		{
			LOGE("[%s][%d] ==>  select failed errno = %d (%s)", __FUNCTION__,
					__LINE__, errno, strerror(errno));
			return -1;
		}
		else if (0 == ret)
		{
		}
		else
		{
#ifdef BUILD_NDK
			p = mDevices;
			do {
				if (FD_ISSET(p->fd, &input)) {
					readDevice(p, buffer, bufferSize);
				}
				p = p->next;
			} while (p);
#else
			for (int i = 0; i < mDevices.size(); i++) {
				Device *device = mDevices.valueAt(i);
				if (FD_ISSET(device->fd, &input)) {
					readDevice(device, buffer, bufferSize);
				}
			}
#endif
			return 1;
		}
		return 0;
	}

	int EventHub::readDevice(Device *device, RawEvent *buffer, int bufferSize) {
		struct input_event readBuffer[bufferSize];
		RawEvent *event = buffer;
		int32_t readSize = read(device->fd, readBuffer,
				sizeof(struct input_event) * bufferSize);
#ifdef BUILD_NDK
		//	LOGE("[%s][%d] ==> fd = %d path = %s readSize = %d", __FUNCTION__, __LINE__, device->fd, device->path, readSize);
#else
#if DEBUG_SWITCH
		LOGE("[%s][%d] ==> fd = %d path = %s readSize = %d", __FUNCTION__, __LINE__, device->fd, device->path.string(), readSize);
#endif
#endif
		if (readSize == 0 || (readSize < 0 && errno == ENODEV)) {
			LOGE(
					"[%s][%d] ==> could not get event, removed ? (fd = %d size = %d bufferSize = %d \
					errno = %d (%s)",
					__FUNCTION__, __LINE__, device->fd, readSize, bufferSize, errno,
					strerror(errno));
			closeDeviceLocked(device);
		} else if (readSize < 0) {
			LOGE("[%s][%d] ==> could not get event error = %d (%s)", __FUNCTION__,
					__LINE__, errno, strerror(errno));
		} else if (readSize % sizeof(struct input_event) != 0) {
			LOGE("[%s][%d] ==> could not get event wrong size = %d", __FUNCTION__,
					__LINE__, readSize);
		} else {
			int count = readSize / sizeof(struct input_event);
			//	LOGE("[%s][%d] ==> count = %d", __FUNCTION__, __LINE__, count);
			for (int i = 0; i < count; i++) {
				const struct input_event &iev = readBuffer[i];
				event->deviceId = device->id;
				event->type = iev.type;
				event->scanCode = iev.code;
				event->value = iev.value;
				event->keyCode = 0;
				event->flags = 0;
				event->count = count;
				event++;
#if DEBUG_SWITCH

#endif
			}
			return count;
		}
		return 0;
	}

	void EventHub::closeDeviceLocked(Device *device) {
		//device->close();
#ifdef BUILD_NDK
		struct Device *p = mDevices;
		if (p == device) {
			LOGE("[%s][%d] ==> mDevices->fd = %d p->next = %d", __FUNCTION__, __LINE__, mDevices->fd,  p->next);
			mDevices = p->next;
			LOGE("[%s][%d] ==> p->fd = %d p = %d", __FUNCTION__, __LINE__, p->fd, p);
			//	delete p;
		} else {
			struct Device *pn = p->next;
			while (pn) {
				if (pn == device) {
					p->next = pn->next;
					LOGE("[%s][%d] ==> pn->fd = %d pn = %d", __FUNCTION__, __LINE__, pn->fd, pn);
					//			delete (pn);
					break;
				}
				p = pn;
				pn = pn->next;
			}
		}
#else
		mDevices.removeItem(device->id);
#endif
	}

	int EventHub::readNotifyLocked() {
		int res;
		char devName[PATH_MAX];
		char *fileName;
		char event_buf[512];
		int event_size;
		int event_pos = 0;
		struct inotify_event *event;
#if DEBUG_SWITCH
		//LOGE("[%s][%d] ==> readNotify fd = %d", __FUNCTION__, __LINE__, iNotifyFd);
#endif
		res = read(iNotifyFd, event_buf, sizeof(event_buf));
		if (res < (int) sizeof(*event)) {
			if (errno == EINTR) {
				return 0;
			}
			LOGE("[%s][%d] ==> could not get inotify event errno = %d (%s)",
					__FUNCTION__, __LINE__, errno, strerror(errno));
			return -1;
		}

		strcpy(devName, DEVICE_PATH);
		fileName = devName + strlen(devName);
		*fileName++ = '/';

		while (res >= (int) sizeof(*event)) {
			event = (struct inotify_event*) (event_buf + event_pos);
			if (event->len) {
				strcpy(fileName, event->name);
				if (event->mask & IN_CREATE) {
					LOGE("[%s][%d] ==> added event %s", __FUNCTION__, __LINE__, devName);
					openDeviceLocked(devName);
				} else {
					LOGE("[%s][%d] ==> removed event %s", __FUNCTION__, __LINE__, devName);
					//		closeDeviceByPathLocked(devName);
				}
			}
			event_size = sizeof(*event) + event->len;
			res -= event_size;
			event_pos += event_size;
		}
		memset(devicelist, NULL, sizeof(devicelist));
		struct Device *tmp = mDevices;
		for(int i = 0; tmp != NULL; i++)
		{
			devicelist[i] = tmp->identifier->name;
			LOGE("[%s][%d] ==> device name %s", __FUNCTION__, __LINE__, devicelist[i]);
			tmp = tmp->next;
		}
		return 0;
	}

	int EventHub::closeDeviceByPathLocked(char *devicePath) {
		Device* device = getDeviceByPathLocked(devicePath);
		if (device) {
			closeDeviceLocked(device);
			return 0;
		}
		LOGE("Remove device: %s not found, device may already have been removed.",
				devicePath);
		return -1;
	}

	EventHub::Device* EventHub::getDeviceByPathLocked(char* devicePath) {
#ifdef BUILD_NDK
		struct Device *p = mDevices;
		struct Device *retp = NULL;
		if (p == NULL) return NULL;
		do {
			if (0 == strcmp(p->path, devicePath)) {
				retp = p;
				break;
			}
			p = p->next;
		} while (p);
		if (retp == mDevices) {
			mDevices = retp->next;
			retp->next = NULL;
		}
		return retp;
#else
		for (size_t i = 0; i < mDevices.size(); i++) {
			Device* device = mDevices.valueAt(i);
			if (device->path == devicePath) {
				return device;
			}
		}
		return NULL;
#endif
	}

	EventHub::Device *EventHub::getDeviceByClassesLocked(uint32_t classes) {
#ifdef BUILD_NDK
		struct Device *p = mDevices;
		if (p == NULL) return NULL;
		do {
			if ((p->classes & classes) == classes) {
				return p;
			}
			p = p->next;
		} while (p);
		return NULL;
#else
		for (size_t i = 0; i < mDevices.size(); i++) {
			Device *device = mDevices.valueAt(i);
			if ((device->classes & classes) == classes) {
				return device;
			}
		}
		return NULL;
#endif
	}

#ifdef BUILD_NDK
#else
};
#endif
