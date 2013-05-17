package com.jnselectronics.ime;

import java.util.ArrayList;
import java.util.List;

import com.jnselectronics.ime.jni.InputAdapter;


import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class JnsIMEControllerActivity  extends Activity{
	public static final String TAG = "BlueoceanControllerActivity";
	private List<String> controllerlist = new ArrayList<String>();
	private List<String> data = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_controller);
		final ListView list = (ListView) this.findViewById(R.id.listView1);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, 
				data);
		list.setAdapter(adapter);
		final Handler hander = new Handler()
		{
			@SuppressLint("HandlerLeak")
			public void handleMessage(Message msg)
			{
				adapter.clear();
				for(int i = 0; i< controllerlist.size(); i++)
				{
					if(data.contains(controllerlist.get(i)))
						continue;
					adapter.add(controllerlist.get(i));
				}
				adapter.notifyDataSetChanged();
			}
		};
		new Thread(new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub	
				while(true)
				{	
					controllerlist  = InputAdapter.getDeviceList();
					hander.sendMessage(new Message());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
		JnsIMECoreService.activitys.add(this);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e(TAG, " onkeydown keycode = " + keyCode + " scancode = " + event.getScanCode());
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		JnsIMECoreService.activitys.remove(this);
	}
}
