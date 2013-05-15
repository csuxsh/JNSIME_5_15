package com.jnselectronics.ime.uiadapter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.jnselectronics.im.hardware.JoyStickTypeF;
import com.jnselectronics.ime.R;
import com.jnselectronics.ime.bean.JnsIMEKeyMap;
import com.jnselectronics.ime.bean.JnsIMETypeFKeyMap;
import com.jnselectronics.ime.util.DrawableUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

public class JnsIMEKeyMapView extends ImageView {


	public  static final int JoyStickTypeFID = 1;
	private int ScreeanWidth;
	private int ScreeanHeight;
	private int buttonWidth;
	private int buttonHeight;
	private Activity activity;
	private Bitmap edit_n;
	private Bitmap edit_i;
	private int startX=0;
	private int startY=0;
	private int hardwareId;
	@SuppressWarnings("unused")
	private static final String TAG="JnsKeyMapView";
	private int touchedRow = -1;
	private int touchedCol = -1;

	public   int gamePadButoonIndex[][];
	public  String gamePadButoonLable[][];
	public   int diplayRow = 0;
	public   int diplayCol = 0;

	public void setHardWare(int hardwareId)
	{
		switch(hardwareId)
		{
		case JoyStickTypeFID:
			this.diplayRow = JoyStickTypeF.DISPLAY_ROW;
			this.diplayCol = JoyStickTypeF.DISPLAY_COL;
			gamePadButoonIndex = new int[diplayCol][diplayRow];
			gamePadButoonLable = new String[diplayCol][diplayRow];
			copyArray(JoyStickTypeF.gamePadButoonLable, JoyStickTypeF.gamePadButoonIndex);
			this.hardwareId = hardwareId;
			break;
		}
	}
	private void copyArray(String[][] lable, int[][] index)
	{
		for(int i = 0; i< diplayCol; i++)
			for(int j = 0; j< diplayRow; j++)
			{
				gamePadButoonIndex[i][j] = index[i][j];
				gamePadButoonLable[i][j] = lable[i][j];
			}
	}
	public int getHardwareId() {
		return hardwareId;
	}
	public JnsIMEKeyMap getJnsIMEKeyMap()
	{
		switch(getHardwareId())
		{
			case JnsIMEKeyMapView.JoyStickTypeFID:
				return  new JnsIMETypeFKeyMap();
				
		}
		return null;
	}
	public JnsIMEKeyMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		activity = (Activity) context;
		getButtonSize();
		loadRes();
	}
	public JnsIMEKeyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		activity = (Activity) context;
		getButtonSize();
		loadRes();
	}
	
	public void setLableDisplay(Map<String, Integer> map)
	{
		Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator();
		for(int i = 0; i <diplayCol; i++)
			for(int j = 0; j < diplayRow; j++)
			{
				if(gamePadButoonLable[i][j] != null)
					gamePadButoonLable[i][j] = "";
			}
		while(iterator.hasNext())
		{
			Entry<String, Integer> key = iterator.next(); 
			if(gamePadButoonLable[key.getValue()/diplayRow][key.getValue()%diplayRow] != null)
			gamePadButoonLable[key.getValue()/diplayRow][key.getValue()%diplayRow] = key.getKey();
		}
	}
	private void getButtonSize()
	{
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		ScreeanWidth = dm.widthPixels;
		ScreeanHeight = dm.heightPixels;
		buttonWidth = ScreeanWidth * 19 / 20 /16;
		buttonHeight = ScreeanWidth / 32;
		startY=  ScreeanHeight/30;  //* 8/7;
		startX = ScreeanWidth / 34;
	}
	private void loadRes()
	{
		InputStream is;
		BitmapFactory.Options options=new BitmapFactory.Options(); 
		options.inJustDecodeBounds = false; 
		options.inSampleSize = 1;   
		is = activity.getResources().openRawResource(R.drawable.key_edit_n);
		edit_n = BitmapFactory.decodeStream(is,null,options);
		edit_n =DrawableUtil.zoomBitmap(edit_n, buttonWidth,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.key_edit_i);
		edit_i = BitmapFactory.decodeStream(is,null,options);
		edit_i =DrawableUtil.zoomBitmap(edit_i, buttonWidth,buttonHeight);

	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(ScreeanWidth, startY + buttonHeight * 8);
	}
	public JnsIMEKeyMap findTouchedEdit(int x, int y, int action)
	{
		JnsIMEKeyMap keymap = null;
		int location[] = new int[2];
		this.getLocationOnScreen(location);
		String lable=null;
		if(x - location[0] - startX< 0 || y - location[1] - startY< 0)
			return null;
		if(action == MotionEvent.ACTION_DOWN)
		{	
			touchedRow = (x - location[0] - startX)/buttonWidth ;
			touchedCol = (y - location[1] - startY)/buttonHeight ;
			if(touchedRow > (diplayRow -1 ) || touchedCol > (diplayCol -1))
			{
				touchedRow = -1;
				touchedCol = -1;
				return null;
			}
			lable = gamePadButoonLable[touchedCol][touchedRow];
			if(lable == null)
			{
				touchedRow = -1;
				touchedCol = -1;
				return null;
			}
			keymap = new JnsIMETypeFKeyMap();
			keymap.setLable(lable);
			keymap.setGamPadIndex(gamePadButoonIndex[touchedCol][touchedRow]);
			postInvalidate();
		}
		return keymap;
	}

	@Override
	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		Paint p = new Paint();
		//	p.setColor(Color.WHITE);
		for(int i=0; i< diplayCol; i++)
		{	
			for(int j = 0; j < diplayRow; j++)
			{
				if(!(gamePadButoonLable[i][j]==null))
				{	
					if( touchedRow == j && touchedCol == i)
					{	
						c.drawBitmap(edit_i, j * buttonWidth +startX, i* buttonHeight + startY, p);
						p.setColor(Color.BLACK);
					}
					else
					{	
						c.drawBitmap(edit_n, j * buttonWidth + startX, i* buttonHeight + startY, p);
						p.setColor(Color.WHITE);
					}	
					p.setTextSize(10);
					int textWidth = (int) p.measureText(gamePadButoonLable[i][j]);
					int textHeight =(int) p.getTextSize();
					c.drawText(gamePadButoonLable[i][j], j * buttonWidth + startX +(buttonWidth-textWidth)/2, 
							i * buttonHeight + startY + (buttonHeight+textHeight)/2, p);

				}

			}
		}
	}
}
