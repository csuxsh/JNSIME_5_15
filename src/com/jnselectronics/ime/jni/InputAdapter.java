package com.jnselectronics.ime.jni;

import java.util.List;

import com.jnselectronics.im.hardware.JoyStickTypeF;
import com.jnselectronics.ime.JnsIMECoreService;
import com.jnselectronics.ime.JnsIMEInputMethodService;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class InputAdapter {
	private static final int SELECT_SCANCODE = 314;
	private static final int START_SCANCODE = 315;
	private static byte mCheckByte = 0x00;
	private static boolean  mIMEMode = false;
	public static Context mcontext;
	private static final String TAG = "InputAdapter";
	private static RawEvent keyEvent = new RawEvent();
	private static RawEvent oldKeyEvent = new RawEvent();
	private static JoyStickEvent JoyEvent = new JoyStickEvent();
	private static boolean hatUpPressed = false;
	private static boolean hatDownPressed = false;
	private static boolean hatLeftPressed = false;
	private static boolean hatRightPressed = false;
	
	public static boolean gHatUpPressed = false;
	public static boolean gHatDownPressed = false;
	public static boolean gHatLeftPressed = false;
	public static boolean gHatRightPressed = false;
	//private static JoyStickEvent oldJoyEvent = new JoyStickEvent();


	public static void test() {

	}

	public final void onInputAdapterKeyDown(int scanCode, int value) {
		Log.e(TAG, "onInputAdapterKeyDown");
	}

	public static final void onInputAdapterKeyUp(int scanCode, int value) {
		Log.e(TAG, "onInputAapterKeyUp");
	}

	public static final void onInputAdapterJoystickChange(int scanCode, int value) {
		Log.e(TAG, "onInputAdapterJoystickChange");
	}

	private static Runnable getKeyRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				getKey(keyEvent);
				Log.d(TAG, "keyEvent.scanCode="+keyEvent.scanCode+"keyEvent.value"+keyEvent.value );
				if(JnsIMEInputMethodService.validAppName.equals("com.silvertree.cordy"))
					keyEvent.deviceId = 0;
				if (keyEvent.value == 1) 
				{
					keyEvent.value = KeyEvent.ACTION_DOWN;
					CheckIMESwitch();
					Log.d(TAG, "get a key down");
					onRawKeyDown(keyEvent);
				} 
				else if(keyEvent.value == 2)
				{
					//	onRawKeyLongDown(keyEvent);
				}
				else if(keyEvent.value == 0)
				{
					if(keyEvent.scanCode == START_SCANCODE) 
						mCheckByte  = (byte) (mCheckByte & 0xfe);
					if(keyEvent.scanCode == SELECT_SCANCODE) 
						mCheckByte =  (byte) (mCheckByte & 0xfd);
					Log.d(TAG, "get a key up");
					keyEvent.value = KeyEvent.ACTION_UP;
					onRawKeyUp(keyEvent);
				}

			}
		}

	};
	private static Runnable getJoyStickRunnable = new Runnable() {

		@Override
		public void run() 
		{
			Log.d(TAG, "x = "+JoyEvent.x+ ", y = "+JoyEvent.y + "z = "+JoyEvent.z+  "rz = "+JoyEvent.rz);

			// TODO Auto-generated method stub
			while (true) 
			{
				//		
				if(getJoyStick(JoyEvent))
				{
					if(JnsIMEInputMethodService.validAppName.equals("com.silvertree.cordy"))
						JoyEvent.deviceId = 0;
					if((JoyEvent.hat_y == 0) && hatUpPressed)
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_UP_SCANCODE, KeyEvent.ACTION_UP, JoyEvent.deviceId);
						hatUpPressed = false;
						onRawKeyUp(keyevent);
					}
					if((JoyEvent.hat_y == 0) && hatDownPressed)
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_DOWN_SCANCODE, KeyEvent.ACTION_UP, JoyEvent.deviceId);
						hatDownPressed = false;
						onRawKeyUp(keyevent);
					}
					if((JoyEvent.hat_y == -1) && (!hatUpPressed))
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_UP_SCANCODE, KeyEvent.ACTION_DOWN, JoyEvent.deviceId);
						hatUpPressed = true;
						gHatUpPressed = true;
						onRawKeyDown(keyevent);
					}
					if((JoyEvent.hat_y == 1) && (!hatDownPressed))
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_DOWN_SCANCODE, KeyEvent.ACTION_DOWN, JoyEvent.deviceId);
						hatDownPressed =true;
						gHatDownPressed =true;
						onRawKeyDown(keyevent);
					}
					
					if((JoyEvent.hat_x == 0) && hatRightPressed)
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_RIGHT_SCANCODE, KeyEvent.ACTION_UP, JoyEvent.deviceId);
						hatRightPressed = false;
						onRawKeyUp(keyevent);
					}
					if((JoyEvent.hat_x == 0) && hatLeftPressed)
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_LEFT_SCANCODE, KeyEvent.ACTION_UP, JoyEvent.deviceId);
						hatLeftPressed = false;
						onRawKeyUp(keyevent);
					}
					if((JoyEvent.hat_x == 1) && (!hatRightPressed))
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_RIGHT_SCANCODE, KeyEvent.ACTION_DOWN, JoyEvent.deviceId);
						hatRightPressed = true;
						gHatRightPressed = true;
						onRawKeyDown(keyevent);
					}
					if((JoyEvent.hat_x == -1) && (!hatLeftPressed))
					{
						RawEvent keyevent = new RawEvent(0, JoyStickTypeF.BUTTON_LEFT_SCANCODE, KeyEvent.ACTION_DOWN, JoyEvent.deviceId);
						hatLeftPressed =true;
						gHatLeftPressed =true;
						onRawKeyDown(keyevent);
					}
					Log.d(TAG, "x = "+JoyEvent.x+ ", y = "+JoyEvent.y + "z = "+JoyEvent.z+  "rz = "+JoyEvent.rz+"  hat_x = "+ JoyEvent.hat_x +" hat y ="+  JoyEvent.hat_y );
					Message msg = new Message();
					msg.what = JnsIMECoreService.HAS_STICK_DATA;
					JnsIMECoreService.stickQueue.add(JoyEvent);
					JnsIMECoreService.DataProcessHandler.sendMessage(msg);
				}
			}

		}
	};
	private static void CheckIMESwitch()
	{
		if(keyEvent.scanCode == START_SCANCODE) 
			mCheckByte  = (byte) (mCheckByte | 0x01);
		if(keyEvent.scanCode == SELECT_SCANCODE) 
			mCheckByte =  (byte) (mCheckByte | 0x02);
		Log.d(TAG, "mCheckByte="+mCheckByte+",mIMEMode="+mIMEMode);
		if(mCheckByte == 0x03)
		{
			//	Toast.makeText(mcontext, "qiehuan ime", Toast.LENGTH_LONG).show();
			//String imeStr = "";
			/*
			if (!mIMEMode) 
			{
				//imeStr = BlueoceanCore.JNSIMEID;
				mIMEMode = true;
			} 
			else 
			{
				//imeStr = BlueoceanCore.lastIMEID;
				mIMEMode = false;
			}
			//	Intent intent = new Intent();
			//	intent.setAction("COM.BLUEOCEAN_IME_SWITCH_IME");
			//	intent.putExtra("COM.BLUEOCEAN_IME_IMEID", imeStr);
			//	mcontext.sendBroadcast(intent);
			*/
			mCheckByte = 0x00;
			Intent intent = new Intent();
			intent.setAction("android.settings.SHOW_INPUT_METHOD_PICKER");
			mcontext.sendBroadcast(intent);
		}
	}
	private static void onRawKeyDown(RawEvent keyEvent) {
		//Log.e(TAG, "onRawKeyDown scanCode = " + keyEvent.scanCode + " value = " + keyEvent.value);
		Message msg = new Message();
		msg.what = JnsIMECoreService.HAS_KEY_DATA;
		RawEvent event = new RawEvent(keyEvent.keyCode, keyEvent.scanCode, keyEvent.value, keyEvent.deviceId);
		JnsIMECoreService.keyQueue.add(event);
		JnsIMECoreService.DataProcessHandler.sendMessage(msg);
		Log.d(TAG, "current time is "+System.currentTimeMillis());
	}

	private static void onRawKeyUp(RawEvent keyEvent) {
		Log.e(TAG, "onRawKeyUp scanCode = " + keyEvent.scanCode + " value = " + keyEvent.value);
		Message msg = new Message();
		msg.what = JnsIMECoreService.HAS_KEY_DATA;
		RawEvent event = new RawEvent(keyEvent.keyCode, keyEvent.scanCode, keyEvent.value, keyEvent.deviceId);
		JnsIMECoreService.keyQueue.add(event);
		JnsIMECoreService.DataProcessHandler.sendMessage(msg);
	}

	public static void getKeyThreadStart() {
		new Thread(getKeyRunnable).start();
		new Thread(getJoyStickRunnable).start();
	}

	public static native boolean init();
	public static native boolean start();
	public static native boolean stop();
	public static native void getKey(RawEvent event);
	public static native boolean getJoyStick(JoyStickEvent event);
	public static native List<String> getDeviceList();

	static {
		System.loadLibrary("jni_input_adapter");
	}
}
