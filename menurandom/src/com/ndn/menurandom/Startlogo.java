package com.ndn.menurandom;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import com.ndn.menurandom.db.DBHandler;



public class Startlogo extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logo);
	
		
		View img = (View) findViewById(R.id.logo);
		
		DBHandler dbhandler = DBHandler.open(this);
		Cursor cursor = null;
		cursor = dbhandler.getCheckGPS();
			
		startManagingCursor(cursor);
		cursor.moveToFirst(); // 커서 처음으로 이동 시킴
		String check_gps = cursor.getString(cursor.getColumnIndex("check_gps"));
		dbhandler.close();
		
		if(check_gps.equals("N")){
			AlertDialog.Builder ab=new AlertDialog.Builder(this);
			ab.setMessage("위치 사용정보에 동의 하십니까?").setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					            // Action for 'Yes' Button
							DBHandler setdbhandler = DBHandler.open(getApplicationContext());
							Cursor cursor = null;
							cursor = setdbhandler.setCheckGPS("Y");
								
							setdbhandler.close();
					        }
					        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int id) {
					            // Action for 'NO' Button
					            dialog.cancel();
					        }
					        });
					    AlertDialog alert = ab.create();
					    // Title for AlertDialog
					    alert.setTitle("사용자 동의");
					    // Icon for AlertDialog
					    alert.show();
			
		}
	
		
		final Object obj = new Object();
		img.setTag(obj);
		img.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if(v.getTag() == obj) {
		    		Intent intent = new Intent().setClass(Startlogo.this, MainTabWidgetActivity.class);
		    		startActivity(intent);
		    		finish();
		    	}
		    }
		});
	}
}
