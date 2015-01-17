package com.xgr.wonderful.adapter;

import java.util.List;

import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.HelpActivity;
import com.xgr.wonderful.ui.MainActivity;
import com.xgr.wonderful.ui.SplashActivity;
import com.xgr.wonderful.utils.Constant;
import com.xgr.wonderful.utils.Sputil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 *     class desc: ����ҳ��������
 */
public class ViewPagerAdapter extends PagerAdapter
{

	// �����б�
	private List<View> mViews;
	private Activity mActivity;
    private Sputil sputil;
	private static final String SHAREDPREFERENCES_NAME = "first_pref";

	public ViewPagerAdapter(List<View> mViews, Activity mActivity)
	{
		this.mViews = mViews;
		this.mActivity = mActivity;
		if(null == sputil){
			sputil = new Sputil(mActivity, Constant.PRE_NAME);
		}
	}

	// ����arg1λ�õĽ���
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2)
	{
		((ViewPager) arg0).removeView(mViews.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0)
	{
	}

	// ��õ�ǰ������
	@Override
	public int getCount()
	{
		if (mViews != null)
		{
			return mViews.size();
		}
		return 0;
	}

	// ��ʼ��arg1λ�õĽ���
	@Override
	public Object instantiateItem(View arg0, int arg1)
	{
		((ViewPager) arg0).addView(mViews.get(arg1), 0);
		if (mActivity instanceof HelpActivity)
		{
			if (arg1 == mViews.size() - 1)
			{
				Button btn = (Button) arg0.findViewById(R.id.help_back);
				btn.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						
						mActivity.finish();
					}
				});
			}
			return mViews.get(arg1);
		}

		if (arg1 == mViews.size() - 1)
		{
			Button mStartWeiboImageButton = (Button) arg0
					.findViewById(R.id.iv_start_weibo);
			mStartWeiboImageButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// �����Ѿ�����
					setGuided();
					goHome();

				}

			});
		}
		return mViews.get(arg1);
	}

	private void goHome()
	{
		// ��ת
		Intent intent = new Intent(mActivity, MainActivity.class);
		mActivity.startActivity(intent);
		mActivity.finish();
	}

	/**
	 * 
	 * method desc�������Ѿ��������ˣ��´����������ٴ�����
	 */
	private void setGuided()
	{
		sputil.setInstalled(mActivity);
//		SharedPreferences preferences = mActivity.getSharedPreferences(
//				SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//		Editor editor = preferences.edit();
//		// ��������
//		editor.putBoolean("first_install", false);
//		// �ύ�޸�
//		editor.commit();
	}

	// �ж��Ƿ��ɶ������ɽ���
	@Override
	public boolean isViewFromObject(View arg0, Object arg1)
	{
		return (arg0 == arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1)
	{
	}

	@Override
	public Parcelable saveState()
	{
		return null;
	}

	@Override
	public void startUpdate(View arg0)
	{
	}

}