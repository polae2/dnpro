package com.ndn.menurandom;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ndn.menurandom.data.MenuData;
import com.ndn.menurandom.db.DBHelper;

public class RecommandEngine {
	private Context mContext;
	private DBHelper mHelper;
	private SQLiteDatabase mDB;
	
	private int mWeather;
	private Date mTime;
	
	private RecommandEngine(Context context) {
		mContext = context;
		mHelper = new DBHelper(context);
		
		try {
			mHelper.createDataBase();
			mDB = mHelper.openDataBase();
		} catch (Exception e) {
			Log.e("NHK", "Error ocurred to create Database!!!");
		}
		
		mTime = Calendar.getInstance().getTime();
	}
	
	public RecommandEngine(Context context, int weather) {
		this(context);
		
		mWeather = weather;
	}
	
	public MenuData getRecommandMenuData() {
		Cursor cursor = null;

		switch (mWeather) {
		case MainTab2Activity.NONE:
		case MainTab2Activity.SUNNY:
		case MainTab2Activity.CLOUDY:
		case MainTab2Activity.RAINY:
		case MainTab2Activity.SNOWY:
			break;
		}
		String name = "감자수프";
		String imgName = "img1.png";
		String searchName = "스프";
		String explanation = "하하하하";
		
		return new MenuData(name, imgName, searchName, explanation);
	}
}
