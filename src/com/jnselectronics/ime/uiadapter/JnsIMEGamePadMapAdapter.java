package com.jnselectronics.ime.uiadapter;

import com.jnselectronics.ime.R;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class JnsIMEGamePadMapAdapter extends BaseAdapter {
	
	public String gamePadButtonText[] = 
	{
		"",
		"L1",
		"Select",
		"Start",
		"R1",
		"L2",
		"R2",
		"Up",
		"Y",
		"Left",
		"Right",
		"X",
		"B",
		"Down",
		"A",
		"Y+",
		"RZ+",
		"X-",
		"X+",
		"Z-",
		"Z+",
		"Y-",
		"RZ+"	
	};
	
	public final static int gamePadButoonIndex[] = 
	{
		0,  0,  0,  1,  0,  0,  0,  2,  3,  0,  0,  0,  4, 0,  0,  0,
		0,  0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  6, 0,  0,
		0,  7,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8, 0,
		9,  0, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  11, 0, 12,
		0,  13, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  14,  0,
		0,  15, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  16,  0,
		17,  0 ,18,0,  0,  0,  0,  0,  0,  0,  0,  0,   0,  19, 0,  20,
		0,  21,0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,   22,0,
	};
	LayoutInflater inflater;
	private Activity activity;                                    
	public JnsIMEGamePadMapAdapter(Activity activity)
	{
		inflater = activity.getLayoutInflater();
		activity.getWindow().getDecorView().getWidth();
		this.activity = activity;
	}
	                                   

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return gamePadButoonIndex.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return gamePadButoonIndex[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		EditText et;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.key_edit_item, parent, false);
		}
		et = (EditText) convertView;
		//et.setWidth(this.screenWidth/DIS_BILI);
		//et.setHeight(et.getHeight() /2 );
		if(gamePadButoonIndex[arg0] == 0)
		{	
			et.setVisibility(View.GONE);
			return et;
		}
		et.setBackgroundResource(R.drawable.keyeditbg);
		et.setText(""+gamePadButtonText[gamePadButoonIndex[arg0]]);
		
		InputMethodManager imm =(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE); 
	    imm.hideSoftInputFromWindow(et.getWindowToken(), 0); 
		et.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
	    return et;
	}

}
