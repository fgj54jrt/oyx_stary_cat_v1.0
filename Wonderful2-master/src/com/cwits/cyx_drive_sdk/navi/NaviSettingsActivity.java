package com.cwits.cyx_drive_sdk.navi;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviViewOptions;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.oyx_MyApplication;
import com.xgr.wonderful.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * 导航设置界面
 * 
 */
public class NaviSettingsActivity extends Activity {
	// ----------------View

	private ImageView mBackView;// 返回按钮
	private TextView title;
	private ImageView img_back;
	private ImageView roadreport, eleeye;// 路线规划，路线播报，电子眼播报
	private boolean mTrafficFlag;// 是否路况播报
	private boolean mCameraFlag;// 是否电子眼播报
	private boolean flag = false;
	private boolean flags = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cyx_navi_settings);// R.layout.activity_navisetting);
		Bundle bundle = getIntent().getExtras();
		processBundle(bundle);
		initView();
		oyx_MyApplication.getInstance().addActivity(this);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		title = (TextView) findViewById(R.id.TextTitle);
		img_back = (ImageView) findViewById(R.id.btn_back);
		title.setText(R.string.setting);
		img_back.setOnClickListener(new OnClickListener() {

			// @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(NaviSettingsActivity.this,
						NaviCustomActivity.class);
				intent.putExtras(getBundle());
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			}
		});

		roadreport = (ImageView) findViewById(R.id.road_report);
		if (mTrafficFlag) {
			roadreport.setImageResource(R.drawable.open);

		} else {
			roadreport.setImageResource(R.drawable.close);

		}
		roadreport.setOnClickListener(new listener());
		eleeye = (ImageView) findViewById(R.id.ele_eye_report);
		if (mCameraFlag) {
			eleeye.setImageResource(R.drawable.open);

		} else {
			eleeye.setImageResource(R.drawable.close);

		}
		eleeye.setOnClickListener(new listener());
		// mDayNightGroup = (RadioGroup) findViewById(MResource.getID(
		// NaviSettingsActivity.this, "day_night_group"));//
		// R.id.day_night_group);白天 黑夜
		//

	}

	/**
	 * 处理具体的bundle
	 * 
	 * @param bundle
	 */
	private void processBundle(Bundle bundle) {
		if (bundle != null) {

			mTrafficFlag = bundle.getBoolean(Utils.TRAFFIC);
			mCameraFlag = bundle.getBoolean(Utils.CAMERA);

		}
	}

	/**
	 * 根据当前界面的设置设置，构建bundle
	 * 
	 * @return
	 */
	private Bundle getBundle() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(Utils.TRAFFIC, mTrafficFlag);
		bundle.putBoolean(Utils.CAMERA, mCameraFlag);
		return bundle;
	}

	class listener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (v.getId() == R.id.road_report) {
				if (mTrafficFlag) {
					roadreport.setImageResource(R.drawable.close);
					mTrafficFlag = Utils.CLOSE_MODE;
				} else {
					roadreport.setImageResource(R.drawable.open);
					mTrafficFlag = Utils.OPEN_MODE;
				}

			} else if (v.getId() == R.id.ele_eye_report) {
				if (mCameraFlag) {
					eleeye.setImageResource(R.drawable.close);
					mCameraFlag = Utils.CLOSE_MODE;
				} else {
					eleeye.setImageResource(R.drawable.open);
					mCameraFlag = Utils.OPEN_MODE;
				}

			}
		}

	}

	/**
	 * 返回键监听
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(NaviSettingsActivity.this,
					NaviCustomActivity.class);
			intent.putExtras(getBundle());
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		oyx_MyApplication.getInstance().removeActivity(this);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// Bundle bd=getIntent().getExtras();
		// processBundle(bd);
		
	}
}
