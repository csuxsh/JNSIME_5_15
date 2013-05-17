package com.jnselectronics.ime;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.util.Log;



public class JnsIMESettingActivity extends PreferenceActivity implements OnPreferenceClickListener{
	public static final String TAG = "BlueoceanControllerActivity";
	Preference quit;
	Preference changeime;
	Preference help;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		quit = this.findPreference(this.getString(R.string.quit));
		changeime = this.findPreference(this.getString(R.string.changeime));
		help = this.findPreference(this.getString(R.string.help));
		quit.setOnPreferenceClickListener(this);
		changeime.setOnPreferenceClickListener(this);
		help.setOnPreferenceClickListener(this);
		JnsIMECoreService.activitys.add(this);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e(TAG, "  onkeydown keycode = " + keyCode + " scancode = " + event.getScanCode());
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPreferenceClick(Preference arg0) {
		// TODO Auto-generated method stub
		if (arg0.getKey().equals(quit.getKey())) {
			this.finish();
		}
		if(arg0.getKey().equals(changeime.getKey()))
		{
			Intent intent = new Intent();
			intent.setAction("android.settings.SHOW_INPUT_METHOD_PICKER");
			this.sendBroadcast(intent);
		}
		if(arg0.getKey().equals(help.getKey()))
		{
			Intent intent = new Intent();
			intent.setClass(this, com.jnselectronics.ime.JnsIMEHelpActivity.class);
			this.startActivity(intent);
		}
		return false;
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		JnsIMECoreService.activitys.remove(this);
	}
}
