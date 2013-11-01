package com.viaplay.ime.jni;

/**
 * 截图类，当前应用中仅在android2.2以下使用，4.2以上时用这个方法有时候会出截图异常。
 * 
 * @author Steven.xu
 *
 */

public class ScreenShot {


	public static native boolean getScreenShot();

	static {
		System.loadLibrary("screenshot");
	}
	
}
