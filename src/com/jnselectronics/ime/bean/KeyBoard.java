package com.jnselectronics.ime.bean;

import java.util.HashMap;
import java.util.Map;

import android.view.KeyEvent;

public class KeyBoard {
	
	public static  final String keyboard_abc[][]=
	{
		{"Q","W","E","R","T","Y","U","I","O","P"},
		{"A","S","D","F","G","H","J","K","L","Enter"},
		{"123","Z","X","C","V","B","N","M","BackSpace",""},
		{"LShift","LCtrl","LAtl","Space","","","","RAlt","RCtrl","RShift"}
	};
	public static  final String keyboard_123[][]=
	{
		{"1","2","3","4","5","6","7","8","9","0"},
		{"Esc","Tab","-","=","","","","Back","Inset","Del"},
		{"F1","F2","F3","F4","F5","F6","F7","F8","F9","F10"},
		{"abc","F11","F12","","","","P-Up","P-Down","Home","End"}
	};
	public static  final String keyboard_gamepad[][]=
	{
		{"L1","L2","","","","","","","R1","R2"},
		{"","Up","","","","","","GameA","GameB","GameC"},
		{"Left","Center","Right","Select","","","Start","GameX","GameY","GameZ"},
		{"","Down","","ThumbL","","","ThumbL","","",""}
	};
	public final static int LAYOUT_ABC_INDEX = 1;
	public final static int LAYOUT_123_INDEX = 2;	
	public final static int LAYOUT_GAMEPAD_INDEX = 3;
	
	private Map<String, Integer> keyMap = new HashMap<String, Integer>();
	private String keyboardLayout[][];

	public KeyBoard(int Layout_Indx)
	{
		switch(Layout_Indx)
		{
			case LAYOUT_ABC_INDEX:
				putKeyBoard_ABCMap();
				keyboardLayout = keyboard_abc;
				break;
			case LAYOUT_123_INDEX:
				putKeyBoard_123Map();
				keyboardLayout = keyboard_123;
				break;
			case LAYOUT_GAMEPAD_INDEX:
				putKeyBoard_GamePadMap();
				keyboardLayout = keyboard_gamepad;
				break;
		}
		
	}
	public Integer getKeyCode(String lable)
	{
		return keyMap.get(lable);
	}
	public String[][] getKeyboardLayout() {
		return keyboardLayout;
	}

	public void setKeyboardLayout(String[][] keyboardLayout) {
		this.keyboardLayout = keyboardLayout;
	}
	
