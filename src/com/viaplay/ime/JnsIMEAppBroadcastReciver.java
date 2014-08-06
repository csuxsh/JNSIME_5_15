package com.viaplay.ime;

import com.viaplay.ime.util.AppHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class JnsIMEAppBroadcastReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String packageName = intent.getDataString().substring(8);
		String action = intent.getAction();
		if(JnsIMECoreService.aph == null)
			JnsIMECoreService.aph = new AppHelper(context);
		if(action.equals(Intent.ACTION_PACKAGE_ADDED))
		{
			if(JnsIMECoreService.aph.Qurey(packageName).getCount() > 0)
				JnsIMECoreService.aph.updateExit(packageName, "true");
		}
		if(action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED))
		{	
			if(JnsIMECoreService.aph.Qurey(packageName).getCount() > 0)
				JnsIMECoreService.aph.updateExit(packageName, "false");
		}
	}

}
