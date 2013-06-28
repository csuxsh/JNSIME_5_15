package com.jnselectronics.ime.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.jnselectronics.im.hardware.JoyStickTypeF;
import com.jnselectronics.ime.JnsIMECoreService;
import com.jnselectronics.ime.JnsIMEInputMethodService;
import com.jnselectronics.ime.bean.JnsIMEPosition;
import com.jnselectronics.ime.bean.JnsIMEProfile;
import com.jnselectronics.ime.jni.JoyStickEvent;
import com.jnselectronics.ime.jni.RawEvent;

public class SendEvent {


	public final static String TAG= "SendEvent";
	public final static String pkgName ="com.jnselectronics.ime";
	private final static int  STICK_MOVE_IRQ_TIME = 5;
	private final static String TOUCH = "injectTouch";
	private final static String KEY = "injectKey";
	private final static String TOKEN=  ":";
	private static Socket socket;
	private static PrintWriter pw;
	static DataInputStream dis;

	private boolean rightMotionKey = false;
	private boolean leftMotionKey = false;
	private float rightJoystickCurrentPosX = 0.0f;
	private float rightJoystickCurrentPosY = 0.0f;
	private float leftJoystickCurrentPosX = 0.0f;
	private float leftJoystickCurrentPosY = 0.0f;
	private float joystickR = 0.0f;
	private float rightJoystickCurrentR = 0.0f;
	private float leftJoystickCurrentR = 0.0f;
	private boolean LeftJoystickPresed = false;
	private boolean RightJoystickPresed = false;
	private long last_left_press_time = 0;
	private long last_right_press_time = 0;
	private boolean joy_xi_pressed = false;
	private boolean joy_xp_pressed =false;
	private boolean joy_yi_pressed = false;
	private boolean joy_yp_pressed =false;
	private boolean joy_zi_pressed = false;
	private boolean joy_zp_pressed =false;
	private boolean joy_rzi_pressed = false;
	private boolean joy_rzp_pressed =false;


