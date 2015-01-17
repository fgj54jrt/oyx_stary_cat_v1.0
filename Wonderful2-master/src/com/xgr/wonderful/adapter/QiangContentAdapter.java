package com.xgr.wonderful.adapter;

import com.xgr.wonderful.ui.QiangContentFragment;
import com.xgr.wonderful.utils.Constant;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class QiangContentAdapter extends SmartFragmentStatePagerAdapter {

	private static int NUM_ITEMS = 1;

	public QiangContentAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		if(this.getRegisteredFragment(position)!=null){
			Log.w("oyx","getRegisteredFragment is not  null");
			return getRegisteredFragment(position);
		}else{
			Log.w("oyx","getRegisteredFragment is null");
			//return FavFragment.newInstance();//QiangContentFragment.newInstance(position);
			return QiangContentFragment.newInstance(position);
		}
//		return MenuContentFragment.newInstance("pager" + position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return NUM_ITEMS;
	}

}
