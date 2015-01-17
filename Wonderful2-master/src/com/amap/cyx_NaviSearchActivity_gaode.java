package com.amap;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.cwits.cyx_drive_sdk.db.DBManager;
import com.userinfo.ContantUserInfo;
import com.xgr.wonderful.R;
import com.xgr.wonderful.ui.oyx_MyApplication;

public class cyx_NaviSearchActivity_gaode extends Activity implements OnPoiSearchListener{
	private static final String TAG = "cyx_NaviSearchActivity";
	private ListView searchListView;
	private EditText place_edt;
	private LinearLayout history_layout;
	private ListView search_historyListView;
	private TextView clear_history,btn_search;
//	private Button btn_search;
//	private MKSearch mSearch;
	private String city;
	private AutoCompleteAdapter autoAdapter;
	private List<String> mPlaceList;
	private ProgressBar search_bar;
	private ImageView img_back;
	// private Button btnVoiceSearch;
	private DBManager dBManager;
	private SearchHistoryAdapter historyAdapter;
	private ArrayList<String> historyList;
	TextView tv_main_search;
	private ProgressDialog mProgressDialog = null;
	Runnable mTimeOutRunnable; // 超时处理
	private Handler mHandler;
	String search_name;
	// 用户收藏列表

	private ArrayList<ContentValues> collectList;
//	private LinearLayout collect_layout;
	private ListView collectListView;
	// private ListView collect_historyListView;
	private CollectHistoryAdapter collectAdapter;

	// private ArrayList allList;
	private static final int TYPE_COLLECT = 1;
	private static final int TYPE_HISTORY = 2;
	private static final int TYPE_SEG = 3;
	private long segIndex;// collectLiset historyList列表分割位置，segIndex =
							// collectList的最大index
	private ArrayList<String> totalLiset;
	
