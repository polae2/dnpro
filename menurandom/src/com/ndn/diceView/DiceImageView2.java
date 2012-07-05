package com.ndn.diceView;

import com.ndn.menurandom.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import com.ndn.diceView.Dice;

public class DiceImageView2 extends SurfaceView implements SurfaceHolder.Callback {
	private Thread mThread;	
	private SurfaceHolder mSurfaceHolder;
	private int mWidth;
	private int mHeight;
	
	private int mDeviceWidth, mDeviceHeight;
	
	private Dice dice;
	private Bitmap[] bitmap = new Bitmap[12];
	
	private Object mLock = new Object();
	
	
	public DiceImageView2(Context context) {
		super(context);
	 
		
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		
		
		
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
	
//	public void startThread() {
//		if (!mThread.isAlive())		
//			mThread.start();
//	}
	
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mWidth = width;
		mHeight = height;
		
		Canvas c = null;
		synchronized(mLock){
			try {
				
				c = mSurfaceHolder.lockCanvas(null);
				if(c != null)
				{
					dice = null;
					dice = new Dice();
					
					dice.x = (int) (Math.random() * (mWidth-dice.width));
					dice.y = (int) (Math.random() * (mHeight-dice.height));
					
					c.drawARGB(255, 255, 255, 255);				
					c.drawARGB(0, 0,0,0);
					c.drawBitmap(bitmap[0], (int)dice.x, (int)dice.y, null);
				}
			} finally {
				if (c != null)
					mSurfaceHolder.unlockCanvasAndPost(c);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (mThread == null)
		{
			mThread = new DiceThread();
			mThread.start();
		}
			
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mThread != null && mThread.isAlive())
		{
			mThread.interrupt();
			mThread = null;
		}
	}
public	class DiceThread extends Thread {
		public void run() {
			int i = 0;
			while (!isInterrupted()) {
				synchronized (mLock) {
	
					if(dice != null)
					{
						Canvas c = null;
						try {
							c = mSurfaceHolder.lockCanvas(null);
							if(c != null ) {
								c.drawARGB(255,255,255,255);
								c.drawARGB(0, 0,0,0);
								c.drawBitmap(bitmap[i], (int)dice.x, (int)dice.y, null);
							}
						} finally {
							if (c != null)
								mSurfaceHolder.unlockCanvasAndPost(c);
						}
						
						updatePhisics();
						// change bitmap image
						if (++i > 11)
							i = 0;
						
						// stop the dice
						if ( Math.abs( (int)dice.dx ) < 2 || Math.abs( (int) dice.dy ) < 2 )
							break;
					}
				}
				
				try {
					sleep(50);
				} catch (InterruptedException e) {
					//interrupt();
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
	}
}
