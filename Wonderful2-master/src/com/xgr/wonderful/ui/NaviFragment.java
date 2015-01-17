package com.xgr.wonderful.ui;


//import net.youmi.android.offers.OffersManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.xgr.wonderful.R;
import com.xgr.wonderful.utils.Constant;
import com.xgr.wonderful.utils.LogUtils;



/**
 * 导航栏
 * 
 * @author yl
 * 
 */
public class NaviFragment extends Fragment implements OnClickListener
         {

    private static final int HOMEFRAGMENT = 0;
    private static final int SETTINGSFRAGMENT = 1;
    private static final int FEEDBACKFRAGMENT = 2;
    private static final int INTROFRAGMENT = 3;
    private static final int ABOUTFRAGMENT = 4;
    private static final int DINNERFRAGMENT = 5;

    private MainActivity mActivity;
    private TextView navi_home;
    private TextView navi_settings;
    private TextView navi_feedback;
    private TextView navi_intro;
    private TextView navi_about;

    Mainfragment mMainFMainfragment;
    SettingsFragment mSettingsfragment;
//    AboutFragment mAboutFragment;
    FavFragment mFavFragment;

    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View rootView;// 缓存Fragment view

    /**
     * 显示左边导航栏fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_navi, null);
        }

        fragmentManager = getFragmentManager();

        init();

        return rootView;

    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    /**
     * 初始化，设置点击事件
     */
    private void init() {
        navi_home = (TextView) rootView.findViewById(R.id.tv_navi_main);
        navi_feedback = (TextView) rootView.findViewById(R.id.tv_navi_feedback);
        navi_settings = (TextView) rootView.findViewById(R.id.tv_navi_settings);
        navi_intro = (TextView) rootView.findViewById(R.id.tv_navi_intro);
        navi_about = (TextView) rootView.findViewById(R.id.tv_navi_about);

        navi_home.setSelected(true);// 榛樿閫変腑鑿滃崟
        navi_feedback.setSelected(false);
        navi_settings.setSelected(true);
        navi_intro.setSelected(false);
        navi_about.setSelected(false);

        OnTabSelected(HOMEFRAGMENT);

        navi_home.setOnClickListener(this);
        navi_feedback.setOnClickListener(this);
        navi_settings.setOnClickListener(this);
        navi_intro.setOnClickListener(this);
        navi_about.setOnClickListener(this);
    }

    /**
     * 点击导航栏切换 同时更改标题
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.tv_navi_main:

            navi_home.setSelected(true);// 菜单设置为被选中状态，其余设置为非选中状态
            navi_feedback.setSelected(false);
            navi_settings.setSelected(false);
            navi_intro.setSelected(false);
            navi_about.setSelected(false);
            
            OnTabSelected(HOMEFRAGMENT);
            break;
        case R.id.tv_navi_feedback:

            navi_home.setSelected(false);
            navi_feedback.setSelected(true);
            navi_settings.setSelected(false);
            navi_intro.setSelected(false);
            navi_about.setSelected(false);

            OnTabSelected(FEEDBACKFRAGMENT);
            break;
        case R.id.tv_navi_settings:// 

            navi_home.setSelected(false);
            navi_feedback.setSelected(false);
            navi_settings.setSelected(true);
            navi_intro.setSelected(false);
            navi_about.setSelected(false);

            OnTabSelected(SETTINGSFRAGMENT);
            break;
        case R.id.tv_navi_intro:

            navi_home.setSelected(false);
            navi_feedback.setSelected(false);
            navi_settings.setSelected(false);
            navi_intro.setSelected(true);
            navi_about.setSelected(false);

            OnTabSelected(INTROFRAGMENT);
            break;
        case R.id.tv_navi_about:
        	 navi_home.setSelected(false);
             navi_feedback.setSelected(false);
             navi_settings.setSelected(false);
             navi_intro.setSelected(false);
             navi_about.setSelected(true);
             
             OnTabSelected(ABOUTFRAGMENT);
        	break;
        }
        mActivity.getSlidingMenu().toggle();
    }
    
    //选中导航中对应的tab选项
    private void OnTabSelected(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        switch (index) {
        case HOMEFRAGMENT:
        	hideFragments(transaction);
        	oyx_MyApplication.getInstance().setCurrentFragment(Constant.MAIN_FRAGMENT);
          if (!oyx_MyApplication.getInstance().isMainFMainfragmentHasGet()) {
        	  LogUtils.w("oyx", "OnTabSelected home_tab_main is null ");
              mMainFMainfragment = oyx_MyApplication.getInstance().getMainfragment();
              transaction.add(R.id.center, mMainFMainfragment);
          } else {
        	  LogUtils.w("oyx", "OnTabSelected home_tab_main is not null ");
        	  mMainFMainfragment = oyx_MyApplication.getInstance().getMainfragment();
              transaction.show(mMainFMainfragment);
              
          }
            break;
        case SETTINGSFRAGMENT: 
        	oyx_MyApplication.getInstance().setCurrentFragment(Constant.SETTING_FRAGMENT);
        	hideFragments(transaction);
        	if (!oyx_MyApplication.getInstance().isSettingsFragmentHasGet()) {
        		mSettingsfragment = oyx_MyApplication.getInstance().getSettingsFragment();
                transaction.add(R.id.center, mSettingsfragment);
            } else {
            	mSettingsfragment = oyx_MyApplication.getInstance().getSettingsFragment();
                transaction.show(mSettingsfragment);
            }
            break;
        case FEEDBACKFRAGMENT:

//        	FeedbackAgent agent = new FeedbackAgent(mActivity);
//			agent.startFeedbackActivity();
			oyx_MyApplication.getInstance().setCurrentFragment(Constant.FAV_FRAGMENT);
        	BmobUser currentUser = BmobUser.getCurrentUser(mActivity);
			if (currentUser != null) {
				//// 允许用户使用应用,即有了用户的唯一标识符，可以作为发布内容的字段
				hideFragments(transaction);
	        	if(!oyx_MyApplication.getInstance().isFavFragmentfragmentHasGet()){
	        		mFavFragment =oyx_MyApplication.getInstance().getFavFragment();
	        		transaction.add(R.id.center, mFavFragment);
	        	}else{
	        		mFavFragment =oyx_MyApplication.getInstance().getFavFragment();
	        		transaction.show(mFavFragment);
	        	}
			} else {
				// 缓存用户对象为空时， 可打开用户注册界面…
				Toast.makeText(mActivity, getString(R.string.oyx_please_login),
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setClass(mActivity, RegisterAndLoginActivity.class);
				startActivity(intent);
			}
            break;
        case INTROFRAGMENT:

//        	OffersManager.getInstance(mActivity).showOffersWall();
            break;
        case ABOUTFRAGMENT:
        	hideFragments(transaction);
//        	if(null == mAboutFragment){
//        		mAboutFragment = new AboutFragment();
//        		transaction.add(R.id.center, mAboutFragment);
//        	}else{
//        		transaction.show(mAboutFragment);
//        	}
        	break;
        }
        transaction.commit();
    }

    /**
     * 将所有fragment都置为隐藏状态
     * 
     *@param transaction
     *            用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
    	if(mMainFMainfragment!=null){
    		transaction.hide(mMainFMainfragment);
    	}
    	if(mSettingsfragment!=null){
    		transaction.hide(mSettingsfragment);
    	}
//    	if(mAboutFragment!=null){
//    		transaction.hide(mAboutFragment);
//    	}
    	if(mFavFragment!=null){
    		transaction.hide(mFavFragment);
    	}
    }

}

