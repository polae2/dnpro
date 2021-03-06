package com.ndn.menurandom;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ndn.menurandom.data.MenuData;
import com.ndn.menurandom.db.DBHandler;
import com.ndn.menurandom.search.SearchMapActivity;

public class MainTab2Activity extends Activity implements OnClickListener {
	private String currentState = STATE_FIRST;
	private static String STATE_FIRST = "0";
	private ImageDownloader downloader;
	
	/////////////////////////////////////////////////////
	// Back button variable
	private int backPressedCount = 0;
	private long backPressedStartTime = 0;
	private int doublePressedTimeThresHold = 300;

	/////////////////////////////////////////////////////
	// Tab2 Variable
//	private static final String DAUM_API_KEY = "80eff4071090b19ab6ec0fc09de77f39f5cefee6";
	private static final String KMA_URL = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=60&gridy=127";	// Jong-Ro 2 st.
	
	
	
	/////////////////////////////////////////////////////
	// Location Variable
//    private LocationManager locationManager;
//    private LocationListener locationListener;
//    private double latitude;
//    private double longitude;
    
	/////////////////////////////////////////////////////
	// Weather Variable
    public static final int NONE = 1;
    public static final int SUNNY = NONE + 1;
    public static final int CLOUDY = NONE + 2;
    public static final int RAINY = NONE + 3;
    public static final int SNOWY = NONE + 4;
    
    private int weather = NONE;
	
	/////////////////////////////////////////////////////
	// Menu Variable	
	private MenuData menuData;
	
	
	

	private HashMap<String, String> map = new HashMap<String, String>();
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initView();
	}
	
	protected void onStart() {
		super.onStart();
//		findMyLocation();
		getWeatherInformation();
		getRecommandMenu();
	}
	
	protected void onResume() {
		super.onResume();
	}
	
	protected void onStop() {
//		stopSearching();
		
		super.onStop();
	}
	
	private void initView() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.tab2);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.tab_2, null);
		layout.addView(view);
	}
	
	private void drawWeather() {
		ImageView imageView = (ImageView) findViewById(R.id.weatherImage);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
		switch (weather) {
		case SUNNY:
	        // if it's night
	        if( 20 < date.getHours() || 6 > date.getHours() )
	        	imageView.setImageResource(R.drawable.weather_moon);
	        // if it's daytime
	        else
	        	imageView.setImageResource(R.drawable.weather_sun);
			break;
		
		case CLOUDY:
	        // if it's night
	        if( 20 < date.getHours() || 6 > date.getHours() )
	        	imageView.setImageResource(R.drawable.weather_moon);
	        // if it's daytime
	        else
				imageView.setImageResource(R.drawable.weather_cloud);
			break;
		
		case RAINY:
			imageView.setImageResource(R.drawable.weather_rain);
			break;
		
		case SNOWY:
			imageView.setImageResource(R.drawable.weather_snow);
			break;
		
		case NONE:
		default:
			imageView.setImageResource(R.drawable.weather_none);
			break;
		}
	}
	
	private void drawMenu(MenuData menuData) {
		
		ImageView imageview = (ImageView) findViewById(R.id.weatherfood_view);
		downloader = new ImageDownloader(this, "/cache/menurandom/png", R.drawable.ic_launcher, false);
		//ImageView imageView = (ImageView) findViewById(R.id.img_View);
		String url = "http://211.190.5.182/jpgdown/png/" + menuData.imgName + ".png";
		downloader.download(url, imageview);
		
//		ImageView image = (ImageView) findViewById(R.id.menu_image);
//		image.setImageResource(R.drawable.img1);

		TextView text = (TextView) findViewById(R.id.menu_explanation);
		text.setText(menuData.explanation);
		
		// search other menu
		Button btn1 = (Button) findViewById(R.id.btn_othermenu);
		btn1.setOnClickListener(this);

		// search a restaurant
		Button btn2 = (Button) findViewById(R.id.btn_search);
		btn2.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		
		switch (v.getId()) {

		case R.id.btn_othermenu:
			getRecommandMenu();
			break;
			
		case R.id.btn_search:
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
			
			if (networkInfo == null)
				Toast.makeText(this, "네트워크 연결이 안되어 있습니다.", Toast.LENGTH_LONG).show();
			else
				searchMap();
			break;
		}
	}
	
