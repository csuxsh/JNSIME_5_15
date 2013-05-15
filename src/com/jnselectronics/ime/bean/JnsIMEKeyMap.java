package com.jnselectronics.ime.bean;

public abstract class JnsIMEKeyMap {
	
	private int gamPadIndex;
	private String lable;
	private int keyCode;
	
	abstract public int getScanCode();
	
	public int getGamPadIndex() {
		return gamPadIndex;
	}
	public void setGamPadIndex(int gamPadIndex) {
		this.gamPadIndex = gamPadIndex;
	}
	public String getLable() {
		return lable;
	}
	public void setLable(String lable) {
		this.lable = lable;
	}
	public int getKeyCode() {
		return keyCode;
	}
	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

}