	private static SendEvent sendEvent = null;
	private SendEvent()
	{
		super();
	}
	public static SendEvent getSendEvent()
	{
		if(null == sendEvent)
			sendEvent = new SendEvent();
		return sendEvent;
	}
	public  boolean connectJNSInputServer() {

		boolean connect= false;

		while(!connect)
		{	
			try {  
				connect = true;
				socket = new Socket("localhost", 44444);
				socket.setTcpNoDelay(true);
				dis = new DataInputStream(socket.getInputStream());
				pw = new PrintWriter(socket.getOutputStream());
				Log.e(TAG, "socket isConnected = " + socket.isConnected());
			} 
			catch(Exception e)
			{	
				e.printStackTrace();
				JnsEnvInit.startJnsInputServier();
				connect = false;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		//isConnectiong = false;

		return true;
	}

	@SuppressWarnings("unused")
	private static JnsIMEProfile iteratorKeyList(List<JnsIMEProfile> keylist, int scancode)
	{
		Log.d(TAG, "list size"+keylist.size());
		if(keylist==null)
			return null;
		for(JnsIMEProfile keyProfile : keylist)
			if(keyProfile.key == scancode&& keyProfile.posType >1)
				return keyProfile;
		return null;
	}

	public boolean getEventDownLock() throws Exception
	{
		String data[];
		pw.print("geteventlock\n");
		pw.flush();

		@SuppressWarnings("deprecation")
		String response = dis.readLine();
		Log.d(TAG, response);
		data = response.split(":");
		if(data[0].equals("lock"))
		{
			return Boolean.getBoolean(data[1]);
		}

		return false;

	}
	public  boolean sendKey(RawEvent keyevent)
	{ 
		//if(JnsIMEInputMethodService.currentAppName.equals(pkgName))
		//	return true;
		Log.d(TAG,"scancode="+keyevent.scanCode);
		JnsIMEProfile keyProfile =  iteratorKeyList(JnsIMECoreService.keyList, keyevent.scanCode);
		if(null == keyProfile)
		{	
			Log.d(TAG, "keyprofile  is  null");
			if(!JnsIMECoreService.keyMap.containsKey(keyevent.scanCode))
				return false;
			try
			{
				keyevent.keyCode = JnsIMECoreService.keyMap.get(keyevent.scanCode);
				pw.print(keyString(keyevent));
				pw.flush();
			}
			catch(Exception e)
			{	
				//connectJNSInputServer();
			}
		}
		Log.d(TAG, "keyprofile  is not null");

		try{
			//	socket.getOutputStream().write(posString((int)keyProfile.posX, (int)keyProfile.posY, keyevent.value).getBytes());
			pw.print(posString((int)keyProfile.posX, (int)keyProfile.posY, keyevent.value));
			pw.flush();
			Log.d(TAG,"send pos x="+keyProfile.posX+", pos y = "+keyProfile.posY+"action = "+keyevent.value);
			Log.d(TAG, "current time is "+System.currentTimeMillis());
		}
		catch(Exception e)
		{
			//	e.printStackTrace();
			//connectJNSInputServer();
		}
		return true;
	}
	public void sendJoy(JoyStickEvent joyevent)
	{
		//if(JnsIMEInputMethodService.currentAppName.equals(pkgName))
		//	return ;
		processRightJoystickData(joyevent.getZ(), joyevent.getRz(), joyevent.getDeviceId());
		processLeftJoystickData(joyevent.getX(), joyevent.getY(), joyevent.getDeviceId());
	}
	private static String keyString(RawEvent keyevent)
	{
		if(keyevent.value == KeyEvent.ACTION_DOWN)
			JnsIMECoreService.eventDownLock++;
		else if(keyevent.value == KeyEvent.ACTION_UP)
			JnsIMECoreService.eventDownLock--;
		return KEY+TOKEN+keyevent.keyCode+TOKEN+keyevent.scanCode+TOKEN+keyevent.value+ TOKEN +keyevent.deviceId+"\n";
	}
	private static String posString(int x, int y, int value)
	{
		if(value == KeyEvent.ACTION_DOWN)
			JnsIMECoreService.eventDownLock++;
		else if(value == KeyEvent.ACTION_UP)
			JnsIMECoreService.eventDownLock--;
		return TOUCH+TOKEN+x+TOKEN+y+TOKEN+0xFF+TOKEN+value+"\n";
	}
	private static String posString(float x, float y, int tag, int value)
	{
		if(value == KeyEvent.ACTION_DOWN)
			JnsIMECoreService.eventDownLock++;
		else if(value == KeyEvent.ACTION_UP)
			JnsIMECoreService.eventDownLock--;
		return TOUCH+TOKEN+x+TOKEN+y+TOKEN+tag+TOKEN+value+"\n";
	}
	private double calcSinA(int bx, int by, int joystickType) {
		int ox = 0x7f;
		int oy = 0x7f;
		int x = Math.abs(ox - bx);
		int y = Math.abs(oy - by);
		double r = Math.sqrt(Math.pow((double) x, 2) + Math.pow((double)y, 2));
		if (joystickType == JnsIMEPosition.TYPE_LEFT_JOYSTICK) {
			this.leftJoystickCurrentR = (float) r;
		} else if (joystickType == JnsIMEPosition.TYPE_RIGHT_JOYSTICK) {
			this.rightJoystickCurrentR = (float) r;
		}
		this.joystickR = 127;
		double sin = ((double)y) / r;
		return sin;
	}

	private void processRightJoystickData(int i, int j, int deviceId) { // x = buffer[3] y = buffer[4]
		int ox = 0x7f;
		int oy = 0x7f;
		int ux = i;
		int uy = j;
		if (i < 0) ux = 256 + i;
		if (j < 0) uy = 256 + j;
		boolean touchMapped = false;


		if (JnsIMECoreService.keyList != null) 
		{
			for (JnsIMEProfile bp:JnsIMECoreService.keyList)
			{
				if (bp.posR > 0 && bp.posType == JnsIMEPosition.TYPE_RIGHT_JOYSTICK) 
				{
					touchMapped = true;
					double sin = calcSinA(ux, uy, JnsIMEPosition.TYPE_RIGHT_JOYSTICK);
					double touchR1 = (bp.posR/this.joystickR) * this.rightJoystickCurrentR;
					// Log.e(TAG, "touchR1 = " + touchR1 + " bp.posR" + bp.posR + " joystickR = " + joystickR + " rightJoystickCurrentR = " + rightJoystickCurrentR);
					double y = touchR1 * sin;
					double x = Math.sqrt(Math.pow(touchR1, 2) - Math.pow(y, 2));
					float rawX = 0.0f;
					float rawY = 0.0f;
					if (ux < ox && uy < oy) {  //坐标轴上半部的左
						rawX = bp.posX - (float)x;
						rawY = bp.posY - (float)y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis positive left part");
					} else if (ux > ox && uy < oy) {  //坐标轴上半部的右
						rawX = bp.posX + (float) x;
						rawY = bp.posY - (float) y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis positive right part");
					} else if (ux < ox && uy > oy) { //坐标轴下半部的左
						rawX = bp.posX  - (float) x;
						rawY = bp.posY + (float) y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis negtive left part");
					} else if (ux > ox && uy > oy) { //坐标轴下半部的右
						rawX = bp.posX + (float) x;
						rawY = bp.posY + (float) y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis negtiveleft part");
					} else if (ux == ox && uy < oy) { //Y轴变�?
						rawX = bp.posX;
						rawY = bp.posY - (float)y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis Y < 0x7f");
					} else if (ux == ox && uy > oy) { //Y轴变�?
						rawX = bp.posX;
						rawY = bp.posY + (float) y;
						rightMotionKey = true;
						//	Log.e(TAG, "axis Y > 0x7f");
					} else if (ux < ox && uy == oy) { //X轴变�?
						rawX = bp.posX - (float)x;
						rawY = bp.posY;
						rightMotionKey = true;
						Log.e(TAG, "axis X < 0x7f");
					} else if (ux > ox && uy == oy) { //X轴变�?
						rawX = bp.posX + (float) x;
						rawY = bp.posY;
						rightMotionKey = true;
						Log.e(TAG, "axis X  > 0x7f");
					} else if (ux == ox && uy == oy && rightMotionKey) {
						Log.e(TAG, "right  you release map");
						pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.RIGHT_JOYSTICK_TAG, MotionEvent.ACTION_MOVE));
						pw.flush();
						pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.RIGHT_JOYSTICK_TAG, MotionEvent.ACTION_UP));
						pw.flush();
						rightMotionKey = false;
						RightJoystickPresed = false;
					}
					if (rightMotionKey) {
						if(!RightJoystickPresed)
						{
							pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.RIGHT_JOYSTICK_TAG, MotionEvent.ACTION_DOWN));
							pw.flush();
							RightJoystickPresed = true;								
						}

						if(RightJoystickPresed)
						{
							if((rawX != rightJoystickCurrentPosX) && (rawY != rightJoystickCurrentPosY))
								if(System.currentTimeMillis() - last_right_press_time > STICK_MOVE_IRQ_TIME)
								{		
									pw.print(posString(rawX, rawY, JoyStickTypeF.RIGHT_JOYSTICK_TAG, MotionEvent.ACTION_MOVE));
									pw.flush();
									last_right_press_time = System.currentTimeMillis();
								}
						}
					}
					rightJoystickCurrentPosX = rawX;
					rightJoystickCurrentPosY = rawY;
				}
			}
		}
		Log.d(TAG,"z="+ux+", rz="+uy);
		if(!touchMapped)
		{
			int z = ux;
			int rz = uy;
			RawEvent keyevent;

			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_ZP_SCANCODE))
			{
				if(z > 200 )
				{
					if(!joy_zp_pressed)
					{
						joy_zp_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZP_SCANCODE),
								JoyStickTypeF.BUTTON_ZP_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{
					if(joy_zp_pressed)
					{
						joy_zp_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZP_SCANCODE),
								JoyStickTypeF.BUTTON_ZP_SCANCODE, KeyEvent.ACTION_UP,deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}

			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_ZI_SCANCODE))
			{
				if(z < 50)
				{
					if(!joy_zi_pressed)
					{
						joy_zi_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZI_SCANCODE),
								JoyStickTypeF.BUTTON_ZI_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}	
				else
				{	
					if(joy_zi_pressed)
					{
						joy_zi_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZI_SCANCODE),
								JoyStickTypeF.BUTTON_ZI_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}

			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_RZI_SCANCODE))
			{				
				if(rz > 200)
				{

					if(!joy_rzi_pressed)
					{

						joy_rzi_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZI_SCANCODE),
								JoyStickTypeF.BUTTON_RZI_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{	
					if(joy_rzi_pressed)
					{

						joy_rzi_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZI_SCANCODE),
								JoyStickTypeF.BUTTON_RZI_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}

			}

			if( JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_RZP_SCANCODE))
			{
				if(rz < 50)
				{
					if(!joy_rzp_pressed)
					{
						joy_rzp_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZP_SCANCODE),
								JoyStickTypeF.BUTTON_RZP_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{
					if(joy_rzp_pressed)
					{
						joy_rzp_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZP_SCANCODE),
								JoyStickTypeF.BUTTON_RZP_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}
		}
		/*
		if(!touchMapped)
		{
			int z = ux;
			int rz = uy;
			RawEvent keyevent;;
			if(z > 200 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_ZP_SCANCODE)&& 
					((System.currentTimeMillis() - last_right_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZP_SCANCODE),
						JoyStickTypeF.BUTTON_ZP_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_right_press_time = System.currentTimeMillis();
			}
			if(z < 50 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_ZI_SCANCODE)&& 
					((System.currentTimeMillis() - last_right_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_ZI_SCANCODE),
						JoyStickTypeF.BUTTON_ZI_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_right_press_time = System.currentTimeMillis();
			}
			if(rz > 200 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_RZI_SCANCODE)&& 
					((System.currentTimeMillis() - last_right_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZI_SCANCODE),
						JoyStickTypeF.BUTTON_RZI_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_right_press_time = System.currentTimeMillis();
			}
			if(rz < 50 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_RZP_SCANCODE)&& 
					((System.currentTimeMillis() - last_right_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_RZP_SCANCODE),
						JoyStickTypeF.BUTTON_RZP_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_right_press_time = System.currentTimeMillis();
			}
		}
		 */
	}

	private void processLeftJoystickData(int i, int j, int deviceId) { // x = buffer[3] y = buffer[4]
		int ox = 0x7f;
		int oy = 0x7f;
		int ux = i;
		int uy = j;
		if (i < 0) ux = 256 + i;
		if (j < 0) uy = 256 + j;
		boolean touchMapped = false;
		//		 if (bx != 0x7f || by != 0x7f) {
		if (JnsIMECoreService.keyList != null) 
		{
			for (JnsIMEProfile bp: JnsIMECoreService.keyList) 
			{

				if (bp.posR > 0 && bp.posType == JnsIMEPosition.TYPE_LEFT_JOYSTICK) 
				{
					touchMapped = true;
					Log.d(TAG, "r="+bp.posR+", postype="+bp.posType);
					double sin = calcSinA(ux, uy, JnsIMEPosition.TYPE_LEFT_JOYSTICK);
					//						 double y = bp.posR * sin;
					//						 double x = Math.sqrt(Math.pow(bp.posR, 2) - Math.pow(y, 2));
					double touchR1 = (bp.posR/this.joystickR) * this.leftJoystickCurrentR;
					//	 Log.e(TAG, "touchR1 = " + touchR1 + " bp.posR" + bp.posR + " joystickR = " + joystickR + " leftJoystickCurrentR = " + leftJoystickCurrentR);
					double y = touchR1 * sin;
					double x = Math.sqrt(Math.pow(touchR1, 2) - Math.pow(y, 2));
					float rawX = 0.0f;
					float rawY = 0.0f;
					Log.d(TAG, "ox ="+ox+",ux="+ux+",oy="+oy+",uy="+uy);
					if (ux < ox && uy < oy) {  //坐标轴上半部的左
						rawX = bp.posX - (float)x;
						rawY = bp.posY - (float)y;
						leftMotionKey = true;
						Log.d(TAG, "axis positive left part");
					} else if (ux > ox && uy < oy) {  //坐标轴上半部的右
						rawX = bp.posX + (float) x;
						rawY = bp.posY - (float) y;
						leftMotionKey = true;
						Log.d(TAG, "axis positive right part");
					} else if (ux < ox && uy > oy) { //坐标轴下半部的左
						rawX = bp.posX  - (float) x;
						rawY = bp.posY + (float) y;
						leftMotionKey = true;
						Log.d(TAG, "axis negtive left part");
					} else if (ux > ox && uy > oy) { //坐标轴下半部的右
						rawX = bp.posX + (float) x;
						rawY = bp.posY + (float) y;
						leftMotionKey = true;
						Log.d(TAG, "axis negtiveleft part");
					} else if (ux == ox && uy < oy) { //Y轴变�?
						rawX = bp.posX;
						rawY = bp.posY - (float)y;
						leftMotionKey = true;
						Log.d(TAG, "axis Y < 0x7f");
					} else if (ux == ox && uy > oy) { //Y轴变�?
						rawX = bp.posX;
						rawY = bp.posY + (float) y;
						leftMotionKey = true;
						Log.d(TAG, "axis Y > 0x7f");
					} else if (ux < ox && uy == oy) { //X轴变�?
						rawX = bp.posX - (float)x;
						rawY = bp.posY;
						leftMotionKey = true;
						Log.d(TAG, "axis X < 0x7f");
					} else if (ux > ox && uy == oy) { //X轴变�?
						rawX = bp.posX + (float) x;
						rawY = bp.posY;
						leftMotionKey = true;
						Log.d(TAG, "axis X  > 0x7f");
					} else if (ux == ox && uy == oy && leftMotionKey) {
						Log.e(TAG, "left joystick you release map");
						pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.LEFT_JOYSTICK_TAG, MotionEvent.ACTION_MOVE));
						pw.flush();
						pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.LEFT_JOYSTICK_TAG, MotionEvent.ACTION_UP));
						pw.flush();
						leftMotionKey = false;
						LeftJoystickPresed = false;
					}

					Log.d(TAG, "leftMotionKey="+leftMotionKey);

					if (leftMotionKey) 
					{
						Log.d(TAG, "LeftJoystickPresed="+LeftJoystickPresed);
						if(!LeftJoystickPresed)
						{	
							//pw.print(posString(bp.posX, bp.posY, JoyStickTypeF.LEFT_JOYSTICK_TAG, MotionEvent.ACTION_DOWN));
							//pw.flush();
							try {
								socket.getOutputStream().write(posString(bp.posX, bp.posY, JoyStickTypeF.LEFT_JOYSTICK_TAG, MotionEvent.ACTION_DOWN).getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							LeftJoystickPresed = true;
						}

						if(LeftJoystickPresed)
						{
							if((rawX != leftJoystickCurrentPosX) && (rawY != leftJoystickCurrentPosY))
								if(System.currentTimeMillis() - last_left_press_time > STICK_MOVE_IRQ_TIME)
								{	
									pw.print(posString(rawX, rawY, JoyStickTypeF.LEFT_JOYSTICK_TAG, MotionEvent.ACTION_MOVE));
									pw.flush();
									last_left_press_time = System.currentTimeMillis();
								}
						}

					}
					leftJoystickCurrentPosX = rawX;
					leftJoystickCurrentPosY = rawY;
				}
			}
		}
		Log.d(TAG,"z="+ux+", y="+uy);
		if(!touchMapped)
		{
			int x = ux;
			int y = uy;
			RawEvent keyevent;
			// ����ҡ��ת���ɰ���ӳ��

			// ҡ������
			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_XP_SCANCODE))
			{	
				if(x > 200)
				{	
					if(!joy_xp_pressed)
					{	
						joy_xp_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XP_SCANCODE),
								JoyStickTypeF.BUTTON_XP_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else 
				{
					if(joy_xp_pressed)
					{
						joy_xp_pressed=false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XP_SCANCODE),
								JoyStickTypeF.BUTTON_XP_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}

			// ҡ������
			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_XI_SCANCODE))
			{
				if(x < 50)
				{
					if(!joy_xi_pressed)
					{	
						this.joy_xi_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XI_SCANCODE),
								JoyStickTypeF.BUTTON_XI_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{
					if(joy_xi_pressed)
					{	
						this.joy_xi_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XI_SCANCODE),
								JoyStickTypeF.BUTTON_XI_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}

			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_YI_SCANCODE))
			{		
				if(y > 200)
				{
					if(!joy_yi_pressed)
					{	
						joy_yi_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YI_SCANCODE),
								JoyStickTypeF.BUTTON_YI_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{
					if(joy_yi_pressed)
					{	
						joy_yi_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YI_SCANCODE),
								JoyStickTypeF.BUTTON_YI_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}

				}
			}

			if(JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_YP_SCANCODE))
			{	
				if(y < 50)
				{
					if(!joy_yp_pressed)
					{
						joy_yp_pressed = true;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YP_SCANCODE),
								JoyStickTypeF.BUTTON_YP_SCANCODE, KeyEvent.ACTION_DOWN, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
				else
				{
					if(joy_yp_pressed)
					{
						joy_yp_pressed = false;
						keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YP_SCANCODE),
								JoyStickTypeF.BUTTON_YP_SCANCODE, KeyEvent.ACTION_UP, deviceId);
						pw.print(keyString(keyevent));
						pw.flush();
					}
				}
			}
		}
		/*
		if(!touchMapped)
		{
			int x = ux;
			int y = uy;
			RawEvent keyevent;;
			if(x > 200 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_XP_SCANCODE)&& 
					((System.currentTimeMillis() - last_left_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XP_SCANCODE),
						JoyStickTypeF.BUTTON_XP_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_left_press_time = System.currentTimeMillis();
			}
			if(x < 50 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_XI_SCANCODE)&& 
					((System.currentTimeMillis() - last_left_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_XI_SCANCODE),
						JoyStickTypeF.BUTTON_XI_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_left_press_time = System.currentTimeMillis();
			}
			if(y > 200 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_YI_SCANCODE)&& 
					((System.currentTimeMillis() - last_left_press_time) > 200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YI_SCANCODE),
						JoyStickTypeF.BUTTON_YI_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_left_press_time = System.currentTimeMillis();
			}
			if(y < 50 && JnsIMECoreService.keyMap.containsKey(JoyStickTypeF.BUTTON_YP_SCANCODE)&& 
					((System.currentTimeMillis() - last_left_press_time) >200))
			{
				keyevent = new RawEvent(JnsIMECoreService.keyMap.get(JoyStickTypeF.BUTTON_YP_SCANCODE),
						JoyStickTypeF.BUTTON_YP_SCANCODE, KeyEvent.ACTION_DOWN);
				pw.print(keyString(keyevent));
				pw.flush();
				keyevent.value = KeyEvent.ACTION_UP;
				pw.print(keyString(keyevent));
				pw.flush();
				last_left_press_time = System.currentTimeMillis();
			}
		}
		 */
	}

}