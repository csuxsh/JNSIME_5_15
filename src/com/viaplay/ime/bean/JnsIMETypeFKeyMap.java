package com.viaplay.ime.bean;

import com.viaplay.im.hardware.JoyStickTypeF;
/**
 * Type¦Ì?¡ã¡ä?¨¹¨®3¨¦?¨¤¨¤¡ê???¡Àe?¨²¨®¨²scancode¦Ì?¡ã¡ä?¨¹¦Ì??¡Â¨°y
 * 
 *  
 * @author Steven.xu
 *
 */
public class JnsIMETypeFKeyMap extends JnsIMEKeyMap{

	@Override
	public int getScanCode() {
		// TODO Auto-generated method stub
		return JoyStickTypeF.gamePadButoonScanCode[this.getGamPadIndex()/JoyStickTypeF.DISPLAY_ROW][this.getGamPadIndex()%JoyStickTypeF.DISPLAY_ROW];
	}

}
