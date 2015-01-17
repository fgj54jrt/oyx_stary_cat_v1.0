package com.amap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.alertdialog.cyx_CustomAlertDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.cwits.cyx_drive_sdk.db.DBManager;
import com.cwits.cyx_drive_sdk.navi.NaviCustomActivity;
import com.cwits.cyx_drive_sdk.navi.TTSController;
import com.userinfo.ContantUserInfo;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.MainActivity;
import com.xgr.wonderful.ui.oyx_MyApplication;
import com.xgr.wonderful.utils.Coordinate;
import com.xgr.wonderful.utils.JourneyTool;
import com.xgr.wonderful.utils.MResource;

/**
 * 展示搜索结果的页面
 * 
 * @author lxh
 * 
 */
public class cyx_SearchResultMapActivity_gaode extends Activity implements
		OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener,
		OnMapLoadedListener, OnClickListener, InfoWindowAdapter,
		LocationSource, AMapLocationListener, OnCameraChangeListener {
	public static final String TAG = "cyx_SearchResultMapActivity";
	MapView mMapView = null; // 地图View
	LatLonPoint locData = null;
	private TextView tv_main_title_right;
	private LinearLayout llSearchList;
	LinearLayout search_layout;
	private String city = "深圳"; // 当前城市
	boolean isFirstLoc = true; // 是否首次定位
	Button btnZoomin, btnZoomout; // 地图缩放按钮
	private ArrayList<MyPoiResult> myPoiResultList; // 传递到搜索列表界面中的结果集合
	private Gallery myGallery;
	private MyGalleryAdapter myAdapter;
	private Button btn_traffic; // 交通路况按钮
	private Button btn_getLocation; // 定位按钮
	private boolean isShowTraffic = false;
	private ImageView img_back;
	private PoiResult resultList;
	ImageView poi_toLast, poi_toNext;
	private int lastIndex = 0;
	private int nowIndex = 0;
	public int totalLong = 0;
	public int currentNum = 0;
	String searchName;
	FrameLayout searchLayout;
	FrameLayout searchMap_layout_bottom;
	boolean isSelect = false;
	Handler mHandler;
	private boolean intentToList = false;
	private DBManager dBManager;

	private SharedPreferences adrPreference;

	LinearLayout btn_getRoutePlan;
	LinearLayout btn_collectPlace;
	private TextView collectTextView;
	private cyx_CustomAlertDialog dialog;

	private ImageView collectImageView = null;
	private OnLocationChangedListener mListener;
	private AMap aMap;
	private LocationManagerProxy mAMapLocationManager;
	public static AMapLocation preLocation;
	public ArrayList<MarkerOptions> markerOptionlst;
	private List<Marker> markerlst;
	private boolean isMarkClick = false;// 点击mark了？
	// 记录导航种类，用于记录当前选择是驾车还是步行
	private int mTravelMethod = DRIVER_NAVI_METHOD;
	private static final int DRIVER_NAVI_METHOD = 0;// 驾车导航
	private static final int WALK_NAVI_METHOD = 1;// 步行导航
	// 计算路的状态
	private final static int GPSNO = 0;// 使用我的位置进行计算、GPS定位还未成功状态
	private final static int CALCULATEERROR = 1;// 启动路径计算失败状态
	private final static int CALCULATESUCCESS = 2;// 启动路径计算成功状态
	private AMapNavi mAmapNavi;
	private AMapNaviListener mAmapNaviListener;
	private ProgressDialog mProgressDialog;// 路径规划过程显示状态
	private ProgressDialog mGPSProgressDialog;// GPS过程显示状态
	// 驾车路径规划起点，途经点，终点的list
	private List<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
	private List<NaviLatLng> mWayPoints = new ArrayList<NaviLatLng>();
	private List<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
	// 记录起点、终点、途经点位置
	private NaviLatLng mStartPoint = new NaviLatLng();
	private NaviLatLng mEndPoint = new NaviLatLng();
	private NaviLatLng mWayPoint = new NaviLatLng();
	private boolean mIsGetGPS = false;// 记录GPS定位是否成功
	private boolean mIsStart = false;// 记录是否已我的位置发起路径规划
	private float mapZoom = 16;
	private RelativeLayout rl;

	private boolean isNewCreateActivity = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.cyx_search_result_gaode_map_layout);
		oyx_MyApplication.getInstance().addActivity(this);
		mMapView = (MapView) findViewById(R.id.mMapView_searchResult);
		mMapView.onCreate(savedInstanceState); // 此方法必须重写
		dBManager = DBManager
				.getInstance(cyx_SearchResultMapActivity_gaode.this);

		init();

	}

	private void init() {
		isNewCreateActivity = true;
		markerOptionlst = new ArrayList<MarkerOptions>();

		mHandler = new Handler();
		myPoiResultList = new ArrayList<MyPoiResult>();
		city = getSharedPreferences(ContantUserInfo.ADDRESS_INFO, MODE_PRIVATE)
				.getString(ContantUserInfo.ADDRESS_CITY, "深圳");
		btnZoomin = (Button) findViewById(R.id.btn_zoomin);
		btnZoomout = (Button) findViewById(R.id.btn_zoomout);
		btnZoomin.setOnClickListener(clickListener);
		btnZoomout.setOnClickListener(clickListener);
		// rl=(RelativeLayout) findViewById(MResource.getLayoutId(
		// getApplicationContext(), "a"));
		// rl.setBackgroundColor(MResource.getColorId(getApplicationContext(),
		// "green2"));
		// rl.invalidate();
		btn_collectPlace = (LinearLayout) findViewById(R.id.main_btn_collectPlace);
		btn_getRoutePlan = (LinearLayout) findViewById(R.id.main_btn_getRoutePlan);
		collectImageView = (ImageView) findViewById(R.id.collect_imageview);
		btn_collectPlace.setOnClickListener(clickListener);
		btn_getRoutePlan.setOnClickListener(clickListener);

		if (aMap == null) {
			setUpMap();
		}
		SharedPreferences sh = getSharedPreferences("lastLocation",
				Activity.MODE_PRIVATE);
		String lat = sh.getString("lat", "");
		String lon = sh.getString("lon", "");
		search_layout = (LinearLayout) findViewById(R.id.search_search_layout);
		search_layout.setOnClickListener(clickListener);
		btn_traffic = (Button) findViewById(R.id.map_traffic);
		btn_traffic.setOnClickListener(clickListener);
		btn_getLocation = (Button) findViewById(R.id.btn_getLocation);
		btn_getLocation.setOnClickListener(clickListener);
		tv_main_title_right = (TextView) findViewById(R.id.tv_main_title_right);
		llSearchList = (LinearLayout) findViewById(R.id.list_btn_layout);
		tv_main_title_right.setOnClickListener(clickListener);
		llSearchList.setOnClickListener(clickListener);
		collectTextView = (TextView) findViewById(R.id.collect_place_text);
		myGallery = (Gallery) findViewById(R.id.my_grally);
		img_back = (ImageView) findViewById(R.id.search_img_back);
		img_back.setOnClickListener(clickListener);
		img_back.setVisibility(View.VISIBLE);
		poi_toLast = (ImageView) findViewById(R.id.img_poi_lastDetial);
		poi_toNext = (ImageView) findViewById(R.id.img_poi_nextDetial);
		poi_toLast.setOnClickListener(clickListener);
		poi_toNext.setOnClickListener(clickListener);
		updateCollectButton();
		searchMap_layout_bottom = (FrameLayout) findViewById(R.id.searchMap_layout_bottom);
		myGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub

				Log.e("lxh", "onItemSelected");
				if (position == 0)
					poi_toLast.setVisibility(View.INVISIBLE);
				else
					poi_toLast.setVisibility(View.VISIBLE);
				if (position == resultList.getPois().size() - 1)
					poi_toNext.setVisibility(View.INVISIBLE);
				else
					poi_toNext.setVisibility(View.VISIBLE);
				resultList.getPois().get(position).getLatLonPoint()
						.getLatitude();

				nowIndex = position;

				updateCollectButton();
				if (!isMarkClick)
					onMarkerClick(markerlst.get(position));
				setPointToCenter(markerlst.get(position), mapZoom, null);
				updateMarker(markerlst.get(position));
				if (newb != null && !newb.isRecycled()) {
					newb.recycle();
					newb = null;
				}
				isMarkClick = false;

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		adrPreference = getSharedPreferences(ContantUserInfo.ADDRESS_INFO,
				MODE_PRIVATE);
		if (!TextUtils
				.isEmpty(adrPreference.getString(ContantUserInfo.LAT, ""))
				&& !TextUtils.isEmpty(adrPreference.getString(
						ContantUserInfo.LON, ""))) {
			locData = new LatLonPoint(Double.valueOf(adrPreference.getString(
					ContantUserInfo.LAT, "")), Double.valueOf(adrPreference
					.getString(ContantUserInfo.LON, "")));
			initPoi();
			if (resultList != null && resultList.getPois().size() > 0) {
				searchMap_layout_bottom.setVisibility(View.VISIBLE);
				if (resultList.getPois().size() == 1) {
					poi_toLast.setVisibility(View.INVISIBLE);
					poi_toNext.setVisibility(View.INVISIBLE);
				}
			}
			updateCollectButton();
		} else {
			dialog();
		}

	}

	private void setUpMap() {
		// 初始语音播报资源
		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
		TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
		mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
		ttsManager.init();
		if(mAmapNavi==null){
			Log.w("oyx","mAmapNavi is null");
		}
		mAmapNavi.setAMapNaviListener(ttsManager);// 设置语音模块播报
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// R.drawable.gaode_location_marker));//
		// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.argb(80, 100, 149, 237));// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(50, 100, 149, 237));//
		// 设置圆形的填充颜色
		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
		aMap = mMapView.getMap();
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
		aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
		aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		aMap.setOnCameraChangeListener(this);
		CameraUpdateFactory.zoomTo(mapZoom);// 缩放级别

		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.setOnCameraChangeListener(this);
	}

	private void initPoi() {
		searchName = getIntent().getStringExtra("search_name");
		PoiResult res = oyx_MyApplication.getMyPoiResult_gaode();
		if (res != null && res.getPois() != null) {
			showPoiInfo(res);
			totalLong = res.getPois().size();

		}
	}

	OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Intent intent;
			float maxZoomLevel = aMap.getMaxZoomLevel();
			float minZoomLevel = aMap.getMinZoomLevel();
			float currentInZoon = 0, currentOutZoon = 0;
			if (id == R.id.btn_zoomin) {
				changeCamera(CameraUpdateFactory.zoomIn(), null);
				CameraUpdateFactory.zoomBy(3);

			} else if (id == R.id.btn_zoomout) {
				changeCamera(CameraUpdateFactory.zoomOut(), null);
				CameraUpdateFactory.zoomBy(3);

			} else if (id == R.id.btn_getLocation) {
				if (preLocation != null) {
					mListener.onLocationChanged(preLocation);// 显示系统小蓝点
					changeCamera(
							CameraUpdateFactory.newCameraPosition(new CameraPosition(
									new LatLng(preLocation.getLatitude(),
											preLocation.getLongitude()), aMap
											.getCameraPosition().zoom, 30, 0)),
							null);
					btn_getLocation
							.setBackgroundResource(R.drawable.ic_location);
					btn_getLocation.setEnabled(true);
				} else {
					showToast("正在定位中，请稍后再试");
				}

			} else if (id == R.id.search_search_layout) {
				intent = new Intent(cyx_SearchResultMapActivity_gaode.this,
						cyx_NaviSearchActivity_gaode.class);
				if (city == null || city.equals(""))
					city = "深圳";
				intent.putExtra("city", city);
				startActivity(intent);
				finish();
			} else if (id == R.id.tv_main_title_right) {
				Log.w("lxh", "click list_btn");
				intentToList = true;
				intent = new Intent(cyx_SearchResultMapActivity_gaode.this,
						cyx_PoiDetailListActivity_gaode.class);
				intent.putExtra("search_name", searchName);

				if (myPoiResultList != null && myPoiResultList.size() > 0) {
					Bundle bundle = new Bundle();
					bundle.putSerializable("myPoiResult", myPoiResultList);
					intent.putExtras(bundle);
				}

				startActivity(intent);
			} else if (id == R.id.map_traffic) {
				if (isShowTraffic) {
					aMap.setTrafficEnabled(false);
					btn_traffic.setBackgroundResource(R.drawable.lukuang);
					isShowTraffic = false;
					Toast.makeText(cyx_SearchResultMapActivity_gaode.this,
							getResources().getString(R.string.close_traffic),
							Toast.LENGTH_SHORT).show();
				} else {
					aMap.setTrafficEnabled(true);
					btn_traffic
							.setBackgroundResource((R.drawable.lukuang_press));
					isShowTraffic = true;
					Toast.makeText(cyx_SearchResultMapActivity_gaode.this,
							getResources().getString(R.string.open_traffic),
							Toast.LENGTH_SHORT).show();
				}
			} else if (id == R.id.search_img_back) {
				intent = new Intent(cyx_SearchResultMapActivity_gaode.this,
						AMapMainActivity.class);

				startActivity(intent);
				cyx_SearchResultMapActivity_gaode.this.finish();
			} else if (id == R.id.img_poi_lastDetial) {
				if (nowIndex > 0) {
					myGallery.setSelection(nowIndex - 1);
					updateMarker(markerlst.get(nowIndex - 1));
					if (newb != null && !newb.isRecycled()) {
						newb.recycle();
						newb = null;
					}

					updateCollectButton();
				}
			} else if (id == R.id.main_btn_collectPlace) {
				if (nowIndex < resultList.getPois().size()) {
					collect();
					updateCollectButton();
				}
			} else if (id == R.id.main_btn_getRoutePlan) {
				if (preLocation != null) {
					calculateRoute();
				} else {
					showToast("正在定位中，请稍后再试");
				}

			} else if (id == R.id.img_poi_nextDetial) {
				if (nowIndex + 1 < resultList.getPois().size()) {
					myGallery.setSelection(nowIndex + 1);
					updateMarker(markerlst.get(nowIndex + 1));
					if (newb != null && !newb.isRecycled()) {
						newb.recycle();
						newb = null;
					}

					updateCollectButton();
				}
			}

		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cyx_SearchResultMapActivity_gaode.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		try {

			SharedPreferences sh = getSharedPreferences("lastLocation",
					Activity.MODE_PRIVATE);
			if (locData != null) {
				sh.edit().putString("lat", locData.getLatitude() + "").commit();
				sh.edit().putString("lon", locData.getLongitude() + "")
						.commit();
			}
			if (mMapView != null) {
				mMapView.onDestroy();
				mMapView = null;
			}
			if (newb != null && !newb.isRecycled()) {
				newb.recycle();
				newb = null;
			}
			resultList = null;
			// myLocationOverlay = null;
			// mOverlay = null;
			if (myPoiResultList != null) {
				myPoiResultList.clear();
				myPoiResultList = null;
			}

			// 如果不是跳转至列表，则将内存中的数据清空
			oyx_MyApplication.getInstance().removeActivity(this);
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		AMapNavi.getInstance(this).destroy();// 销毁导航
		// TTSController.getInstance(this).stopSpeaking();// 停止播报
		// TTSController.getInstance(this).destroy();// 销毁播报模块
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		deactivate();
		dBManager.close();
		super.onPause();

	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		dBManager.open();
		super.onResume();
		// 以上两句必须重写
		// 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
		AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
		mAmapNavi.startGPS();
	}

	private void showPoiInfo(PoiResult res) {
		if (res.getPois() != null) {
			markerOptionlst.clear();
			for (int i = 0; i < res.getPois().size(); i++) {
				double[] db = Coordinate.wgtochina(res.getPois().get(i)
						.getLatLonPoint().getLongitude(), res.getPois().get(i)
						.getLatLonPoint().getLatitude());
				LatLng LatLng = new LatLng(res.getPois().get(i)
						.getLatLonPoint().getLatitude(), res.getPois().get(i)
						.getLatLonPoint().getLongitude());
				String bitmapName="b_poi_"+(i+1);
				createBitmap2(bitmapName);
				MarkerOptions markerOption1 = new MarkerOptions()
						.anchor(0.5f, 0.5f).position(LatLng)
						.icon(BitmapDescriptorFactory.fromBitmap(newb))
						.perspective(true).draggable(true).period(50);
				markerOptionlst.add(markerOption1);
				markerlst = aMap.addMarkers(markerOptionlst, true);
			}

			isFirstLoc = false;
			resultList = res;
			tv_main_title_right.setVisibility(View.VISIBLE);
			llSearchList.setVisibility(View.VISIBLE);

			searchMap_layout_bottom.setVisibility(View.VISIBLE);

			for (int i = 0; i < res.getPois().size(); i++) {
				MyPoiResult result = new MyPoiResult();
				result.setEnd_latitude(res.getPois().get(i).getLatLonPoint()
						.getLatitude());
				result.setEnd_longitude(res.getPois().get(i).getLatLonPoint()
						.getLongitude());
				result.setPoiAddress(res.getPois().get(i).getSnippet());
				result.setPoiName(res.getPois().get(i).getTitle());
				myPoiResultList.add(result);
			}
			oyx_MyApplication.setMyPoiResult_gaode(res);
			myAdapter = new MyGalleryAdapter(res,
					cyx_SearchResultMapActivity_gaode.this, locData, mMapView);
			myGallery.setAdapter(myAdapter);
			myGallery.refreshDrawableState();
		}
	}

	Bitmap newb = null;

	/**
	 * 创建搜索结果图标
	 * 
	 * @param imgName
	 *            底图图片资源
	 * @param index
	 *            数字
	 * @param textColor
	 *            字体颜色
	 * @param textSize
	 *            字体大小
	 * @return
	 */
	private void createBitmap(String imgName, int index, String textColor,
			int textSize) {
		BitmapDrawable src = (BitmapDrawable) getResources().getDrawable(
				MResource.getDrawableId(getApplicationContext(), imgName));// "unclick"
																			// ,
		int w = src.getBitmap().getWidth();
		int h = src.getBitmap().getHeight();
		// create the new blank bitmap
		newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建�?��新的和SRC长度宽度�?��的位�?
		Canvas cv = new Canvas(newb);
		// draw src into
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(
				MResource.getColorId(getApplicationContext(), textColor)));
		paint.setTextSize(textSize);
		paint.setTextAlign(Align.CENTER);
		FontMetrics fontMetrics = paint.getFontMetrics();
		// 计算文字高度
		float fontHeight = fontMetrics.bottom - fontMetrics.top;
		// 计算文字baseline
		float textBaseY = h - (h - fontHeight) / 2 - fontMetrics.bottom;
		cv.drawBitmap(src.getBitmap(), 0, 0, null);// �?0�?坐标�?��画入src
		cv.drawText(String.valueOf(index), newb.getWidth() / 2 + 2,
				textBaseY - 5, paint);
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		src = null;
	}

	/**
	 * 创建搜索结果图标
	 * 
	 * @param imgName
	 *            底图图片资源
	 * @param index
	 *            数字
	 * @param textColor
	 *            字体颜色
	 * @param textSize
	 *            字体大小
	 * @return
	 */
	private void createBitmap2(String imgName) {
		BitmapDrawable src = (BitmapDrawable) getResources().getDrawable(
				MResource.getDrawableId(getApplicationContext(), imgName));// "unclick"
																			// ,
		int w = src.getBitmap().getWidth();
		int h = src.getBitmap().getHeight();
		// create the new blank bitmap
		newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建�?��新的和SRC长度宽度�?��的位�?
		Canvas cv = new Canvas(newb);
		cv.drawBitmap(src.getBitmap(), 0, 0, null);// �?0�?坐标�?��画入src
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		src = null;

	}

	public class MyGalleryAdapter extends BaseAdapter {

		private PoiResult result;
		private LatLonPoint location;

		// private int DRIVING_POLICY = MKSearch.ECAR_AVOID_JAM;

		public MyGalleryAdapter(PoiResult result, Context context,
				LatLonPoint myLocation, MapView mapView) {
			this.result = result;
			this.location = myLocation;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return result.getPois().size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return result.getPois().get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater;
			final ViewHolder holder;
			nowIndex = position;
			if (convertView == null) {
				inflater = LayoutInflater
						.from(cyx_SearchResultMapActivity_gaode.this);
				convertView = inflater.inflate(R.layout.cyx_searchresult_bottom, null);
				holder = new ViewHolder();
				holder.tv_place_name = (TextView) convertView
						.findViewById(R.id.place_name);
				holder.tv_place_Distance = (TextView) convertView
						.findViewById(R.id.place_Distance);
				holder.tv_place_detial = (TextView) convertView
						.findViewById(R.id.place_detial);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (result.getPois() != null && result.getPois().size() > 0) {
				if (result.getPois().get(position).getTitle().length() > 8) {
					holder.tv_place_name.setText((position + 1)
							+ "."
							+ result.getPois().get(position).getTitle()
									.substring(0, 8) + "...");
				} else {
					holder.tv_place_name.setText((position + 1) + "."
							+ result.getPois().get(position).getTitle());
				}
				if (preLocation != null) {
					double distance = JourneyTool.getDistance(
							preLocation.getLatitude(),
							preLocation.getLongitude(),
							result.getPois().get(position).getLatLonPoint()
									.getLatitude(),
							result.getPois().get(position).getLatLonPoint()
									.getLongitude());
					holder.tv_place_Distance.setText(distance + "km");
				}

				holder.tv_place_detial.setText(result.getPois().get(position)
						.getSnippet());
			}
			return convertView;

		}

		class ViewHolder {
			// LinearLayout btn_getRoutePlan;
			// LinearLayout btn_collectPlace;
			TextView tv_place_name;
			TextView tv_place_Distance;
			TextView tv_place_detial;
		}

	}

	// -------------生命周期必须重写方法----------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 用户收藏按钮触发方法
	 */
	private void collect() {
		// dBManager.open();
		BmobUser currentUser = BmobUser
				.getCurrentUser(cyx_SearchResultMapActivity_gaode.this);
		String userID = currentUser.getUsername();
		String placeName;
		double longitude = resultList.getPois().get(nowIndex).getLatLonPoint()
				.getLongitude();
		double latitude = resultList.getPois().get(nowIndex).getLatLonPoint()
				.getLatitude();
		if (resultList.getPois().get(nowIndex).getTitle().length() > 10) {
			placeName = resultList.getPois().get(nowIndex).getTitle()
					.substring(0, 10)
					+ "...";
		} else {
			placeName = resultList.getPois().get(nowIndex).getTitle();
		}
		String address = resultList.getPois().get(nowIndex).getAdName();

		// 先判断改位置是否已经收藏
		boolean isHasCollected = dBManager.checkUpAdress(userID, longitude,
				latitude, placeName, address);
		// 已收藏则删除
		if (isHasCollected) {
			dBManager.delectUserCollect(userID, longitude, latitude, placeName,
					address);
			// 设置收藏按钮文本为“收藏此地址”

			collectTextView.setText(R.string.collect_place);
			// collectTextView.setText(R.string.collect_place);
			Toast.makeText(this, "已取消收藏", Toast.LENGTH_LONG).show();
		} else {
			boolean flag = dBManager.saveUserCollect(userID, longitude,
					latitude, placeName, address);
			if (flag) {
				// 设置收藏按钮文本为“取消收藏”
				collectTextView.setText(R.string.cancel_collct);
				// collectTextView.setText(R.string.cancel_collct);
				Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "收藏成功");
				Log.d(TAG, userID);
				Log.d(TAG, String.valueOf(longitude));
				Log.d(TAG, String.valueOf(latitude));
				Log.d(TAG, placeName);
				Log.d(TAG, address);
			} else {
				Log.d(TAG, "收藏失败");
			}
		}
	}

	/**
	 * 更新收藏按钮的文本
	 */
	private void updateCollectButton() {
		try {
			// <string name="collect_place">收藏此地址</string>
			// <string name="cancel_collct">取消收藏</string>

			BmobUser currentUser = BmobUser
					.getCurrentUser(cyx_SearchResultMapActivity_gaode.this);
			String userID = currentUser.getUsername();
			String placeName;
			double longitude = resultList.getPois().get(nowIndex)
					.getLatLonPoint().getLongitude();
			double latitude = resultList.getPois().get(nowIndex)
					.getLatLonPoint().getLatitude();
			if (resultList.getPois().get(nowIndex).getTitle().length() > 12) {
				placeName = resultList.getPois().get(nowIndex).getTitle()
						.substring(0, 10)
						+ "...";
			} else {
				placeName = resultList.getPois().get(nowIndex).getTitle();
			}
			String address = resultList.getPois().get(nowIndex).getAdName();

			// 先判断改位置是否已经收藏
			boolean isHasCollected = dBManager.checkUpAdress(userID, longitude,
					latitude, placeName, address);
			if (isHasCollected) {
				// 如果已经收藏，设置收藏按钮文本为“取消收藏”
				collectTextView.setText(R.string.cancel_collct);
				collectImageView.setImageDrawable(getResources().getDrawable(
						R.drawable.cyx_ic_collect));
				// collectTextView.setText(R.string.cancel_collct);
			} else {
				// 设置收藏按钮文本为“收藏此地址”
				collectTextView.setText(R.string.collect_place);
				collectImageView.setImageDrawable(getResources().getDrawable(
						R.drawable.cyx_ic_uncollect));

				// collectTextView.setText(R.string.collect_place);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 自定义对话框
	private void dialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		if (dialog != null)
			dialog = null;
		dialog = new cyx_CustomAlertDialog(
				cyx_SearchResultMapActivity_gaode.this);
		dialog.setTitle(getString(R.string.notice));
		dialog.setNumberVisible(false);
		dialog.setMessageVisible(true);
		dialog.setMessage(getString(R.string.cannot_navigation));
		dialog.setPositiveButton(
				getString(R.string.yes),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						Intent intent;
						intent = new Intent(
								cyx_SearchResultMapActivity_gaode.this,
								AMapMainActivity.class);

						startActivity(intent);
						cyx_SearchResultMapActivity_gaode.this.finish();
					}
				});
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d("lxh", "onClick(View v) ");
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
		// 第一个地址居中
		if (markerlst != null && markerlst.size() > 0) {
			setPointToCenter(markerlst.get(0), 15, null);
		}
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		isMarkClick = true;

		updateMarker(marker);

		return false;
	}

	/**
	 * 重排marker点，是当前位于最上方，颜色为红色，其他为绿色
	 * 
	 * @param marker
	 */
	private void updateMarker(Marker marker) {
		// isMarkClick = true;

		for (int i = 0; i < markerlst.size(); i++) {
			if (markerlst.get(i).getId().equals(marker.getId())) {
				String bitmapName="b_poi_"+(i+1)+"_hl";
				createBitmap2(bitmapName);
				myGallery.setSelection(i);
				markerlst.get(i).setIcon(
						BitmapDescriptorFactory.fromBitmap(newb));
				NaviLatLng naviLatLng = new NaviLatLng(markerOptionlst.get(i)
						.getPosition().latitude, markerOptionlst.get(i)
						.getPosition().longitude);

				mEndPoints.clear();
				mEndPoints.add(naviLatLng);
			} else {
				String bitmapName="b_poi_"+(i+1);
				createBitmap2(bitmapName);
				markerlst.get(i).setIcon(
						BitmapDescriptorFactory.fromBitmap(newb));

			}
		}

		marker.setToTop();
	}

	/**
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 */
	private void changeCamera(CameraUpdate update, CancelableCallback callback) {
		aMap.animateCamera(update, 500, callback);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		// TODO Auto-generated method stub
		if (mListener != null && aLocation != null
				&& aLocation.getAMapException().getErrorCode() == 0) {
			// mListener.onLocationChanged(aLocation);// 显示系统小蓝点
			// 第一次定位成功，搜索结果第一个居中
			if (isNewCreateActivity) {
				// 第一个地址居中
				if (markerlst != null && markerlst.size() > 0) {
					setPointToCenter(markerlst.get(0), 15, null);
					isNewCreateActivity = false;
				}
			}

			Log.w("lxh", "onLocationChanged = " + aLocation.getLatitude()
					+ "  " + aLocation.getLongitude());
			preLocation = aLocation;
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

	// ----------具体处理方法--------------
	/**
	 * 算路的方法，根据选择可以进行行车和步行两种方式进行路径规划
	 */
	private void calculateRoute() {
		switch (mTravelMethod) {
		// 驾车导航
		case DRIVER_NAVI_METHOD:
			int driverIndex = calculateDriverRoute();
			if (driverIndex == CALCULATEERROR) {
				showToast("路线计算失败,检查参数情况");
				return;
			} else if (driverIndex == GPSNO) {
				return;
			}
			break;
		// 步行导航
		case WALK_NAVI_METHOD:
			int walkIndex = calculateWalkRoute();
			if (walkIndex == CALCULATEERROR) {
				showToast("路线计算失败,检查参数情况");
				return;
			} else if (walkIndex == GPSNO) {
				return;
			}
			break;
		}
		// 显示路径规划的窗体
		showProgressDialog();
	}

	// ---------------UI操作----------------
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 对行车路线进行规划
	 */
	private int calculateDriverRoute() {
		int code = CALCULATESUCCESS;

		// if (!mIsGetGPS) {
		// showGPSProgressDialog();
		// code = GPSNO;
		// mIsStart = true;
		// }
		// 支持多个终点，终点列表的首点为导航终点，终点列表按车行方向排列，带有方向信息，可有效避免算路到马路的另一侧；
		NaviLatLng naviLatLng = new NaviLatLng(preLocation.getLatitude(),
				preLocation.getLongitude());
		Log.w("lxh", "aLocation.getLatitude() = " + preLocation.getLatitude()
				+ "aLocation.getLongitude() = " + preLocation.getLongitude());
		mStartPoints.clear();
		mWayPoints.clear();
		mStartPoints.add(naviLatLng);
		mWayPoints.add(naviLatLng);
		Log.w("lxh", "mEndPoints.getLatitude() = "
				+ mEndPoints.get(0).getLatitude()
				+ "mEndPoints.getLongitude() = "
				+ mEndPoints.get(0).getLongitude());

		if (mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints, mWayPoints,
				0)) {
			code = CALCULATESUCCESS;
		} else {

			code = CALCULATEERROR;
		}
		return code;
	}

	/**
	 * 对步行路线进行规划
	 */
	private int calculateWalkRoute() {
		int code = CALCULATEERROR;
		// if (!mIsGetGPS) {
		// showGPSProgressDialog();
		// mIsStart = true;
		// code = GPSNO;
		// }
		NaviLatLng naviLatLng = new NaviLatLng(preLocation.getLatitude(),
				preLocation.getLongitude());
		Log.w("lxh", "aLocation.getLatitude() = " + preLocation.getLatitude()
				+ "aLocation.getLongitude() = " + preLocation.getLongitude()
				+ mEndPoints.get(0).getLatitude() + "  " + mEndPoints.get(0));
		mStartPoints.add(naviLatLng);

		if (mAmapNavi
				.calculateWalkRoute(mStartPoints.get(0), mEndPoints.get(0))) {
			code = CALCULATESUCCESS;
		} else {

			code = CALCULATEERROR;
		}
		return code;

	}

	/**
	 * 导航回调函数
	 * 
	 * @return
	 */
	private AMapNaviListener getAMapNaviListener() {
		if (mAmapNaviListener == null) {

			mAmapNaviListener = new AMapNaviListener() {

				@Override
				public void onTrafficStatusUpdate() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStartNavi(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onReCalculateRouteForYaw() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onReCalculateRouteForTrafficJam() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLocationChange(AMapNaviLocation location) {
					// GPS位置更新回调函数
					Log.w("lxh", "GPS位置更新回调函数");
					mIsGetGPS = true;
					NaviLatLng naviLatLang = location.getCoord();
					dissmissGPSProgressDialog();
					if (mIsStart) {
						if (preLocation != null) {
							calculateRoute();
							mIsStart = false;
						}

					}
				}

				@Override
				public void onInitNaviSuccess() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onInitNaviFailure() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGetNavigationText(int arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onEndEmulatorNavi() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onCalculateRouteSuccess() {
					dissmissProgressDialog();
					Intent intent = new Intent(
							cyx_SearchResultMapActivity_gaode.this,
							NaviCustomActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);

				}

				@Override
				public void onCalculateRouteFailure(int arg0) {
					Log.w("lxh", "onCalculateRouteFailure = " + arg0);
					dissmissProgressDialog();
					showToast("路径规划出错");
				}

				@Override
				public void onArrivedWayPoint(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onArriveDestination() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onGpsOpenStatus(boolean arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onNaviInfoUpdated(AMapNaviInfo arg0) {
					// TODO Auto-generated method stub

				}
			};
		}
		return mAmapNaviListener;
	}

	/**
	 * 显示GPS进度框
	 */
	private void showGPSProgressDialog() {
		if (mGPSProgressDialog == null)
			mGPSProgressDialog = new ProgressDialog(this);
		mGPSProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mGPSProgressDialog.setIndeterminate(false);
		mGPSProgressDialog.setCancelable(true);
		mGPSProgressDialog.setMessage("GPS定位中");
		mGPSProgressDialog.show();
	}

	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (mProgressDialog == null)
			mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setMessage("线路规划中");
		mProgressDialog.show();
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissGPSProgressDialog() {
		if (mGPSProgressDialog != null) {
			mGPSProgressDialog.dismiss();
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		mapZoom = arg0.zoom;
		Log.d("lxh", "mapZoom : " + mapZoom);
		if (arg0.zoom == aMap.getMaxZoomLevel()) {
			btnZoomin.setBackgroundResource(R.drawable.ic_zoomin);
			btnZoomin.setEnabled(false);
		} else {
			btnZoomin.setEnabled(true);
			btnZoomin.setBackgroundResource(R.drawable.btn_zoom_in);
		}

		if (arg0.zoom == aMap.getMinZoomLevel()) {
			btnZoomout.setBackgroundResource(R.drawable.ic_zoomout);
			btnZoomout.setEnabled(false);
		} else {
			btnZoomout.setEnabled(true);
			btnZoomout.setBackgroundResource(R.drawable.btn_zoom_out);
		}
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 * 
	 * @param longigude
	 *            真实坐标 经度
	 * @param latitude
	 *            真实坐标 纬度
	 * 
	 * @param callback
	 *            回调接口
	 */
	private void setPointToCenter(double longitude, double latitude,
			CancelableCallback callback) {

		double[] db = Coordinate.wgtochina(longitude, latitude);
		if (db != null) {
			CameraUpdate update = CameraUpdateFactory
					.newCameraPosition(new CameraPosition(new LatLng(db[1],
							db[0]), aMap.getCameraPosition().zoom, 30, 0));
			aMap.animateCamera(update, 500, callback);
		}

	}

	/**
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 * 
	 * @param marker
	 *            marker点
	 * @param callback
	 *            回调接口
	 */
	private void setPointToCenter(Marker marker, float mapZoom,
			CancelableCallback callback) {

		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(
				marker.getPosition().latitude, marker.getPosition().longitude),
				mapZoom);
		aMap.animateCamera(update, 500, callback);

	}

	protected void onStart() {
		super.onStart();
	}

}
