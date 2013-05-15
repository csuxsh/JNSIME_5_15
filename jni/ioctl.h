#ifndef __ASENSOR_H__
#define __ASENSOR_H__

#define SENSOR_IOM	'S'
#define SENSOR_IOC_ENABLE		_IO(SENSOR_IOM, 0X00)
#define SENSOR_IOC_DISABLE		_IO(SENSOR_IOM, 0X01)
#define SENSOR_IOC_SET_DELAY		_IOW(SENSOR_IOM, 0X02, int)
#define SENSOR_IOC_GET_OFFSET		_IOR(SENSOR_IOM, 0X03, int[3])
#define SENSOR_IOC_GET_SENSITIVITY	_IOR(SENSOR_IOM, 0X04, int[3])
#define SENSOR_IOC_GET_INSTALL_DIR	_IOR(SENSOR_IOM, 0X05, int)
#define SENSOR_IOC_GET_DEV_DESCRIPTION	_IOR(SENSOR_IOM, 0X06, char*)
#define SENSOR_IOC_GET_DEV_VENDOR	_IOR(SENSOR_IOM, 0X07, char*)
#define SENSOR_IOC_GET_STATUS		_IOR(SENSOR_IOM, 0X08, int)
#define SENSOR_IOC_SET_INSTALL_DIR 	_IOW(SENSOR_IOM, 0X09, int)
#define SENSOR_IOC_SET_DATA		_IOW(SENSOR_IOM, 0X0A, int[3]) 

struct jnsime_touch_panel_data {
	uint8_t state;
	int x[2];
	int y[2];
	int touch_major;
	int width_major;
};

#define JNSIME_IOM	'T'
#define JNSIME_IOCTL_TOUCH_DOWN		_IOW(JNSIME_IOM, 0X01, struct jnsime_touch_panel_data*)
#define JNSIME_IOCTL_TOUCH_UP		_IOW(JNSIME_IOM, 0X02, struct jnsime_touch_panel_data*)

//#############################################################################
struct key_event {
        int type;
        int scan_code;
        int key_code;
        int value;
};
#define KEY_MANAGER     'I'
#define KEY_MANAGER_GET_EVENT           _IOR(KEY_MANAGER, 0X01, struct key_event *)
#define KEY_MANAGER_SET_EVENT           _IOW(KEY_MANAGER, 0X02, struct key_event *)

#endif
