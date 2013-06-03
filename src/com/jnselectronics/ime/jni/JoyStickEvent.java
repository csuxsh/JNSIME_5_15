package com.jnselectronics.ime.jni;

public class JoyStickEvent {
	
	public JoyStickEvent()
	{
		x = 0x7f;
		y = 0x7f;
		y = 0x7f;
		rz = 0x7f;
		hat_x = 0;
		hat_y =0;
		deviceId = 0;
	}
	public int getHat_x() {
		return hat_x;
	}
	public void setHat_x(int hat_x) {
		this.hat_x = hat_x;
	}
	public int getHat_y() {
		return hat_y;
	}
	public void setHat_y(int hat_y) {
		this.hat_y = hat_y;
	}
	int x;
	int y;
	int hat_x;
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	int hat_y;
	int deviceId;
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}
	public int getRz() {
		return rz;
	}
	public void setRz(int rz) {
		this.rz = rz;
	}
	int z;
	int rz;
}
