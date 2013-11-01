package com.viaplay.ime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.viaplay.im.hardware.JoyStickTypeF;
import com.viaplay.ime.R;
import com.viaplay.ime.bean.JnsIMEKeyMap;
import com.viaplay.ime.bean.KeyBoard;
import com.viaplay.ime.uiadapter.JnsIMEKeyMapView;
import com.viaplay.ime.uiadapter.JnsIMEKeyboardView;
import com.viaplay.ime.util.DrawableUtil;

import dalvik.system.VMRuntime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Keymapping的Acivity
 *  
 * @author Steven
 *
 */
public class JnsIMEKeyMappingActivity extends Activity implements OnClickListener{

	private final static String TOKEN=":";
	private final static float TARGET_HEAP_UTILIZATION = 0.75f;
	public int curentKeyBoard;
	private String fileName;
	JnsIMEKeyboardView keyboard_a;
	JnsIMEKeyboardView keyboard_1;
	JnsIMEKeyboardView keyboard_gamepad;
	JnsIMEKeyboardView current_keyboard;
	JnsIMEKeyMapView keyMapView;
	JnsIMEKeyMap currentet_keymap = null;
	@SuppressLint("UseSparseArrays")
	private Map<Integer, JnsIMEKeyMap> oldkeys = new HashMap<Integer, JnsIMEKeyMap>(); ;
	@SuppressLint("UseSparseArrays")
	private Map<Integer , String> mappedKey = new HashMap<Integer , String>();
	@SuppressLint("UseSparseArrays")
	private Map<Integer, JnsIMEKeyMap> keys = new HashMap<Integer, JnsIMEKeyMap>();


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VMRuntime.getRuntime().setMinimumHeapSize(16 * 1024 * 1024); 
		VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
		this.setContentView(R.layout.activity_keymapping);
		TextView title = (TextView) this.findViewById(R.id.keymap_title);
		Intent in = this.getIntent();
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		fileName = (String) in.getExtras().get("filename");
		title.setTextColor(Color.YELLOW);
		title.setText(""+in.getExtras().get("lable"));
		ImageView imageView1 = (ImageView) this.findViewById(R.id.imageView1);
		Drawable controller = this.getResources().getDrawable(R.drawable.key_mapping_bg);
		
