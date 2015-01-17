package com.amap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.alertdialog.cyx_CustomAlertDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.poisearch.PoiResult;
import com.cwits.cyx_drive_sdk.db.DBManager;
import com.cwits.cyx_drive_sdk.navi.NaviCustomActivity;
import com.cwits.cyx_drive_sdk.navi.NaviRouteActivity;
import com.cwits.cyx_drive_sdk.navi.TTSController;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.oyx_MyApplication;
import com.xgr.wonderful.utils.JourneyTool;
import com.xgr.wonderful.utils.MResource;

public class cyx_UserCollectMapActivity_gaode extends Activity implements
		OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener,
		OnMapLoadedListener, OnClickListener, InfoWindowAdapter,
		LocationSource, AMapLocationListener, CancelableCallback,OnCameraChangeListener {
	private static final String TAG = "lxh";

	cyx_CustomAlertDialog finishDialog;

	Runnable mTimeOutRunnable;

	public String JsonMessage;
	JourneyTool journeyTool;

	MapView mMapView = null; // 地图View
	private Handler handler;

	float[] mDirection = null;
	float[] mGspeed = null;
	double[] mLongitude = null;
	double[] mLatitude = null;
	MediaPlayer mMediaPlayer;
	Button btnZoomin, btnZoomout;

	private boolean isFirstLoc = true;
	private boolean isFirstLTime = true;
	private boolean isShowTraffic = false;
	private Button btn_getLocation; // 定位按钮
	private Button btn_traffic; // 交通路况按钮

	public String hostip;

	boolean isRequest = true;

	private cyx_CustomAlertDialog mDialog;
	private Runnable mapMoveRunnable;

	TextView info;
	private String userID = "";
	private String placeName = "";
	private String address = "";
	private double mLon;
	private double mLat;

	private TextView palceNameText;
	private TextView addressText;

	private DBManager dBManager;
	private TextView collectTextView;
	private LinearLayout collectLinear;
	private LinearLayout navigationLinear;
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
	// 记录导航种类，用于记录当前选择是驾车还是步行
	private int mTravelMethod = DRIVER_NAVI_METHOD;
	private static final int DRIVER_NAVI_METHOD = 0;// 驾车导航
	private static final int WALK_NAVI_METHOD = 1;// 步行导航
	// 计算路的状态
	private final static int GPSNO = 0;// 使用我的位置进行计算、GPS定位还未成功状态
	private final static int CALCULATEERROR = 1;// 启动路径计算失败状态
	private final static int CALCULATESUCCESS = 2;// 启动路径计算成功状态

	private ImageView backImageView;
	private ImageView collectImageView = null;
	private AMap aMap;
	private AMapNavi mAmapNavi;
	private AMapLocation preLocation;
	private LocationManagerProxy mAMapLocationManager;
	private OnLocationChangedListener mListener;
	public ArrayList<MarkerOptions> markerOptionlst;
	private List<Marker> markerlst;
	private float mapZoom=16;
	private TextView titleTV ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		userID = bundle.getString("user_id");
		placeName = bundle.getString("place_name");
		address = bundle.getString("address");
		mLon = bundle.getDouble("mLongitude");
		mLat = bundle.getDouble("mLatitude");
		NaviLatLng naviLatLng = new NaviLatLng(mLat, mLon);
		mEndPoints.clear();
		mEndPoints.add(naviLatLng);
		journeyTool = new JourneyTool();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 设置屏幕常亮

		setContentView(R.layout.cyx_user_collect_gaode_map_layout);
		mMapView = (MapView) findViewById(R.id.mMapView);
		mMapView.onCreate(savedInstanceState); // 此方法必须重写
		oyx_MyApplication.getInstance().addActivity(this);

		// mTrafficSourceRunnable = new MyTrafficSourceRunnable();
		dBManager = DBManager
				.getInstance(cyx_UserCollectMapActivity_gaode.this);
		dBManager.open();
		init();
	}


	private Bitmap newb;

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
	private void createBitmap(String imgName) {
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
		cv.drawBitmap(src.getBitmap(), 0, 0, null);// �?0�?坐标�?��画入src
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		src = null;
	}

	private void init() {
		handler = new Handler();
		markerOptionlst = new ArrayList<MarkerOptions>();
		titleTV = (TextView) findViewById(R.id.TextTitle);
		titleTV.setText(getResources().getString(R.string.collect_place_point));
		btnZoomin = (Button) findViewById(R.id.btn_zoomin);
		btnZoomout = (Button) findViewById(R.id.btn_zoomout);
		btnZoomin.setOnClickListener(clickListener);
		btnZoomout.setOnClickListener(clickListener);
		info = (TextView) findViewById(R.id.info);
		palceNameText = (TextView) findViewById(R.id.place_name_text);
		palceNameText.setText(placeName);

		addressText = (TextView) findViewById(R.id.address_text);
		addressText.setText(address);

		collectTextView = (TextView) findViewById(R.id.collect_place_text);
		collectLinear = (LinearLayout) findViewById(R.id.main_btn_collectPlace);
		collectLinear.setOnClickListener(clickListener);
		navigationLinear = (LinearLayout) findViewById(R.id.main_btn_getRoutePlan);
		backImageView = (ImageView) findViewById(R.id.btn_back);
		collectImageView = (ImageView) findViewById(R.id.collect_imageview);
		backImageView.setOnClickListener(clickListener);
		navigationLinear.setOnClickListener(clickListener);
		btn_traffic = (Button) findViewById(R.id.map_traffic);
		btn_traffic.setOnClickListener(clickListener);
		btn_getLocation = (Button) findViewById(R.id.btn_getLocation);
		btn_getLocation.setOnClickListener(clickListener);

		if (!ifGPSOpen()) {
			createDialog();
		}
		if (aMap == null) {
			aMap = mMapView.getMap();
			setUpMap();
		}
		updateCollectButton();
	}

	private void setUpMap() {
		// 初始语音播报资源
		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
		TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
		ttsManager.init();
		mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
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
		
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
		aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
		aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        aMap.setOnCameraChangeListener(this);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		CameraUpdateFactory.zoomTo(mapZoom);// 缩放级别
		initPoi();

	}


	OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			if (id == R.id.btn_zoomin) {
				changeCamera(CameraUpdateFactory.zoomIn(),
						cyx_UserCollectMapActivity_gaode.this);
				CameraUpdateFactory.zoomBy(1);

			} else if (id == R.id.btn_zoomout) {
				changeCamera(CameraUpdateFactory.zoomOut(),
						cyx_UserCollectMapActivity_gaode.this);
			} else if (id ==  R.id.map_traffic) {
				if (isShowTraffic) {
					aMap.setTrafficEnabled(false);
					btn_traffic.setBackgroundResource(R.drawable.lukuang);
					isShowTraffic = false;
					Toast.makeText(
							cyx_UserCollectMapActivity_gaode.this,
							getResources().getString(
									R.string.close_traffic),
							Toast.LENGTH_SHORT).show();
				} else {
					aMap.setTrafficEnabled(true);
					btn_traffic.setBackgroundResource((R.drawable.lukuang_press));
					isShowTraffic = true;
					Toast.makeText(
							cyx_UserCollectMapActivity_gaode.this,
							getResources().getString(
									R.string.open_traffic),
							Toast.LENGTH_SHORT).show();
				}

			} else if (id ==  R.id.btn_getLocation) {
				changeCamera(
						CameraUpdateFactory.newCameraPosition(new CameraPosition(
								new LatLng(preLocation.getLatitude(),
										preLocation.getLongitude()), aMap
										.getCameraPosition().zoom, 30, 0)),
						cyx_UserCollectMapActivity_gaode.this);
				isRequest = true;
			} else if (id ==  R.id.main_btn_collectPlace) {
				collect();
				updateCollectButton();
			} else if (id ==  R.id.main_btn_getRoutePlan) {
				Log.d("qyb", "daohangnanniu");
				calculateRoute();
				// navi(mLat, mLon, placeName);
			} else if (id ==  R.id.btn_back) {
				finishTheActivity();
			}
		}
	};


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// handler.removeCallbacks(mTrafficSourceRunnable);
		// mMapView.getOverlays().remove(myLocationOverlay);
		// myLocationOverlay = null;
		dBManager.close();
		deactivate();
		mMapView.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		isFirstLoc = savedInstanceState.getBoolean("isFirstLoc");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		isFirstLoc = false;
		outState.putBoolean("isFirstLoc", isFirstLoc);
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// initmap();
		mMapView.onResume();
		mMapView.setEnabled(true);
		dBManager.open();
		super.onResume();
		if (!ifGPSOpen()) {
			createDialog();
		}
		// 以上两句必须重写
		// 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
		AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
		mAmapNavi.startGPS();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (newb != null && !newb.isRecycled()) {
			newb.recycle();
			newb = null;
		}
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (mapMoveRunnable != null) {
			handler.removeCallbacks(mapMoveRunnable);
			mapMoveRunnable = null;
		}
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}


		handler.removeCallbacks(mTimeOutRunnable);
		SharedPreferences sh = getSharedPreferences("lastLocation",
				Activity.MODE_PRIVATE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (mMapView != null) {
			mMapView = null;
		}
		deactivate();//销毁定位监听
		AMapNavi.getInstance(this).destroy();// 销毁导航
		oyx_MyApplication.getInstance().removeActivity(this);
	}

	// 自定义对话框
	private void createDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = new cyx_CustomAlertDialog(
				cyx_UserCollectMapActivity_gaode.this);
		mDialog.setTitle(getString(R.string.notice));
		mDialog.setMessage(getString(R.string.open_gps_notice));
		mDialog.setNumberVisible(false);
		mDialog.showLine();
		mDialog.setPositiveButton(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
				Intent intent = new Intent();
				intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					intent.setAction(android.provider.Settings.ACTION_SETTINGS);
					try {
						startActivity(intent);
					} catch (Exception e) {
					}
				}
			}
		});
		mDialog.setNegativeButton(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
				mDialog = null;
			}
		});
	}

	private boolean ifGPSOpen() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finishTheActivity();

	}

	/**
	 * 更新收藏按钮的文本
	 */
	private void updateCollectButton() {
		// <string name="collect_place">收藏此地址</string>
		// <string name="cancel_collct">取消收藏</string>
		BmobUser currentUser = BmobUser.getCurrentUser(cyx_UserCollectMapActivity_gaode.this);
		String userID = currentUser.getUsername();

		// 先判断改位置是否已经收藏
		boolean isHasCollected = dBManager.checkUpAdress(userID, mLon, mLat,
				placeName, address);
		if (isHasCollected) {
			// 如果已经收藏，设置收藏按钮文本为“取消收藏”
			collectTextView.setText(R.string.cancel_collct);
			collectImageView.setImageDrawable(getResources().getDrawable(
					R.drawable.cyx_ic_collect));
		} else {
			// 设置收藏按钮文本为“收藏此地址”
			collectTextView.setText(R.string.collect_place);
			collectImageView.setImageDrawable(getResources().getDrawable(
					R.drawable.cyx_ic_uncollect));
		}
	}

	/**
	 * 已收藏则删除，未收藏则收藏
	 */
	private void collect() {

		// 先判断改位置是否已经收藏
		boolean isHasCollected = dBManager.checkUpAdress(userID, mLon, mLat,
				placeName, address);
		// 已收藏则删除
		if (isHasCollected) {
			dBManager.delectUserCollect(userID, mLon, mLat, placeName, address);
			// 设置收藏按钮文本为“收藏此地址”

			collectTextView.setText(R.string.collect_place);
			Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
		} else {
			boolean flag = dBManager.saveUserCollect(userID, mLon, mLat,
					placeName, address);
			if (flag) {
				// 设置收藏按钮文本为“取消收藏”
				collectTextView.setText(R.string.cancel_collct);
				Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "收藏成功");
				Log.d(TAG, userID);
				Log.d(TAG, String.valueOf(mLon));
				Log.d(TAG, String.valueOf(mLat));
				Log.d(TAG, placeName);
				Log.d(TAG, address);
			} else {
				Log.d(TAG, "收藏失败");
			}
		}
	}


	private void finishTheActivity() {

		// LogUtil.printLog(LogUtil.DEBUG, "--------test message to ar ",
		// message);
		if (mapMoveRunnable != null) {
			handler.removeCallbacks(mapMoveRunnable);
			mapMoveRunnable = null;
		}
		cyx_UserCollectMapActivity_gaode.this.finish();
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
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点

		}
		if (isFirstLTime) {
			isFirstLTime=false;
			changeCamera(
					CameraUpdateFactory.newCameraPosition(new CameraPosition(
							new LatLng(mLat, mLon), 15, 30, 0)),
					cyx_UserCollectMapActivity_gaode.this);
		}
		Log.w("lxh", "onLocationChanged = " + aLocation.getLatitude() + "  "
				+ aLocation.getLongitude());
		preLocation = aLocation;
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

	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub

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
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
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
						calculateRoute();
						mIsStart = false;
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
							cyx_UserCollectMapActivity_gaode.this,
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

	// ----------具体处理方法--------------
	/**
	 * 算路的方法，根据选择可以进行行车和步行两种方式进行路径规划
	 */
	private void calculateRoute() {
		switch (mTravelMethod) {
		// 驾车导航
		case DRIVER_NAVI_METHOD:
			int driverIndex = calculateWalkRoute();
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

		// 支持多个终点，终点列表的首点为导航终点，终点列表按车行方向排列，带有方向信息，可有效避免算路到马路的另一侧；
		NaviLatLng naviLatLng = new NaviLatLng(preLocation.getLatitude(),
				preLocation.getLongitude());
		Log.w("lxh", "aLocation.getLatitude() = " + preLocation.getLatitude()
				+ "aLocation.getLongitude() = " + preLocation.getLongitude());
		mStartPoints.clear();
		mWayPoints.clear();
		mStartPoints.add(naviLatLng);
		mWayPoints.add(naviLatLng);

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
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 */
	private void changeCamera(CameraUpdate update, CancelableCallback callback) {
		aMap.animateCamera(update, 500, callback);
	}

	private void initPoi() {
		showPoiInfo();
	}

	private void showPoiInfo() {

		markerOptionlst.clear();
		// for (int i = 0; i < res.getPois().size(); i++) {
		LatLng LatLng = new LatLng(mLat, mLon);
		// createBitmap("unclick", i + 1, "green2", 35);
		MarkerOptions markerOption1 = new MarkerOptions()
				.anchor(0.5f, 0.5f)
				.position(LatLng)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.collect_position)).perspective(true)
				.draggable(true).period(50);
		markerOptionlst.add(markerOption1);
		markerlst = aMap.addMarkers(markerOptionlst, true);
		// }

		isFirstLoc = false;
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		mapZoom = arg0.zoom;
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

}
