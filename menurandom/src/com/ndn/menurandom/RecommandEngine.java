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
			String query = "SELECT menuName, searchName, pictureName, hot_ment FROM menu WHERE hot ='1'";
			cursor = mDB.rawQuery(query, null);
			break;
		}

		String name = cursor.getString(cursor.getColumnIndex("menuName"));
		String imgName = cursor.getString(cursor.getColumnIndex("pictureName"));
		String searchName = cursor.getString(cursor.getColumnIndex("searchName"));
		String explanation = cursor.getString(cursor.getColumnIndex("hot_ment"));
	
		return new MenuData(name, imgName, searchName, explanation);
	}
}
