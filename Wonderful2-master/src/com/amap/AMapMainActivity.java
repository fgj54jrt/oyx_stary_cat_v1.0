package com.amap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.userinfo.ContantUserInfo;
import com.xgr.wonderful.R;

/**
 * AMapV2地图中简单介绍显示定位小蓝点
 */
public class AMapMainActivity extends Activity implements LocationSource,
		AMapLocationListener ,OnCameraChangeListener,CancelableCallback ,OnMapTouchListener,SensorEventListener{
	private AMap aMap;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private Marker marker;// 定位雷达小图标
	private Marker mGPSMarker;//定位图标
	private Circle circle;// 精确度圆圈
	private float mapZoom = 16;//地图缩放等级
	private boolean isMoving = false;// 地图在滑动中
	private Button btnZoomin, btnZoomout;
	private Button btn_getLocation; // 定位按钮
	private Button btn_traffic; // 交通路况按钮
	LinearLayout search_layout; //搜索框
	private LinearLayout btn_stop_driving; // 结束驾驶
	
	private boolean isShowTraffic = false; //实时路况是否打开
	private Runnable mapMoveRunnable, locationClick;
	private Handler handler;
	private long lastTime = 0;
	private final int TIME_SENSOR = 100;//传感器调用频率
	private float mAngle;///角度
	private boolean isUseSensorRotate = true;//是否用传感器旋转定位箭头
	private SensorManager mSensorManager; // 传感器管理类
	private Sensor mSensor;
	private AMapLocation curLocation;// 当前位置点
	private SharedPreferences adrPreference; // 储存地址
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.amap_main_activity);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		
		mapView = (MapView) findViewById(R.id.mMapView);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		// 初始化传感器
		mSensorManager = (SensorManager) this
						.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		adrPreference = getSharedPreferences(ContantUserInfo.ADDRESS_INFO,
				MODE_PRIVATE);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		handler = new Handler();
		btnZoomin = (Button) findViewById(R.id.btn_zoomin);
		btnZoomout = (Button) findViewById(R.id.btn_zoomout);
		btnZoomin.setOnClickListener(clickListener);

		btnZoomout.setOnClickListener(clickListener);
		
		btn_traffic = (Button) findViewById(R.id.map_traffic);
		btn_traffic.setOnClickListener(clickListener);

		search_layout = (LinearLayout) findViewById(R.id.search_search_layout);
		search_layout.setOnClickListener(clickListener);

		btn_getLocation = (Button) findViewById(R.id.btn_getLocation);
		btn_getLocation.setOnClickListener(clickListener);
		btn_getLocation.setEnabled(false);
		btn_getLocation.setBackgroundResource(R.drawable.ic_location_press);
		btn_stop_driving = (LinearLayout) findViewById(R.id.main_btn_stop_driving_ll);
		btn_stop_driving.setOnClickListener(clickListener);
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		
		mapMoveRunnable = new Runnable() {

			@Override
			public void run() {
				isMoving = false;
				if (mGPSMarker.getPosition() != null) {
					changeCamera(
							CameraUpdateFactory.newCameraPosition(new CameraPosition(
									mGPSMarker.getPosition(), aMap
											.getCameraPosition().zoom, 0, 0)),
							AMapMainActivity.this);
				}
				btn_getLocation.setBackgroundResource(R.drawable.ic_location);
				// aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			}
		};
		locationClick = new Runnable() {

			@Override
			public void run() {
				isMoving = false;
				btn_getLocation.setBackgroundResource(R.drawable.ic_location);
				// aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
			}
		};
		
		
 
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point1));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point2));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point3));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point4));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point5));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point6));
		
		//oyx 设置定位箭头
		marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icons(giflist).period(50));		
		mGPSMarker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
						.fromResource(R.drawable.location_marker)).anchor(
				(float) 0.5, (float) 0.5));//
		circle = aMap.addCircle(new CircleOptions()
				.strokeColor(Color.argb(80, 100, 149, 237))
				.fillColor(Color.argb(50, 100, 149, 237)).strokeWidth(1));
		mGPSMarker.setToTop();
		aMap.setOnMapTouchListener(this);
		aMap.setOnCameraChangeListener(this);
		aMap.getUiSettings().setCompassEnabled(true);//设置指南针是否可用
		aMap.getUiSettings().setAllGesturesEnabled(true);//设置是否支持所有手势
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);// 设置默认放大缩小按钮是否显示
		
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
		
		aMap.setMyLocationRotateAngle(180);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		
		aMap.moveCamera(CameraUpdateFactory.zoomTo(mapZoom));
	}

	 
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		registerSensorListener();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
		
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		curLocation=aLocation;
		if (mListener != null && aLocation != null) {
//			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
			marker.setPosition(new LatLng(aLocation.getLatitude(), aLocation
					.getLongitude()));// 定位雷达小图标
		}
			
			mGPSMarker.setPosition(new LatLng(aLocation.getLatitude(), aLocation
					.getLongitude()));
			circle.setCenter(new LatLng(aLocation.getLatitude(), aLocation
					.getLongitude()));
			circle.setRadius(aLocation.getAccuracy());
			
			btn_getLocation.setEnabled(true);
			if (!isMoving) {
				btn_getLocation.setBackgroundResource(R.drawable.ic_location);
				changeCamera(
						CameraUpdateFactory.newCameraPosition(new CameraPosition(
								new LatLng(aLocation.getLatitude(), aLocation
										.getLongitude()), mapZoom, 0, 0)),
						AMapMainActivity.this);
			}
			/*有gps信号的时候不用传感器改变角度*/
			if (!aLocation.getProvider().equals("lbs")&&aLocation.getSpeed() > 0.5 && aLocation
					.getBearing() != 0.0) {
				isUseSensorRotate=false;//有gps信号的时候不用传感器改变角度
				mGPSMarker.setRotateAngle(-aLocation.getBearing());
			}
		
	}

	/**
	 * 激活定位
	 */
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

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
		unRegisterSensorListener();
	}
	
	
	/**
	 * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
	 */
	private void changeCamera(CameraUpdate update, CancelableCallback callback) {
		aMap.animateCamera(update, 220, callback);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		mapZoom = arg0.zoom;
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			Intent intent;
			if (id == R.id.btn_zoomin) {
				changeCamera(CameraUpdateFactory.zoomIn(), null);
				CameraUpdateFactory.zoomBy(3);
//				handler.removeCallbacks(mTrafficSourceRunnable);
//				handler.post(mTrafficSourceRunnable);

			} else if (id==R.id.btn_zoomout) {

				changeCamera(CameraUpdateFactory.zoomOut(), null);
				CameraUpdateFactory.zoomBy(3);
//				handler.removeCallbacks(mTrafficSourceRunnable);
//				handler.post(mTrafficSourceRunnable);
			} else if (id ==R.id.map_traffic) {
				if (isShowTraffic) {
					aMap.setTrafficEnabled(false);
					btn_traffic.setBackgroundResource(R.drawable.lukuang);
					isShowTraffic = false;
					Toast.makeText(
							AMapMainActivity.this,
							getResources().getString(
									R.string.close_traffic),
							Toast.LENGTH_SHORT).show();
				} else {
					aMap.setTrafficEnabled(true);
					// mMapView.setTraffic(true);
					btn_traffic.setBackgroundResource(R.drawable.lukuang_press);
					isShowTraffic = true;
					Toast.makeText(
							AMapMainActivity.this,
							getResources().getString(R.string.open_traffic),
							Toast.LENGTH_SHORT).show();
				}

			} else if (id ==R.id.search_search_layout) {
				if (curLocation != null) {

					adrPreference
							.edit()
							.putString(ContantUserInfo.LAT,
									String.valueOf(curLocation.getLatitude()))
							.commit();
					adrPreference
							.edit()
							.putString(ContantUserInfo.LON,
									String.valueOf(curLocation.getLongitude()))
							.commit();

					intent = new Intent(AMapMainActivity.this,
							cyx_NaviSearchActivity_gaode.class);
					startActivity(intent);
				}

			} else if (id == R.id.btn_getLocation) {
				if (mGPSMarker.getPosition() != null) {
					isMoving = true;
					changeCamera(
							CameraUpdateFactory.newCameraPosition(new CameraPosition(
									mGPSMarker.getPosition(), mapZoom, 0, 0)),
							AMapMainActivity.this);
					if (mapMoveRunnable != null)
						handler.removeCallbacks(mapMoveRunnable);
					if (locationClick != null)
						handler.removeCallbacks(locationClick);
					handler.postDelayed(locationClick, 1000);
				}

//				isRequest = true;
			} else if (id == R.id.main_btn_stop_driving_ll) {
//				finishDialog();

			} else if (id == R.id.main_btn_report_ll) {
				// 交通信息上报

//				if (preLocation != null) {
//					intent = new Intent(cyx_MainActivity_gaode.this,
//							cyx_TrafficReport_gaode.class);
//					intent.putExtra("lon", preLocation.getLongitude());
//					intent.putExtra("lat", preLocation.getLatitude());
//					startActivity(intent);
//					Log.w("lxh", "// 交通信息上报");
//				}
			}
		}
	};


	@Override
	public void onTouch(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getAction() == MotionEvent.ACTION_MOVE) {
			isMoving = true;
			if (mapMoveRunnable != null)
				handler.removeCallbacks(mapMoveRunnable);
			if (locationClick != null)
				handler.removeCallbacks(locationClick);
			btn_getLocation.setBackgroundResource(R.drawable.ic_location_press);
			handler.postDelayed(mapMoveRunnable, 30 * 1000);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
			return;
		}

		if (!isUseSensorRotate) {
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION: {
			float x = event.values[0];
			x += getScreenRotationOnPhone(this);
			x %= 360.0F;
			if (x > 180.0F)
				x -= 360.0F;
			else if (x < -180.0F)
				x += 360.0F;
			if (Math.abs(mAngle - 90 + x) < 3.0f) {
				break;
			}
			mAngle = x;
			if (mGPSMarker != null) {
				mGPSMarker.setRotateAngle(-mAngle);
				// aMap.invalidate();
			}
			lastTime = System.currentTimeMillis();
		}

		}

	}
	// /**
	// * 获取当前屏幕旋转角度
	// *
	// * @param activity
	// * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
	// */
	public static int getScreenRotationOnPhone(Context context) {
		final Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			return 0;

		case Surface.ROTATION_90:
			return 90;

		case Surface.ROTATION_180:
			return 180;

		case Surface.ROTATION_270:
			return -90;
		}
		return 0;
	}
	public void registerSensorListener() {
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void unRegisterSensorListener() {
		mSensorManager.unregisterListener(this, mSensor);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
