package com.xgr.wonderful.ui;

import java.util.ArrayList;
import java.util.List;

import com.xgr.wonderful.adapter.ViewPagerAdapter;
import com.xgr.wonderful.R;

import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * ��������
 */
public class GuideActivity extends Activity implements OnPageChangeListener {

	private ViewPager vp;
	private ViewPagerAdapter vpAdapter;
	private List<View> views;
	private ViewGroup viewPoints;
	// ��¼��ǰѡ��λ��
	private int currentIndex;
	private ImageView imageView;

	/** 将小圆点的图片用数组表示 */
	private ImageView[] imageViews;
	
	private int PAGE_NUMBER=3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);

		// ��ʼ��ҳ��
		initViews();
		imageViews = new ImageView[PAGE_NUMBER];
		for (int i = 0; i < PAGE_NUMBER; i++) {
			imageView = new ImageView(this);
			// 设置小圆点imageview的参数
			imageView.setLayoutParams(new LayoutParams(30, 30));// 创建一个宽高均为20
																// 的布局
			imageView.setPadding(100, 0, 100, 0);
			// 将小圆点layout添加到数组中
			imageViews[i] = imageView;

			// 默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
			if (i == 0) {
				imageViews[i].setBackgroundResource(R.drawable.point_select);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.point_normal);
			}

			// 将imageviews添加到小圆点视图组
			viewPoints.addView(imageViews[i]);
		}
	}

	private void initViews() {
		LayoutInflater inflater = LayoutInflater.from(this);
		viewPoints = (ViewGroup) findViewById(R.id.viewGroup);
		views = new ArrayList<View>();
		// ��ʼ������ͼƬ�б�
		views.add(inflater.inflate(R.layout.what_new_one, null));
		views.add(inflater.inflate(R.layout.what_new_two, null));
		views.add(inflater.inflate(R.layout.what_new_three, null));

		// ��ʼ��Adapter
		vpAdapter = new ViewPagerAdapter(views, this);

		vp = (ViewPager) findViewById(R.id.viewpager);
		vp.setAdapter(vpAdapter);
		// �󶨻ص�
		vp.setOnPageChangeListener(this);
	}

	// ������״̬�ı�ʱ����
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	// ����ǰҳ�汻����ʱ����
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	// ���µ�ҳ�汻ѡ��ʱ����
	@Override
	public void onPageSelected(int arg0) {
		for (int i = 0; i < PAGE_NUMBER; i++) {
			imageViews[i].setBackgroundResource(R.drawable.point_normal);
		}
		imageViews[arg0].setBackgroundResource(R.drawable.point_select);
	}

}