//	private void findMyLocation() {
//		searhingLatLng();
//	}

	/*
	private void searhingLatLng() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationListener = new LocationListener(){
			public void onLocationChanged(Location loc) {
				Toast.makeText(getApplicationContext(), "Latitude: " + String.valueOf(loc.getLatitude()), Toast.LENGTH_SHORT).show();
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				stopSearching();
	        }
	        public void onProviderDisabled(String provider) {
	            Toast.makeText(getApplicationContext(), "Can't not find current location", Toast.LENGTH_SHORT).show();
	        }
	        public void onProviderEnabled(String provider) {}
	        public void onStatusChanged(String provider, int status, Bundle extras) {}			
		};
		
		int millis = 5000;
        int distance = 5;
                
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, millis, distance, locationListener);
	}
	*/
	
	private void getWeatherInformation() {
		if (!checkNetwork())
			return;
		
		boolean weather = false;
		String weatherData=null;

		try {
			URL url = new URL(KMA_URL);
			XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserCreator.newPullParser();
			parser.setInput(url.openStream(), null);

			int parserEvent = parser.getEventType();

			// section for improving speed
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				if ( parserEvent == XmlPullParser.START_TAG ) {
					if ( parser.getName().equals( "wfKor" ) )
						break;
				}
				parserEvent = parser.next();
			}
	
////////////////////////////////////////////////////
//			Weather Code
//			
//			① Clear
//			② Little Cloudy
//			③ Mostly Cloudy
//			④ Cloudy
//			⑤ Rain
//			⑥ Snow/Rain
//			⑦ Snow
		    
			// get weather data
			boolean isEnd = false;
			while (parserEvent != XmlPullParser.END_DOCUMENT && isEnd == false) {
				switch (parserEvent) {
				case XmlPullParser.START_TAG:
					if (parser.getName().equals("wfEn")) { weather = true; }
					break;

				case XmlPullParser.TEXT:
					if (weather) {
						weatherData = parser.getText();
						isEnd = true;
					}
					break;
				}
				parserEvent = parser.next();
			}
		} catch (Exception e) {
			Log.e("NHK", "weather ERROR");
			
		}
		
		if(weatherData == null)
		{
			weatherData = "UNKNOWN";
		}
		
		if ( weatherData.equals("Clear") || weatherData.equals("Little Cloudy") )
			this.weather = SUNNY;
		else if ( weatherData.equals("Mostly Cloudy") || weatherData.equals("Cloudy") )
			this.weather = CLOUDY;
		else if ( weatherData.equals("Rain") || weatherData.equals("Snow/Rain") )
			this.weather = RAINY;
		else if ( weatherData.equals("Snow") )
			this.weather = SNOWY;
		else
			this.weather = NONE;
		
		drawWeather();
	}
	
	private void getRecommandMenu() {
		menuData = new RecommandEngine(this, weather).getRecommandMenuData();
		drawMenu(menuData);
	}
	
	private void searchMap() {
		DBHandler dbhandler = DBHandler.open(this);
		Cursor cursor = null;
		cursor = dbhandler.getCheckGPS();
			
		startManagingCursor(cursor);
		cursor.moveToFirst(); // 커서 처음으로 이동 시킴
		String check_gps = cursor.getString(cursor.getColumnIndex("check_gps"));
		dbhandler.close();
		
		if(check_gps.equals("Y")){
			Intent intent = new Intent(this, SearchMapActivity.class);
			intent.putExtra("search_menu", menuData.searchName);
			startActivity(intent);
		}
		else{
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
		
	}
	
	private boolean checkNetwork() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		
		if (networkInfo == null)
			return false;
		else
			return true;
	}

	/*
	private void stopSearching() {Log.e("NHK", "stopSearching");
		locationManager.removeUpdates(locationListener);
		Log.e("NHK", "latitude: "+latitude+" longitude: "+longitude);
	}
	*/
	
	/*
	 * 추천 메뉴를 조회식 검색조건 만들어서 Map으로 넘겨줌
	 */
	public HashMap getRecommendedItem(){
		HashMap<String, String> itemMap = new HashMap<String, String>();
		
		int  temp = (int)Math.round(Double.parseDouble(map.get("temp")));
		int  pty = (int)Integer.parseInt((map.get("pty")));
		
		if(temp >= 20){ //20도 이상인지 체크
			itemMap.put("hot", "1");
		}
		
		if(temp < 20){ //20도 미만인지 체크
			itemMap.put("cold", "1");
		}
		
		//pty코드값 == 0:없음, 1:비, 2:비/눈, 3:눈/비, 4:눈
		if(pty == 1 || pty == 2 || pty == 3){ //pty 값이 1,2,3 이면 비옴.
			itemMap.put("rain", "1");
		}		
		
		//pty코드값 == 0:없음, 1:비, 2:비/눈, 3:눈/비, 4:눈
		if(pty == 2 || pty == 3|| pty == 4){ //pty 값이 2,3,4 이면 비옴.
			itemMap.put("snow", "1");
		}				
		
		return itemMap;
	}
	
	private ArrayList getArrayList(String code, String detailCode){
		DBHandler dbhandler = DBHandler.open(this);
		Cursor cursor = dbhandler.getArrayList(code, detailCode);
        startManagingCursor(cursor);
        
		
	   //데이터를 만듬(ac220v)
	   ArrayList<MyItem> arItem = new ArrayList<MyItem>();
       MyItem mi;
        
        if (cursor.moveToFirst()) {
            do {
	            String id = cursor.getString(cursor.getColumnIndex("id"));
	            String menuName = cursor.getString(cursor.getColumnIndex("menuName"));
	            
	            mi = new MyItem(id, menuName, R.drawable.ic_launcher);
	            arItem.add(mi);    
            } while (cursor.moveToNext());
        }
        
		dbhandler.close();
		return arItem;
	}	
	
	
	private String dataSelect(String code){
		DBHandler dbhandler = DBHandler.open(this);
		
		HashMap itemMap = getRecommendedItem();
		itemMap.put("code", code);
		
		Cursor cursor = dbhandler.randomRecommended(itemMap); 
        startManagingCursor(cursor);
        cursor.moveToFirst(); //커서 처음으로 이동 시킴
        String result = cursor.getString(cursor.getColumnIndex("menuName"));
		dbhandler.close();
		return result;
	}
	

	
