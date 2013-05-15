package com.jnselectronics.ime;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class JnsIMESystemBootBroadcast extends BroadcastReceiver {
	

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
		startJnsIMECoreService(arg0, arg1);
	}
	private void startJnsIMECoreService(Context arg0, Intent arg1) {
		Intent intent = new Intent("com.jnselectronics.ime.JnsIMECore");
		arg0.startService(intent);
	}
	
	@SuppressWarnings("unused")
	private void startBlueoceanIMEActivity(Context arg0, Intent arg1) {
		Intent intent = new Intent(arg0, JnsIME.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putBoolean("start_mode", true);
		intent.putExtras(bundle);
		arg0.startActivity(intent);
	}
	
	
}