	private int currentPage = 0;// 当前页面，从0开始计数
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cyx_search_layout);
		oyx_MyApplication.getInstance().addActivity(this);
		init();
	}

	private void init() {
		mHandler = new Handler();
		dBManager = DBManager.getInstance(cyx_NaviSearchActivity_gaode.this);
		dBManager.open();
		city = getSharedPreferences(ContantUserInfo.ADDRESS_INFO, MODE_PRIVATE)
				.getString(ContantUserInfo.ADDRESS_CITY, "深圳");
		historyList = dBManager.getAllSearchHistory();
		searchListView = (ListView) findViewById(R.id.List_search_now);
		place_edt = (EditText) findViewById(R.id.tv_search_AutoComplete);
		clear_history = (TextView) findViewById(R.id.tv_clearSearchHitory);
		btn_search = (TextView) findViewById(R.id.search_btn);
		btn_search.setVisibility(View.VISIBLE);
		search_historyListView = (ListView) findViewById(R.id.List_search_history);
		history_layout = (LinearLayout) findViewById(R.id.search_history_layout);
		tv_main_search = (TextView) findViewById(R.id.tv_main_search);
		tv_main_search.setVisibility(View.GONE);

		// 用户收藏列表
		collectList = dBManager.getAllUserCollect();
//		collect_layout = (LinearLayout) findViewById(R.id.collect_layout);

		segIndex = collectList.size() - 1;
		totalLiset = new ArrayList<String>();

		if (historyList != null && historyList.size() > 0
				|| collectList != null && collectList.size() > 0) {
			historyAdapter = new SearchHistoryAdapter(historyList, collectList,
					this);
			// historyAdapter = new SearchHistoryAdapter(historyList,
			// historyList);
			search_historyListView.setAdapter(historyAdapter);
			search_historyListView.setVisibility(View.VISIBLE);
			history_layout.setVisibility(View.VISIBLE);

			if (historyList == null || historyList.size() <= 0) {
				clear_history.setVisibility(View.GONE);
			}
		}
		
		clear_history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dBManager.deleteSeachHistory();

				historyList.clear();
				// totalLiset.clear();
				int seg = collectList.size();
				int size = totalLiset.size();
				for (int i = size - 1; i >= seg; i--) {
					totalLiset.remove(i);

				}
				historyAdapter.notifyDataSetChanged();
				clear_history.setVisibility(View.GONE);
				 history_layout.setVisibility(View.GONE);
				 search_historyListView.setVisibility(View.GONE);
			}
		});
		mPlaceList = new ArrayList<String>();
		search_bar = (ProgressBar) findViewById(R.id.search_progressBar);
		img_back = (ImageView) findViewById(R.id.search_img_back);
		img_back.setVisibility(View.VISIBLE);
		img_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cyx_NaviSearchActivity_gaode.this.finish();
			}
		});
		place_edt.setVisibility(View.VISIBLE);
		place_edt.addTextChangedListener(new MyTextWatchListener());
		searchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> viewList, View arg1,
					int position, long arg3) {
				String secContent = mPlaceList.get(position).toString();
				if (!TextUtils.isEmpty(secContent)) {
					if (historyList.contains(secContent)) {
						dBManager.delete(DBManager.TABLE_NAME, secContent);
					}
					dBManager.saveSeachHistory(secContent);
				}
				search_name = mPlaceList.get(position).toString();
				showDialog();
				if (mTimeOutRunnable != null)
					mHandler.removeCallbacks(mTimeOutRunnable);
				mHandler.postDelayed(mTimeOutRunnable, 30 * 1000);
				doSearchQuery(10,search_name);
			}

		});

		search_historyListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub
						int type = historyAdapter.getItemViewType(position);
						switch (type) {
						case TYPE_COLLECT:
							jumpToPoint(position);
							break;
						case TYPE_HISTORY:
							if (historyList != null && historyList.size() > 0) {
								doSearchQuery(10,totalLiset.get(position).toString());
								search_name = totalLiset.get(position)
										.toString();
								System.out.println("----------search city:"
										+ city);
								showDialog();
								if (totalLiset.contains(search_name)) {
									dBManager.delete(DBManager.TABLE_NAME,
											search_name);
									dBManager.saveSeachHistory(search_name);
								}
								if (mTimeOutRunnable != null)
									mHandler.removeCallbacks(mTimeOutRunnable);
								mHandler.postDelayed(mTimeOutRunnable,
										30 * 1000);
							}
							break;
						}

					}
				});

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputMethodManager inputManager = (InputMethodManager) place_edt
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(place_edt, 0);
			}
		}, 800);

		mTimeOutRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mProgressDialog.dismiss();
				search_bar.setVisibility(View.GONE);
				Toast.makeText(
						cyx_NaviSearchActivity_gaode.this,
						getResources().getString(R.string.request_timeOut), Toast.LENGTH_SHORT)
						.show();
			}
		};
	}


	class MyTextWatchListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			String newText = s.toString().trim();
			Inputtips inputTips = new Inputtips(cyx_NaviSearchActivity_gaode.this,
					new InputtipsListener() {

						@Override
						public void onGetInputtips(List<Tip> tipList, int rCode) {
							if (rCode == 0) {// 正确返回
								for (int i = 0; i < tipList.size(); i++) {
									mPlaceList.add(tipList.get(i).getName());
									Log.w("lxh","tipList.get(i).getName() = "+tipList.get(i).getName());
								}
								Log.w("lxh","正确返回");								
								autoAdapter = new AutoCompleteAdapter(mPlaceList);
								autoAdapter.notifyDataSetChanged();
								searchListView.setAdapter(autoAdapter);
								if (searchListView.getCount()>0) {
									Log.i("lxh","ListView不为空");	
									searchListView.setVisibility(View.VISIBLE);
								}else {
									Log.i("lxh","ListView为空");	
									searchListView.setVisibility(View.GONE);
								}
								
								search_bar.setVisibility(View.GONE);
								history_layout.setVisibility(View.GONE);
								
							}
						}
					});
			try {
				inputTips.requestInputtips(newText, city);// 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号
				
			} catch (AMapException e) {
				e.printStackTrace();
			}
			
			if (!TextUtils.isEmpty(city) && s.length() >= 1) {
				//mSearch.suggestionSearch(s.toString(), city);
				System.out.println("-----------suggestionSearch city:" + city);
				search_bar.setVisibility(View.VISIBLE);
//				btn_search.setTextColor(getResources().getColor(
//						MResource.getColorId(getApplicationContext(),
//								"btn_world")));
				btn_search.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String secContent = place_edt.getText().toString();
						if (!TextUtils.isEmpty(secContent)) {
							if (historyList.contains(secContent)) {
								dBManager.delete(DBManager.TABLE_NAME,
										secContent);
							}
							dBManager.saveSeachHistory(secContent);
						}

						showDialog();
						if (mTimeOutRunnable != null)
							mHandler.removeCallbacks(mTimeOutRunnable);
						mHandler.postDelayed(mTimeOutRunnable, 30 * 1000);
						doSearchQuery(10,place_edt.getText().toString());

					}
				});
			} else if (s.length() <= 0) {
				search_bar.setVisibility(View.GONE);
				btn_search.setOnClickListener(null);
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cyx_NaviSearchActivity_gaode.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// dBManager.close();
		super.onStop();
	}

	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mTimeOutRunnable != null)
			mHandler.removeCallbacks(mTimeOutRunnable);
