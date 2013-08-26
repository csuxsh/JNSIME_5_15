package com.viaplay.ime.jni;


public class ScreenShot {


	public static native boolean getScreenShot();

	static {
		System.loadLibrary("screenshot");
	}
	
}
