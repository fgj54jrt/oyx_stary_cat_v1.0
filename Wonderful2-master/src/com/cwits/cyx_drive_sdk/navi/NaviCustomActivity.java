package com.cwits.cyx_drive_sdk.navi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;






import android.R.bool;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amap.AMapMainActivity;
import com.amap.api.mapcore.util.i;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.oyx_MyApplication;
import com.xgr.wonderful.utils.Utils;


/**
 * 实时导航界面
 * 
 */
public class NaviCustomActivity extends Activity implements
		AMapNaviViewListener {

	private AMapNaviView mAmapAMapNaviView;
	// 导航可以设置的参数
	private boolean mDayNightFlag = Utils.DAY_MODE;// 默认为白天模式
	private boolean mDeviationFlag = Utils.YES_MODE;// 默认进行偏航重算
	private boolean mJamFlag = Utils.YES_MODE;// 默认进行拥堵重算
	private boolean mTrafficFlag = Utils.OPEN_MODE;// 默认进行交通播报
	private boolean mCameraFlag = Utils.OPEN_MODE;// 默认进行摄像头播报
	private boolean mScreenFlag = Utils.YES_MODE;// 默认是屏幕常亮

	// 导航界面风格
	private int mThemeStle;
	// 导航监听
	private AMapNaviListener mAmapNaviListener;

	private LinearLayout ll_Movie;// 昼夜模式布局
	private ImageView img; // 默认为自动
	private List<ImageView> imgList;// 昼、夜、自动图片集合
	private LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);// 配置高宽
	private boolean isFirst = false;// 是否第一次进入
	private AMapNaviViewOptions viewOptions = new AMapNaviViewOptions();
	private ImageView imgAutomate,imgSun,imgNeight,imgClose,imgNorth;//自动,白天，黑夜，关闭
	private boolean flag=false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_navicustom);
		// 实时导航方式进行导航
		AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);
		oyx_MyApplication.getInstance().addActivity(this);
		setAmapNaviViewOptions();
		initView(savedInstanceState);

	}

	private void initView(Bundle savedInstanceState) {
		mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.customnavimap);
		mAmapAMapNaviView.onCreate(savedInstanceState);
		mAmapAMapNaviView.setAMapNaviViewListener(this);
		setAmapNaviViewOptions();
		imgNorth=(ImageView) findViewById(R.id.img_north);
		imgNorth.setVisibility(View.GONE);
		imgNorth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!flag) {
					imgNorth.setImageResource(R.drawable.north_car);
				}else {
					imgNorth.setImageResource(R.drawable.north_map);
				}
				flag=!flag;
			}
		});
		ll_Movie = (LinearLayout) findViewById(R.id.id_layout_movie);// (R.id.customnavimap);(R.id.id_layout_movie);
		ll_Movie.setVisibility(View.VISIBLE);

		img = new ImageView(this);
		img.setBackgroundResource(R.drawable.white);
		img.setImageResource(R.drawable.zidong);
		img.setAdjustViewBounds(true);// 图片自适配
		// img.setClickable(true);
		ll_Movie.addView(img, lp1);

		initArray();
		img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (img.getVisibility() == 0)
					img.setVisibility(View.GONE);
				if (!isFirst) {
					for (int i = 0; i < imgList.size(); i++) {
						imgList.get(i).setClickable(true);
						ll_Movie.addView(imgList.get(i), lp1);
					}
					isFirst = true;
				} else {
					for (ImageView lv : imgList) {
						lv.setVisibility(View.VISIBLE);
					}
				}

			}
		});

	}

	// 初始化图片集合
	public void initArray() {
		imgList = new ArrayList<ImageView>();
		imgAutomate = new ImageView(this);
		imgAutomate.setBackgroundResource(R.drawable.whites);
		imgAutomate.setImageResource(R.drawable.zidong);
		imgAutomate.setAdjustViewBounds(true);
		imgAutomate.setTag(1);

		imgNeight = new ImageView(getApplicationContext());
		imgNeight.setBackgroundResource(R.drawable.whites);
		imgNeight.setImageResource(R.drawable.night);
		imgNeight.setAdjustViewBounds(true);
		imgNeight.setTag(2);

		imgSun = new ImageView(getApplicationContext());
		imgSun.setBackgroundResource(R.drawable.whites);
		imgSun.setImageResource(R.drawable.sun);
		imgSun.setAdjustViewBounds(true);
		imgSun.setTag(3);

		imgClose = new ImageView(getApplicationContext());
		imgClose.setBackgroundResource(R.drawable.whites);
		imgClose.setImageResource(R.drawable.closes);

		imgClose.setAdjustViewBounds(true);
		imgClose.setTag(4);
		imgList.add(imgAutomate);
		imgList.add(imgNeight);
		imgList.add(imgSun);
		imgList.add(imgClose);

		for (final ImageView imgview : imgList) {
			imgview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (imgview.getTag().equals(1)) {
						closeImgList();
						img.setVisibility(View.VISIBLE);
						img.setImageResource(R.drawable.zidong);
						Calendar cal = Calendar.getInstance();
						int hour = cal.get(Calendar.HOUR_OF_DAY);
						if (hour >= 6 && hour < 18) {
							mDayNightFlag = Utils.DAY_MODE;
							viewOptions.setNaviNight(mDayNightFlag);
							viewOptions.setTrafficBarEnabled(false);
							mAmapAMapNaviView.setViewOptions(viewOptions);// 昼
							viewOptions.setTrafficBarEnabled(true);
						} else {
							mDayNightFlag = Utils.NIGHT_MODE;
							viewOptions.setNaviNight(mDayNightFlag);
							viewOptions.setTrafficBarEnabled(false);
							mAmapAMapNaviView.setViewOptions(viewOptions);// 夜
							viewOptions.setTrafficBarEnabled(true);
						}
						// refresh();
					} else if (imgview.getTag().equals(2)) {
						closeImgList();
						img.setVisibility(View.VISIBLE);
						img.setImageResource(R.drawable.night);
						mDayNightFlag = Utils.NIGHT_MODE;
						viewOptions.setNaviNight(mDayNightFlag);
						viewOptions.setTrafficBarEnabled(false);
						mAmapAMapNaviView.setViewOptions(viewOptions);// 夜
						viewOptions.setTrafficBarEnabled(true);
						// refresh();
					} else if (imgview.getTag().equals(3)) {
						closeImgList();
						img.setVisibility(View.VISIBLE);
						img.setImageResource(R.drawable.sun);
						mDayNightFlag = Utils.DAY_MODE;
						viewOptions.setNaviNight(mDayNightFlag);
						viewOptions.setTrafficBarEnabled(false);
						mAmapAMapNaviView.setViewOptions(viewOptions);// 昼
						viewOptions.setTrafficBarEnabled(true);

					} else if (imgview.getTag().equals(4)) {
						closeImgList();
						img.setVisibility(View.VISIBLE);
					}

				}
			});
		}
	}

	private void closeImgList() {
		for (int i = 0; i < imgList.size(); i++) {
			imgList.get(i).setVisibility(View.GONE);
		}
	}

	/**
	 * 设置导航的参数
	 */
	private void setAmapNaviViewOptions() {
		if (mAmapAMapNaviView == null) {
			return;
		}

		viewOptions.setSettingMenuEnabled(true);// 设置导航setting可用
		viewOptions.setNaviNight(mDayNightFlag);// 设置导航是否为黑夜模式
		viewOptions.setReCalculateRouteForYaw(mDeviationFlag);// 设置导偏航是否重算
		viewOptions.setReCalculateRouteForTrafficJam(mJamFlag);// 设置交通拥挤是否重算
		viewOptions.setTrafficInfoUpdateEnabled(mTrafficFlag);// 设置是否更新路况
		viewOptions.setCameraInfoUpdateEnabled(mCameraFlag);// 设置摄像头播报
		viewOptions.setScreenAlwaysBright(mScreenFlag);// 设置屏幕常亮情况
		viewOptions.setNaviViewTopic(mThemeStle);// 设置导航界面主题样式

		mAmapAMapNaviView.setViewOptions(viewOptions);

	}

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
					// 可以在频繁重算时进行设置,例如五次之后
					// i++;
					// if (i >= 5) {
					// AMapNaviViewOptions viewOptions = new
					// AMapNaviViewOptions();
					// viewOptions.setReCalculateRouteForYaw(false);
					// mAmapAMapNaviView.setViewOptions(viewOptions);
					// }

				}

				@Override
				public void onReCalculateRouteForTrafficJam() {

				}

				@Override
				public void onLocationChange(AMapNaviLocation location) {

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

				}

				@Override
				public void onCalculateRouteFailure(int arg0) {

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

	// -------处理
	/**
	 * 导航界面返回按钮监听
	 * */
	@Override
	public void onNaviCancel() {
		finish();
	}

	/**
	 * 点击设置按钮的事件
	 */
	@Override
	public void onNaviSetting() {
		Bundle bundle = new Bundle();
		bundle.putInt(Utils.THEME, mThemeStle);
		bundle.putBoolean(Utils.DAY_NIGHT_MODE, mDayNightFlag);
		bundle.putBoolean(Utils.DEVIATION, mDeviationFlag);
		bundle.putBoolean(Utils.JAM, mJamFlag);
		bundle.putBoolean(Utils.TRAFFIC, mTrafficFlag);
		bundle.putBoolean(Utils.CAMERA, mCameraFlag);
		bundle.putBoolean(Utils.SCREEN, mScreenFlag);
		Intent intent = new Intent(NaviCustomActivity.this,
				NaviSettingsActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);

	}

	@Override
	public void onNaviMapMode(int arg0) {

	}

	private void processBundle(Bundle bundle) {

		if (bundle != null) {
			mDayNightFlag = bundle.getBoolean(Utils.DAY_NIGHT_MODE,
					mDayNightFlag);
			mDeviationFlag = bundle.getBoolean(Utils.DEVIATION, mDeviationFlag);
			mJamFlag = bundle.getBoolean(Utils.JAM, mJamFlag);
			mTrafficFlag = bundle.getBoolean(Utils.TRAFFIC, mTrafficFlag);
			mCameraFlag = bundle.getBoolean(Utils.CAMERA, mCameraFlag);
			mScreenFlag = bundle.getBoolean(Utils.SCREEN, mScreenFlag);
			mThemeStle = bundle.getInt(Utils.THEME);

		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

//		if (keyCode == KeyEvent.KEYCODE_BACK) {
////			 Intent intent = new Intent(NaviCustomActivity.this,
////			 AMapMainActivity.class);
////			 startActivity(intent);
//			finish();
//
//		}
		return super.onKeyDown(keyCode, event);
	}
   
	// ------------------------------生命周期方法---------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mAmapAMapNaviView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		processBundle(bundle);

		AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
		mAmapAMapNaviView.onResume();
		
	}

	@Override
	public void onPause() {
		mAmapAMapNaviView.onPause();
		super.onPause();
		AMapNavi.getInstance(this)
				.removeAMapNaviListener(getAMapNaviListener());
	}

	@Override
	public void onDestroy(){

		super.onDestroy();
		mAmapAMapNaviView.onDestroy();
		oyx_MyApplication.getInstance().removeActivity(this);
	}

	@Override
	public void onNaviTurnClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNextRoadClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanViewButtonClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		return super.onTouchEvent(event);

	}
	// public void refresh(){
	// finish();
	// Intent intent = new Intent(NaviCustomActivity.this,
	// NaviCustomActivity.class);
	// intent.putExtras(getBundle());
	// startActivity(intent);
	//
	// }
	// public Bundle getBundle(){
	// Bundle bd=new Bundle();
	// bd.putBoolean(Utils.DAY_NIGHT_MODE, mDayNightFlag);
	// // bd.putString("lv1", imgAutomate.getTag().toString());
	// // bd.putString("lv2", imgNeight.getTag().toString());
	// // bd.putString("lv3", imgSun.getTag().toString());
	// // bd.putString("lv4", imgAutomate.getTag().toString());
	// return bd;
	// }

}