//************************************************************************
// 개발자 : 김두현
// 개발버전 : VER 1.000
// 개발일시 : 12. 06. 14
// 개발내용 : 백버튼 클릭시 처리 함수
//************************************************************************
	public void onBackPressed(){

		if(currentState == STATE_FIRST){
		
		// 첫번째 버튼을 클릭하면, 
		// 1. 시간을 측정한다.
		// 2. 뒤로 가기 버튼 클릭 횟수를 증가시킨다.
			long currentTime = System.currentTimeMillis();
			if(backPressedCount == 0)
			{
				Toast toast = Toast.makeText(this, "한번 더 누르면 종료됩니다", 200);
				toast.show();
				backPressedStartTime = currentTime;
				backPressedCount++;
				//Log.d("Test", "currentTime : " + currentTime);
			}
			else if(backPressedCount == 1 && (currentTime - backPressedStartTime) < doublePressedTimeThresHold)
			{
				//Log.d("Test", "double Clicked");
				// 두번째 클릭한 것 처리
				finish();   // 완전종료
				android.os.Process.killProcess(android.os.Process.myPid());
				backPressedCount = 0;
			}
			else
			{
				//Log.d("Test", "Over");
				// 시간을 초과했을 경우
				backPressedStartTime = currentTime;
			}
		}
	}
//******************************* 끝 *************************************
}
