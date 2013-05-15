package com.jnselectronics.ime.uiadapter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jnselectronics.ime.R;
import com.jnselectronics.ime.bean.KeyBoard;
import com.jnselectronics.ime.util.DrawableUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

public class JnsIMEKeyboardView extends ImageView {

	private final static String TAG = "JnsIMEKeyboardView";
	private int buttonWidth = 0;
	private int buttonHeight = 0;
	private int ScreeanWidth = 0;
	private int ScreeanHeight = 0;
	private  static Bitmap button_n;
	private  static Bitmap button_p;
	private  static Bitmap buttonLong_n;
	private  static Bitmap buttonLong_p;
	private  static Bitmap button123;
	private  static Bitmap buttonABC;
	private  static Bitmap buttonSpace_n;
	private  static Bitmap buttonSpace_p;
	private int startX = 0;
	private int startY= 0;
	private  Activity activity;
	private  KeyBoard keyBoard;
	private static final int KEYBOARD_ROW = 10;
	private static final int KEYBOARD_COL = 4;
	private boolean keypressed = false;
	private int pressed_row = 0;
	private int pressed_col = 0;

	private  String keyboardMetric[][] = new String[KEYBOARD_COL][KEYBOARD_ROW];

	public JnsIMEKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public JnsIMEKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Button  button = new Button(context);
		button.setText("test");
		setWillNotDraw(false);
		activity = (Activity) context;
		getButtonSize();
		keyboardLoadRes();
		int location[] = new int[2];
		this.getLocationOnScreen(location);
		startX = 0;//ScreeanWidth / 22;
		startY = buttonHeight /4;//ScreeanHeight - ScreeanWidth / 22 *4;
	}
	private void getButtonSize()
	{
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		ScreeanWidth = dm.widthPixels;
		ScreeanHeight = dm.heightPixels;
		buttonWidth = ScreeanWidth /10;
		buttonHeight=  (ScreeanHeight - ScreeanHeight/10  * 8/7 -  ScreeanWidth / 32 * 8) / 5;// buttonWidth/2; 
		if(buttonHeight > buttonWidth/2)
			buttonHeight=buttonWidth/2;
	}
	private void keyboardLoadRes()
	{
		InputStream is;
		BitmapFactory.Options options=new BitmapFactory.Options(); 
		options.inJustDecodeBounds = false; 
		options.inSampleSize = 1;   
		is = activity.getResources().openRawResource(R.drawable.keyboard_button_n);
		button_n = BitmapFactory.decodeStream(is,null,options);
		button_n =DrawableUtil.zoomBitmap(button_n, buttonWidth,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.keyboard_button_p);
		button_p = BitmapFactory.decodeStream(is,null,options);
		button_p =DrawableUtil.zoomBitmap(button_p, buttonWidth,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.keyboard_button_p);
		buttonLong_p = BitmapFactory.decodeStream(is,null,options);
		buttonLong_p =DrawableUtil.zoomBitmap(buttonLong_p, buttonWidth *2 ,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.keyboard_button_n);
		buttonLong_n = BitmapFactory.decodeStream(is,null,options);
		buttonLong_n =DrawableUtil.zoomBitmap(buttonLong_n,buttonWidth * 2,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.key_button_123);
		button123 = BitmapFactory.decodeStream(is,null,options);
		button123 =DrawableUtil.zoomBitmap(button123, buttonWidth,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.key_button_abc);
		buttonABC = BitmapFactory.decodeStream(is,null,options);
		buttonABC =DrawableUtil.zoomBitmap(buttonABC, buttonWidth,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.keyboard_button_n);
		buttonSpace_n = BitmapFactory.decodeStream(is,null,options);
		buttonSpace_n =DrawableUtil.zoomBitmap(buttonSpace_n, buttonWidth *4 ,buttonHeight);

		is = activity.getResources().openRawResource(R.drawable.keyboard_button_p);
		buttonSpace_p = BitmapFactory.decodeStream(is,null,options);
		buttonSpace_p =DrawableUtil.zoomBitmap(buttonSpace_p, buttonWidth *4 ,buttonHeight);
	}

	public void setKeyBoard(KeyBoard keyboard)
	{
		keyBoard = keyboard;
		keyboardMetric = keyboard.getKeyboardLayout();
	}
	@Override
	public void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		Log.d(TAG, "onDetachedFromWindow be called");
	}
	public Entry<String, Integer>  findKeyCode(int x, int y, int action)
	{
		int location[] = new int[2];
		this.getLocationOnScreen(location);
		Log.d(TAG, "x="+location[0]+"y="+location[1]);
		if(x - location[0] < 0 || y - location[1] < 0)
			return null;
		if(action == MotionEvent.ACTION_DOWN)
		{	
			pressed_row = (x - location[0] - startX)/buttonWidth ;
			pressed_col = (y - location[1] - startY)/buttonHeight ;
			if(pressed_row > (KEYBOARD_ROW -1)|| pressed_col > (KEYBOARD_COL-1))
			{
				pressed_row = -1;
				pressed_col = -1;
				keypressed = false;
	    		return null;
			}
				
		    Integer keycode = keyBoard.getKeyCode(this.keyboardMetric[pressed_col][pressed_row]);
		    if(keyboardMetric[pressed_col][pressed_row].equals("123")||
					keyboardMetric[pressed_col][pressed_row].equals("abc"))
		    {
		    	keypressed = true;
				postInvalidate();
				Map<String, Integer> map = new HashMap<String, Integer>();
				map.put(this.keyboardMetric[pressed_col][pressed_row], keycode);
				return map.entrySet().iterator().next();
		    }
		    while((keycode == null))
		    {
		    	if(pressed_row == 0)
		    	{
		    		pressed_row = -1;
					pressed_col = -1;
					keypressed = false;
		    		return null;
		    	}
		    	pressed_row--;
		    	keycode = keyBoard.getKeyCode(this.keyboardMetric[pressed_col][pressed_row]);
		    	if(this.keyboardMetric[pressed_col][pressed_row].equals("Space") ||
		    			this.keyboardMetric[pressed_col][pressed_row].equals("BackSpace") )
		    		keypressed = true;
		    	else
		    		keycode = null;
		    }
	    	keypressed = true;
			postInvalidate();
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(this.keyboardMetric[pressed_col][pressed_row], keycode);
			return map.entrySet().iterator().next();
		     
		}
		if(action == MotionEvent.ACTION_UP)
		{
			pressed_row = (x - location[0])/buttonWidth ;
			pressed_col = (y - location[1])/buttonHeight ;
			keypressed = false;
			postInvalidate();
		}
		return null;
		
	}
	
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(ScreeanWidth , buttonHeight * 4  + buttonHeight /4);
	}
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		Log.d(TAG, "enter onDraw");
		Log.d(TAG, "startX="+startX+"startY"+startY);
		for(int i=0; i< JnsIMEKeyboardView.KEYBOARD_COL; i++)
		{	
			for(int j = 0; j < JnsIMEKeyboardView.KEYBOARD_ROW; j++)
			{
				if((!keyboardMetric[i][j].equals("")))
				{
					if(keyboardMetric[i][j].equals("123"))
					{	
						if(keypressed && pressed_row == j && pressed_col == i)
							c.drawBitmap(button_p , j * buttonWidth + startX, i* buttonHeight + startY,p);
						else
							c.drawBitmap(button_n, j * buttonWidth + startX, i* buttonHeight + startY,p);
						c.drawBitmap(button123, j * buttonWidth + startX, i* buttonHeight + startY,p);
						c.drawBitmap(button_n, j * buttonWidth + startX, i* buttonHeight + startY,p);
					}
					else if(keyboardMetric[i][j].equals("abc"))
					{	
						if(keypressed && pressed_row == j && pressed_col == i)
							c.drawBitmap(button_p, j * buttonWidth + startX, i* buttonHeight + startY,p);
						else
							c.drawBitmap(button_n, j * buttonWidth + startX, i* buttonHeight + startY,p);
						c.drawBitmap(buttonABC, j * buttonWidth + startX, i* buttonHeight + startY,p);
					

					}
					else if(keyboardMetric[i][j].equals(KeyBoard.keyboard_abc[3][3]))
					{		
						p.setTextSize(20);
						int textWidth = (int) p.measureText(keyboardMetric[i][j]);
						if(keypressed && pressed_row == j && pressed_col == i)
							c.drawBitmap(buttonSpace_p, j * buttonWidth + startX, i* buttonHeight+ startY,p);
						else
							c.drawBitmap(buttonSpace_n, j * buttonWidth + startX, i* buttonHeight + startY,p);
						c.drawText(keyboardMetric[i][j], j * buttonWidth + startX +(buttonWidth * 4 -textWidth)/2, 
								i * buttonHeight + startY + (buttonHeight+20)/2, p);
					}
					else if(keyboardMetric[i][j].equals(KeyBoard.keyboard_abc[2][8]))
					{		
						p.setTextSize(20);
						int textWidth = (int) p.measureText(keyboardMetric[i][j]);
						int textHeight =(int) p.getTextSize();
						if(keypressed && pressed_row == j && pressed_col == i)
							c.drawBitmap(buttonLong_p, j * buttonWidth + startX, i* buttonHeight+ startY,p);
						else
							c.drawBitmap(buttonLong_n, j * buttonWidth + startX, i* buttonHeight+ startY,p);
						c.drawText(keyboardMetric[i][j], j * buttonWidth + startX +(buttonWidth * 2 -textWidth)/2, 
								i * buttonHeight + startY + (buttonHeight+textHeight)/2, p);
					}
					else
					{	
						p.setTextSize(20);
						int textWidth = (int) p.measureText(keyboardMetric[i][j]);
						int textHeight =(int) p.getTextSize();
						if(keypressed && pressed_row == j && pressed_col == i)
							c.drawBitmap(button_p, j * buttonWidth + startX, i* buttonHeight+ startY,p);
						else
							c.drawBitmap(button_n, j * buttonWidth + startX, i* buttonHeight+ startY,p);
						c.drawText(keyboardMetric[i][j], j * buttonWidth + startX +(buttonWidth-textWidth)/2, 
								i * buttonHeight + startY + (buttonHeight+textHeight)/2, p);
					}
				}

			}
		}
	}
}
