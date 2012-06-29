package com.ndn.menurandom.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ndn.menurandom.data.RestaurantData;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

public class SearchMapActivity extends NMapActivity {
	private static final boolean DEBUG_MODE = true;
	// set my API key which is registered for com.ndn.menurandom package
	private static final String NAVER_MAP_KEY = "749a7f89c8934b5d50a24f3a9ca8af01";
	private String SEARCH_MENU;
	private static final int SEARCH_INDEX = 3;
	
	/*	private String BACKBTNCURRENT = BACKBTN_NOTCOMPLEAT;
	private static String BACKBTN_NOTCOMPLEAT = "0";	
	private static String BACKBTN_COMPLEAT = "1";*/
	
	////////////////////////////////////////////////////////////////////////////
	// Essential Variables
	private MapContainerView mMapContainerView;
	private NMapController mMapController;
	private NMapView mMapView;
	private NMapOverlayManager mOverlayManager;
	private SearchMapResourceProvider mMapViewerResourceProvider;
	private NMapLocationManager mMapLocationManager;
	private TextView textView;

	
	
	////////////////////////////////////////////////////////////////////////////
	// for restoreInstance & saveInstance 
	private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.978371, 37.5666091);
	private static final int NMAP_ZOOMLEVEL_DEFAULT = 10;
	private static final String KEY_ZOOM_LEVEL = "MainTab3Activity.zoomLevel";
	private static final String KEY_CENTER_LONGITUDE = "MainTab3Activity.centerLongitudeE6";
	private static final String KEY_CENTER_LATITUDE = "MainTab3Activity.centerLatitudeE6";

	private SharedPreferences mPreferences;
		
	
	
	////////////////////////////////////////////////////////////////////////////
	// for POIitem
//	private NMapPOIitem mPOIitem;
	private SlidingDrawer mSlidingDrawer;
		
	
	
	////////////////////////////////////////////////////////////////////////////
	// for searching restaurant
	private SearchMapParser mSearchMapParser;
	private int searchedRestaurantIndex;
	private RestaurantData[] restaurantData = new RestaurantData[SEARCH_INDEX];
	private NGeoPoint mMyGeoPoint;
	private String currentAddress;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.e("NHK", "========================================================================================");
		Log.e("NHK", "onCreate!");
