package com.ndn.menurandom;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ndn.menurandom.db.DBHandler;

public class MainTab2Activity extends Activity implements OnClickListener {
	private String currentState = STATE_FIRST;
	private static String STATE_FIRST = "0";
	
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
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;
    
	/////////////////////////////////////////////////////
	// Weather Variable
    private static final int NONE = 1;
    private static final int SUNNY = NONE + 1;
    private static final int CLOUDY = NONE + 2;
    private static final int RAINY = NONE + 3;
    private static final int SNOWY = NONE + 4;
    
    private int weather = NONE;
	
	
	
	
	
	
	
	
	
	
	
	
	private TextView anjuTextView = null;
	private TextView siksaTextView = null;
	private View anjuButton;
	private View siksaButton;
	private TextView anjuNameEditText;
	private TextView siksaNameEditText;
	private HashMap<String, String> map = new HashMap<String, String>();
	private MenuSlideView mSlideView;
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initView();
	}
	
	protected void onStart() {Log.e("NHK", "onStart");
		super.onStart();
//		findMyLocation();
		getWeatherInformation();
		drawWeather();
	}
	
	protected void onResume() {Log.e("NHK", "onResume");
		super.onResume();
	}
	
	protected void onStop() {Log.e("NHK", "onStop");
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
		
		switch (weather) {
		case SUNNY:
	        Calendar calendar = Calendar.getInstance();
	        Date date = calendar.getTime();
	        // if it's night
	        if( 20 < date.getHours() || 6 > date.getHours() )
	        	imageView.setImageResource(R.drawable.weather_moon);
	        else
	        	imageView.setImageResource(R.drawable.weather_sun);
			break;
		
		case CLOUDY:
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
	
	private void findMyLocation() {
//		searhingLatLng();
	}

	/*
	private void searhingLatLng() {Log.e("NHK", "searhingLatLng");
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationListener = new LocationListener(){
			public void onLocationChanged(Location loc) {
				Toast.makeText(getApplicationContext(), "Latitude: " + String.valueOf(loc.getLatitude()), Toast.LENGTH_SHORT).show();
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				stopSearching();
	        }
	        public void onProviderDisabled(String provider) {
	            Toast.makeText(getApplicationContext(), "현재 위치를 알 수 없습니다.", Toast.LENGTH_SHORT).show();
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
//			② Partly Cloudy
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
			return;
		}
		
		if ( weatherData.equals("Clear") || weatherData.equals("Partly Cloudy") )
			this.weather = SUNNY;
		else if ( weatherData.equals("Mostly Cloudy") || weatherData.equals("Cloudy") )
			this.weather = CLOUDY;
		else if ( weatherData.equals("Rain") || weatherData.equals("Snow/Rain") )
			this.weather = RAINY;
		else if ( weatherData.equals("Snow") )
			this.weather = SNOWY;
		else
			this.weather = NONE;
	}

	/*
	private void stopSearching() {Log.e("NHK", "stopSearching");
		locationManager.removeUpdates(locationListener);
		Log.e("NHK", "latitude: "+latitude+" longitude: "+longitude);
	}
	*/
	
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//		
//		//checkGps();//GPS 상태체크 //일단 보류 
//		
//		LinearLayout frameLayout = (LinearLayout) findViewById(R.id.tab2);
//
//		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//		View view2 = inflater.inflate(R.layout.tab_2, null);
//		frameLayout.addView(view2);
//		
//		
//		anjuTextView = (TextView) findViewById(R.id.anjuTextView);
//		siksaTextView = (TextView) findViewById(R.id.siksaTextView);
//		
//		anjuNameEditText = (TextView) findViewById(R.id.anjuNameEditText);
//		siksaNameEditText = (TextView) findViewById(R.id.siksaNameEditText);
//		
//		anjuButton = findViewById(R.id.anjuButton);
//		siksaButton = findViewById(R.id.siksaButton);
//		
//		anjuButton.setOnClickListener(this);
//		siksaButton.setOnClickListener(this);
//		
//		//layout = (LinearLayout)findViewById(R.id.tLayout);
//		
//		loadKmaXmlRead();//기상청 xml 파싱
//		
//		recommendedAnjuMenu();//안주메뉴추천
//		recommendedSiksaMenu();//안주메뉴추천
//		
//		/*
//		ArrayList arItem = getArrayList("1", "K");;
//        //어댑터를 만듬
//        MyListAdapter MyAdapter = new MyListAdapter(this, R.layout.mylist, arItem);
//       
//        ListView MyList = (ListView)findViewById(R.id.list);
//        //어댑터와 데이터를 연결해서 원하는 리스트뷰에 뿌리게됨
//        MyList.setAdapter(MyAdapter);
//        */
//		mSlideView= (MenuSlideView)findViewById(R.id.menu_slide);
//	}

	public void loadKmaXmlRead(){

		try {
			String html = loadKmaData();

			// DOM 파싱.
			ByteArrayInputStream bai = new ByteArrayInputStream(html.getBytes());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setIgnoringElementContentWhitespace(true);//화이트스패이스 생략
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document parse = builder.parse(bai);// DOM 파서
			// 태그 검색
			NodeList datas = parse.getElementsByTagName("data");

			
			
			// 17개의 data태그를 순차로 접근
			for (int idx = 0; idx < 1; idx++) { //첫번째 로우만 가져옴
				// 필요한 정보들을 담을 변수 생성
				
				
				Node node = datas.item(idx);// data 태그 추출
	
				int childLength = node.getChildNodes().getLength();
				// 자식태그 목록 수정
				NodeList childNodes = node.getChildNodes();
				for (int childIdx = 0; childIdx < childLength; childIdx++) {
					Node childNode = childNodes.item(childIdx);
					int count = 0;
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						count++;
						// 태그인 경우만 처리
						// 금일,내일,모레 구분(시간정보 포함)
						if (childNode.getNodeName().equals("day")) { // (0:오늘/1:내일/2:모레)
							map.put("day", childNode.getFirstChild().getNodeValue());
						} else if (childNode.getNodeName().equals("hour")) {//시간
							map.put("hour", childNode.getFirstChild().getNodeValue());
							// 하늘상태코드 분석
						} else if (childNode.getNodeName().equals("sky")) {//1:맑음  2:구름조금  3:구름많음  4:흐림
							map.put("sky", childNode.getFirstChild().getNodeValue());
						} else if (childNode.getNodeName().equals("wfKor")) {//날씨 한국어
							map.put("wfKor", childNode.getFirstChild().getNodeValue());
						} else if (childNode.getNodeName().equals("temp")) {//현재시간온도
							map.put("temp", childNode.getFirstChild().getNodeValue());
						}else if (childNode.getNodeName().equals("pty")){ //0 : 없음  1 : 비 2 : 비/눈  3 : 눈/비  4 : 눈
							map.put("pty", childNode.getFirstChild().getNodeValue());
						}else if (childNode.getNodeName().equals("reh")){ // 습도%
							map.put("reh", childNode.getFirstChild().getNodeValue());
						}
					}
				}// end 안쪽 for문
				
			}// end 바깥쪽 for문
			
			
		} catch (Exception e) {
			siksaTextView.setText("오류" + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	
	public void onClick(View v) {
		
		if(v.equals(anjuButton)){//안주버튼 클릭시 실행
			Intent intent = new Intent(this, SearchMapActivity.class); //지도 검색 Class 설정
			intent.putExtra("search_menu", anjuNameEditText.getText().toString());
			startActivity(intent); // 액티비티를 실행합니다.
		}
		
		if(v.equals(siksaButton)){//식사버튼 클릭시 실행
			Intent intent = new Intent(this, SearchMapActivity.class); //지도 검색 Class 설정
			intent.putExtra("search_menu", siksaNameEditText.getText().toString());
			startActivity(intent); // 액티비티를 실행합니다.
		}
	}
	
	/*
	 * 안주 메뉴추천
	 */
	public void recommendedAnjuMenu(){
		try {
			String resultMenu = menuSelection(map);//날씨상태 파라메터 설정
			/*
			resultMenu += " 추천 메뉴는 : " + dataSelect("2");//1:식사, 2:안주
			HashMap itemMap = getRecommendedItem();
			resultMenu += "\n snow : " +  itemMap.get("snow");
			resultMenu += " rain : " +  itemMap.get("rain");
			resultMenu += " hot : " +  itemMap.get("hot");
			resultMenu += " cold : " +  itemMap.get("cold");
			*/
			
			anjuTextView.setText(resultMenu);
			anjuNameEditText.setText(dataSelect("2"));//1:식사, 2:안주
			
			
			//int resId = getResources().getIdentifier("img1", "drawable", "com.ndn.menurandom");
			
			//ImageView image = new ImageView(this);
			
			//image.setImageResource(resId);
			//layout.removeAllViews();
			//layout.addView(image);
			
		} catch (Exception e) {
			anjuTextView.setText("오류" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/*
	 * 식사 메뉴추천
	 */
	public void recommendedSiksaMenu(){
		try {
			String resultMenu = menuSelection(map); //날씨상태 파라메터 설정
			/*
			resultMenu += " 추천 메뉴는 : " + dataSelect("1");//1:식사, 2:안주
			HashMap itemMap = getRecommendedItem();
			resultMenu += "\n snow : " +  itemMap.get("snow");
			resultMenu += " rain : " +  itemMap.get("rain");
			resultMenu += " hot : " +  itemMap.get("hot");
			resultMenu += " cold : " +  itemMap.get("cold");
			*/
			siksaTextView.setText(resultMenu);
			siksaNameEditText.setText(dataSelect("1"));//1:식사, 2:안주
			
		} catch (Exception e) {
			siksaTextView.setText("오류" + e.getMessage());
			e.printStackTrace();
		}
	}	
	
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
	
	private String dispName(String code){
		String dispName = "";
		
		char c = code.charAt(0);
		switch(c){
		case '0' : dispName = "없음";
				   break;
		case '1' : dispName = "비";
		   break;
		case '2' : dispName = "비/눈";
		   break;		   
		case '3' : dispName = "눈/비";
		   break;	
		case '4' : dispName = "눈";
		   break;
		}
		
		return dispName;
	}
	
	private String menuSelection(HashMap map){
		
		String result = "";
		
		result = "날씨 : " + map.get("wfKor") + "\n";
		result += "온도 : " + map.get("temp") + "도\n"; 
		result += "습도 : " + map.get("reh") + "%\n";
		result += dispName(map.get("pty").toString()) + "%";
		
		return result;
	
	}

	// 기상청 날씨정보 추출
	private String loadKmaData() throws Exception {
		String page = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=63&gridy=123";
		URL url = new URL(page);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		if (urlConnection == null)
			return null;
		urlConnection.setConnectTimeout(10000);// 최대 10초 대기
		urlConnection.setUseCaches(false);// 매번 서버에서 읽어오기
		StringBuilder sb = new StringBuilder();// 고속 문자열 결합체
		if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream inputStream = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);

			// 한줄씩 읽기
			BufferedReader br = new BufferedReader(isr);
			while (true) {
				String line = br.readLine();// 웹페이지의 html 코드 읽어오기
				if (line == null)
					break;// 스트림이 끝나면 null리턴
				sb.append(line + "\n");
			}// end while
			br.close();
		}// end if
		return sb.toString();
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
