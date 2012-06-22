package com.ndn.menurandom;

import com.ndn.menurandom.db.DBHandler;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class MainTabWidgetActivity extends TabActivity implements OnTabChangeListener{
	/** Called when the activity is first created. */
	TabHost tabHost;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, MainTab1Activity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("Random")
				.setIndicator("랜덤선택",res.getDrawable(R.drawable.ic_tab1_state))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, MainTab2Activity.class);
		spec = tabHost
				.newTabSpec("Recommended")
				.setIndicator("메뉴추천", res.getDrawable(R.drawable.ic_tab2_state))
				.setContent(intent);
		tabHost.addTab(spec);

//		intent = new Intent().setClass(this, MainTab3Activity.class);
		spec = tabHost
				.newTabSpec("Map")
				.setIndicator("주변검색", res.getDrawable(R.drawable.ic_tab3_state))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setOnTabChangedListener(this);

		tabHost.setCurrentTab(0);
		onTabChanged("Random");
		
		for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
	    {
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.rgb(0, 0, 0));
			
	    }
				
				
				//TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	}

	public void onTabChanged(String tabId) {
		TabWidget tw = tabHost.getTabWidget();
		  
	     if(tabId.equals("Random")) {
	         tw.getChildAt(0).setBackgroundResource(R.drawable.ic_tab1);
	         tw.getChildAt(1).setBackgroundResource(R.drawable.ic_tab2);
	         tw.getChildAt(2).setBackgroundResource(R.drawable.ic_tab2);
	     } else if(tabId.equals("Recommended")) {
	         tw.getChildAt(0).setBackgroundResource(R.drawable.ic_tab2);
	         tw.getChildAt(1).setBackgroundResource(R.drawable.ic_tab1);
	         tw.getChildAt(2).setBackgroundResource(R.drawable.ic_tab2);
	     } else if(tabId.equals("Map")) {
	         tw.getChildAt(0).setBackgroundResource(R.drawable.ic_tab2);
	         tw.getChildAt(1).setBackgroundResource(R.drawable.ic_tab2);
	         tw.getChildAt(2).setBackgroundResource(R.drawable.ic_tab1);
	     }
	}
}