//		 dBManager.close();
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		oyx_MyApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	// 自动补全列表Adapter
	class AutoCompleteAdapter extends BaseAdapter {

		List<String> placeList;

		public AutoCompleteAdapter(List<String> placeList) {
			this.placeList = placeList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return placeList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return placeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			LayoutInflater inflater;
			if (convertView == null) {
				inflater = LayoutInflater.from(cyx_NaviSearchActivity_gaode.this);
				convertView = inflater.inflate(R.layout.cyx_search_list_item, null);
				holder = new ViewHolder();
				holder.tv_place = (TextView) convertView.findViewById(R.id.tv_search_textView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_place.setText(placeList.get(position));
			return convertView;
		}

		class ViewHolder {
			TextView tv_place;
		}
	}

	class SearchHistoryAdapter extends BaseAdapter {

		List<String> historyList;
		List<ContentValues> collectList;
		private Context context;
		private float downX = 0; // 点下时候获取的x坐标
		private float upX = 1000; // 手指离开时候的x坐标
		private Button button; // 用于执行删除的button
		private Animation animation; // 删除时候的动画
		private Animation itemAnimation; // 滑动收藏时的动画
		private Animation itemBackAnimation;
		private View view;

		// int count = historyList.size() + collectList.size();

		public SearchHistoryAdapter(List<String> historyList,
				List<ContentValues> collectList, Context context) {
			this.historyList = historyList;
			this.collectList = collectList;
			animation = AnimationUtils.loadAnimation(context,R.anim.oyx_push_out);
			itemAnimation = AnimationUtils.loadAnimation(context,R.anim.oyx_item_animation);
			itemBackAnimation = AnimationUtils.loadAnimation(context,R.anim.oyx_item_back_animation);
			totalLiset.clear();
			for (int i = 0; i < collectList.size(); i++) {
				totalLiset.add(collectList.get(i).getAsString(
						DBManager.FIELD_PLACE_NAME));
			}
			for (int i = 0; i < historyList.size(); i++) {
				totalLiset.add(historyList.get(i));
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return totalLiset.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return totalLiset.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public int getItemViewType(int position) {
			int type = -1;
			long collectListEnd = collectList.size();
			if (position < collectListEnd) {
				type = TYPE_COLLECT;
			} else {
				type = TYPE_HISTORY;
			}
			return type;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			CollectViewHolder collectHolder = null;
			HistoryViewHolder historyHolder = null;

			LayoutInflater inflater = null;

			int type = getItemViewType(position);

			switch (type) {
			case TYPE_COLLECT:
				inflater = LayoutInflater.from(cyx_NaviSearchActivity_gaode.this);
				convertView = inflater
						.inflate(R.layout.cyx_collect_list_item, null);
				convertView.setOnTouchListener(new OnTouchListener() {

					@SuppressLint("ClickableViewAccessibility")
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						Log.d(TAG, "触发onTouch事件 ");
						// TODO Auto-generated method stub
						final CollectViewHolder holder = (CollectViewHolder) v
								.getTag();

						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							downX = event.getX();
							if (button != null) {
								button.setVisibility(View.GONE); // 影藏显示出来的button
							}

							break;
						case MotionEvent.ACTION_MOVE:
							upX = event.getX();
							Log.e(TAG, "event.getX()" + event.getX());
							break;
						case MotionEvent.ACTION_UP:
							if (downX - upX < 200) {
								jumpToPoint(position);
							}
							break;
						}

						Log.d(TAG, "downX = " + String.valueOf(downX));
						Log.d(TAG, "upX = " + String.valueOf(upX));
						if (holder.delectBt != null) {
							Log.d(TAG, "holder.delectBt != null");
							if (downX - upX > 200) {// 2次坐标的绝对值如果大于200，就认为是左右滑动
								Log.e("lxh", "downX - upX" + (downX - upX)
										+ "  " + downX + "  " + upX);
								button = holder.delectBt;
								if (button != null) {

									// 显示删除delectBt
									ItemBackAnimation(v, position);
									// 得到itemview，在上面加动画
									view = v;
								}
								Log.d(TAG,
										"Math.abs(downX - upX) = "
												+ String.valueOf(Math.abs(downX
														- upX)));
								// 终止事件
								return true;
							}
							// 释放事件，使onitemClick可以执行
							return true;
						}
						return true;
					}
				});

				collectHolder = new CollectViewHolder();
				collectHolder.tv_place = (TextView) convertView
						.findViewById(R.id.tv_collect_textView);
				collectHolder.imageView = (ImageView) convertView
						.findViewById(R.id.collect_img_left);
				collectHolder.delectBt = (Button) convertView
						.findViewById(R.id.delect_collect);

				convertView.setTag(collectHolder);
				// 为delectBt绑定事件
				collectHolder.delectBt
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								if (button != null) {
									button.setVisibility(View.GONE); // 点击删除按钮后，影藏按钮
									deleteItem(view, position); // 删除数据，加动画
								}

							}
						});

				String placeName = totalLiset.get(position);
				collectHolder.tv_place.setText(placeName);
				collectHolder.imageView.setImageResource(R.drawable.cyx_ic_collect);

				break;
			case TYPE_HISTORY:

				inflater = LayoutInflater.from(cyx_NaviSearchActivity_gaode.this);
				convertView = inflater.inflate(R.layout.cyx_search_list_item, null);
				historyHolder = new HistoryViewHolder();
				historyHolder.tv_place = (TextView) convertView
						.findViewById(R.id.tv_search_textView);
				historyHolder.imageView = (ImageView) convertView
						.findViewById(R.id.search_img_left);
				convertView.setTag(historyHolder);
				historyHolder.tv_place.setText(totalLiset.get(position));
				historyHolder.imageView.setImageResource(R.drawable.search_history_associate_icon);
				break;
			}

			return convertView;
		}

		class HistoryViewHolder {
			TextView tv_place;
			ImageView imageView;
		}

		class CollectViewHolder {
			TextView tv_place;
			ImageView imageView;
			Button delectBt;
		}

		public void deleteItem(View view, final int position) {
			view.startAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// 动画执行完毕
					// 删除数据库的数据
					dBManager.deleteUserCollect(collectList.get(position)
							.getAsInteger(DBManager.FIELD_ID));
					// 删除列表的数据
					collectList.remove(position);
					totalLiset.remove(position);
					Log.d(TAG, "已删除");
					notifyDataSetChanged();

					// 如果数据删除完，隐藏列表
					if (collectList.size() <= 0 && historyList.size() <= 0) {

						history_layout.setVisibility(View.GONE);
					}
				}
			});

		}

		public void ItemAnimation(final View view, final int position) {

			view.startAnimation(itemAnimation);
			itemAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// 动画执行完毕
					// ItemBackAnimation(view,position);

				}
			});

		}

		public void ItemBackAnimation(View view, final int position) {

			view.startAnimation(itemBackAnimation);
			itemBackAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// 动画执行完毕
					button.setVisibility(View.VISIBLE);

				}
			});

		}
	}
	class CollectHistoryAdapter extends BaseAdapter {
		List<ContentValues> collectList;
		private Context context;
		private float downX = 0; // 点下时候获取的x坐标
		private float upX = 1000; // 手指离开时候的x坐标
		private Button button; // 用于执行删除的button
		private Animation animation; // 删除时候的动画
		private Animation itemAnimation; // 滑动收藏时的动画
		private View view;

		public CollectHistoryAdapter(List<ContentValues> collectList,
				Context context) {
			this.collectList = collectList;
			// 用xml获取一个动画
			animation = AnimationUtils.loadAnimation(context,
					R.anim.oyx_push_out);// R.anim.push_out);
			itemAnimation = AnimationUtils.loadAnimation(context,
					R.anim.oyx_item_animation);// R.anim.item_animation);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return collectList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return collectList.get(position);
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
			ViewHolder holder = null;
			LayoutInflater inflater;
			if (convertView == null) {
				inflater = LayoutInflater.from(cyx_NaviSearchActivity_gaode.this);
				convertView = inflater
						.inflate(R.layout.cyx_collect_list_item, null);
				convertView.setOnTouchListener(new OnTouchListener() {

					@SuppressLint("ClickableViewAccessibility")
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						Log.d(TAG, "触发onTouch事件 ");
						// TODO Auto-generated method stub
						final ViewHolder holder = (ViewHolder) v.getTag();

						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							downX = event.getX();
							if (button != null) {
								button.setVisibility(View.GONE); // 影藏显示出来的button
							}

							break;
						case MotionEvent.ACTION_MOVE:
							upX = event.getX();
							break;
						case MotionEvent.ACTION_UP:
							break;
						}

						Log.d(TAG, "downX = " + String.valueOf(downX));
						Log.d(TAG, "upX = " + String.valueOf(upX));
						if (holder.delectBt != null) {

							if (downX - upX > 200) {// 2次坐标的绝对值如果大于100，就认为是左右滑动
								button = holder.delectBt;
								if (button != null) {

									// 显示删除delectBt
									ItemAnimation(v, position);
									view = v;

								}
								// 终止事件
								return true;
							}
							upX = 1000;
							downX = 0;
							// 释放事件，使onitemClick可以执行
							return false;
						}
						return false;
					}
				});

				holder = new ViewHolder();
				holder.tv_place = (TextView) convertView.findViewById(R.id.tv_collect_textView);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.collect_img_left);
				holder.delectBt = (Button) convertView.findViewById(R.id.delect_collect);

				convertView.setTag(holder);
				Log.d("hold", "converView != null");
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 为delectBt绑定事件
			holder.delectBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (button != null) {
						button.setVisibility(View.GONE); // 点击删除按钮后，影藏按钮
						deleteItem(view, position); // 删除数据，加动画
					}

				}
			});

			String placeName = collectList.get(position).getAsString(
					DBManager.FIELD_PLACE_NAME);
			holder.tv_place.setText(placeName);
			holder.imageView.setImageResource(R.drawable.cyx_ic_uncollect);

			return convertView;
		}

		public void deleteItem(View view, final int position) {

			view.startAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// 动画执行完毕
					// 删除数据库的数据
					dBManager.deleteUserCollect(collectList.get(position)
							.getAsInteger(DBManager.FIELD_ID));
					// 删除列表的数据
					collectList.remove(position);
					Log.d(TAG, "已删除");
					notifyDataSetChanged();

					// 如果数据删除完，隐藏列表
					if (collectList.size() <= 0) {

						// collect_layout.setVisibility(View.GONE);
					}
				}
			});

		}

		public void ItemAnimation(View view, final int position) {

			view.startAnimation(itemAnimation);
			itemAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// 动画执行完毕
					button.setVisibility(View.VISIBLE);

				}
			});

		}

		class ViewHolder {
			TextView tv_place;
			ImageView imageView;
			Button delectBt;
		}

	}

	private void showDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.cancel();
			mProgressDialog = null;
		}
		mProgressDialog = new ProgressDialog(cyx_NaviSearchActivity_gaode.this);
		mProgressDialog.setTitle(getString(R.string.notice));
		mProgressDialog.setMessage(getString(R.string.search_ing));
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.show();
	}

	/**
	 * 跳到用户收藏地点
	 * @param
	 * 		position 收藏列表下标
	 */
	private void jumpToPoint(int position) {
		ContentValues values = collectList.get(position);
		String userID = values.getAsString(DBManager.FIELD_USER_ID);
		double lon = values.getAsDouble(DBManager.FIELD_ADDRESS_LONGITUDE);
		double lat = values.getAsDouble(DBManager.FIELD_ADDRESS_LATITUDE);
		String placeName = values.getAsString(DBManager.FIELD_PLACE_NAME);
		String address = values.getAsString(DBManager.FIELD_ADDRESS);
		Intent intent = new Intent();
		intent.setClass(cyx_NaviSearchActivity_gaode.this,
				cyx_UserCollectMapActivity_gaode.class);
		intent.putExtra("user_id", userID);
		intent.putExtra("place_name", placeName);
		intent.putExtra("address", address);
		intent.putExtra("mLatitude", lat);
		intent.putExtra("mLongitude", lon);
		startActivity(intent);
		cyx_NaviSearchActivity_gaode.this.finish();
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		//dissmissProgressDialog();// 隐藏对话框
		mProgressDialog.dismiss();
		Log.w("lxh","lalalallalal");
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.cancel();
		}
		
		if (rCode != 0 || result == null||result.getPois()==null||result.getPois().size()==0) {
			Toast.makeText(
					cyx_NaviSearchActivity_gaode.this,
					getString(R.string.no_search_result),
					Toast.LENGTH_LONG).show();
			search_bar.setVisibility(View.GONE);
			if (historyList != null && historyList.size() > 0) {
				historyList.remove(search_name);
				historyList.add(0, search_name);
				if (historyAdapter != null)
					historyAdapter.notifyDataSetChanged();
			}
			return;
		}
		if (mTimeOutRunnable != null)
			mHandler.removeCallbacks(mTimeOutRunnable);
		oyx_MyApplication.setMyPoiResult_gaode(result);
		Intent intent = new Intent();
		intent.setClass(cyx_NaviSearchActivity_gaode.this,
				cyx_SearchResultMapActivity_gaode.class);
		intent.putExtra("search_name", search_name);
		startActivity(intent);
		cyx_NaviSearchActivity_gaode.this.finish();
	
	}
		/**
		 * 开始进行poi搜索
		 */
		protected void doSearchQuery(int pageSize ,String keyWord) {
			//showProgressDialog();// 显示进度框
			
			currentPage = 0;
			query = new PoiSearch.Query(keyWord, "", city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
			query.setPageSize(pageSize);// 设置每页最多返回多少条poiitem
			query.setPageNum(currentPage);// 设置查第一页

			poiSearch = new PoiSearch(this, query);
			poiSearch.setOnPoiSearchListener(this);
			poiSearch.searchPOIAsyn();
		}
		
		protected void onResume() {
			super.onResume();
			dBManager.open();
			
		}
		
		
		protected void onPause(){
			super.onPause();
			dBManager.close();

		}

}
