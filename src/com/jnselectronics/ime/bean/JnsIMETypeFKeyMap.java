package com.jnselectronics.ime.bean;

import com.jnselectronics.im.hardware.JoyStickTypeF;

public class JnsIMETypeFKeyMap extends JnsIMEKeyMap{

	@Override
	public int getScanCode() {
		// TODO Auto-generated method stub
		return JoyStickTypeF.gamePadButoonScanCode[this.getGamPadIndex()/JoyStickTypeF.DISPLAY_ROW][this.getGamPadIndex()%JoyStickTypeF.DISPLAY_ROW];
	}

}
