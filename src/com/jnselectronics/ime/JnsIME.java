package com.jnselectronics.ime;

import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;


@SuppressWarnings("deprecation")
public class JnsIME extends TabActivity {
	
	private TabHost mTabHost;
	private LinearLayout ll; 
	private TabWidget tw;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createTab();
        if(JnsIMECoreService.initialed)
        	return;
        Intent intent = new Intent("com.jnselectronics.ime.JnsIMECore");
		this.startService(intent);
		JnsIMECoreService.activitys.add(this);
    }
    
    private void createTab() {
    	mTabHost = getTabHost();
    	ll = (LinearLayout)mTabHost.getChildAt(0);
    	tw = (TabWidget)ll.getChildAt(0);

    	RelativeLayout tabIndicator1 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab = (TextView)tabIndicator1.findViewById(R.id.title);
    	tvTab.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.gamelist_title), null, null);
    
    	RelativeLayout tabIndicator2 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab1 = (TextView)tabIndicator2.findViewById(R.id.title);
    	tvTab1.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.controller_title), null, null);
 	
    	RelativeLayout tabIndicator3 = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tab_indicator, tw, false);
    	TextView tvTab2 = (TextView)tabIndicator3.findViewById(R.id.title);
    	tvTab2.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.settings_title), null, null);
  	
    	
    	Intent gameIntent = new Intent();
    	gameIntent.setClass(this, JnsIMEGameListActivity.class);
    	TabHost.TabSpec gameSpec = mTabHost.newTabSpec("GameList").setIndicator(tabIndicator1).setContent(gameIntent);
    	mTabHost.addTab(gameSpec);
    	
    	Intent controlIntent = new Intent();
    	controlIntent.setClass(this, JnsIMEControllerActivity.class);
    	TabHost.TabSpec controlSpec = mTabHost.newTabSpec("control").setIndicator(tabIndicator2).setContent(controlIntent);
    	mTabHost.addTab(controlSpec);
    	
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
    		Intent intent = new Intent();
			intent.setAction("android.settings.SHOW_INPUT_METHOD_PICKER");
			edit.commit();
			this.sendBroadcast(intent);
			
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
}
