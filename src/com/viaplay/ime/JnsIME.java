package com.viaplay.ime;

import java.io.File;

import com.viaplay.ime.R;
import com.viaplay.ime.util.AppHelper;
import com.viaplay.ime.util.DBHelper;
import com.viaplay.ime.util.JnsEnvInit;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ó|ó??÷????,μúò?′???DDê±oò??ê?settingò3￡?????ê±oò??ê?ó??·áD±í
 * 
 * @author Steveb
 *
 */
@SuppressWarnings("deprecation")
public class JnsIME extends TabActivity {
	
	private TabHost mTabHost;
	private LinearLayout ll; 
	private TabWidget tw;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        createTab();
		JnsIMECoreService.activitys.add(this);
		createTmpDir();
    	initData();
    }
    
    private void createTab() {
    	mTabHost = getTabHost();
    	ll = (LinearLayout)mTabHost.getChildAt(0);
    	tw = (TabWidget)ll.getChildAt(0);

    	RelativeLayout tabIndicator1 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab = (TextView)tabIndicator1.findViewById(R.id.title);
    	tvTab.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.gamelist_title), null, null);
    	/*
    	RelativeLayout tabIndicator2 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab1 = (TextView)tabIndicator2.findViewById(R.id.title);
    	tvTab1.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.controller_title), null, null);
 		*/
    	RelativeLayout tabIndicator3 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab2 = (TextView)tabIndicator3.findViewById(R.id.title);
    	tvTab2.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.settings_title), null, null);
  	
    	
    	Intent gameIntent = new Intent();
    	gameIntent.setClass(this, JnsIMEGameListActivity.class);
    	TabHost.TabSpec gameSpec = mTabHost.newTabSpec("GameList").setIndicator(tabIndicator1).setContent(gameIntent);
    	mTabHost.addTab(gameSpec);
    	/*
    	Intent controlIntent = new Intent();
    	controlIntent.setClass(this, JnsIMEControllerActivity.class);
    	TabHost.TabSpec controlSpec = mTabHost.newTabSpec("control").setIndicator(tabIndicator2).setContent(controlIntent);
    	mTabHost.addTab(controlSpec);
    	*/
    	Intent settingsIntent = new Intent();
    	settingsIntent.setClass(this, JnsIMESettingActivity.class);
    	TabHost.TabSpec settingsSpec = mTabHost.newTabSpec("setting").setIndicator(tabIndicator3).setContent(settingsIntent);
    	mTabHost.addTab(settingsSpec);
    	SharedPreferences perfer = this.getSharedPreferences("guide", Activity.MODE_PRIVATE);
    	boolean guide = perfer.getBoolean("guide", true);
    	if(guide)
    	{
    		mTabHost.setCurrentTab(2);
    		SharedPreferences.Editor  edit = perfer.edit();
    		edit.putBoolean("guide", false);
    		/*
    		Intent intent = new Intent();
			intent.setAction("android.settings.SHOW_INPUT_METHOD_PICKER");
			edit.commit();
			this.sendBroadcast(intent);
			*/
    		//Intent intent = new Intent();
    		edit.commit();
    		//intent.setClass(this, com.viaplay.imeC.JnsIMEHelpActivity.class);
			//this.startActivity(intent);
    	}
    	else
    		mTabHost.setCurrentTab(0); 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
	public void onDestroy()
	{
		super.onDestroy();
		JnsIMECoreService.activitys.remove(this);
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
    @SuppressLint("SdCardPath")
    /**
     *  初始化数据库以及运行环境
     */
	private void initData()
	{
    	JnsEnvInit.mContext = this;
    	if(JnsIMECoreService.aph == null)
    		JnsIMECoreService.aph = new AppHelper(this);
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
					Dialog dialog = new AlertDialog.Builder(this).setMessage(this.getString(R.string.reboot_notice)).setNegativeButton(this.getString(R.string.i_get), null).create();
					dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  

					WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();    
					WindowManager wm = (WindowManager)this   
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

			// 获得原数据库游戏列表
			Cursor tmpC = db.query("_jns_ime", null, "_name=?", selection, null, null, null);

			// 向数据库中插入更新的游戏内容
			if(tmpC.getCount() == 0)
			{	
				ContentValues cv = new ContentValues();
				PackageManager pm = this.getPackageManager();
				cv.put("_name", cursor.getString(cursor.getColumnIndex("_name")));
				cv.put("_description", cursor.getString(cursor.getColumnIndex("_description")));
				try { 
					pm.getApplicationLabel(pm.getApplicationInfo(cv.getAsString("_name"), PackageManager.GET_UNINSTALLED_PACKAGES));
					cv.put("_exists", "true");
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					cv.put("_exists", "false");
				}
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
			tmpC.close();
			cursor.moveToNext();
		}
		cursor.close();
		cursor = db.query("_jns_ime", null, null,
				null, null, null, "_exists desc,_description");
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
			PackageManager pm = this.getPackageManager();
			cv.put("_name", cursor.getString(cursor.getColumnIndex("_name")));
			cv.put("_description", cursor.getString(cursor.getColumnIndex("_description")));
			try { 
				pm.getApplicationLabel(pm.getApplicationInfo(cv.getAsString("_name"), PackageManager.GET_UNINSTALLED_PACKAGES));
				cv.put("_exists", "true");
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				cv.put("_exists", "false");
			}
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
}
