package com.jnselectronics.ime.bean;

public class JnsIMEPosition {
	public static final int TYPE_LEFT_JOYSTICK = 0;
	public static final int TYPE_RIGHT_JOYSTICK = 1;
	public static final int TYPE_OTHERS = 2;
	public float x;
	public float y;
	public float r;
	public float type; //区域类型：左摇杆，右摇杆 按键
	public int resId;
	public int color;
	public int scancode;
}
