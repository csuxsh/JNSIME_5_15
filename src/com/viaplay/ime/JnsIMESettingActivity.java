package com.viaplay.ime;


import com.viaplay.ime.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.util.Log;

/**
 * Ö÷½çÃæTabActivityµÄSettingÒ³
 * 
 * @author Steven
 *
 */

public class JnsIMESettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener{
	public static final String TAG = "BlueoceanControllerActivity";
	Preference quit;
	Preference changeime;
	Preference help; 
	static CheckBoxPreference cp;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		quit = this.findPreference(this.getString(R.string.quit));
		changeime = this.findPreference(this.getString(R.string.changeime));
		help = this.findPreference(this.getString(R.string.help));
		cp = (CheckBoxPreference) this.findPreference(this.getString(R.string.floatViewS));
		quit.setOnPreferenceClickListener(this);
		changeime.setOnPreferenceClickListener(this);
		help.setOnPreferenceClickListener(this);
		cp.setOnPreferenceChangeListener(this);
		JnsIMECoreService.activitys.add(this);
		SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
		if(pre.getBoolean("floatViewS", false))
		{
			cp.setChecked(true);
		}
		else
			cp.setChecked(false);
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
			intent.setClass(this, com.viaplay.ime.JnsIMEHelpActivity.class);
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


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if(preference == cp)
		{	
			if(!cp.isChecked())
			{
				//cp.setSummary("true");
				cp.setChecked(true);
				SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
				Editor edit = pre.edit();
				edit.putBoolean("floatViewS", true);
				edit.commit();
				if(JnsIMEInputMethodService.floatingHandler != null)
				{
					Message msg = new Message();
					msg.what = 1;
					JnsIMEInputMethodService.floatingHandler.sendMessage(msg);
				}
			}
			else
			{
				cp.setChecked(false);
				SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
				Editor edit = pre.edit();
				edit.putBoolean("floatViewS", false);
				edit.commit();
				if(JnsIMEInputMethodService.floatingHandler != null)
				{
					Message msg = new Message();
					msg.what = 2;
					JnsIMEInputMethodService.floatingHandler.sendMessage(msg);
				}
			}
		}
		return false;
	}
}
