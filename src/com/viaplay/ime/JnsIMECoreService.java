package com.viaplay.ime;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.viaplay.ime.R;
import com.viaplay.ime.bean.JnsIMEProfile;
import com.viaplay.ime.jni.InputAdapter;
import com.viaplay.ime.jni.JoyStickEvent;
import com.viaplay.ime.jni.RawEvent;
import com.viaplay.ime.uiadapter.JnsIMEScreenView;
import com.viaplay.ime.util.AppHelper;
import com.viaplay.ime.util.JnsEnvInit;
import com.viaplay.ime.util.SendEvent;

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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * GameCenter服务核心类
 * 
 * @author Administrator
 *
 */
@SuppressLint("SdCardPath")
public class JnsIMECoreService extends Service {

	@SuppressWarnings("unused")
	private  final static String TAG = "JnsIMECore";
	public final static int HAS_KEY_DATA = 1;
	public final static int START_TPCFG =2;
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
	public static JnsIMEInputMethodService ime = null;
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
	
	static class CoreHandler extends Handler
	{
		WeakReference<JnsIMECoreService> mActivity;
		   
		CoreHandler(JnsIMECoreService context) {
                mActivity = new WeakReference<JnsIMECoreService>(context);
        }

        @Override
        public void handleMessage(Message msg) {
        
          }
        
	};
	/**
	 *  开启事件消息接收loop
	 */
	private void startDataProcess()
	{
		DataProcessHandler = new CoreHandler(this)
		{
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				case JnsIMECoreService.HAS_KEY_DATA:
					RawEvent keyevent = keyQueue.poll();
					if(keyevent!=null)
						SendEvent.getSendEvent().sendKey(keyevent);
					break;
				case JnsIMECoreService.START_TPCFG:
					Toast.makeText(JnsIMECoreService.this, JnsIMECoreService.this.getString(R.string.screen_shot), Toast.LENGTH_SHORT).show();
					break;
				case JnsIMECoreService.HAS_STICK_DATA:
					JoyStickEvent stickevent = stickQueue.poll();
					if(stickevent != null)
						SendEvent.getSendEvent().sendJoy(stickevent);
					break;
				}
				super.handleMessage(msg);
			}
		};
	}
	/**
	 * native环境初始化
	 * @param context
	 */
	private void initJni(Context context)
	{
		if(initialed)
			return;
		initialed = true;
		JnsEnvInit.mContext = this;
		while(!JnsEnvInit.root())
		{
			Message msg = new Message();
			msg.what = JnsIMECoreService.ROOT_FAILED;
			Alerthandle.sendMessage(msg);
			try {
				Thread.sleep(6000);
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
	/**
	 *	检查root权限
	 */
	private void CheckInit()
	{
		final OnClickListener ocl = new OnClickListener()
		{

			@SuppressLint("HandlerLeak")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which)
				{
				case DialogInterface.BUTTON_NEGATIVE:
					alertDialogEnable = false;
					break;
				case DialogInterface.BUTTON_POSITIVE:
					/*
					Uri uri = Uri.parse("http://forum.xda-developers.com/showthread.php?t=833953");  
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
					intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");  
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					JnsIMECoreService.this.startActivity(intent);  
					 */
					break;
				}
				alertDialogShow = false;

			}

		};
		Alerthandle  = new CoreHandler(this)
		{
			@SuppressWarnings("deprecation")
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				case JnsIMECoreService.ROOT_SUCCESE:
					break;
				case JnsIMECoreService.ROOT_FAILED:
					SharedPreferences sp = JnsIMECoreService.this.getApplicationContext(). getSharedPreferences("popwin", Context.MODE_PRIVATE); 
					alertDialogEnable = sp.getBoolean("pop", true);

					if(alertDialogEnable)
					{	
						SharedPreferences.Editor  edit = sp.edit();
						edit.putBoolean("pop", false);
						edit.commit();

						Dialog dialog = new AlertDialog.Builder(JnsIMECoreService.this).setMessage(JnsIMECoreService.this.getString(R.string.root_notice)).setNegativeButton(JnsIMECoreService.this.getString(R.string.i_get), ocl).create();
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
					/*
					if(!alertDialogShow)
					{	
						Intent in = new Intent(JnsIMECoreService.this, JnsIMERootNotice.class);
						in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						JnsIMECoreService.this.startActivity(in);
						alertDialogShow = true;
					}*/
					break;
				}
				super.handleMessage(msg);
			}
		};
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		initialed = false;
		InputAdapter.stop();
		NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(1);
		if(JnsIMEInputMethodService.jnsIMEInUse)
		{	
			 Intent intent = new Intent("com.viaplay.ime.JnsIMECore");
			 this.startService(intent);
		}
	}

	/**
	 * 显示通知栏
	 * 
	 * @param info
	 */
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
		JnsEnvInit.mContext = this;
		CheckInit();
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
		new Thread(new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				JnsIMECoreService.this.initJni(JnsIMECoreService.this);
			}

		}).start();
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
