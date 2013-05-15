package com.jnselectronics.ime.bean;


public class JnsIMEProfile {
	public static final int LEFT_JOYSTICK = 0;
	public static final int RIGHT_JOYSTICK = 1;
	public int keyCode;
	public int key;
	public float posX; //涓績鐐筙
	public float posY; //涓績鐐筜
	public float posR; //鍖哄煙鐨勫崐寰�	
	public float posType; //鍖哄煙绫诲瀷锛氬乏鎽囨潌锛屽彸鎽囨潌
	
	public JnsIMEProfile()
	{
		posType = 2;
	}
}