/*		BACKBTNCURRENT = BACKBTN_NOTCOMPLEAT;*/
		// retrieve menu value
		Intent intent = getIntent();
        if( intent.hasExtra("search_menu"))
        	SEARCH_MENU = intent.getExtras().getString("search_menu");
		
		// create map view
		mMapView = new NMapView(this);
		mMapView.setApiKey(NAVER_MAP_KEY);
		mMapContainerView = new MapContainerView(this);
		
		initializeView();
		initializeNMap();
		initializeSearch();
		
		setContentView(mMapContainerView);
	}
	
	protected void onStart() {
		super.onStart();
		Log.e("NHK", "onStart!");
	
		startMyLocation();
	}

	protected void onResume() {
		super.onResume();
		Log.e("NHK", "onResume!");
		restoreInstanceState();
	}
	
	protected void onDestroy() {
		Log.e("NHK", "onDestroy");
		// save map view state such as map center position and zoom level.
		saveInstanceState();

		super.onDestroy();
	}
	
	/*	public void onBackPressed(){

	if(BACKBTNCURRENT == BACKBTN_COMPLEAT){
		finish();
	}
	else
	{
		
	}
	BACKBTNCURRENT=BACKBTN_COMPLEAT;
	}*/

	private void initializeView() {
		// add NMapView
		mMapContainerView.addView(mMapView);
		
		textView = new TextView(this);
		mMapContainerView.addView(textView);
	}
	
	private void initializeNMap() {
		mMapView.setClickable(true);
		mMapView.setEnabled(true);
		mMapView.setFocusable(true);
		mMapView.setFocusableInTouchMode(true);
		mMapView.requestFocus();

		// register listener for map state changes
//		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
//		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
//		mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);
	
		// use map controller to zoom in/out, pan and set map center, zoom level etc.
		mMapController = mMapView.getMapController();
	
		// use built in zoom controls
		NMapView.LayoutParams lp = new NMapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
		mMapView.setBuiltInZoomControls(true, lp);
	
		// create resource provider
		mMapViewerResourceProvider = new SearchMapResourceProvider(this);
	
		// create overlay manager
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		// register callout overlay listener to customize it.
		mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);

		// set data provider listener
		super.setMapDataProviderListener(onDataProviderListener);
		
		// location manager
		mMapLocationManager = new NMapLocationManager(this);
	}
	
	private void initializeSearch() {
		mSearchMapParser = new SearchMapParser();
		for (int i=0; i<SEARCH_INDEX; i++)
			restaurantData[i] = new RestaurantData();
	}
	
	private void startMyLocation() {
		if(DEBUG_MODE)
			Log.d("NHK", "start searching my location");
		
		boolean is = mMapLocationManager.isMyLocationEnabled();
		if (is) {
			
		} else {
			boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(false);
			
			if ( !isMyLocationEnabled ) {
				if(DEBUG_MODE) {
					Log.d("NHK", "Cannot start searching my location");
					Log.d("NHK", "Move to setting page");
				}
				
				Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(goToSettings);
				
				return ;
			}
		}

		mMapLocationManager.setOnLocationChangeListener(new NMapLocationManager.OnLocationChangeListener() {
			public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
				if(DEBUG_MODE)
					Log.d("NHK", "Current Latitude: " + String.valueOf(myLocation.getLatitude()) + "   Longitude: " + String.valueOf(myLocation.getLongitude()));
				
				mMyGeoPoint = myLocation;
				
				findPlacemarkAtLocation(mMyGeoPoint.getLongitude(), mMyGeoPoint.getLatitude());
				stopMyLocation();
				return true;
			}

			public void onLocationUnavailableArea(NMapLocationManager arg0,	NGeoPoint arg1) {
				Toast.makeText(SearchMapActivity.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
				stopMyLocation();
			}

			public void onLocationUpdateTimeout(NMapLocationManager arg0) {
				Toast.makeText(SearchMapActivity.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();
				stopMyLocation();
			}
		});
	}

	private void stopMyLocation() {
		if(DEBUG_MODE)
			Log.d("NHK", "stop searching my location");
		mMapLocationManager.disableMyLocation();
	}

	private void displayRestaurantItem() {
		if(DEBUG_MODE)
			Log.d("NHK", "Display restaurants");
		
		int markerId = SearchMapPOIflagType.PIN;

		NMapPOIdata poiData = new NMapPOIdata(searchedRestaurantIndex, mMapViewerResourceProvider);
		poiData.beginPOIdata(searchedRestaurantIndex);
		for(int i=0; i<searchedRestaurantIndex; i++)
			poiData.addPOIitem(Double.parseDouble(restaurantData[i].sMapX), Double.parseDouble(restaurantData[i].sMapY), restaurantData[i].sTitle, markerId, 0);
		poiData.endPOIdata();

		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		
		poiDataOverlay.showAllPOIdata(0);
	}
	
	private void searchRestaurant() {
		Log.d("NHK", "start searching near Restaurants");
				
		searchedRestaurantIndex = mSearchMapParser.search(restaurantData, currentAddress+" "+SEARCH_MENU, SEARCH_INDEX, 1);
		
		if(DEBUG_MODE)
			Log.d("NHK", "" + String.valueOf(searchedRestaurantIndex) + " Restaurants have found");
		
		if (searchedRestaurantIndex != 0) {
			displayRestaurantItem();
		} else if (searchedRestaurantIndex == 0){
			if(DEBUG_MODE)
				Log.d("NHK", "Naver search key: " + currentAddress+" "+SEARCH_MENU);

			textView.setTextSize(20);
			textView.setText("검색값이 없으면 여기에 없다고 표시가 나와야 \n 하이하이");			
			mMapController.setMapCenter(mMyGeoPoint, 10);

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////              //////////////////////////////////////////////
			/////////                      ///////////////////////   ///////     ////////////////////////////////////////////
			///////////////////   ////////////////////////////////   /////////      /////////////////////////////////////////
			///////////////////   ////////////////////////////////   //////////     /////////////////////////////////////////
			///////////////////   ///////////////        /////////   ///////////     //////////         /////////////////////
			///////////////////   /////////////   /////    ///////   //////////     /////////    /////    ///////////////////
			///////////////////   ///////////   ////////    //////   /////////     ////////    ////////    //////////////////
			///////////////////   //////////   //////////    /////   ///////     //////////    /////////    /////////////////
			///////////////////   ///////////   ////////    //////   ////     //////////////    ///////    //////////////////
			///////////////////   /////////////          /////////          /////////////////            ////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}
	}
	
	private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {
		public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {
			// handle overlapped items
			if (itemOverlay instanceof NMapPOIdataOverlay) {
				NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

				// check if it is selected by touch event
				if (!poiDataOverlay.isFocusedBySelectItem()) {
					int countOfOverlappedItems = 1;

					NMapPOIdata poiData = poiDataOverlay.getPOIdata();
					for (int i = 0; i < poiData.count(); i++) {
						NMapPOIitem poiItem = poiData.getPOIitem(i);

						// skip selected item
						if (poiItem == overlayItem) {
							
							
							
							continue;
						}

						// check if overlapped or not
						if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
							countOfOverlappedItems++;
						}
					}

					if (countOfOverlappedItems > 1) {
						String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
						Toast.makeText(SearchMapActivity.this, text, Toast.LENGTH_LONG).show();
						return null;
					}
				}
			}

			return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);			
		}

	};
	
	/* NMapDataProvider Listener */
	private final NMapActivity.OnDataProviderListener onDataProviderListener = new NMapActivity.OnDataProviderListener() {
		
		public void onReverseGeocoderResponse(NMapPlacemark placeMark, NMapError errInfo) {
			currentAddress = placeMark.doName + " " + placeMark.siName + " " + placeMark.dongName;

			if (errInfo != null) {
				Log.e("NHK", "Failed to findPlacemarkAtLocation: error=" + errInfo.toString());

				Toast.makeText(SearchMapActivity.this, errInfo.toString(), Toast.LENGTH_LONG).show();
				return;
			}
			
			searchRestaurant();			
		}
	};

	private void restoreInstanceState() {
		mPreferences = getPreferences(MODE_PRIVATE);

		int longitudeE6 = mPreferences.getInt(KEY_CENTER_LONGITUDE,	NMAP_LOCATION_DEFAULT.getLongitudeE6());
		int	latitudeE6 = mPreferences.getInt(KEY_CENTER_LATITUDE, NMAP_LOCATION_DEFAULT.getLatitudeE6());
		int level = mPreferences.getInt(KEY_ZOOM_LEVEL, NMAP_ZOOMLEVEL_DEFAULT);

		mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6),	level);
	}

	private void saveInstanceState() {
		if (mPreferences == null)
			return;

		NGeoPoint center = mMapController.getMapCenter();
		int level = mMapController.getZoomLevel();

		SharedPreferences.Editor edit = mPreferences.edit();

		edit.putInt(KEY_CENTER_LONGITUDE, center.getLongitudeE6());
		edit.putInt(KEY_CENTER_LATITUDE, center.getLatitudeE6());
		edit.putInt(KEY_ZOOM_LEVEL, level);

		edit.commit();
	}
	
	private class MapContainerView extends ViewGroup {

		public MapContainerView(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			final int width = getWidth();
			final int height = getHeight();
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);
				final int childWidth = view.getMeasuredWidth();
				final int childHeight = view.getMeasuredHeight();
				final int childLeft = (width - childWidth) / 2;
				final int childTop = (height - childHeight) / 2;

				if (i==0)
					view.layout(childLeft, childTop, childLeft+childWidth, (childTop+childHeight)*4/5);
				else
					view.layout(childLeft+10, (childTop+childHeight)*4/5+10, childLeft+childWidth-10, childTop+childHeight);
			}

			if (changed) {
				mOverlayManager.onSizeChanged(width, height);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			int sizeSpecWidth = widthMeasureSpec;
			int sizeSpecHeight = heightMeasureSpec;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);

				if (view instanceof NMapView) {
					if (mMapView.isAutoRotateEnabled()) {
						int diag = (((int) (Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
						sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
						sizeSpecHeight = sizeSpecWidth;
					}
				}

				view.measure(sizeSpecWidth, sizeSpecHeight);
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}