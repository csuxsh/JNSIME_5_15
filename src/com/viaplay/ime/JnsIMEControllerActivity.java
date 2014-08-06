package com.viaplay.ime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.viaplay.ime.R;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * ÏÔÊ¾µ±Ç°ÒÑ¾­Á¬½ÓµÄ²Ù¿ØÆ÷
 * 
 * @author Steven
 *
 */
public class JnsIMEControllerActivity  extends Activity{
	
	private List<String> controllerlist = new ArrayList<String>();
	private List<String> data = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	private final String deviceID[] =
		{
			"0x05ac3212", // Bordcaom BT
			"0x05ac0220",
			"0xa7253324"  //Bordcaom 2.4G
		};
	private final String deviceName[] =
		{
			"JNS JNS 2.4G Wireless Device",
			"SmartGamePad1234",
			"Callstel Gaming-Controller",
			"2.4G  Wireless  ProV2.0 2.4G  Wireless  ProV2.0",
			"BT HID"
		};

	static class DeviceHandler extends Handler
	{
		WeakReference<JnsIMEControllerActivity> mActivity;
		   
		DeviceHandler(JnsIMEControllerActivity context) {
                mActivity = new WeakReference<JnsIMEControllerActivity>(context);
        }

        @Override
        public void handleMessage(Message msg) {
        
          }
        
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_controller);
		final ListView list = (ListView) this.findViewById(R.id.listView1);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, 
				data);
		list.setAdapter(adapter);
		final Handler hander = new DeviceHandler(this)
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
					//controllerlist  = InputAdapter.getDeviceList();
					asixProc();
					hander.sendMessage(new Message());
					try {
						Thread.sleep(2000);
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
	public void onDestroy()
	{
		super.onDestroy();
		JnsIMECoreService.activitys.remove(this);
	}
	boolean deviceFilter(String vid, String pid, String name)
	{
		//int id = ((Integer.parseInt(vid, 16)) << 16) | ((Integer.parseInt(pid, 16)));
		String id = "0x" + vid + pid;
		int i = 0;
		for(; i < deviceID.length; i++)
		{
			if(id.equalsIgnoreCase(deviceID[i]))
				return true;
		}
		if(i == deviceID.length)
		{
			for(; i < deviceName.length; i++)
			{
				if(name.equals("\""+deviceName[i]+"\""))
					return true;
			}
		}
		return false;
	}
	private boolean asixProc()
	{
		try {
			FileInputStream is = new FileInputStream("/proc/bus/input/devices");
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			String vid = null;
			String pid = null;
			String name = null;
			controllerlist.clear();
			while(line != null)
			{	
				if(line.startsWith("I"))
				{
					vid = "";
					pid = "";
					name = "";
					String[] info =  line.split(" ");
					for(int i = 0; i < info.length; i++)
					{
						if(info[i].startsWith("Vendor"))
							vid = (info[i].split("="))[1];
						else if(info[i].startsWith("Product"))	
							pid = (info[i].split("=")[1]);
					}
				}
				else if(line.startsWith("N"))
				{	
					String[] info =  line.split(" ");
					int i  = 0;
					for(; i < info.length; i++)
					{
						if(info[i].startsWith("Name"))
						 name = (info[i].split("=")[1]);
						if(i > 1)
							name = name+" "+info[i];
					}
					if(deviceFilter(vid, pid, name))
						controllerlist.add(name);
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			if(controllerlist.isEmpty())
				controllerlist.add("read /proc/input/devices error!");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(controllerlist.isEmpty())
				controllerlist.add("read /proc/input/devices error!");
			e.printStackTrace();
		}
		return false;
	}
}