		if(dm.density  < 3.0)
		{	
			controller = DrawableUtil.zoomDrawable(controller, (int)(dm.widthPixels * dm.density), 
				(int)((controller.getIntrinsicHeight() * dm.widthPixels / controller.getIntrinsicWidth())*dm.density));
			imageView1.setScaleType(ImageView.ScaleType.MATRIX);
			imageView1.setImageDrawable(controller);
		}
		/*
		 BitmapFactory.Options options=new BitmapFactory.Options(); 
		 options.inJustDecodeBounds = false;  
		 InputStream is = this.getResources().openRawResource(R.drawable.key_mapping_bg);
		 Bitmap	 bitmap = BitmapFactory.decodeStream(is,null,options);
		 imageView1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView1.setImageBitmap(bitmap);
		*/
	//	imageView1.setScaleType(ImageView.ScaleType.FIT_START);
	//	imageView1.setImageDrawable(controller);
	//	imageView1.setImageBitmap(bm)
		keyMapView = (JnsIMEKeyMapView) this.findViewById(R.id.key_edit_grid);	
		keyMapView.setHardWare(JnsIMEKeyMapView.JoyStickTypeFID);
		if(!loadFile())
			Log.d("error", "load file failed");
		keyMapView.setLableDisplay(mappedKey);
		keyMapView.postInvalidate();
		KeyBoard keyboard = new KeyBoard(KeyBoard.LAYOUT_ABC_INDEX);
		keyboard_a= (JnsIMEKeyboardView) this.findViewById(R.id.keyboardview_a);
		keyboard_a.setKeyBoard(keyboard);
		keyboard = new KeyBoard(KeyBoard.LAYOUT_123_INDEX);
		keyboard_1= (JnsIMEKeyboardView) this.findViewById(R.id.keyboardview_1);
		keyboard_1.setKeyBoard(keyboard);
		keyboard = new KeyBoard(KeyBoard.LAYOUT_GAMEPAD_INDEX);
		keyboard_gamepad= (JnsIMEKeyboardView) this.findViewById(R.id.gamepad_view);
		keyboard_gamepad.setKeyBoard(keyboard);
		curentKeyBoard = KeyBoard.LAYOUT_ABC_INDEX;
		keyboard_gamepad.setVisibility(View.GONE);
		keyboard_1.setVisibility(View.GONE); 
		current_keyboard = keyboard_a;
		Button back = (Button) this.findViewById(R.id.key_map_back);
		Button save = (Button) this.findViewById(R.id.key_map_save);
		Button reset = (Button) this.findViewById(R.id.key_map_reset);
		Button clear = (Button) this.findViewById(R.id.key_map_clear);
		Button keyboardbutton = (Button) this.findViewById(R.id.key_map_keyboard);
		Button gamepadbutton = (Button) this.findViewById(R.id.key_map_gamepad);
		back.setOnClickListener(this);
		save.setOnClickListener(this);
		reset.setOnClickListener(this);
		clear.setOnClickListener(this);
		keyboardbutton.setOnClickListener(this);
		gamepadbutton.setOnClickListener(this);
		JnsIMECoreService.activitys.add(this);

	} 
	/**
	 * 加载需要配置的应用对应的配置文件
	 * 
	 * @return 加载成功返回true,加载失败返回false
	 */
	private boolean loadFile()
	{

		if(!keys.isEmpty())
			keys.clear();
		if(mappedKey.isEmpty())
			mappedKey.clear();

		String data[];
		try {
			FileInputStream fis = this.openFileInput(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String tmp = br.readLine();
			while((tmp != null)&&(!tmp.equals("")) )
			{	
				data = tmp.split(TOKEN);
				JnsIMEKeyMap key = keyMapView.getJnsIMEKeyMap();
				key.setGamPadIndex(Integer.parseInt(data[0]));
				key.setKeyCode(Integer.parseInt(data[3]));
				key.setLable(data[1]);
				Log.d("fread", key.getGamPadIndex()+":"+key.getLable()+":"+key.getKeyCode());
				oldkeys.put(key.getGamPadIndex(), key);
				keys.put(key.getGamPadIndex(), key);
				mappedKey.put(key.getGamPadIndex(), key.getLable());
				tmp = br.readLine();
			}
			//keys.putAll(oldkeys);
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block

			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean saveFile()
	{
		try {
			FileOutputStream fos = this.openFileOutput(fileName, Context.MODE_PRIVATE);
			Iterator<Entry<Integer, JnsIMEKeyMap>> iterator  = keys.entrySet().iterator();
			while(iterator.hasNext())
			{
				JnsIMEKeyMap  keymap = iterator.next().getValue();
				fos.write((keymap.getGamPadIndex()+":"+keymap.getLable()+":"+keymap.getScanCode()+":"+keymap.getKeyCode()+"\n").getBytes());
				if(keymap.getScanCode() == JoyStickTypeF.BUTTON_L2_SCANCODE)
					fos.write((keymap.getGamPadIndex()+":"+keymap.getLable()+":"+JoyStickTypeF.BUTTON_GAS_SCANCODE+":"+keymap.getKeyCode()+"\n").getBytes());
				if(keymap.getScanCode() == JoyStickTypeF.BUTTON_R2_SCANCODE)
					fos.write((keymap.getGamPadIndex()+":"+keymap.getLable()+":"+JoyStickTypeF.BUTTON_BRAKE_SCANCODE+":"+keymap.getKeyCode()+"\n").getBytes());
			}
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{		
		JnsIMEKeyMap keymap =keyMapView.findTouchedEdit((int)event.getX(), (int)event.getY(), event.getAction());	
		if(keymap != null)
		{	
			currentet_keymap = keymap;
			return false;
		}


		// 获得当前按键的lable和keycode信息
		Entry<String, Integer> entry=
			current_keyboard.findKeyCode((int)event.getX(), (int)event.getY(), event.getAction());
		if(entry != null && entry.getKey().equals("123"))
		{
			current_keyboard.setVisibility(View.GONE);
			keyboard_1.setVisibility(View.VISIBLE); 
			curentKeyBoard = KeyBoard.LAYOUT_123_INDEX;
			current_keyboard = keyboard_1;
		}
		else if(entry != null && entry.getKey().equals("abc"))
		{
			current_keyboard.setVisibility(View.GONE);
			keyboard_a.setVisibility(View.VISIBLE); 
			curentKeyBoard = KeyBoard.LAYOUT_ABC_INDEX;
			current_keyboard = keyboard_a;
		}
		else if(entry != null && currentet_keymap !=null)
		{	
			currentet_keymap.setLable(entry.getKey());
			currentet_keymap.setKeyCode(entry.getValue());

			// 判断当前按键值是否已经被配置过
			/*
			if(mappedKey.containsKey((currentet_keymap.getLable())))
			{		
				//将原来配置的地方置空
				int perButtonIndex = mappedKey.get(currentet_keymap.getLable());
				keyMapView.gamePadButoonLable[perButtonIndex/keyMapView.diplayRow][perButtonIndex%keyMapView.diplayRow] = "";
				mappedKey.remove(currentet_keymap.getLable());
				Log.d("tag", "rm "+perButtonIndex);
				keys.remove(perButtonIndex);
			}
			*/
			if(mappedKey.containsValue(currentet_keymap.getGamPadIndex()))
			{
				Log.d("tag", "rm currentet_keymap.getGamPadIndex()"+currentet_keymap.getGamPadIndex());
				mappedKey.remove(keys.get(currentet_keymap.getGamPadIndex()).getLable());
				keys.remove(currentet_keymap.getGamPadIndex());
			}
			keyMapView.gamePadButoonLable[currentet_keymap.getGamPadIndex()/keyMapView.diplayRow][currentet_keymap.getGamPadIndex()%keyMapView.diplayRow] = entry.getKey();
			keyMapView.postInvalidate();
			mappedKey.put(currentet_keymap.getGamPadIndex(), entry.getKey());
			keys.put(currentet_keymap.getGamPadIndex(), currentet_keymap);
			currentet_keymap = null;

		}
		return false;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.key_map_save:
			saveFile();
			this.finish();
			break;
		case R.id.key_map_reset:
			keys.clear();
			mappedKey.clear();
			if(!loadFile())
				Log.d("error", "load file failed");
			keyMapView.setLableDisplay(mappedKey);
			keyMapView.postInvalidate();
			break;
		case R.id.key_map_clear:
			keys.clear();
			mappedKey.clear();
			keyMapView.setLableDisplay(mappedKey);
			keyMapView.postInvalidate();
			break;
		case R.id.key_map_gamepad:
			current_keyboard.setVisibility(View.GONE);
			keyboard_gamepad.setVisibility(View.VISIBLE); 
			curentKeyBoard = KeyBoard.LAYOUT_GAMEPAD_INDEX;
			current_keyboard = keyboard_gamepad;
			break;
		case R.id.key_map_keyboard:
			current_keyboard.setVisibility(View.GONE);
			keyboard_a.setVisibility(View.VISIBLE); 
			curentKeyBoard = KeyBoard.LAYOUT_ABC_INDEX;
			current_keyboard = keyboard_a;
			break;
		case R.id.key_map_back:
			this.finish();
			break;
		}
	};
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		JnsIMECoreService.activitys.remove(this);
	}
}
