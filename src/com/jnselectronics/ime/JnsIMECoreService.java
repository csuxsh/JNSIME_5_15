package com.jnselectronics.ime;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jnselectronics.ime.bean.JnsIMEProfile;
import com.jnselectronics.ime.jni.InputAdapter;
import com.jnselectronics.ime.jni.JoyStickEvent;
import com.jnselectronics.ime.jni.RawEvent;
import com.jnselectronics.ime.uiadapter.JnsIMEScreenView;
import com.jnselectronics.ime.util.AppHelper;
import com.jnselectronics.ime.util.JnsEnvInit;
import com.jnselectronics.ime.util.SendEvent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class JnsIMECoreService extends Service {

	public final static String TAG = "JnsIMECore";
	public final static int HAS_KEY_DATA = 1;
	public final static int HAS_STICK_DATA = 3;
	private final static int ROOT_SUCCESE = 1;
	private final static int ROOT_FAILED = 2;

	private Handler Alerthandle = null;
	private boolean alertDialogEnable = true;
	private boolean alertDialogShow = false;

	public static boolean initialed = false;
	public static boolean touchConfiging = false;
	static boolean gameStart = false;
	public static Handler DataProcessHandler = null;
	public static AppHelper  aph;
	public static int eventDownLock = 0;

	public static List<JnsIMEProfile> keyList = new  ArrayList<JnsIMEProfile>();
	@SuppressLint("UseSparseArrays")
	public static Map<Integer, Integer> keyMap = new HashMap<Integer, Integer>();
	public static  Queue<RawEvent> keyQueue = new ConcurrentLinkedQueue<RawEvent>();
	public static  Queue<JoyStickEvent> stickQueue = new ConcurrentLinkedQueue<JoyStickEvent>();
	public static int currentDeaultIndex = 0;
	static List<Activity> activitys = new ArrayList<Activity>();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressLint("HandlerLeak")
	private void startDataProcess()
	{
		DataProcessHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				Log.d("JnsEnvInit", "alertDialogEnable="+alertDialogEnable);
				switch(msg.what)
				{
				case JnsIMECoreService.HAS_KEY_DATA:
					RawEvent keyevent = keyQueue.poll();
					if(keyevent!=null)
					{
						Log.d(TAG, "get a key event");
						Log.d(TAG, "the action is "+ keyevent.value);
						SendEvent.getSendEvent().sendKey(keyevent);
					}
					break;
				case JnsIMECoreService.HAS_STICK_DATA:
					JoyStickEvent stickevent = stickQueue.poll();
					if(stickevent != null)
					{
						Log.d(TAG, "send joy stick event");
						SendEvent.getSendEvent().sendJoy(stickevent);
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
	}
	private void initJni(Context context)
	{
		if(initialed)
			return;
		initialed = true;
		//	JnsIMERoot.setContext(this);
		JnsEnvInit.mContext = this;
		while(!JnsEnvInit.root())
		{
			Message msg = new Message();
			msg.what = JnsIMECoreService.ROOT_FAILED;
			Alerthandle.sendMessage(msg);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Message msg = new Message();
		msg.what = JnsIMECoreService.ROOT_SUCCESE;
		Alerthandle.sendMessage(msg);
		InputAdapter.mcontext = context;
		InputAdapter.init();
		InputAdapter.start();
		InputAdapter.getKeyThreadStart();
	}
	@SuppressLint({ "HandlerLeak", "HandlerLeak", "HandlerLeak" })
	private void CheckInit()
	{
		final OnClickListener ocl = new OnClickListener()
		{

			@SuppressLint("HandlerLeak")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which)
				{
				case DialogInterface.BUTTON_NEGATIVE:
					alertDialogEnable = false;
					break;
				case DialogInterface.BUTTON_POSITIVE:
					 Uri uri = Uri.parse("http://forum.xda-developers.com/showthread.php?t=833953");  
					 Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
					 intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");  
					 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 JnsIMECoreService.this.startActivity(intent);  
					 
					 break;
				}
				alertDialogShow = false;

			}

		};
		Alerthandle  = new Handler()
		{
			@SuppressWarnings("deprecation")
			public void handleMessage(Message msg)
			{
				Log.d("JnsEnvInit", "alertDialogEnable="+alertDialogEnable);
				switch(msg.what)
				{
				case JnsIMECoreService.ROOT_SUCCESE:
					Toast.makeText(JnsIMECoreService.this, "root succese", Toast.LENGTH_LONG).show();
					break;
				case JnsIMECoreService.ROOT_FAILED:
					if(alertDialogEnable)
					{	
						Dialog dialog = new AlertDialog.Builder(JnsIMECoreService.this).setMessage(JnsIMECoreService.this.getString(R.string.root_notice) ).setPositiveButton("sure",
								ocl).setNegativeButton("cancle", ocl).create();
						dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  

						WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();    
						WindowManager wm = (WindowManager)JnsIMECoreService.this   
						.getSystemService(Context.WINDOW_SERVICE);    
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
						Log.d("JnsEnvInit", "showdialog");
						if(!alertDialogShow)
						{	
							dialog.show();  
							alertDialogShow = true;
						}
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
	}
	@SuppressWarnings("deprecation")
	public  void updateNotification(String info) {
		NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher, this.getString(R.string.app_name) + info, System.currentTimeMillis());
		Intent intent = new Intent(this.getApplicationContext(), JnsIME.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(this, this.getString(R.string.app_name), this.getString(R.string.app_name), mPendingIntent);
		notificationManager.notify(1, notification);
	}
	@Override
	public void onCreate()
	{
		Log.d("JnsIME", "JnsIMECore start");
		if(aph == null)
			aph = new AppHelper(this);
		CheckInit();
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				JnsIMECoreService.this.initJni(JnsIMECoreService.this);
			}

		}).start();
		createTmpDir();
		startDataProcess();
		JnsIMEScreenView.context = this;
		JnsIMEScreenView.loadTpMapRes();
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				SendEvent.getSendEvent().connectJNSInputServer();
			}

		}).start();
		updateNotification(this.getString(R.string.app_name));
	}
	void createTmpDir()
	{
		File rdir = new File("mnt/sdcard/jnsinput");
		if(!rdir.exists())
			rdir.mkdir();
		File dir = new File("mnt/sdcard/jnsinput/app_icon");
		if(!dir.exists())
			dir.mkdir();
	}
}
