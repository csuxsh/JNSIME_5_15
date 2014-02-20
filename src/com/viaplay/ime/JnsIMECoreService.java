package com.viaplay.ime;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.viaplay.ime.util.DBHelper;
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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Ó¦ÓÃºóÌ¨ÔËÐÐµÄÖ÷·þÎñ
 * 
 * @author Administrator
 *
 */
public class JnsIMECoreService extends Service {

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

	@SuppressLint("HandlerLeak")
	/**
	 *  Êý¾Ý´¦Àíloop
	 */
	private void startDataProcess()
	{
		DataProcessHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				//Log.d("JnsEnvInit", "alertDialogEnable="+alertDialogEnable);
				switch(msg.what)
				{
				case JnsIMECoreService.HAS_KEY_DATA:
					RawEvent keyevent = keyQueue.poll();
					if(keyevent!=null)
					{
						//Log.d(TAG, "get a key event");
						SendEvent.getSendEvent().sendKey(keyevent);
					}
					break;
				case JnsIMECoreService.START_TPCFG:
					Toast.makeText(JnsIMECoreService.this, JnsIMECoreService.this.getString(R.string.screen_shot), Toast.LENGTH_SHORT).show();
					break;
				case JnsIMECoreService.HAS_STICK_DATA:
					JoyStickEvent stickevent = stickQueue.poll();
					if(stickevent != null)
					{
						//Log.d(TAG, "send joy stick event");
						SendEvent.getSendEvent().sendJoy(stickevent);
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
	}
	/**
	 * Ó¦ÓÃjni³õÊ¼»¯µÄÈë¿Ú
	 * @param context
	 */
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
	@SuppressLint({ "HandlerLeak", "HandlerLeak", "HandlerLeak" })
	/**
	 *	µ¯³öÊÇ·ñroot³É¹¦µÄ¶Ô»°¿òÌáÊ¾
	 */
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
		Alerthandle  = new Handler()
		{
			@SuppressWarnings("deprecation")
			public void handleMessage(Message msg)
			{
				//Log.d("JnsEnvInit", "alertDialogEnable="+alertDialogEnable);
				switch(msg.what)
				{
				case JnsIMECoreService.ROOT_SUCCESE:
					//	Toast.makeText(JnsIMECoreService.this, "Root Success", Toast.LENGTH_LONG).show();
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
	@SuppressWarnings("deprecation")
	/**
	 * ÓÃÓÚ½«Ó¦ÓÃÍ¼±êÏÔÊ¾ÔÚ×´Ì¬À¸
	 * 
	 * @param info
	 */
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
	private void initData()
	{
		SharedPreferences sp = this.getApplicationContext(). getSharedPreferences("init", Context.MODE_PRIVATE); 
		SharedPreferences.Editor  edit = sp.edit();
		SharedPreferences versionsp = this.getApplicationContext(). getSharedPreferences("init", Context.MODE_PRIVATE); 
		SharedPreferences.Editor  versionedit = sp.edit();
		PackageManager packageManager = getPackageManager();
		int cVersionNum = 0;
		int Version = versionsp.getInt("version", 0);
		try 
		{
			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			cVersionNum = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int i = sp.getInt("boolean", 0);
		if(i == 0)
		{
			if(CopyDatabase())
			{
				edit.putInt("boolean", 1);
				edit.commit();
				CopyMappings();
				versionedit.putInt("version", cVersionNum);
				versionedit.commit();
			}
			else
			{
				Toast.makeText(this, "Init failed", Toast.LENGTH_SHORT).show();
			}
		}
		else if(Version < cVersionNum)
		{
			if(updataDatabase())
			{	
				versionedit.putInt("version", cVersionNum);
				versionedit.commit();
				Toast.makeText(this, this.getText(R.string.update_list), Toast.LENGTH_SHORT).show();
			}
			if(Version < 22)
			{
				JnsEnvInit.movingFile("/mnt/sdcard/jnsinput/app_icon/com.kaasa.gianasisters.icon.png", "com.kaasa.gianasisters.icon.png");
				if(Build.VERSION.SDK_INT > 17 && Version != 0)
				{	
					Dialog dialog = new AlertDialog.Builder(JnsIMECoreService.this).setMessage(JnsIMECoreService.this.getString(R.string.reboot_notice)).setNegativeButton(JnsIMECoreService.this.getString(R.string.i_get), null).create();
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
					dialog.show();
				}
			}
		}
	}
	@SuppressLint("SdCardPath")
	private boolean updataDatabase()
	{
		if(!JnsEnvInit.movingFile("/mnt/sdcard/jnsinput/_jns_ime","_jns_ime"))
		{	
			Toast.makeText(this, "Copy databases failed", Toast.LENGTH_SHORT).show();
			return false;
		}
		String filename = "/mnt/sdcard/jnsinput/_jns_ime";

		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(filename, null);
		SQLiteDatabase db = JnsIMECoreService.aph.dbh.getReadableDatabase();
		Cursor cursor= null;

		cursor = sqLiteDatabase.query("_jns_ime", null, null,
				null, null, null, "_description");
		cursor.moveToFirst();

		while(!cursor.isLast())
		{
			String name = cursor.getString(cursor.getColumnIndex("_name"));
			String selection[]  = {name};
			
			if(name.equals("com.madfingergames.deadtrigger"))
			{
				Cursor tmpB = db.query("_jns_ime", null, "_name=?", new String[]{"com.incross.deadtrigger.kr.samsungapps"}, null, null, null);
				if(tmpB.getCount() > 0)
				{
					db.delete("_jns_ime", "_name=?", new String[]{"com.incross.deadtrigger.kr.samsungapps"});
				}
				tmpB.close();
			}

			// ���ԭ���ݿ���Ϸ�б�
			Cursor tmpC = db.query("_jns_ime", null, "_name=?", selection, null, null, null);

			// �����ݿ��в�����µ���Ϸ����
			if(tmpC.getCount() == 0)
			{	
				ContentValues cv = new ContentValues();
				cv.put("_name", cursor.getString(cursor.getColumnIndex("_name")));
				cv.put("_description", cursor.getString(cursor.getColumnIndex("_description")));
				cv.put("_exists", true);
				try 
				{
					if(db.insert(DBHelper.TABLE, "", cv) < 0)
					{	
						Toast.makeText(this, "Init databases failed", Toast.LENGTH_SHORT).show();
						return false;
					}
					String apkname = cursor.getString(cursor.getColumnIndex("_name"));
					JnsEnvInit.movingFile(this.getFilesDir()+"/"+ apkname + ".keymap", apkname+ ".keymap") ;
					JnsEnvInit.movingFile("/mnt/sdcard/jnsinput/app_icon/"+ apkname + ".icon.png", apkname + ".icon.png");
					tmpC.close();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return false;
				}
			}
			cursor.moveToNext();
		}
		cursor.close();
		cursor = db.query("_jns_ime", null, null,
				null, null, null, "_description");
		if(JnsIMEGameListActivity.gameAdapter != null)
		{
			JnsIMEGameListActivity.gameAdapter.setCursor(cursor);
			JnsIMEGameListActivity.gameAdapter.notifyDataSetChanged();
		}
		return true;
	}
	@SuppressLint("SdCardPath")
	private void CopyMappings()
	{
		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/jnsinput/_jns_ime", null);
		Cursor cursor= null;

		cursor = sqLiteDatabase.query("_jns_ime", null, null,
				null, null, null, "_description");
		cursor.moveToFirst();
		while(!cursor.isLast())
			//for(int i = 0; i < mappingFiles.length; i++)
		{
			String apkname = cursor.getString(cursor.getColumnIndex("_name"));
			JnsEnvInit.movingFile(this.getFilesDir()+"/"+ apkname + ".keymap", apkname+ ".keymap") ;
			JnsEnvInit.movingFile("/mnt/sdcard/jnsinput/app_icon/"+ apkname + ".icon.png", apkname + ".icon.png");
			cursor.moveToNext();
		}
	}
	@SuppressLint("SdCardPath")
	private boolean CopyDatabase()
	{
		if(!JnsEnvInit.movingFile("/mnt/sdcard/jnsinput/_jns_ime","_jns_ime"))
		{	
			Toast.makeText(this, "Copy databases failed", Toast.LENGTH_SHORT).show();
			return false;
		}
		String filename = "/mnt/sdcard/jnsinput/_jns_ime";

		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(filename, null);
		Cursor cursor= null;

		cursor = sqLiteDatabase.query("_jns_ime", null, null,
				null, null, null, "_description");
		cursor.moveToFirst();

		while(!cursor.isLast())
		{
			SQLiteDatabase db = JnsIMECoreService.aph.dbh.getReadableDatabase();
			try
			{
				db.delete(DBHelper.TABLE, "_name=?", new String[] { cursor.getString(cursor.getColumnIndex("_name")) });
			}
			catch(Exception e)
			{

			}
			ContentValues cv = new ContentValues();
			cv.put("_name", cursor.getString(cursor.getColumnIndex("_name")));
			cv.put("_description", cursor.getString(cursor.getColumnIndex("_description")));
			cv.put("_exists", true);
			try {
				if(db.insert(DBHelper.TABLE, "", cv) < 0)
				{	
					Toast.makeText(this, "Init databases failed", Toast.LENGTH_SHORT).show();
					return false;
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
			cursor.moveToNext();
		}
		if(JnsIMEGameListActivity.gameAdapter != null)
		{
			JnsIMEGameListActivity.gameAdapter.setCursor(cursor);
			JnsIMEGameListActivity.gameAdapter.notifyDataSetChanged();
		}
		return true;
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
		initData();
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
