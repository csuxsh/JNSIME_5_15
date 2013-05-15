package com.jnselectronics.ime.jni;


public class RawEvent {
	
	public RawEvent(int keyCode, int scanCode, int value)
	{
		this.value = value;
		this.keyCode = keyCode;
		this.scanCode = scanCode;
	}
	public RawEvent()
	{
		
	}
	public int scanCode = 0;
	public int value = 0;
	public int keyCode = 0;
}
