package com.viaplay.ime.bean;

/**
 *  映射信息的数据结构,不同的硬件需要自行继承这个类,并实现getScanCode方法
 *  
 *  
 * @author Administrator
 *
 */
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
