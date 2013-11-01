package com.viaplay.ime.uiadapter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.viaplay.ime.R;
import com.viaplay.im.hardware.JoyStickTypeF;
import com.viaplay.ime.bean.JnsIMEKeyMap;
import com.viaplay.ime.bean.JnsIMETypeFKeyMap;
import com.viaplay.ime.util.DrawableUtil;

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

/**
 * keymmaping时用到得配置编辑框，这个view包含了上部分的所有编辑框
 * 
 * @author Steven
 *
 */
public class JnsIMEKeyMapView extends ImageView {


	public  static final int JoyStickTypeFID = 1;
	private int ScreeanWidth;
	private int ScreeanHeight;
	/**
	 * 编辑框的宽度
	 */
	private int buttonWidth;
	/**
	 * 编辑框的高度
	 */
	private int buttonHeight;
	private Activity activity;
	/**
	 * 编辑框处于非编辑状态时的图标资源
	 */
	private Bitmap edit_n;
	/**
	 * 编辑框处于编辑状态时的图标资源
	 */
	private Bitmap edit_i;
	/**
	 * 绘制编辑框的起始x
	 */
	private int startX=0;
	/**
	 * 绘制编辑框的起始y
	 */
	private int startY=0;
	/**
	 * 需要配置的操控器硬件ID号
	 */
	private int hardwareId;
	/**
	 * 被触摸到得行号
	 */
	private int touchedRow = -1;
	/**
	 * 被触摸到得列号
	 */
	private int touchedCol = -1;
	
	/**
	 * 各编辑框对应的按键ID
	 */
	public   int gamePadButoonIndex[][];
	/**
	 * 各编辑框显示的字符信息
	 */
	public  String gamePadButoonLable[][];
	/**
	 * 显示的行总数
	 */
	public   int diplayRow = 0;
	/**
	 * 显示的列总数
	 */
	public   int diplayCol = 0;
	
	/**
	 *  设置当前配置的操控器硬件id
	 *  
	 * @param hardwareId 要设置的硬件ID
	 */
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
	/**
	 * 根据硬件ID号返回对应的keymap对象
	 * 
	 * @return 非法ID返回null,合法id返回对应的对象
	 */
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
	
	/**
	 * 设置整个编辑框的显示内容
	 * 
	 * @param mappedKey 配对好的按键映射表
	 */
	public void setLableDisplay(Map<Integer, String> mappedKey)
	{
		Iterator<Entry<Integer, String>> iterator = mappedKey.entrySet().iterator();
		for(int i = 0; i <diplayCol; i++)
			for(int j = 0; j < diplayRow; j++)
			{
				if(gamePadButoonLable[i][j] != null)
					gamePadButoonLable[i][j] = "";
			}
		while(iterator.hasNext())
		{
			Entry<Integer, String> key = iterator.next(); 
			if(gamePadButoonLable[key.getKey()/diplayRow][key.getKey()%diplayRow] != null)
			gamePadButoonLable[key.getKey()/diplayRow][key.getKey()%diplayRow] = key.getValue();
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
	/**
	 * 查找当前触摸到的编辑框
	 * 
	 * @param x 触摸点的x坐标
	 * @param y 触摸点的y坐标
	 * @param action 触摸动作
	 * @return 当前编辑的按键配置信息
	 */
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
