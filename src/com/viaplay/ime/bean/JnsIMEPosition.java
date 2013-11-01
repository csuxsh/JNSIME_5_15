package com.viaplay.ime.bean;

/**
 * 触摸配置时对应点绘制信息的数据结构
 * 
 * 
 * @author Seven
 *
 */
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
