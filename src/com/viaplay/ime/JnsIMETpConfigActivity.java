package com.viaplay.ime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.viaplay.ime.R;
import com.viaplay.im.hardware.JoyStickTypeF;
import com.viaplay.ime.JnsIMECoreService;
import com.viaplay.ime.bean.JnsIMEPosition;
import com.viaplay.ime.bean.JnsIMEProfile;
import com.viaplay.ime.uiadapter.JnsIMEScreenView;
import com.viaplay.ime.util.DrawableUtil;
import com.viaplay.ime.util.JnsEnvInit;

import dalvik.system.VMRuntime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 *  按search键 后进入的触摸配置activity,当前运行时候有时候还时会出现OOM错误
 *  
 * @author Sevent
 *
 */
public class JnsIMETpConfigActivity extends Activity implements OnTouchListener, OnClickListener {
	
	private static final String TAG = "BlueoceanTpConfigActivity";
	/**
	 *  显示当前触摸配置信息的view
	 */
	private JnsIMEScreenView screenView = null;
	private int backKeyCount = 0;
	private int oldKey;
	public List<JnsIMEProfile> keyList;
	private boolean debug = true;
	private boolean touched = false;
	private JnsIMEPosition perbop = new JnsIMEPosition();
	/**
	 *  当前界面是否有进行过触摸配置
	 */
	private boolean noTouchData = true;
	/**
	 * 最新的触摸点对象
	 */
	private JnsIMEPosition bop;
	/**
	 * 触摸点的x坐标
	 */
	private float touchX = 0.0f;
	/**
	 * 触摸点的y坐标
	 */
	private float touchY = 0.0f;
	/**
	 * 触摸点的半径
	 */
	private float touchR = 0.0f;
	/**
	 * 当前的配置中是否已经有了左摇杆配置
	 */
	private boolean hasLeftJoystick = false;
	/**
	 * 当前的配置中是否已经有了右摇杆配置
	 */
	private boolean hasRightJoystick = false;
	private Button cancel;
	private Button reset;
	private Button save;
	private Button exit;
	private ImageView backGrand;
	private int screenWidth = 0;
	private int screeanHeight = 0;
	private Bitmap tmp_bmp = null;
	Drawable draw = null;
	private final static float TARGET_HEAP_UTILIZATION = 0.75f;
	private boolean saved = true;
	@SuppressLint({ "NewApi", "HandlerLeak" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		VMRuntime.getRuntime().setMinimumHeapSize(16 * 1024 * 1024); 
		VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getLayoutInflater();
		//	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//	getWindow().getDecorView().setSystemUiVisibility(View.STATUS_BAR_HIDDEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_FULLSCREEN); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.input_key_tip);;
		backGrand = (ImageView) this.findViewById(R.id.imgbg);
		screenView = (JnsIMEScreenView)findViewById(R.id.screenView01);
		screenView.setOnTouchListener(this);
		screenView.setOnClickListener(this);
		cancel = (Button)findViewById(R.id.cancel);
		reset = (Button)findViewById(R.id.reset);
		save = (Button)findViewById(R.id.save);
		exit =(Button)findViewById(R.id.exit);
		cancel.setOnClickListener(this);
		reset.setOnClickListener(this);
		save.setOnClickListener(this);
		exit.setOnClickListener(this);
		keyList = JnsIMECoreService.keyList;
		JnsIMECoreService.touchConfiging = true;
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screeanHeight = dm.heightPixels;

		final Handler handle = new Handler()
		{
			@SuppressWarnings("deprecation")
			@SuppressLint({ "HandlerLeak", "HandlerLeak", "HandlerLeak", "HandlerLeak", "SdCardPath" })
			public void handleMessage(Message msg) 
			{
				if(JnsEnvInit.rooted)
				{	
					Bitmap draw_bmp = null;
					while(draw_bmp ==null)
					{	
						draw_bmp  = BitmapFactory.decodeFile("/mnt/sdcard/jnsinput/tmp.bmp");		
						//draw_bmp = Drawable.createFromPath("/mnt/sdcard/jnsinput/tmp.bmp");
					}
					// 如果是是手机则需要旋转切图
					if((screeanHeight - screenWidth) * (draw_bmp.getHeight()- draw_bmp.getWidth()) < 0)
					{
						Matrix matrix = new Matrix();
						int rotation = JnsIMETpConfigActivity.this.getWindowManager().getDefaultDisplay().getRotation();
						switch (rotation) {  
						case Surface.ROTATION_0:
							//matrix.setRotate(0);
							//break;
						case Surface.ROTATION_90:  
							matrix.setRotate(270);
							break;
						case Surface.ROTATION_180:  
							//matrix.setRotate(180);
							//break;
						case Surface.ROTATION_270:  
							matrix.setRotate(90); 
							break;
						}  
						// 旋转图片
						//	Bitmap tmp_bmp = DrawableUtil.getBitmap(JnsIMETpConfigActivity.this, "/mnt/sdcard/jnsinput/tmp.bmp");
						tmp_bmp = Bitmap.createBitmap(draw_bmp, 0, 0, draw_bmp.getWidth(), draw_bmp.getHeight(), matrix, true);
						draw_bmp.recycle();
						draw_bmp = null;
						/*
						while(draw_bmp ==null)
						{	
							draw_bmp = new BitmapDrawable(tmp_bmp);
						}
						 */
					}
					else
						tmp_bmp = draw_bmp;
					if(dm.density != 1)
					{	
						if(dm.density > 1.5)
						{
							draw = new BitmapDrawable(tmp_bmp);
							backGrand.setImageDrawable(draw);
							backGrand.setScaleType((ImageView.ScaleType.CENTER_CROP));
						}
						else
						{	
							//Bitmap bmp = tmp_bmp;
							Bitmap bmp = DrawableUtil.zoomBitmap(tmp_bmp, (int)(tmp_bmp.getWidth() * dm.density), (int)(tmp_bmp.getHeight() * dm.density));
							tmp_bmp.recycle();
							tmp_bmp = bmp;
							bmp = null;
						}
					}
					if(dm.density < 1.5 || dm.density == 1.5)
					{	
						draw = new BitmapDrawable(tmp_bmp);
						backGrand.setImageDrawable(draw);
						backGrand.setScaleType((ImageView.ScaleType.MATRIX));
					}
				}	
			}
		};


		Thread loadthread = new Thread(new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(8);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = new Message();
				handle.sendMessage(msg);
			}

		});
		loadthread.start();
	}
	@SuppressLint("NewApi")
	@Override
	public void onPause()
	{
		super.onPause(); 
		this.finish();
	}
	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		JnsIMECoreService.touchConfiging = false;
		backGrand.setImageDrawable(null);
		draw = null;
		while(tmp_bmp != null && !tmp_bmp.isRecycled())
		{
			tmp_bmp.recycle();
			tmp_bmp = null;
		}
		for(Activity activity : JnsIMECoreService.activitys)
		{
			activity.finish();
			System.gc();
		}
	}
	DialogInterface.OnClickListener  ocl = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch(which)
			{
			case DialogInterface.BUTTON_POSITIVE:
				saveFile(JnsIMEInputMethodService.validAppName);
				JnsIMECoreService.aph.Insert(JnsIMEInputMethodService.validAppName, "true");
				saved = true;
			default:
				JnsIMETpConfigActivity.this.finish();	
				break;
			}
		}

	};
	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {

		switch(arg0.getId())
		{
		case R.id.cancel:
			//this.finish();
			// cancle  清楚当前操作。
			screenView.drawNow(true, false);
			touched = false;
			JnsIMECoreService.gameStart = true;
			touchR = 0;
			break;
		case R.id.reset:
			screenView.posList.clear();
			keyList.clear();
			saved = false;
			screenView.drawNow(true, false);
			break;
		case R.id.save:
			saveFile(JnsIMEInputMethodService.validAppName);
			JnsIMECoreService.aph.Insert(JnsIMEInputMethodService.validAppName, "true");
			saved = true;
			break;
			//JnsIMETpConfigActivity.this.finish();
		case R.id.exit:
			if(!saved)
				(new AlertDialog.Builder(this).setMessage(getString(R.string.save_notice) ).setPositiveButton("save",
						ocl).setNegativeButton("cancle", ocl).create()).show();
			else
				JnsIMETpConfigActivity.this.finish();
			break;
		}
	}
	@SuppressLint({ "NewApi", "FloatMath", "FloatMath" })
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = arg1.getRawX(); //yuan dian
			touchY = arg1.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			float tx = arg1.getRawX(); //zhong dian
			float ty = arg1.getRawY();
			float tr = (float)Math.sqrt(Math.pow(Math.abs(touchX - tx) , 2) + Math.pow(Math.abs(touchY - ty) , 2));
			touchR = tr;
			bop = new JnsIMEPosition();
			bop.x = touchX;
			bop.y = touchY;
			bop.r = touchR;
			bop.resId = 0xFF;
			backKeyCount = 0;
			touched = true;
			//	saveFileDialog.saved = false;
			noTouchData = false;
			screenView.drawNow(true, true);
			screenView.drawCircle2(touchX, touchY, touchR);
			Log.e(TAG, "touch R = " + touchR + " touchX = " + touchX + " touchY = " + touchY);
			break;
		case MotionEvent.ACTION_UP:
			if(touchR < 20)
				touchR = 0;
			if (touchR == 0) {
				bop = new JnsIMEPosition();
				bop.type = JnsIMEPosition.TYPE_OTHERS;
				bop.x = arg1.getRawX();
				bop.y = arg1.getRawY();
				bop.r = 0;
				bop.resId = 0xFF;
				screenView.drawNow(true, true);
				screenView.drawBitmap(arg1.getRawX(), arg1.getRawY(), JnsIMEScreenView.BUTTOM_BG);
				backKeyCount = 0;
				touched = true;
				//	saveFileDialog.saved = false;
				noTouchData = false;
			}
			break;
		}
		return false;
	}
	/**
	 * 得到当前触摸点配置的图片资源和其他各种绘制需要的信息
	 * 
	 * @param event	当前按下的键
	 * @return 无效配置返回false,有效返回true
	 */
	private boolean drawInfo(KeyEvent event) {
		bop.scancode = event.getScanCode();
		if (bop.r > 0 && (event.getScanCode() == 0)) 
		{ //touchR == 0 则是触摸点和按键的映射，touchR > 0则是摇杆区域映射
			bop.color = Color.GREEN;
			DisplayMetrics dm = this.getResources().getDisplayMetrics();
			//if ((bop.x > (dm.widthPixels / 2)) && ((bop.x - bop.r) <=  (dm.widthPixels / 2))) 
			//{ //如果圆的中心点X坐标bop.x落在屏的右半边，则这个圆是右摇杆区域，反之则是左摇杆区域
			//	Toast.makeText(this, this.getString(R.string.invalid_joystick_area), Toast.LENGTH_SHORT).show();
			//	return false;
			//}
			//else if ((bop.x < (dm.widthPixels /2)) && ((bop.x + bop.r) >= (dm.widthPixels /2)))
			//{
			//	Toast.makeText(this, this.getString(R.string.invalid_joystick_area), Toast.LENGTH_SHORT).show();
			//	return false;
			//}
			//else 
			if (bop.r <= 30)
			{
				Log.e(TAG, "bop.r = " + bop.r + " invalid joystick_area");
				Toast.makeText(this, this.getString(R.string.invalid_joystick_area), Toast.LENGTH_SHORT).show();
				return false;
			}
			if(event.getKeyCode() == KeyEvent.KEYCODE_R )
				//((bop.x - bop.r) > (dm.widthPixels/2)) 
			{ //右摇杆区坿				
				if (hasRightJoystick &&( perbop.r == bop.r)){
				//	Toast.makeText(this, this.getString(R.string.has_right_joystick), Toast.LENGTH_SHORT).show();
					return false;
				}
				perbop.r = bop.r;
				bop.resId = JnsIMEScreenView.STICK_R;
				hasRightJoystick = true;
				bop.type = JnsIMEPosition.TYPE_RIGHT_JOYSTICK;
				bop.scancode = JoyStickTypeF.STICK_R;
				screenView.setCircleType(bop.type);
				int i = 0;
				for(i = 0; i < keyList.size(); i++)
				{	
					JnsIMEProfile profile = keyList.get(i);
					if(profile.posType == JnsIMEPosition.TYPE_RIGHT_JOYSTICK)
					{
						keyList.remove(i);
					}
				}
				for(i = 0; i < screenView.posList.size(); i++)
				{	
					JnsIMEPosition postion = screenView.posList.get(i);
					if(postion.type == JnsIMEPosition.TYPE_RIGHT_JOYSTICK)
					{
						screenView.posList.remove(i);
					}
				}
			} else {
				if (event.getKeyCode()== KeyEvent.KEYCODE_L)
				{
					if(hasLeftJoystick && (perbop.r == bop.r))
					//Toast.makeText(this, this.getString(R.string.has_left_joystick), Toast.LENGTH_SHORT).show();
					return false;
				}
				perbop.r = bop.r;
				bop.resId = JnsIMEScreenView.STICK_L;
				hasLeftJoystick = true;
				bop.type =JnsIMEPosition.TYPE_LEFT_JOYSTICK;
				bop.scancode = JoyStickTypeF.STICK_L;
				screenView.setCircleType(bop.type);
				int i = 0;
				for(i = 0; i < keyList.size(); i++)
				{	
					JnsIMEProfile profile = keyList.get(i);
					if(profile.posType == JnsIMEPosition.TYPE_LEFT_JOYSTICK)
					{
						keyList.remove(i);
					}
				}
				for(i = 0; i < screenView.posList.size(); i++)
				{	
					JnsIMEPosition postion = screenView.posList.get(i);
					if(postion.type == JnsIMEPosition.TYPE_LEFT_JOYSTICK)
					{
						screenView.posList.remove(i);
					}
				}
			}
			screenView.posList.add(bop); 
			touchR = 0;
			return true;
		} else if (touchR > 0) return false;
		bop.color = Color.GREEN;
		screenView.setCircleType(bop.type);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_0:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_1:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			bop.resId = 0xFF;;
			break;
		case KeyEvent.KEYCODE_2:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_3:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_4:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_5:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_6:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_7:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_8:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_9:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_A:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_ALT_LEFT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_ALT_RIGHT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_APOSTROPHE:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_B:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_BACKSLASH:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_C:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_COMMA:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_D:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_E:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_EQUALS:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_F:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_G:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_H:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_I:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_J:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_K:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_L:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_N:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_M:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_O:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_P:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_Q:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_R:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_S:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_T:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_U:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_V:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_W:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_X:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_Y:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_Z:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_BUTTON_A:
			bop.resId = JnsIMEScreenView.BUTTON_A;
			break;
		case KeyEvent.KEYCODE_BUTTON_B:
			bop.resId = JnsIMEScreenView.BUTTON_B;
			break;
		case KeyEvent.KEYCODE_BUTTON_C:
			bop.resId =0xFF;
			break;
		case KeyEvent.KEYCODE_BUTTON_L1:
			bop.resId = JnsIMEScreenView.BUTTON_L1;
			break;
		case KeyEvent.KEYCODE_BUTTON_L2:
			bop.resId = JnsIMEScreenView.BUTTON_L2;
			break;
		case KeyEvent.KEYCODE_BUTTON_MODE:
			bop.resId  = 0xFF;
			break;
		case KeyEvent.KEYCODE_BUTTON_R1:
			bop.resId = JnsIMEScreenView.BUTTON_R1;
			break;
		case KeyEvent.KEYCODE_BUTTON_R2:
			bop.resId = JnsIMEScreenView.BUTTON_R2;
			break;
		case KeyEvent.KEYCODE_BUTTON_SELECT:
			bop.resId = JnsIMEScreenView.BUTTON_SELECT;
			break;
		case KeyEvent.KEYCODE_BUTTON_START:
			bop.resId = JnsIMEScreenView.BUTTON_START;
			break;
		case KeyEvent.KEYCODE_BUTTON_THUMBL:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_BUTTON_THUMBR:
			bop.resId = 0xFF;;
			break;
		case KeyEvent.KEYCODE_BUTTON_X:
			bop.resId = JnsIMEScreenView.BUTTON_X;
			break;
		case KeyEvent.KEYCODE_BUTTON_Y:
			bop.resId = JnsIMEScreenView.BUTTON_Y;
			break;
		case KeyEvent.KEYCODE_BUTTON_Z:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			bop.resId = JnsIMEScreenView.BUTTON_DOWN;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			bop.resId = JnsIMEScreenView.BUTTON_LEFT;
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			bop.resId = JnsIMEScreenView.BUTTON_RIGHT;
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			bop.resId = JnsIMEScreenView.BUTTON_UP;
			break;
		case KeyEvent.KEYCODE_LEFT_BRACKET:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_REWIND:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MEDIA_STOP:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_MINUS:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_NUM:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_PAGE_DOWN:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_PAGE_UP:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_PICTSYMBOLS:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_PLUS:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_POUND:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_RIGHT_BRACKET:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SEARCH:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SEMICOLON:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SHIFT_LEFT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SLASH:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SOFT_LEFT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SOFT_RIGHT:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SPACE:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_STAR:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_SYM:
			bop.resId = 0xFF;
			break;
		case KeyEvent.KEYCODE_TAB:
			bop.resId = 0xFF;
			break;
			/*	
		case BlueoceanCore.KEYCODE_BUTTON_1:
			bop.msg = "BUTTON_1";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_2:
			bop.msg = "BUTTON_2";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_3:
			bop.msg = "BUTTON_3";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_4:
			bop.msg = "BUTTON_4";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_5:
			bop.msg = "BUTTON_5";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_6:
			bop.msg = "BUTTON_6";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_7:
			bop.msg = "BUTTON_7";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_8:
			bop.msg = "BUTTON_8";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_9:
			bop.msg = "BUTTON_9";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_10:
			bop.msg = "BUTTON_10";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_11:
			bop.msg = "BUTTON_11";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_12:
			bop.msg = "BUTTON_12";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_13:
			bop.msg = "BUTTON_13";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_14:
			bop.msg = "BUTTON_14";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_15:
			bop.msg = "BUTTON_15";
			break;
		case BlueoceanCore.KEYCODE_BUTTON_16:
			bop.msg = "BUTTON_16";
			break;
			 */
		default: break;
		}
		if(bop.resId == 0xFF)
		{	
			Toast.makeText(this, "This Button is  not avalid!", Toast.LENGTH_LONG).show();
			return false;
		}
		screenView.posList.add(bop);
		return true;
	}
	private  int iteratorKeyList(List<JnsIMEProfile> keylist, KeyEvent event)
	{
		if(keylist==null)
			return -1;
		JnsIMEProfile keyProfile ;
		for( int i = 0; i < keylist.size(); i++)
		{	
			keyProfile = keylist.get(i);
			if(keyProfile.key == event.getScanCode())
			{
				return i;
			}
		}
		return -1;
	}
	private  int iteratorPosList(List<JnsIMEPosition> poslist, int scancode)
	{
		if(poslist==null)
			return -1;
		JnsIMEPosition keyProfile ;
		for( int i = 0; i < poslist.size(); i++)
		{	
			keyProfile = poslist.get(i);
			if(keyProfile.scancode == scancode)
			{
				return i;
			}
		}
		return -1;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e(TAG, "onkeyDOwn"); 
		if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
			backKeyCount ++;
			if (backKeyCount == 1 && noTouchData) return super.onKeyDown(keyCode, event);
			if (backKeyCount < 2) {
				screenView.drawNow(true, false);
				touched = false;
				JnsIMECoreService.gameStart = true;
				touchR = 0;
				return false;
			}
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getScanCode() == 0)
		{
			event = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, JoyStickTypeF.BUTTON_UP_SCANCODE, 0);
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getScanCode() == 0)
		{
			event = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0, JoyStickTypeF.BUTTON_DOWN_SCANCODE, 0);
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getScanCode() == 0)
		{
			event = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, JoyStickTypeF.BUTTON_LEFT_SCANCODE, 0);
		}
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getScanCode() == 0)
		{
			event = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, JoyStickTypeF.BUTTON_RIGHT_SCANCODE, 0);
		}
		Log.e(TAG, "touched = " + touched + " oldkey = " +oldKey + " event.getKeyCode = " + event.getKeyCode());
		if (touched) {
			if (!drawInfo(event)) return false;
			JnsIMEProfile mProfile = new JnsIMEProfile();
			mProfile.key = event.getScanCode();
			mProfile.keyCode = event.getKeyCode();
			mProfile.posX = screenView.getTouchX();  
			mProfile.posY = screenView.getTouchY();
			mProfile.posR = screenView.getTouchR();
			mProfile.posType = screenView.getCircleType();
			if(mProfile.posType == JnsIMEProfile.LEFT_JOYSTICK)
				mProfile.key = JoyStickTypeF.STICK_L;
			if(mProfile.posType == JnsIMEProfile.RIGHT_JOYSTICK)
				mProfile.key = JoyStickTypeF.STICK_R;
			Log.d(TAG, "keyList.size="+keyList.size());
			int i = iteratorKeyList(keyList,  event);
			if(i > -1)
				keyList.remove(i);
			keyList.add(mProfile);
			saved = false;
			i = iteratorPosList(screenView.posList,  event.getScanCode());
			if(i > -1 && (i != (screenView.posList.size()-1)))
				screenView.posList.remove(i);
			JnsIMECoreService.keyList = keyList;
			Log.d(TAG, "keyList.size="+keyList.size());
			Log.d(TAG, "JnsIMECore.keyList.size="+JnsIMECoreService.keyList.size());
			if (event.getKeyCode() != KeyEvent.KEYCODE_SEARCH) { //配置joystick的按钿	oldKey = event.getKeyCode();
			}
			if (debug) Log.e(TAG, "config a key pos scankey= " + oldKey + " posx= " + mProfile.posX + " posy= " + mProfile.posY);
			screenView.drawNow(true, false);
			touched = false;
			backKeyCount ++;
			return true;
		}
		if (KeyEvent.KEYCODE_BACK == event.getKeyCode()){ //&& !saveFileDialog.saved) {
			requestToSaveFile();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void requestToSaveFile() {
		//saveFileDialog.show();

	}
	/**
	 * 保存当前的配置文件
	 * 
	 * @param path 要保存的文件名
	 */
	private void saveFile(String path) {
		try {
			FileOutputStream fos = this.openFileOutput(path, Context.MODE_PRIVATE);

			if (keyList != null) {
				Log.d(TAG, "keyList.size="+keyList.size());
				Log.d(TAG, "JnsIMECore.keyList.size="+JnsIMECoreService.keyList.size());
				for (int i = 0; i < JnsIMECoreService.keyList.size(); i ++) {
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).key).getBytes());
					fos.write("\n".getBytes());
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).keyCode).getBytes());
					fos.write("\n".getBytes());
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).posX).getBytes());
					fos.write("\n".getBytes());
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).posY).getBytes());
					fos.write("\n".getBytes());
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).posR).getBytes());
					fos.write("\n".getBytes());
					fos.write(String.valueOf(JnsIMECoreService.keyList.get(i).posType).getBytes());
					fos.write("\n".getBytes());
				}
			}
			//		saved = true;
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//	infoTv.setText(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
