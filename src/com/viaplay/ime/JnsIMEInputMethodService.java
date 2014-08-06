package com.viaplay.ime;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.viaplay.ime.bean.JnsIMEProfile;
import com.viaplay.ime.hardware.JoyStickTypeF;
import com.viaplay.ime.jni.InputAdapter;
import com.viaplay.ime.uiadapter.FloatView;
import com.viaplay.ime.util.JnsEnvInit;
import com.viaplay.ime.util.SendEvent;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class JnsIMEInputMethodService extends InputMethodService implements android.content.DialogInterface.OnClickListener {

	private final static String TAG = "JnsIMEMethod";
	private final static String KEY_MAP_FILE_TAG = ".keymap";
	public final static int SHOW_WINDOW = 1;
	public final static int CLOSE_WINDOW = 2;
	public final static int UPDATE_WINDOW = 3;
	public final static int ALERT_ROOT = 4;

	private boolean isTakePic = false;
	public static String validAppName = "";
	static String lastAppName = "";
	public static  String currentAppName = "";
	public static boolean jnsIMEInUse = false;
	static Process process=null;
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;
    private boolean ret = false;
	private FloatView floatingView = null;
	public static Handler floatingHandler = null;
    private boolean iscapture = false;


	static class InputHandler extends Handler
	{
		WeakReference<JnsIMEInputMethodService> mActivity;

		InputHandler(JnsIMEInputMethodService context) {
			mActivity = new WeakReference<JnsIMEInputMethodService>(context);
		}

		@Override
		public void handleMessage(Message msg) {

		}

	};
	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {  
		@Override  
		public void onReceive(final Context context, final Intent intent) {  
			final String action = intent.getAction();  
			if(Intent.ACTION_USER_PRESENT.equals(action)){  
				int i = 0;
				for(; i< FloatingFunc.excepApps.length; i++)
				{
					if(JnsIMEInputMethodService.currentAppName.equals(FloatingFunc.excepApps[i]))
						break;
				}
				if(i < FloatingFunc.excepApps.length)
				{	
					Toast.makeText(JnsIMEInputMethodService.this,JnsIMEInputMethodService.this.getString(R.string.alert_move), Toast.LENGTH_LONG).show();
					//Toast.makeText(JnsIMEInputMethodService.this, "如果游戏无法恢复画面，请移动一下浮动窗口", Toast.LENGTH_LONG).show();
				}
			}
		}  
	};  
	@SuppressLint({ "SdCardPath"})
	@Override
	public void onCreate()
	{
		super.onCreate();
		jnsIMEInUse = true;
		JnsIMECoreService.ime = this;
		// 启动核心服务
		if(JnsIMECoreService.initialed)
			return;
		Intent intent = new Intent("com.viaplay.ime.JnsIMECore");
		this.startService(intent);
		// 设置浮动窗属性
		floatingView = new FloatView(this);  
		floatingView.setImageResource(R.drawable.shot_normal);
		floatingView.setScaleType(ScaleType.FIT_CENTER);
		floatingView.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				FloatingFunc.onTouchEvent(event, v);
				return true;
			}

		});
		IntentFilter filter = new IntentFilter();  
		filter.addAction(Intent.ACTION_USER_PRESENT);  
		registerReceiver(mBatInfoReceiver, filter);  
		// 首次使用弹出浮动窗开启询问
		SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
		if(pre.getBoolean("init", true))
		{
			showPopWin();
			Editor ed = pre.edit();
			ed.putBoolean("init", false);
			ed.commit();
		}
		if(pre.getBoolean("floatViewS", false))	
			FloatingFunc.show(this.getApplicationContext(), null, floatingView);
		floatingHandler = new InputHandler(this)
		{
			@Override
			public void handleMessage(Message  msg)
			{
				switch(msg.what)
				{
				case ALERT_ROOT:
					Toast.makeText(JnsIMEInputMethodService.this, JnsIMEInputMethodService.this.getString(R.string.alert_root), Toast.LENGTH_LONG).show();
					break;
				case SHOW_WINDOW:
					FloatingFunc.show(getApplicationContext(), null, floatingView);
					break;
				case CLOSE_WINDOW:
					FloatingFunc.close(getApplicationContext());
					break;
				case UPDATE_WINDOW:
					int i = 0;
					for(; i< FloatingFunc.excepApps.length; i++)
					{
						if(JnsIMEInputMethodService.currentAppName.equals(FloatingFunc.excepApps[i]))
							break;
					}
					if(i < FloatingFunc.excepApps.length)
					{	
						Toast.makeText(JnsIMEInputMethodService.this, JnsIMEInputMethodService.this.getString(R.string.alert_move), Toast.LENGTH_LONG).show();
						//Toast.makeText(JnsIMEInputMethodService.this, "如果游戏无法恢复画面，请移动一下浮动窗口", Toast.LENGTH_LONG).show();
					}
					try{
						FloatingFunc.updateViewPosition(floatingView);
					}
					catch(Exception e)
					{
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					process = Runtime.getRuntime().exec("su");
					dos = new DataOutputStream(process.getOutputStream());
					dis = new DataInputStream(process.getInputStream());
					dos.write("id \n".getBytes());
					dos.flush();
					String line = dis.readLine();
					if (line.contains("uid=0(root)"))
						ret = true;
					else
					{
						Message msg = new Message();
						msg.what = ALERT_ROOT;
						floatingHandler.sendMessage(msg);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Message msg = new Message();
					msg.what = ALERT_ROOT;
					floatingHandler.sendMessage(msg);
				}
			}
		}).start();
		// 开启当前应用检测线程
		new Thread(new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true)
				{	  
					ActivityManager am = (ActivityManager) JnsIMEInputMethodService.this.getSystemService(ACTIVITY_SERVICE);
					String tmp = am.getRunningTasks(1).get(0).topActivity.getPackageName();
					currentAppName = tmp;
					if(!lastAppName.equals(tmp))
					{
						if(jnsIMEInUse)
						{
							Message msg = new Message();
							msg.what = UPDATE_WINDOW;
							if(JnsIMEInputMethodService.floatingHandler != null)
								JnsIMEInputMethodService.floatingHandler.sendMessage(msg);
							reLoadPofileFile(tmp);
						}
					}
					if(!tmp.equals(JnsIMEInputMethodService.this.getPackageName()))
					{	
						JnsIMEInputMethodService.validAppName = tmp;
					}
					lastAppName = tmp;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}
	@SuppressWarnings("deprecation")
	void showPopWin()
	{
		Dialog dialog = new AlertDialog.Builder(this).setMessage(
				this.getString(R.string.floating_notice)).setNegativeButton(
						this.getString(R.string.no), this).setPositiveButton(
								this.getString(R.string.yes), this).create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  

		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();    
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);    
		Display display = wm.getDefaultDisplay();    
		if (display.getHeight() > display.getWidth())    
		{    
			lp.width = (int) (display.getWidth() * 1.0);    
		}    
		else    
		{    
			lp.width = (int) (display.getWidth() * 0.5);    
		}    

		dialog.getWindow().setAttributes(lp);  
		dialog.show();
	}
	@Override
	public void onDestroy () 
	{
		this.onCreateInputView();
		super.onDestroy();
		jnsIMEInUse = false;
		ret = false;
		floatingHandler = null;
		try {
			dos.writeBytes("exit\n");
			dos.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JnsIMECoreService.keyList.clear();
		JnsIMECoreService.keyMap.clear();
		FloatingFunc.close(this);
		Intent intent = new Intent("com.viaplay.ime.JnsIMECore");
		this.stopService(intent);
		try{
			unregisterReceiver(mBatInfoReceiver);  
		}
		catch(Exception e)
		{

		}
	}
	/**
	 * 虏茅脩炉碌卤脟掳碌脛脢脗录镁脢脟路帽脌麓脭麓脫脷脪隆赂脣禄貌脮脽脥路驴酶
	 * 
	 * @param event 碌卤脟掳碌脛keyevent
	 * @return 脌麓脭麓脫脷脪隆赂脣禄貌脥路驴酶脭貌禄谩陆芦scancode赂脛鲁脡露脭脫娄碌脛scancode
	 */
	KeyEvent mathJoyStick(KeyEvent event)
	{
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getScanCode() == 0)
		{
			if(InputAdapter.gHatUpPressed)
			{	
				if(event.getAction() == KeyEvent.ACTION_UP)
					InputAdapter.gHatUpPressed = false;
				return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						event.getAction(), KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, 
						JoyStickTypeF.BUTTON_UP_SCANCODE, 0);
			}
			return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					event.getAction(), KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, 
					JoyStickTypeF.BUTTON_YP_SCANCODE, 0);
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getScanCode() == 0)
		{	
			if(InputAdapter.gHatDownPressed)
			{
				if(event.getAction() == KeyEvent.ACTION_UP)
					InputAdapter.gHatDownPressed = false;
				return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 
						event.getAction(), KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0, 
						JoyStickTypeF.BUTTON_DOWN_SCANCODE, 0);

			}
			return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 
					event.getAction(), KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0,
					JoyStickTypeF.BUTTON_YI_SCANCODE, 0);
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getScanCode() == 0)
		{
			if(InputAdapter.gHatLeftPressed)
			{
				if(event.getAction() == KeyEvent.ACTION_UP)
					InputAdapter.gHatLeftPressed = false;
				return  new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						event.getAction(), KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 
						JoyStickTypeF.BUTTON_LEFT_SCANCODE, 0);
			}
			return  new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 
					event.getAction(), KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 
					JoyStickTypeF.BUTTON_XI_SCANCODE, 0);
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getScanCode() == 0)
		{
			if(InputAdapter.gHatRightPressed)
			{
				if(event.getAction() == KeyEvent.ACTION_UP)
					InputAdapter.gHatRightPressed = false;
				return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 
						event.getAction(), KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 
						JoyStickTypeF.BUTTON_RIGHT_SCANCODE, 0);
			}
			return new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 
					event.getAction(), KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 
					JoyStickTypeF.BUTTON_XP_SCANCODE, 0);
		}
		return null;
	}
	public boolean startTpConfig()
	{
		JnsIMECoreService.touchConfiging = true;
		if(iscapture)
			return false;
		iscapture = true;
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				//		isTakePic = com.jnselectronics.ime.jni.ScreenShot.getScreenShot();
				if(JnsEnvInit.rooted)
					Screencap();
				Log.d("JnsIME", "take pic "+isTakePic);
				Intent in = new Intent(JnsIMEInputMethodService.this.getApplicationContext(), JnsIMETpConfigActivity.class);
				//in.putExtra("screenshot", isTakePic);
				in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				JnsIMEInputMethodService.this.startActivity(in);
				iscapture = false;
			}
		}).start();
		return true;
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		/*
		if(KeyEvent.KEYCODE_SEARCH == keyCode && (!JnsIMECoreService.touchConfiging) && JnsEnvInit.rooted)
		{
			if(currentAppName.equals(this.getPackageName()))
				return false;
			else
			{
				Toast.makeText(this, this.getString(R.string.screen_shot), Toast.LENGTH_SHORT).show();
				return startTpConfig();
			}
		}
		 */
		if(JnsIMECoreService.touchConfiging)
			return false;
		KeyEvent tmpEvent = mathJoyStick(event);
		if(tmpEvent != null)
			event = tmpEvent;
		if(iteratorKeyList(JnsIMECoreService.keyList, event.getScanCode())!= null)
			return true;
		if(JnsIMECoreService.keyMap.containsKey(event.getScanCode()))
		{	
			if(!JnsEnvInit.rooted)
			{	
				event =  new KeyEvent(event.getDownTime(), event.getDownTime(), 
						KeyEvent.ACTION_DOWN, JnsIMECoreService.keyMap.get(event.getScanCode()),
						0, event.getMetaState(), event.getDeviceId(), 0);
				//event =  new KeyEvent(KeyEvent.ACTION_DOWN, JnsIMECoreService.keyMap.get(event.getScanCode()));
				this.getCurrentInputConnection().sendKeyEvent(event);
			}
			return true;
		}
		return false;
	}
	/**
	 *  陆脴脥录路陆路篓隆拢2.3脪脭脡脧脥篓鹿媒脰麓脨脨screencap脙眉脕卯陆脴脥录拢卢2.2脪脭脧脗脰卤陆脫露脕fb陆脴脥录
	 */
	void Screencap()
	{
		int sdkNum =  Build.VERSION.SDK_INT;
		try {
			if(sdkNum < 9)
				com.viaplay.ime.jni.ScreenShot.getScreenShot();
			else 
			{	//if(process == null && JnsEnvInit.rooted)
				//{
				File f = new File("/mnt/sdcard/jnsinput/tmp.bmp");
				if(!ret)
				{	
					process = Runtime.getRuntime().exec("su");
					dos = new DataOutputStream(process.getOutputStream());
					dis = new DataInputStream(process.getInputStream());
					dos.write("id \n".getBytes());
					dos.flush();
					String line = dis.readLine();
					if (line.contains("uid=0(root)"))
						ret = true;
					else
						return;
				}
				if(f.exists())
				{
					dos.writeBytes("rm /mnt/sdcard/jnsinput/tmp.bmp\n");
					dos.flush();
					dos.writeBytes("sync\n");
					dos.flush();
					dos.writeBytes("sync\n");
					dos.flush();
				}
				while(f.exists())
				{
					Log.d(TAG, "file exit");
					Thread.sleep(100);
				}
				dos.writeBytes("screencap -p /mnt/sdcard/jnsinput/tmp.bmp\n");
				dos.flush();
				dos.writeBytes("sync\n");
				dos.flush();
				dos.writeBytes("sync\n");
				dos.flush();
				while(!f.exists())
				{
					Log.d(TAG, "shoot failed");
					Thread.sleep(100);
				}
			}

		} catch (IOException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(JnsIMECoreService.touchConfiging)
			return false;
		KeyEvent tmpEvent = mathJoyStick(event);
		if(keyCode == KeyEvent.KEYCODE_SEARCH)
			return true;
		if(tmpEvent != null)
		{
			if( iteratorKeyList(JnsIMECoreService.keyList, tmpEvent.getScanCode())!= null)
			{
				event = tmpEvent;
				return true;
			}
			if(JnsIMECoreService.keyMap.containsKey(tmpEvent.getScanCode()))
			{
				event = tmpEvent;
				return true;
			}
		}
		if( iteratorKeyList(JnsIMECoreService.keyList, event.getScanCode())!= null)
			return true;
		if(JnsIMECoreService.keyMap.containsKey(event.getScanCode()))
		{
			if(!JnsEnvInit.rooted)
			{	
				//event =  new KeyEvent(event.getDownTime(), event.getDownTime(), 
				///			KeyEvent.ACTION_UP, JnsIMECoreService.keyMap.get(event.getScanCode()),
				//			0, event.getMetaState(), event.getDeviceId(), 0);
				event =  new KeyEvent(event.getDownTime(), event.getDownTime(), 
						KeyEvent.ACTION_UP, JnsIMECoreService.keyMap.get(event.getScanCode()),
						0, event.getMetaState(), event.getDeviceId(), 0);
				this.getCurrentInputConnection().sendKeyEvent(event);
			}
			return true;
		}
		return false;
	}
	@SuppressLint("UseSparseArrays")
	private void reLoadPofileFile(String name)
	{
		Log.d(TAG, "reload file");
		if(JnsIMEInputMethodService.currentAppName.equals(this.getPackageName()))
			return;
		if (JnsIMECoreService.keyList == null) JnsIMECoreService.keyList = new ArrayList<JnsIMEProfile>();
		if (JnsIMECoreService.keyMap == null) JnsIMECoreService.keyMap = new HashMap<Integer, Integer>();
		try {
			while(SendEvent.getSendEvent().getEventDownLock())
			{
				try {
					Log.d(TAG, "has motion not release");
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"SOCKET EOOR");
			e.printStackTrace();
		}
		Log.d("SendEvent", "lock="+JnsIMECoreService.eventDownLock);
		Log.d("SendEvent", "reload file" + name);
		if (JnsIMECoreService.keyList.size() > 0)  JnsIMECoreService.keyList.clear();
		if (!JnsIMECoreService.keyMap.isEmpty())  JnsIMECoreService.keyMap.clear();
		Log.d(TAG, "reload file" + name);
		reloadTouchMap(name);
		reloadKeyMap(name + KEY_MAP_FILE_TAG);
	}
	private boolean reloadTouchMap(String name)
	{
		try 
		{
			FileReader fr = new FileReader(this.getFilesDir() + "/" + name);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			Log.d(TAG, "find file"+name);
			String val = br.readLine();
			while (null != val) {
				if (val.equals('\n')) val = br.readLine();
				JnsIMEProfile bp = new JnsIMEProfile();
				if (val != null && !val.equals("")) {
					bp.key = Integer.valueOf(val);	
				}
				val = br.readLine();
				if ( val != null && !val.equals("")) {
					bp.keyCode = Integer.valueOf(val);
				}
				val = br.readLine();
				if ( val != null && !val.equals("")) {
					bp.posX = Float.valueOf(val);
				}
				val = br.readLine();
				if (val != null && !val.equals("")) {
					bp.posY = Float.valueOf(val);
				}
				val = br.readLine();
				if (val != null && !val.equals("")) {
					bp.posR = Float.valueOf(val);
				}
				val = br.readLine();
				if (val != null && !val.equals("")) {
					bp.posType = Float.valueOf(val);
				}
				//					listConfig.add(bp);
				JnsIMECoreService.keyList.add(bp);
				val = br.readLine();
				//	BlueoceanCore.gameStart = true;
			}
			Log.d(TAG, "KeyList size ="+JnsIMECoreService.keyList.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d(TAG, "can not find file"+name);
			if(JnsIMECoreService.keyList==null)
				return false;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@SuppressWarnings("resource")
	@SuppressLint("UseSparseArrays")
	private boolean reloadKeyMap(String name)
	{
		try {
			FileReader fr = new FileReader(this.getFilesDir() + "/" + name);
			BufferedReader br = new BufferedReader(fr);
			Log.d(TAG, "find file"+name);

			String val = br.readLine();
			while (val != null)
			{
				String data[] = val.split(":");
				JnsIMECoreService.keyMap.put(Integer.parseInt(data[2]), Integer.parseInt(data[3]));
				val = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			FileReader defaultfr;
			try {
				defaultfr = new FileReader(this.getFilesDir() + "/" + "default"+(JnsIMECoreService.currentDeaultIndex+1)+KEY_MAP_FILE_TAG);
				BufferedReader br = new BufferedReader(defaultfr);
				Log.d(TAG, "find file"+name);

				String val = br.readLine();
				while (val != null)
				{
					String data[] = val.split(":");
					JnsIMECoreService.keyMap.put(Integer.parseInt(data[2]), Integer.parseInt(data[3]));
					val = br.readLine();
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				return false;
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	private static JnsIMEProfile iteratorKeyList(List<JnsIMEProfile> keylist, int scancode)
	{
		if(keylist==null)
			return null;
		for(JnsIMEProfile keyProfile : keylist)
			if(keyProfile.key == scancode)
				return keyProfile;
		return null;
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch(which)
		{
			case DialogInterface.BUTTON_POSITIVE:
				 SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
				 Editor ed = pre.edit();
				 ed.putBoolean("floatViewS", true);
				 ed.commit();
				 Message msg = new Message();
				 msg.what = SHOW_WINDOW;
				 floatingHandler.sendMessage(msg);
				 if(JnsIMESettingActivity.cp != null)
					 JnsIMESettingActivity.cp.setChecked(true);
				 break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
		}
	}

}
