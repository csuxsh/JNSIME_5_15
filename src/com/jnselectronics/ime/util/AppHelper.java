package com.jnselectronics.ime.util;

import java.io.File;
import java.io.FileOutputStream;


import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;


public class AppHelper {

	public final DBHelper dbh ;
	private Context context;

	public AppHelper(Context context)
	{
		dbh  = DBHelper.getDBHelper(context);
		this.context = context;
	}

	synchronized public boolean Insert(String name, String exists)
	{
		PackageManager pm = context.getPackageManager();
		String lable = "";
		Bitmap icon = null;
		try {
			lable = (String) pm.getApplicationLabel(pm.getApplicationInfo(name, PackageManager.GET_UNINSTALLED_PACKAGES));
			icon = DrawableUtil.drawableToBitmap(pm.getApplicationIcon(name));
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		File icon_file = new File("/mnt/sdcard/jnsinput/app_icon/"+name+".icon.png");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(icon_file);
			icon.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		SQLiteDatabase db = dbh.getReadableDatabase();
		db.delete(DBHelper.TABLE, "_name=?", new String[] { name });
		ContentValues cv = new ContentValues();
		cv.put("_name", name);
		cv.put("_description", lable);
		cv.put("_exists", exists);
		try {
		//	delete(name);
			if(db.insert(DBHelper.TABLE, "", cv)> -1)
				return true;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	synchronized public boolean delete(String name)
	{
		SQLiteDatabase db = dbh.getReadableDatabase();
		File file = new File("mnt/sdcard/jnsinput/app_icon/"+name+".icon.png");
		if(file.exists())
			file.delete();
		if(db.delete(DBHelper.TABLE, "_name=?", new String[] { name }) >0)
			return false;
		return true;
	}
	synchronized public Cursor Qurey(String arg)
	{
		//String arg = startdate+" and "+enddate;
		String selection = null;


		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.query(DBHelper.TABLE, null, selection,
					null, null, null, "_description");
			if(cursor.moveToFirst())
			{
				System.out.println("cuisor has content");
			}
			else
			{
				System.out.println("cuisor has none");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return cursor;
	}
}
