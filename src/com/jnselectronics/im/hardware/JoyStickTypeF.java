package com.jnselectronics.im.hardware;



public class JoyStickTypeF {
	public static final int BUTTON_SEARCH_SCANCODE = 217;
	public static final int BUTTON_A_SCANCODE = 304;
	public static final int BUTTON_B_SCANCODE = 305;
	public static final int BUTTON_X_SCANCODE = 307;
	public static final int BUTTON_Y_SCANCODE = 308;
	public static final int BUTTON_UP_SCANCODE = 103;
	public static final int BUTTON_DOWN_SCANCODE = 108;
	public static final int BUTTON_RIGHT_SCANCODE = 106;
	public static final int BUTTON_LEFT_SCANCODE = 105;
	public static final int BUTTON_SELECT_SCANCODE = 314;
	public static final int BUTTON_START_SCANCODE = 315;
	public static final int BUTTON_L1_SCANCODE = 310;
	public static final int BUTTON_L2_SCANCODE = 312;
	public static final int BUTTON_R1_SCANCODE = 311;
	public static final int BUTTON_R2_SCANCODE = 313;
	public static final int STICK_L = 15;
	public static final int STICK_R = 16;
	public static final int BUTTON_XP_SCANCODE = 0x7f01;
	public static final int BUTTON_XI_SCANCODE = 0x7f02;
	public static final int BUTTON_YP_SCANCODE = 0x7f03;
	public static final int BUTTON_YI_SCANCODE = 0x7f04;
	public static final int BUTTON_ZP_SCANCODE = 0x7f05;
	public static final int BUTTON_ZI_SCANCODE = 0x7f06;
	public static final int BUTTON_RZP_SCANCODE = 0x7f07;
	public static final int BUTTON_RZI_SCANCODE = 0x7f08;
	
	public static final int RIGHT_JOYSTICK_TAG = 1;
	public static final int LEFT_JOYSTICK_TAG = 2;
	public static final int JOYSTICK_ZOOM_1_TAG =0x3;
	public static final int JOYSTICK_ZOOM_2_TAG =0x4;
	
	
	private final static int BUTTON_L1=18;//3;
	private final static int BUTTON_SELECT=7;
	private final static int BUTTON_START=8;
	private final static int BUTTON_R1=29;//12;
	private final static int BUTTON_L2=3;//18;
	private final static int BUTTON_R2=12;//29;
	private final static int BUTTON_UP=33;
	private final static int BUTTON_Y=46;
	private final static int BUTTON_LEFT=48;
	private final static int BUTTON_RIGHT=50;
	private final static int BUTTON_X=61;
	private final static int BUTTON_B=63;
	private final static int BUTTON_DOWN=65;
	private final static int BUTTON_A=78;
	private final static int BUTTON_YP=81;
	private final static int BUTTON_RZP=94;
	private final static int BUTTON_XI=96;
	private final static int BUTTON_XP=98;
	private final static int BUTTON_ZI=109;
	private final static int BUTTON_ZP=111;
	private final static int BUTTON_YI=113;
	private final static int BUTTON_RZI=126;
	
	/*
	private final static int DISPLAY_COL = 8;
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
	*/
	
	public final static int DISPLAY_ROW = 16;
	public final static int DISPLAY_COL = 8;
	
	
	public final static int gamePadButoonScanCode[][] = 
	{
		{0,  0,  0,  BUTTON_L2_SCANCODE,  0,  0,  0,  BUTTON_SELECT_SCANCODE,  BUTTON_START_SCANCODE,  0,  0,  0, BUTTON_R2_SCANCODE, 0,  0,  0},
		{0,  0,  BUTTON_L1_SCANCODE,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_R1_SCANCODE, 0,  0},
		{0,  BUTTON_UP_SCANCODE,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_Y_SCANCODE, 0},
		{BUTTON_LEFT_SCANCODE,  0,	BUTTON_RIGHT_SCANCODE,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  BUTTON_X_SCANCODE, 0,BUTTON_B_SCANCODE},
		{0,  	BUTTON_DOWN_SCANCODE, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_A_SCANCODE,  0},
		{0,  BUTTON_YP_SCANCODE, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_RZP_SCANCODE,  0},
		{BUTTON_XI_SCANCODE,  0 ,BUTTON_XP_SCANCODE,0,  0,  0,  0,  0,  0,  0,  0,  0,   0,  BUTTON_ZI_SCANCODE, 0, BUTTON_ZP_SCANCODE},
		{0,  BUTTON_YI_SCANCODE,0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  BUTTON_RZI_SCANCODE,0},
	};
	public final static int gamePadButoonIndex[][] = 
	{
		{0,  0,  0,  BUTTON_L2,  0,  0,  0,  BUTTON_SELECT,  BUTTON_START,  0,  0,  0, BUTTON_R2, 0,  0,  0},
		{0,  0,  BUTTON_L1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_R1, 0,  0},
		{0,  BUTTON_UP,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_Y, 0},
		{BUTTON_LEFT,  0,	BUTTON_RIGHT,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  BUTTON_X, 0,BUTTON_B},
		{0,  	BUTTON_DOWN, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_A,  0},
		{0,  BUTTON_YP, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, BUTTON_RZP,  0},
		{BUTTON_XI,  0 ,BUTTON_XP,0,  0,  0,  0,  0,  0,  0,  0,  0,   0,  BUTTON_ZI, 0, BUTTON_ZP},
		{0,  BUTTON_YI,0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  BUTTON_RZI,0},
	};
	/*
	public final static String gamePadButoonLable[][] = 
	{
		{null,  null,  null,  "L1",  null,  null,  null,  "Select",  "Start",  null,  null,  null,  "R1", null,  null,  null},
		{null,  null,  "L2",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "R2", null,  null},
		{null,  "Up",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "Y", null},
		{"Left",  null,	"Right",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "X", null, "B"},
		{null,  	"Down", null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null, "A",  null},
		{null,  "Y+", null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null, "RZ+",  null},
		{"X-",  null ,"X+",null,  null,  null,  null,  null,  null,  null,  null,  null,   null,  "Z-", null, "Z-"},
		{null,   "Y-",null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "RZ+",null},
	};*/
	public final static String gamePadButoonLable[][] = 
	{
		{null,  null,  null,  "",  null,  null,  null,  "",  "",  null,  null,  null,  "", null,  null,  null},
		{null,  null,  "",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "", null,  null},
		{null,  "",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "", null},
		{"",  null,	"",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "", null, ""},
		{null,  "", null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null, "",  null},
		{null,  "", null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null, "",  null},
		{"",  null ,"",null,  null,  null,  null,  null,  null,  null,  null,  null,   null,  "", null, ""},
		{null,   "",null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "",null},
	};

}
