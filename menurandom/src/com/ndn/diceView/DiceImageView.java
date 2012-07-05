package com.ndn.diceView;

import com.ndn.menurandom.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import com.ndn.diceView.Dice;

public class DiceImageView extends View {
	private Thread mThread;	
	private SurfaceHolder mSurfaceHolder;
	private int mWidth;
	private int mHeight;
	
	private int mDeviceWidth, mDeviceHeight;
	
	private Dice dice;
	private Bitmap[] bitmap = new Bitmap[12];
	private int bitmapIndex = 0;
	
	private Object mLock = new Object();
	private boolean diceStop = false;
	
	public DiceImageView(Context context) {
		super(context);
		
		// get Device width, height
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		mDeviceWidth = display.getWidth();
		mDeviceHeight = display.getHeight();
		
		// load dice images
		bitmap[0] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_1_1);
		bitmap[1] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_1);
		bitmap[2] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_2_1);
		bitmap[3] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_2);
		bitmap[4] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_3_1);
		bitmap[5] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_3);
		bitmap[6] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_4_1);
		bitmap[7] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_4);
		bitmap[8] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_5_1);
		bitmap[9] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_5);
		bitmap[10] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_6_1);
		bitmap[11] =BitmapFactory.decodeResource(getResources(), R.drawable.red_dice_6);
		
		
		
	}
	
	@Override
	public void draw(Canvas c) {
		
		c.drawARGB(0, 255, 255, 255);				
		c.drawARGB(0, 0,0,0);
		if(dice != null)
		{
			c.drawBitmap(bitmap[bitmapIndex], (int)dice.x, (int)dice.y, null);
			
			if(!diceStop)
			{
				updatePhisics();
				// change bitmap image
				if (++bitmapIndex > 11)
					bitmapIndex = 0;
			}
			
			// stop the dice
			if ( Math.abs( (int)dice.dx ) < 2 || Math.abs( (int) dice.dy ) < 2 )
			{
				diceStop = true;
				if(mThread != null)
				{
					mThread.interrupt();
					mThread = null;
				}
			}
		}
	}
	
	
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if(visibility == View.GONE)
		{
			dice = null;
			
			if (mThread != null && mThread.isAlive())
			{
				mThread.interrupt();
				mThread = null;
			}
		}
		else if(visibility == View.VISIBLE)
		{
			diceStop = false;
			
			if(dice == null)
			{
				dice = new Dice();
				dice.x = (int) (Math.random() * (mWidth-dice.width));
				dice.y = (int) (Math.random() * (mHeight-dice.height));
			}
			
			if (mThread == null)
			{
				mThread = new DiceThread();
				mThread.start();
			}
		
		}
	}
	
	public void updatePhisics() {
		float temp_x, temp_y;
		
		temp_x = dice.x + dice.dx;
		temp_y = dice.y + dice.dy;

		// boundary check
		if (temp_x < 0) {
			dice.dx = dice.dx * -1;
			temp_x += getDistance(temp_x, 0);
		} else if (temp_x + dice.width > mDeviceWidth ) {
			dice.dx = dice.dx * -1;
			temp_x = mDeviceWidth - getDistance(temp_x, mDeviceWidth);
		}
		dice.x = temp_x;

		if (temp_y < 0 ) {
			dice.dy = dice.dy * -1;
			temp_y += getDistance(temp_y, 0);
		} else if ( temp_y + dice.height > mDeviceHeight-65 ) {		// except for height of status bar(40) and title bar(25) 
			dice.dy = dice.dy * -1;
			temp_y = mDeviceHeight - getDistance(temp_y, mDeviceHeight);
		}
		dice.y = temp_y;
		
		// decrease velocity by friction force
		dice.dx = dice.dx * 0.96f;
		dice.dy = dice.dy * 0.96f;
		
		Log.e("NHK", "x: " + String.valueOf(dice.x) + "    y: " + String.valueOf(dice.y));
	}
	
	private float getDistance(float a, float b) {
		return (float) (Math.sqrt(Math.pow(a-b, 2)));
	}

	
	public	class DiceThread extends Thread {
		public void run() {
			while (!isInterrupted()) {
	
				postInvalidate();
				try {
					sleep(50);
				} catch (InterruptedException e) {
					//interrupt();
				}
			}
		}
		
	}
}
