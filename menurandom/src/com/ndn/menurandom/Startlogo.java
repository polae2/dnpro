package com.ndn.menurandom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Startlogo extends Activity {
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			Intent intent = new Intent(this, MainTabWidgetActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
}
