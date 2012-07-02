package com.ndn.menurandom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class Startlogo extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logo);

		View img = (View) findViewById(R.id.logo);
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