	private void putKeyBoard_ABCMap()
	{
		keyMap.put("Q", KeyEvent.KEYCODE_Q);
		keyMap.put("W", KeyEvent.KEYCODE_W); 
		keyMap.put("E", KeyEvent.KEYCODE_E); 
		keyMap.put("R", KeyEvent.KEYCODE_R); 
		keyMap.put("T", KeyEvent.KEYCODE_T); 
		keyMap.put("Y", KeyEvent.KEYCODE_Y); 
		keyMap.put("U", KeyEvent.KEYCODE_U); 
		keyMap.put("I", KeyEvent.KEYCODE_I); 
		keyMap.put("O", KeyEvent.KEYCODE_O); 
		keyMap.put("P", KeyEvent.KEYCODE_P); 
		keyMap.put("A", KeyEvent.KEYCODE_A); 
		keyMap.put("S", KeyEvent.KEYCODE_S); 
		keyMap.put("D", KeyEvent.KEYCODE_D); 
		keyMap.put("F", KeyEvent.KEYCODE_F); 
		keyMap.put("G", KeyEvent.KEYCODE_G); 
		keyMap.put("H", KeyEvent.KEYCODE_H); 
		keyMap.put("J", KeyEvent.KEYCODE_J); 
		keyMap.put("K", KeyEvent.KEYCODE_K); 
		keyMap.put("L", KeyEvent.KEYCODE_L); 
		keyMap.put("Enter", KeyEvent.KEYCODE_ENTER); 
		keyMap.put("123", null); 
		keyMap.put("Z", KeyEvent.KEYCODE_Z); 
		keyMap.put("X", KeyEvent.KEYCODE_X); 
		keyMap.put("C", KeyEvent.KEYCODE_C); 
		keyMap.put("V", KeyEvent.KEYCODE_V); 
		keyMap.put("B", KeyEvent.KEYCODE_B); 
		keyMap.put("N", KeyEvent.KEYCODE_N); 
		keyMap.put("M", KeyEvent.KEYCODE_M); 
		keyMap.put("BackSpace", KeyEvent.KEYCODE_FORWARD_DEL);
		keyMap.put("LShift", KeyEvent.KEYCODE_SHIFT_LEFT);
		keyMap.put("LCtrl", KeyEvent.KEYCODE_CTRL_LEFT);
		keyMap.put("LAtl", KeyEvent.KEYCODE_ALT_LEFT);
		keyMap.put("Space", KeyEvent.KEYCODE_SPACE);
		keyMap.put("RShift", KeyEvent.KEYCODE_SHIFT_RIGHT);
		keyMap.put("RCtrl", KeyEvent.KEYCODE_CTRL_RIGHT);
		keyMap.put("RAlt", KeyEvent.KEYCODE_ALT_RIGHT);
	}
	private void putKeyBoard_123Map()
	{
		keyMap.put("1", KeyEvent.KEYCODE_1);
		keyMap.put("2", KeyEvent.KEYCODE_2); 
		keyMap.put("3", KeyEvent.KEYCODE_3); 
		keyMap.put("4", KeyEvent.KEYCODE_4); 
		keyMap.put("5", KeyEvent.KEYCODE_5); 
		keyMap.put("6", KeyEvent.KEYCODE_6); 
		keyMap.put("7", KeyEvent.KEYCODE_7); 
		keyMap.put("8", KeyEvent.KEYCODE_8); 
		keyMap.put("9", KeyEvent.KEYCODE_9); 
		keyMap.put("0", KeyEvent.KEYCODE_0); 
		keyMap.put("F1", KeyEvent.KEYCODE_F1); 
		keyMap.put("F2", KeyEvent.KEYCODE_F2); 
		keyMap.put("F3", KeyEvent.KEYCODE_F3); 
		keyMap.put("F4", KeyEvent.KEYCODE_F4); 
		keyMap.put("F5", KeyEvent.KEYCODE_F5); 
		keyMap.put("F5", KeyEvent.KEYCODE_F6); 
		keyMap.put("F7", KeyEvent.KEYCODE_F7); 
		keyMap.put("F8", KeyEvent.KEYCODE_F8); 
		keyMap.put("F9", KeyEvent.KEYCODE_F9); 
		keyMap.put("F10", KeyEvent.KEYCODE_F10); 
		keyMap.put("F11", KeyEvent.KEYCODE_F11); 
		keyMap.put("F12", KeyEvent.KEYCODE_F12); 
		keyMap.put("Esc", KeyEvent.KEYCODE_ESCAPE); 
		keyMap.put("Tab", KeyEvent.KEYCODE_TAB); 
		keyMap.put("Back", KeyEvent.KEYCODE_BACK);
		keyMap.put("Inset", KeyEvent.KEYCODE_INSERT); 
		keyMap.put("Del", KeyEvent.KEYCODE_DEL); 
		keyMap.put("P-Up", KeyEvent.KEYCODE_PAGE_UP); 
		keyMap.put("P-Down", KeyEvent.KEYCODE_PAGE_DOWN); 
		keyMap.put("Home", KeyEvent.KEYCODE_HOME); 
		keyMap.put("End", KeyEvent.KEYCODE_ENDCALL);
		keyMap.put("ABC", null);
		keyMap.put("-", KeyEvent.KEYCODE_MINUS);
		keyMap.put("=", KeyEvent.KEYCODE_EQUALS);
		
	}
	private void putKeyBoard_GamePadMap()
	{
		keyMap.put("L1", KeyEvent.KEYCODE_BUTTON_L1);
		keyMap.put("L2", KeyEvent.KEYCODE_BUTTON_L2); 
		keyMap.put("L3", null); 
		keyMap.put("R1", KeyEvent.KEYCODE_BUTTON_R1); 
		keyMap.put("R2", KeyEvent.KEYCODE_BUTTON_R2); 
		keyMap.put("R3", null); 
		keyMap.put("Up", KeyEvent.KEYCODE_DPAD_UP); 
		keyMap.put("Down", KeyEvent.KEYCODE_DPAD_DOWN); 
		keyMap.put("Left", KeyEvent.KEYCODE_DPAD_LEFT); 
		keyMap.put("Right", KeyEvent.KEYCODE_DPAD_RIGHT); 
		keyMap.put("Center", KeyEvent.KEYCODE_DPAD_CENTER); 
		keyMap.put("GameA", KeyEvent.KEYCODE_BUTTON_A); 
		keyMap.put("GameB", KeyEvent.KEYCODE_BUTTON_B); 
		keyMap.put("GameC", KeyEvent.KEYCODE_BUTTON_C); 
		keyMap.put("GameX", KeyEvent.KEYCODE_BUTTON_X); 
		keyMap.put("GameY", KeyEvent.KEYCODE_BUTTON_Y); 
		keyMap.put("GameZ", KeyEvent.KEYCODE_BUTTON_Z); 
		keyMap.put("Select", KeyEvent.KEYCODE_BUTTON_SELECT); 
		keyMap.put("Start", KeyEvent.KEYCODE_BUTTON_START); 
		keyMap.put("ThumbL", KeyEvent.KEYCODE_BUTTON_THUMBL); 
		keyMap.put("ThumbR", KeyEvent.KEYCODE_BUTTON_THUMBR); 
		
	}

}
