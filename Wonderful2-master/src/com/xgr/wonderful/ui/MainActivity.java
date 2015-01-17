package com.xgr.wonderful.ui;

//import net.youmi.android.offers.OffersManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import com.amap.AMapMainActivity;
import com.amap.api.mapcore.util.r;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.xgr.wonderful.MyApplication;
import com.xgr.wonderful.R;
import com.xgr.wonderful.utils.ActivityUtil;
import com.xgr.wonderful.utils.Constant;
import com.xgr.wonderful.utils.LogUtils;

public class MainActivity extends SlidingFragmentActivity implements
		OnClickListener, OnCheckedChangeListener {

	public static final String TAG = "MainActivity";
	private NaviFragment naviFragment;
	private ImageView leftMenu;
	private ImageView rightMenu;
	private SlidingMenu mSlidingMenu;
	private RadioGroup mRadioGroup;
	private RadioButton ButtonHome;
	private Mainfragment mMainFMainfragment;
	private FragmentManager fragmentManager;
	private SettingsFragment mSettingsfragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.center_frame);
		leftMenu = (ImageView) findViewById(R.id.topbar_menu_left);
		rightMenu = (ImageView) findViewById(R.id.topbar_menu_right);
		leftMenu.setOnClickListener(this);
		rightMenu.setOnClickListener(this);
		mRadioGroup = (RadioGroup) findViewById(R.id.home_radio_button_group);
		mRadioGroup.setOnCheckedChangeListener(this);
		mRadioGroup.setVisibility(View.VISIBLE);
		ButtonHome = (RadioButton) findViewById(R.id.home_tab_main);
		ButtonHome.setChecked(true);
		initFragment();
		// 显示提示对话框
		// showDialog();
	}

	Dialog dialog;

	private void showDialog() {
		dialog = new AlertDialog.Builder(this)
				.setTitle(getString(R.string.oyx_main_notify))
				.setMessage(R.string.dialog_tips)
				.setPositiveButton(getString(R.string.oyx_main_confirim),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						})
				.setNegativeButton(getString(R.string.oyx_main_cancle),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								dialog.dismiss();

							}
						}).create();
		dialog.show();
	}

	private void initFragment() {
		mSlidingMenu = getSlidingMenu();
		setBehindContentView(R.layout.frame_navi); // 给滑出的slidingmenu的fragment制定layout
		naviFragment = new NaviFragment();
		fragmentManager = naviFragment.getFragmentManager();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_navi, naviFragment).commit();
		// 设置slidingmenu的属性
		mSlidingMenu.setMode(SlidingMenu.LEFT);// 设置slidingmeni从哪侧出现
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);// 只有在边上才可以打开
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);// 偏移量
		mSlidingMenu.setFadeEnabled(true);
		mSlidingMenu.setFadeDegree(1.0f);
		mSlidingMenu.setMenu(R.layout.frame_navi);

		Bundle mBundle = null;
		// 导航打开监听事件
		mSlidingMenu.setOnOpenListener(new OnOpenListener() {
			@Override
			public void onOpen() {
			}
		});
		// 导航关闭监听事件
		mSlidingMenu.setOnClosedListener(new OnClosedListener() {

			@Override
			public void onClosed() {

			}
		});
		mSlidingMenu.setOnCloseListener(new OnCloseListener() {

			@Override
			public void onClose() {
				// TODO Auto-generated method stub
				if (oyx_MyApplication.getInstance().getCurrentFragment() == Constant.SETTING_FRAGMENT) {
					mRadioGroup.setVisibility(View.GONE);
				}else{
					mRadioGroup.setVisibility(View.VISIBLE);
				}
				// ButtonHome.setChecked(true);
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {  
		case R.id.topbar_menu_left:
			mSlidingMenu.toggle();
			break;
		case R.id.topbar_menu_right:
			// 当前用户登录
			BmobUser currentUser = BmobUser.getCurrentUser(MainActivity.this);
			if (currentUser != null) {
				// 允许用户使用应用,即有了用户的唯一标识符，可以作为发布内容的字段
				String name = currentUser.getUsername();
				String email = currentUser.getEmail();
				LogUtils.i(TAG, "username:" + name + ",email:" + email);
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, EditActivity.class);
				startActivity(intent);
			} else {
				// 缓存用户对象为空时， 可打开用户注册界面…
				Toast.makeText(MainActivity.this,
						getString(R.string.oyx_please_login),
						Toast.LENGTH_SHORT).show();
				// redictToActivity(mContext, RegisterAndLoginActivity.class,
				// null);
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,
						RegisterAndLoginActivity.class);
				startActivity(intent);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// OffersManager.getInstance(MainActivity.this).onAppExit();
	}

	private static long firstTime;

	/**
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (firstTime + 2000 > System.currentTimeMillis()) {
			MyApplication.getInstance().exit();
			super.onBackPressed();
		} else {
			ActivityUtil.show(MainActivity.this,
					getString(R.string.oyx_main_exit));
		}
		firstTime = System.currentTimeMillis();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		LogUtils.w("oyx", "checkedId = " + checkedId);
		switch (checkedId) {
		case R.id.home_tab_main:
			LogUtils.w("oyx", "home_tab_main ischecked ");
			oyx_MyApplication.getInstance().setCurrentFragment(
					Constant.MAIN_FRAGMENT);
			hideFragments(transaction);
			if (!oyx_MyApplication.getInstance().isMainFMainfragmentHasGet()) {
				LogUtils.w("oyx", "home_tab_main ischecked is null");
				mMainFMainfragment = oyx_MyApplication.getInstance()
						.getMainfragment();
				transaction.add(R.id.center, mMainFMainfragment);
			} else {
				mMainFMainfragment = oyx_MyApplication.getInstance()
						.getMainfragment();
				LogUtils.w("oyx", "home_tab_main ischecked is not null");
				transaction.show(mMainFMainfragment);
			}

			break;
		case R.id.home_tab_message:
			// hideFragments(transaction);
			Intent intent = new Intent(this, AMapMainActivity.class);
			startActivity(intent);
			break;
		case R.id.home_tab_add:
			hideFragments(transaction);
			oyx_MyApplication.getInstance().setCurrentFragment(
					Constant.SETTING_FRAGMENT);
			mRadioGroup.setVisibility(View.GONE);
			if (!oyx_MyApplication.getInstance().isSettingsFragmentHasGet()) {
				mSettingsfragment = oyx_MyApplication.getInstance()
						.getSettingsFragment();
				transaction.add(R.id.center, mSettingsfragment);
			} else {
				mSettingsfragment = oyx_MyApplication.getInstance()
						.getSettingsFragment();
				transaction.show(mSettingsfragment);
			}
			break;
		case R.id.home_tab_find:
			hideFragments(transaction);
			LogUtils.w("oyx", "home_tab_find ischecked ");
			break;
		case R.id.home_tab_personal:
			hideFragments(transaction);
			LogUtils.w("oyx", "home_tab_personal ischecked ");
			break;
		}

		transaction.commit();
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (mMainFMainfragment != null) {
			transaction.hide(mMainFMainfragment);
		}
		if (mSettingsfragment != null) {
			transaction.hide(mSettingsfragment);
		}
		// if(mAboutFragment!=null){
		// transaction.hide(mAboutFragment);
		// }
	}
}
