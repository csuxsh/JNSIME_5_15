package com.jnselectronics.ime.uiadapter;

import java.io.File;

import com.jnselectronics.ime.JnsIMECoreService;
import com.jnselectronics.ime.JnsIMEGameListActivity;
import com.jnselectronics.ime.JnsIMEKeyMappingActivity;
import com.jnselectronics.ime.R;
import com.jnselectronics.ime.util.GameInfo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class JnsIMEGameListAdapter extends BaseAdapter implements OnClickListener{

	final static String TAG = "JNS_GAME_ADAPTER";
	private Activity activity;
	private LayoutInflater inflater;
	private Cursor cursor;
	private ImageView icon;
	private TextView title;

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	private ImageButton get;
	private ImageButton play;
	private ImageButton keymapping;
	private ImageButton delete;
	private PackageManager pm;

	public JnsIMEGameListAdapter(Cursor cursor, Activity activity)
	{
		this.cursor = cursor;
		this.activity = activity;
		inflater = activity.getLayoutInflater();
		pm = activity.getPackageManager();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cursor.getCount();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		 cursor.moveToPosition(arg0);
		 return cursor;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressLint("UseValueOf")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.game_list_item, parent, false);
		}
		if(!cursor.moveToPosition(position))
			Log.d(TAG, "cursor erro at postion"+position);
		GameInfo game = new GameInfo();
		game.setPkgname(cursor.getString(cursor.getColumnIndex("_name")));
		game.setExists(new Boolean(cursor.getString(cursor.getColumnIndex("_exists"))));
		game.setLable(cursor.getString(cursor.getColumnIndex("_description")));
		Drawable icon_pic = null;
		String lable= "";
		try {
			icon_pic = pm.getApplicationIcon(game.getPkgname());
			lable =  (String) pm.getApplicationLabel(pm.getApplicationInfo(game.getPkgname(), PackageManager.GET_UNINSTALLED_PACKAGES));
			game.setExists(true);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			icon_pic = Drawable.createFromPath("mnt/sdcard/jnsinput/app_icon/"+game.getPkgname()+".icon.png");
			lable = cursor.getString(cursor.getColumnIndex("_description"));
			game.setExists(false);
			e.printStackTrace();
		}

		icon = (ImageView) convertView.findViewById(R.id.img);
		title = (TextView) convertView.findViewById(R.id.title);
		get = (ImageButton) convertView.findViewById(R.id.get);
		play = (ImageButton) convertView.findViewById(R.id.play);
		keymapping = (ImageButton) convertView.findViewById(R.id.keymapping);
		delete = (ImageButton) convertView.findViewById(R.id.delete);
		Log.d(TAG, cursor.getString(cursor.getColumnIndex("_exists")));
		get.setOnClickListener(this);
		keymapping.setOnClickListener(this);
		delete.setOnClickListener(this);
		play.setOnClickListener(this);
		
		if(game.isExists())
		{	
			get.setBackgroundResource(R.drawable.get_p);
			play.setImageResource(R.drawable.play);
			keymapping.setBackgroundResource(R.drawable.game_mapping);
			get.setOnClickListener(null);
		}
		else
		{
			get.setBackgroundResource(R.drawable.game_get);
			play.setImageResource(R.drawable.game_play_p);
			keymapping.setBackgroundResource(R.drawable.game_mapping_p);
			keymapping.setOnClickListener(null);
			play.setOnClickListener(null);
		}
		icon.setImageDrawable(icon_pic);
		title.setText(lable);

		icon.setTag(game.getPkgname());
		title.setTag(game.getPkgname());
		get.setTag(game.getPkgname());
		keymapping.setTag(game.getPkgname());
		delete.setTag(game.getPkgname());
		play.setTag(game.getPkgname());

		return convertView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		PackageManager pm = activity.getPackageManager();

		switch(v.getId())
		{
		case R.id.get:
			Log.d(TAG, "click get of "+ v.getTag());
			Uri uri = Uri.parse("market://details?id="+v.getTag());  
			Intent it = new Intent(Intent.ACTION_VIEW, uri);   
			activity.startActivity(it);   
			break;
		case R.id.keymapping:
			Log.d(TAG, "click keymapping of "+ v.getTag());
			String lable="";
			try {
				lable = (String) pm.getApplicationLabel(pm.getApplicationInfo(""+v.getTag(), PackageManager.GET_UNINSTALLED_PACKAGES));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent mapin = new Intent();
			mapin.setClass(activity, JnsIMEKeyMappingActivity.class);
			mapin.putExtra("filename",  v.getTag()+".keymap");
			mapin.putExtra("lable", lable);
			activity.startActivity(mapin);
			break;
		case R.id.delete:
			Log.d(TAG, "delte keymapping of "+ v.getTag());
			JnsIMECoreService.aph.delete(""+v.getTag());
			File touchmapping = new File(activity.getFilesDir()+"/"+v.getTag());
			touchmapping.delete();
			File keymapping = new File(activity.getFilesDir()+"/"+v.getTag()+".keymap");
			keymapping.delete();
			Cursor cursor = JnsIMECoreService.aph.Qurey(null);
			JnsIMEGameListActivity.gameAdapter.setCursor(cursor);
			JnsIMEGameListActivity.gameAdapter.notifyDataSetChanged();
			break;
		case R.id.play:
			Intent in = new Intent(pm.getLaunchIntentForPackage(""+v.getTag()));
			activity.startActivity(in);
			break;
		}
	}

}
