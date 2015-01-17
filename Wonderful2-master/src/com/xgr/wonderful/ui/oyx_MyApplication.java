package com.xgr.wonderful.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.security.auth.PrivateCredentialPermission;

import com.amap.api.services.poisearch.PoiResult;
import com.xgr.wonderful.R;
import com.xgr.wonderful.utils.LogUtils;

import android.R.integer;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class oyx_MyApplication extends Application {
	private static final String TAG = "cyx_MyApplication";
	private static oyx_MyApplication mInstance = null;
	// 用于存放�?��日志模式的year、month、day
	public static int year;
	public static int month;
	public static int day;
	private static Stack<Activity> activityStack;

	public static Mainfragment mMainFMainfragment;
	public static SettingsFragment mSettingsfragment;
	public static FavFragment mFavFragment;

	public static boolean isMainFMainfragmentHasGet = false;
	public static boolean isSettingsFragmentHasGet = false;
	public static boolean isFavFragmentfragmentHasGet = false;
	public static PoiResult myPoiResult_gaode;

	public static int mCurrentFragment = 0;// 当前的fragment

	public static final int MAIN_FRAGMENT = 0;// 主fragment
	public static final int SETTING_FRAGMENT = 1;// 设置
	public static final int FAV_FRAGMENT = 2;// 收藏

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("--------" + TAG, "app oncreate");
		mInstance = this;

	}

	public static oyx_MyApplication getInstance() {
		if (null == mInstance) {
			mInstance = new oyx_MyApplication();
		}

		return mInstance;
	}

	/**
	 * add Activity 添加Activity到栈
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	public Stack<Activity> getActivityStack() {
		if (activityStack == null)
			return new Stack<Activity>();
		return activityStack;
	}

	/**
	 * 移除指定的Activity
	 */
	public void removeActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity = null;
		}
	}

	public void exitApplication() {
		if (activityStack != null) {
			for (int i = 0, size = activityStack.size(); i < size; i++) {
				activityStack.get(i).finish();
			}
		}
		// System.exit(0);
	}

	public void finishActivityStack() {
		if (activityStack != null) {
			for (int i = 0, size = activityStack.size(); i < size; i++) {
				activityStack.get(i).finish();
			}
		}
		// System.exit(0);
	}

	public static Mainfragment getMainfragment() {
		if (null == mMainFMainfragment) {
			mMainFMainfragment = new Mainfragment();
			isMainFMainfragmentHasGet = false;
		} else {
			isMainFMainfragmentHasGet = true;
		}
		return mMainFMainfragment;
	}

	public static SettingsFragment getSettingsFragment() {
		if (null == mSettingsfragment) {
			mSettingsfragment = new SettingsFragment();
			isSettingsFragmentHasGet = false;
		} else {
			isSettingsFragmentHasGet = true;
		}
		return mSettingsfragment;
	}

	public static FavFragment getFavFragment() {
		if (null == mMainFMainfragment) {
			mFavFragment = new FavFragment();
			isFavFragmentfragmentHasGet = false;
		} else {
			isFavFragmentfragmentHasGet = true;
		}
		return mFavFragment;
	}

	public static boolean isMainFMainfragmentHasGet() {
		return mMainFMainfragment != null;
	}

	public static boolean isSettingsFragmentHasGet() {
		return mSettingsfragment != null;
	}

	public static boolean isFavFragmentfragmentHasGet() {
		return mFavFragment != null;
	}

	public static PoiResult getMyPoiResult_gaode() {
		// if(null == myPoiResult_gaode){
		// myPoiResult_gaode = new PoiResult();
		// }
		return myPoiResult_gaode;
	}

	public static void setMyPoiResult_gaode(PoiResult result) {
		// if(null != result) {
		// if(null == myPoiResult_gaode) {
		// myPoiResult_gaode = new PoiResult(null, null);
		// }else{
		// myPoiResult_gaode = null;
		// }
		// }
		myPoiResult_gaode = result;
	}

	public static void setCurrentFragment(int currentID) {
		mCurrentFragment = currentID;
	}

	public static int getCurrentFragment() {
		return mCurrentFragment;
	}
    /*
     * 每次打开程序的时候给它们赋初值,要不下次进去列表刷新不出来
     */
	public static void ClearFragment() {
		mMainFMainfragment = null;
		mSettingsfragment = null;
		mFavFragment = null;
		isMainFMainfragmentHasGet = false;
		isSettingsFragmentHasGet = false;
		isFavFragmentfragmentHasGet = false;
	}
}
