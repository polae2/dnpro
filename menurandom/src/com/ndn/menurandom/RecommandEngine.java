package com.ndn.menurandom;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.ndn.menurandom.data.MenuData;
import com.ndn.menurandom.data.MenuInfo;
import com.ndn.menurandom.db.DBHandler;
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
		String name = null;
		String imgName = null;;
		String searchName = null;;
		String explanation = null;;
		switch (mWeather) {
		case MainTab2Activity.NONE:
		case MainTab2Activity.SUNNY:
		case MainTab2Activity.CLOUDY:
		case MainTab2Activity.RAINY:
		case MainTab2Activity.SNOWY:
			break;
		}
		
		Random random = new Random(System.nanoTime());
		int r = random.nextInt();
		Cursor cursor1 = getWheatherCount();
		cursor1.moveToFirst(); // 커서 처음으로 이동 시킴
		String result = cursor1.getString(cursor1.getColumnIndex("cnt"));
		
		int temp_random_cnt = Integer.parseInt(result);
		int temp_random = r % temp_random_cnt;
		
		Cursor cursor2 = getWeatherRandomList();
		int cnt = 0;
        if (cursor2.moveToFirst()) {
            do {
            	if(temp_random==cnt){
            		name = cursor2.getString(cursor2.getColumnIndex("menuName"));
        			imgName = cursor2.getString(cursor2.getColumnIndex("pictureName"));
            		searchName = cursor2.getString(cursor2.getColumnIndex("searchName"));
            		explanation = cursor2.getString(cursor2.getColumnIndex("hot_ment"));
            	}
            	cnt = cnt + 1;
            	
            } while (cursor2.moveToNext());
        }
		//String temp_String2 = String.valueOf(r);
		//String dtemp_String = temp_String2.substring(temp_String2
		//		.length() - 1);
		//int abc = Integer.parseInt(dtemp_String);
		Toast toast = Toast.makeText(mContext, name, 2);
		toast.show();
		Toast toast2 = Toast.makeText(mContext, imgName, 2);
		toast2.show();
		
		return new MenuData(name, imgName, searchName, explanation);
	}
	
	
    public Cursor getWheatherCount() throws SQLException{
    	Cursor cursor = null;
    	
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("select count(id) cnt ");
    	sb.append(" from menu ");
    	sb.append(" where  hot='1'");
    	
    	cursor=mDB.rawQuery(sb.toString(), null);
    	
    	return cursor;
    }
    
    
    public Cursor getWeatherRandomList() throws SQLException{
    	Cursor cursor = null;
    	
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("select id, menuName, pictureName, SearchName, hot_ment ");
    	sb.append(" from menu ");
    	sb.append(" where  hot='1'");
    	
    	cursor=mDB.rawQuery(sb.toString(), null);
    	
    	return cursor;
    }
}
