package com.amap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.cwits.cyx_drive_sdk.navi.NaviCustomActivity;
import com.cwits.cyx_drive_sdk.navi.NaviRouteActivity;
import com.cwits.cyx_drive_sdk.navi.TTSController;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.oyx_MyApplication;
import com.xgr.wonderful.utils.JourneyTool;

public class cyx_PoiDetailListActivity_gaode extends Activity {
	private TextView tv_title;
	private TextView tv_Map;
	private ImageView img_back;
	private ListView detial_list;
	private double mLongitude, mLatitude;
	ArrayList<MyPoiResult> resultList;
	MyAdapter adapter;
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
	// 记录导航种类，用于记录当前选择是驾车还是步行
	private int mTravelMethod = DRIVER_NAVI_METHOD;
	private static final int DRIVER_NAVI_METHOD = 0;// 驾车导航
	private static final int WALK_NAVI_METHOD = 1;// 步行导航
	// 计算路的状态
	private final static int GPSNO = 0;// 使用我的位置进行计算、GPS定位还未成功状态
	private final static int CALCULATEERROR = 1;// 启动路径计算失败状态
	private final static int CALCULATESUCCESS = 2;// 启动路径计算成功状态
	private NaviLatLng preLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		oyx_MyApplication.getInstance().addActivity(this);
		setContentView(R.layout.cyx_poilist_layout);
		init();
		initMapAndNavi();
	}

	private void initMapAndNavi() {
		// 初始语音播报资源
		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
		TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
		ttsManager.init();
		mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
		mAmapNavi.setAMapNaviListener(ttsManager);// 设置语音模块播报
	}

	private void init() {

		tv_title = (TextView) findViewById(R.id.TextTitle);
		tv_title.setText(getIntent().getStringExtra("search_name"));
		tv_Map = (TextView) findViewById(R.id.TextTitle_Right);
		tv_Map.setVisibility(View.VISIBLE);
//		tv_Map.setBackground(getResources().getDrawable(MResource.getDrawableId(getApplicationContext(), "cyx_searchlist_bg")));
//		
//		tv_Map.setTextColor(MResource.getColorId(getApplicationContext(), "btn_world"));
		
		img_back = (ImageView) findViewById(R.id.btn_back);
		img_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initentToMain();
			}
		});
		detial_list = (ListView) findViewById(R.id.poi_result_listView);
		tv_Map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initentToMain();
			}
		});
		resultList = (ArrayList<MyPoiResult>) getIntent().getSerializableExtra(
				"myPoiResult");
		if (resultList != null && resultList.size() > 0) {
			adapter = new MyAdapter(resultList);
			detial_list.setAdapter(adapter);
		}
	}

	// -------------生命周期必须重写方法----------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 以上两句必须重写
		// 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
		AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
		mAmapNavi.startGPS();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		AMapNavi.getInstance(this)
