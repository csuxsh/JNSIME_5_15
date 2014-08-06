package com.viaplay.ime.uiadapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.widget.ImageView;

public class FloatView extends ImageView {
	
	@SuppressWarnings("unused")
	private boolean clear = false;

	public FloatView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void clearCanvas()
	{
		clear = true;
	}
	@Override
	public void onDraw(Canvas c)
	{
		//if(clear)
		{
			c.drawColor(0,PorterDuff.Mode.CLEAR); 
			clear = false;
		}
		super.onDraw(c);
	}

}
