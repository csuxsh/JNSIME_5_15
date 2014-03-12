
package com.viaplay.ime;

import com.viaplay.ime.uiadapter.FloatView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

/**
 * 可以永远显示在android屏幕最上方的浮动菜单
 * 
 * @author liujl v1.0 需要添加 <uses-permission
 *         android:name="android.permission.SYSTEM_ALERT_WINDOW"
 *         /><!--系统弹出窗口权限-->权限不然会报错
 */
public class FloatingFunc {
	/**
	 * 浮动窗口在屏幕中的x坐标
	 */
	private static float x = 0;
	/**
	 * 浮动窗口在屏幕中的y坐标
	 */
	private static float y = 200;
	/**
	 * 鼠标触摸开始位置
	 */
	private static float mTouchStartX = 0;
	/**
	 * 鼠标触摸结束位置
	 */
	private static float mTouchStartY = 0;
	/**
	 * windows 窗口管理器
	 */
	private static WindowManager wm = null;

	/**
	 * 浮动显示对象
	 */
	private static View floatingViewObj = null;
	
	private static Context mContext = null;

	/**
	 * 参数设定类
	 */
	public static WindowManager.LayoutParams params = new WindowManager.LayoutParams();
	public static int TOOL_BAR_HIGH = 0;
	/**
	 * 要显示在窗口最前面的对象
	 */
	private static View view_obj = null;
	static SoundPool sp;
	static int music;
	private static boolean isMove = false;

	/**
	 * 要显示在窗口最前面的方法
	 * 
	 * @param context
	 *            调用对象Context getApplicationContext()
	 * @param window
	 *            调用对象 Window getWindow()
	 * @param floatingViewObj
	 *            要显示的浮动对象 View
	 */
	public static void show(Context context, Window window, View floatingViewObj) {
		// 加载xml文件中样式例子代码
		// ********************************Start**************************
		// LayoutInflater inflater =
		// LayoutInflater.from(getApplicationContext());
		// View view = inflater.inflate(R.layout.topframe, null);
		// wm =
		// (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		// 加载xml文件中样式例子代码
		// *********************************End***************************
		//
		// 关闭浮动显示对象然后再显示
		close(context);
		mContext = context;
		FloatingFunc.floatingViewObj = floatingViewObj;

		view_obj = floatingViewObj;
		//     Rect frame = new Rect();
		// 这一句是关键，让其在top 层显示
		// getWindow()
		//      window.getDecorView().getWindowVisibleDisplayFrame(frame);
		//      TOOL_BAR_HIGH = frame.top;

		wm = (WindowManager) context// getApplicationContext()
				.getSystemService(Activity.WINDOW_SERVICE);
		params.type  =WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
				| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_HARDWARE_ACCELERATED;


		// 设置悬浮窗口长宽数据
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// 设定透明度
		params.alpha = 80;
		// 设定内部文字对齐方式
		params.gravity = Gravity.LEFT | Gravity.TOP;

		// 以屏幕左上角为原点，设置x、y初始值x
		x =  wm.getDefaultDisplay().getWidth() - 100 - floatingViewObj.getWidth();
		params.x = (int) x; 
		params.y = (int) y;
		// tv = new MyTextView(TopFrame.this);
		sp= new SoundPool(10, AudioManager.STREAM_SYSTEM, 100);
		music = sp.load(mContext, R.raw.keypress, 1);
		wm.addView(floatingViewObj, params);
		Log.d("DEBUG", "show view");
	}

	/**
	 * 跟谁滑动移动
	 * 
	 * @param event
	 *            事件对象
	 * @param view
	 *            弹出对象实例（View）
	 * @return
	 */
	public static boolean onTouchEvent(MotionEvent event, View view) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 获取相对View的坐标，即以此View左上角为原点
			mTouchStartX = event.getX();
			mTouchStartY = event.getY();
			Log.i("startP", "startX" + mTouchStartX + "====startY"
					+ mTouchStartY);// 调试信息
			((FloatView) floatingViewObj).clearCanvas();
			((FloatView) floatingViewObj).setImageResource(R.drawable.shot_press);
			break;
		case MotionEvent.ACTION_MOVE:
			x = event.getRawX()- mTouchStartX;
			y = event.getRawY() - mTouchStartY;
			if(!isMove && (Math.abs(event.getX() - mTouchStartX) < 10) && (Math.abs(event.getY() - mTouchStartY) < 10))
				return  false;
			updateViewPosition(view);
			isMove = true;
			Log.d("FUN", "VIEW MOVE");
			break;

		case MotionEvent.ACTION_UP:
			mTouchStartX = 0;
			mTouchStartY = 0;
			Log.d("UP", "up");
			if(!isMove)
			{	
				if(JnsIMEInputMethodService.jnsIMEInUse)
				{	
					if(JnsIMECoreService.ime != null)
					{	
						if(!JnsIMEInputMethodService.currentAppName.equals(JnsIMECoreService.ime.getPackageName()))
						{
							Message msg = new Message();
							msg.what = JnsIMECoreService.START_TPCFG;
							JnsIMECoreService.DataProcessHandler.sendMessage(msg);
							JnsIMECoreService.ime.startTpConfig();
						}
					}
				}
				sp.play(music, 1, 1, 1, 0, 1);
			}
			((FloatView) floatingViewObj).clearCanvas();
			((ImageView) floatingViewObj).setImageResource(R.drawable.shot_normal);

			isMove = false;
			break;
		}
		return false;
	}

	/**
	 * 关闭浮动显示对象
	 */
	public static void close(Context context) {

		if (view_obj != null && view_obj.isShown()) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Activity.WINDOW_SERVICE);
			wm.removeView(view_obj);
		}
	}

	/**
	 * 更新弹出窗口位置
	 */
	private static void updateViewPosition(View view) {
		// 更新浮动窗口位置参数
		Log.d("TEST", "x = "+x+"y= "+y);
		params.x = (int) x;//(int) (x - mTouchStartX);
		params.y = (int) y;//(int) (y - mTouchStartY);
		wm.updateViewLayout(FloatingFunc.floatingViewObj, params);
	}

}