//				.removeAMapNaviListener(getAMapNaviListener());
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		initentToMain();
		super.onBackPressed();
	}

	private void initentToMain() {
		Intent intent = new Intent(cyx_PoiDetailListActivity_gaode.this,
				cyx_SearchResultMapActivity_gaode.class);
		intent.putExtra("search_name", getIntent()
				.getStringExtra("search_name"));
		startActivity(intent);
//		this.finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (resultList != null) {
			resultList.clear();
			resultList = null;
		}
		oyx_MyApplication.getInstance().removeActivity(this);

		super.onDestroy();
	}

	class MyAdapter extends BaseAdapter {
		ArrayList<MyPoiResult> myResult;

		public MyAdapter(ArrayList<MyPoiResult> resultList) {
			this.myResult = resultList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myResult.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub

			return myResult.get(position);
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
			ViewHolder holder;
			if (convertView == null) {
				inflater = LayoutInflater
						.from(cyx_PoiDetailListActivity_gaode.this);
				convertView = inflater.inflate(R.layout.poi_list_item, null);
				holder = new ViewHolder();
				holder.tv_placeName = (TextView) convertView
						.findViewById(R.id.poi_list_PoiName);
				holder.tv_distance = (TextView) convertView
						.findViewById(R.id.poi_place_Distance);
				holder.tv_place_detial = (TextView) convertView
						.findViewById(R.id.poi_list_PoiAddress);
				holder.btn_navi = (LinearLayout) convertView
						.findViewById(R.id.poiList_btn_navi);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_placeName.setText((position + 1) + "."
					+ myResult.get(position).getPoiName());
			holder.tv_place_detial.setText(myResult.get(position)
					.getPoiAddress());
			final double end_latitude = myResult.get(position)
					.getEnd_latitude();
			final double end_longitude = myResult.get(position)
					.getEnd_longitude();
			Log.w("lxh", " mLatitude = " + mLatitude + "  mLongitude = "
					+ mLongitude + "  end_latitude = " + end_latitude
					+ "  end_longitude = " + end_longitude);
			
			if(cyx_SearchResultMapActivity_gaode.preLocation != null){
				mLongitude = cyx_SearchResultMapActivity_gaode.preLocation.getLongitude();
				mLatitude = cyx_SearchResultMapActivity_gaode.preLocation.getLatitude() ;
				
				double distance = JourneyTool.getDistance(mLatitude, mLongitude,
						end_latitude, end_longitude);
				holder.tv_distance.setText(distance + "km");
	
			}
				holder.btn_navi.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// navi(end_latitude, end_longitude, myResult.get(position)
					// .getPoiName());
					if (cyx_SearchResultMapActivity_gaode.preLocation != null){
						NaviLatLng naviLatLng = new NaviLatLng(myResult.get(
								position).getEnd_latitude(), myResult.get(position)
								.getEnd_longitude());
						mEndPoints.clear();
						mEndPoints.add(naviLatLng);
						calculateRoute();	
					} else {
						showToast("正在定位中，请稍后再试");
					}
					
				}
			});
			return convertView;
		}

		class ViewHolder {
			TextView tv_placeName;
			TextView tv_distance;
			TextView tv_place_detial;
			LinearLayout btn_navi;
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
			int driverIndex = calculateDriverRoute();
			if (driverIndex == CALCULATEERROR) {
				showToast("正在定位中，请稍后再试。");
				return;
			} else if (driverIndex == GPSNO) {
				return;
			}
			break;
		// 步行导航
		case WALK_NAVI_METHOD:
			int walkIndex = calculateWalkRoute();
			if (walkIndex == CALCULATEERROR) {
				showToast("正在定位中,请稍候再试。");
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

		if (cyx_SearchResultMapActivity_gaode.preLocation == null){
			//Toast.makeText(getApplicationContext(), "正在定位中", 1500);
			return code;
		}
		
		mLongitude = cyx_SearchResultMapActivity_gaode.preLocation.getLongitude();
		mLatitude = cyx_SearchResultMapActivity_gaode.preLocation.getLatitude() ;
		preLocation = new NaviLatLng(mLatitude, mLongitude);
		
		// 支持多个终点，终点列表的首点为导航终点，终点列表按车行方向排列，带有方向信息，可有效避免算路到马路的另一侧；
		NaviLatLng naviLatLng = new NaviLatLng(preLocation.getLatitude(),
				preLocation.getLongitude());
		Log.w("lxh", "preLocation.getLatitude() = " + preLocation.getLatitude()
				+ "preLocation.getLongitude() = " + preLocation.getLongitude()
				+ " mEndPoints.getLatitude() =" + mEndPoints.get(0).getLatitude() + " mEndPoints.getLatitude() =  "
				+ mEndPoints.get(0).getLongitude());
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
		if (cyx_SearchResultMapActivity_gaode.preLocation == null){
			//Toast.makeText(getApplicationContext(), "正在定位中", 1500);
			return code;
		}
		
		mLongitude = cyx_SearchResultMapActivity_gaode.preLocation.getLongitude();
		mLatitude = cyx_SearchResultMapActivity_gaode.preLocation.getLatitude() ;
		preLocation = new NaviLatLng(mLatitude, mLongitude);
		
		NaviLatLng naviLatLng = new NaviLatLng(preLocation.getLatitude(),
				preLocation.getLongitude());
		Log.w("lxh", "aLocation.getLatitude() = " + preLocation.getLatitude()
				+ "aLocation.getLongitude() = " + preLocation.getLongitude()
				+ " mEndPoints.get(0).getLatitude() ="
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
							cyx_PoiDetailListActivity_gaode.this,
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
	
}
