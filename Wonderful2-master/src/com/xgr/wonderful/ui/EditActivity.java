package com.xgr.wonderful.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.location.core.GeoPoint;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.xgr.wonderful.R;
import com.xgr.wonderful.entity.QiangYu;
import com.xgr.wonderful.entity.User;
import com.xgr.wonderful.ui.base.BasePageActivity;
import com.xgr.wonderful.utils.ActivityUtil;
import com.xgr.wonderful.utils.CacheUtils;
import com.xgr.wonderful.utils.Constant;
import com.xgr.wonderful.utils.LogUtils;

/**
 * @author kingofglory email: 253475185@qq.com blog: http:www.google.com
 * @date 2014-3-19 TODO
 */

public class EditActivity extends BasePageActivity implements OnClickListener,
		AMapLocationListener {

	private static final int REQUEST_CODE_ALBUM = 1;
	private static final int REQUEST_CODE_CAMERA = 2;
	ActionBar actionbar;
	EditText content;

	LinearLayout openLayout;
	LinearLayout takeLayout;

	ImageView albumPic;
	ImageView takePic;
	// Button commitButton;
	String dateTime;
	private BmobGeoPoint mGeoPoint;
	private LocationManagerProxy mAMapLocationManager;
	private TableLayout tableLayout;
	private TableRow row_one, row_two, row_three;
	private DisplayMetrics displayMetrics;
	private int mImageWidth;
	private int imageNumber = 0;// 添加的图片数量
	private List<RelativeLayout> imageList = new ArrayList<RelativeLayout>();// 图片列表
	private final int ADD_IMAGE = 1;
	private final int DELETE_IMAGE = 2;

	@Override
	protected void setLayoutView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_edit);
		initLocation();
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {

		long time2 = System.currentTimeMillis();
		Log.i("lxh", "starttime=" + time2);
		mAMapLocationManager = LocationManagerProxy.getInstance(this);

		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次
		mAMapLocationManager.requestLocationData(
				LocationProviderProxy.AMapNetwork, -1, 15, this);

		mAMapLocationManager.setGpsEnable(false);

	}

	@Override
	protected void findViews() {
		// TODO Auto-generated method stub
		calculateImageWidth();
		content = (EditText) findViewById(R.id.edit_content);
		actionbar = (ActionBar) findViewById(R.id.actionbar_edit);
		row_one = (TableRow) findViewById(R.id.row_one);
		row_two = (TableRow) findViewById(R.id.row_two);
		row_three = (TableRow) findViewById(R.id.row_three);
		openLayout = (LinearLayout) findViewById(R.id.open_layout);
		takeLayout = (LinearLayout) findViewById(R.id.take_layout);
		tableLayout = (TableLayout) findViewById(R.id.image_table);
		albumPic = (ImageView) findViewById(R.id.open_pic);
		takePic = (ImageView) findViewById(R.id.take_pic);
		// commitButton = (Button)findViewById(R.id.commit_edit);
	}

	private void calculateImageWidth() {
		displayMetrics = new DisplayMetrics();
		// 取得窗口属性
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		float scale = (displayMetrics.xdpi - 50) / displayMetrics.xdpi;
		mImageWidth = (int) (displayMetrics.widthPixels * scale / 3 - 20);
	}

	@Override
	protected void setupViews(Bundle bundle) {
		// TODO Auto-generated method stub
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		actionbar.setTitle(getString(R.string.oyx_edit_publication));
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAction(new Action() {

			@Override
			public void performAction(View view) {
				// TODO Auto-generated method stub
				finish();
			}

			@Override
			public int getDrawable() {
				// TODO Auto-generated method stub
				return R.drawable.logo;
			}
		});

		actionbar.addAction(new Action() {

			@Override
			public void performAction(View view) {
				// TODO Auto-generated method stub
				String commitContent = content.getText().toString().trim();
				if (TextUtils.isEmpty(commitContent)) {
					ActivityUtil.show(mContext,
							getString(R.string.oyx_edit_cant_be_null));
					return;
				}
				if (targeturl == null) {
					publishWithoutFigure(commitContent, null);
				} else {
					publish(commitContent);
				}
			}

			@Override
			public int getDrawable() {
				// TODO Auto-generated method stub
				return R.drawable.btn_comment_publish;
			}
		});

	}

	@Override
	protected void setListener() {
		// TODO Auto-generated method stub
		openLayout.setOnClickListener(this);
		takeLayout.setOnClickListener(this);

		albumPic.setOnClickListener(this);
		takePic.setOnClickListener(this);
		// commitButton.setOnClickListener(this);
	}

	@Override
	protected void fetchData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// case R.id.commit_edit:
		// String commitContent = content.getText().toString().trim();
		// if(TextUtils.isEmpty(commitContent)){
		// ActivityUtil.show(this, "内容不能为空");
		// return;
		// }
		// if(targeturl == null){
		// publishWithoutFigure(commitContent, null);
		// }else{
		// publish(commitContent);
		// }
		// break;
		case R.id.open_layout:
			Date date1 = new Date(System.currentTimeMillis());
			dateTime = date1.getTime() + "";
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
					"image/*");
			startActivityForResult(intent, REQUEST_CODE_ALBUM);
			break;
		case R.id.take_layout:
			Date date = new Date(System.currentTimeMillis());
			dateTime = date.getTime() + "";
			File f = new File(CacheUtils.getCacheDirectory(mContext, true,
					"pic") + dateTime);
			if (f.exists()) {
				f.delete();
			}
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Uri uri = Uri.fromFile(f);
			Log.e("uri", uri + "");

			Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(camera, REQUEST_CODE_CAMERA);
			break;
		default:
			break;
		}
	}

	/*
	 * 发表带图片
	 */
	private void publish(final String commitContent) {

		final BmobFile figureFile = new BmobFile(QiangYu.class, new File(
				targeturl));
		figureFile.upload(mContext, new UploadFileListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				LogUtils.i(TAG,
						getString(R.string.oyx_edit_commit_document_success)
								+ figureFile.getFileUrl());
				publishWithoutFigure(commitContent, figureFile);

			}

			@Override
			public void onProgress(Integer arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				LogUtils.i(TAG,
						getString(R.string.oyx_edit_commit_document_fail)
								+ arg1);
			}
		});

	}

	private void publishWithoutFigure(final String commitContent,
			final BmobFile figureFile) {
		User user = BmobUser.getCurrentUser(mContext, User.class);

		final QiangYu qiangYu = new QiangYu();
		qiangYu.setAuthor(user);
		qiangYu.setContent(commitContent);
		if (figureFile != null) {
			qiangYu.setContentfigureurl(figureFile);
		}
		qiangYu.setGeopoint(mGeoPoint);
		qiangYu.setLove(0);
		qiangYu.setHate(0);
		qiangYu.setShare(0);
		qiangYu.setComment(0);
		qiangYu.setPass(true);
		qiangYu.save(mContext, new SaveListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ActivityUtil.show(EditActivity.this,
						getString(R.string.oyx_edit_commit_success));
				LogUtils.i(TAG, getString(R.string.oyx_edit_commit_success));
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ActivityUtil.show(EditActivity.this,
						getString(R.string.oyx_edit_commit_fail) + arg1);
				LogUtils.i(TAG, getString(R.string.oyx_edit_commit_fail) + arg1);
			}
		});
	}

	String targeturl = null;

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		LogUtils.i(TAG, "get album:");
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ALBUM:
				String fileName = null;
				if (data != null) {
					Uri originalUri = data.getData();
					ContentResolver cr = getContentResolver();
					Cursor cursor = cr.query(originalUri, null, null, null,
							null);
					if (cursor.moveToFirst()) {
						do {
							fileName = cursor.getString(cursor
									.getColumnIndex("_data"));
							LogUtils.i(TAG, "get album:" + fileName);
						} while (cursor.moveToNext());
					}
					Bitmap bitmap = compressImageFromFile(fileName);
					targeturl = saveToSdCard(bitmap);
					albumPic.setBackgroundDrawable(new BitmapDrawable(bitmap));
					takeLayout.setVisibility(View.GONE);
				}
				break;
			case REQUEST_CODE_CAMERA:
				String files = CacheUtils.getCacheDirectory(mContext, true,
						"pic") + dateTime;
				File file = new File(files);
				if (file.exists()) {
					Bitmap bitmap = compressImageFromFile(files);
					targeturl = saveToSdCard(bitmap);
					addRow(bitmap);
					// takePic.setBackgroundDrawable(new
					// BitmapDrawable(bitmap));
					openLayout.setVisibility(View.GONE);
				} else {

				}
				break;
			default:
				break;
			}
		}
	}

	private void addRow(Bitmap bitmap) {
		imageNumber++;
		ImageView deleteButton;// 删除按钮
		deleteButton = new ImageView(this);
		deleteButton.setBackgroundResource(R.drawable.compose_pic_delete);
		RelativeLayout.LayoutParams layoutpare = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		layoutpare.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutpare.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		ImageView CameraImage = new ImageView(this);
		CameraImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
		CameraImage.setPadding(10, 0, 10, 0);
		CameraImage.setLayoutParams(new LayoutParams(mImageWidth, mImageWidth));
		final RelativeLayout imageLayout = new RelativeLayout(this);
		imageLayout.setPadding(10, 0, 10, 0);
		imageLayout.addView(CameraImage);
		imageLayout.addView(deleteButton);
		deleteButton.setLayoutParams(layoutpare);
		imageList.add(imageLayout);
		orderImage(ADD_IMAGE, imageList);// 重新排序

		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageList.remove(imageLayout);
				orderImage(DELETE_IMAGE, imageList);
			}
		});
	}

	private void orderImage(int type, List<RelativeLayout> list) {
		row_one.removeAllViews();
		row_two.removeAllViews();
		row_three.removeAllViews();
		if (type == ADD_IMAGE) {
			/* 如果列表的大小大于1，就说明添加按钮已经存在了，所以要先删除，删除按钮在倒数第二个 */
			if (list.size() > 2)
				list.remove(list.size() - 2);
			if (list.size() > 0) {
				ImageView CameraImage = new ImageView(this);
				CameraImage.setBackgroundResource(R.drawable.composer_pic_add);
				CameraImage.setPadding(10, 0, 10, 0);
				CameraImage.setLayoutParams(new LayoutParams(mImageWidth,
						mImageWidth));
				final RelativeLayout addButton = new RelativeLayout(this);
				addButton.setPadding(10, 0, 10, 0);
				addButton.addView(CameraImage);
				addButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Date date = new Date(System.currentTimeMillis());
						dateTime = date.getTime() + "";
						File f = new File(CacheUtils.getCacheDirectory(
								mContext, true, "pic") + dateTime);
						if (f.exists()) {
							f.delete();
						}
						try {
							f.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						Uri uri = Uri.fromFile(f);
						Log.e("uri", uri + "");

						Intent camera = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
						startActivityForResult(camera, REQUEST_CODE_CAMERA);
					}
				});
				list.add(addButton);
			}
		} else {
			if (list.size() == 1) {
				list.clear();
			}
		}

		for (int i = 0; i < list.size(); i++) {
			if (i > 5 && i < 9) {
				row_three.addView(list.get(i));
			} else if (i > 2) {
				row_two.addView(list.get(i));

			} else {
				row_one.addView(list.get(i));

			}
		}

	}

	private Bitmap compressImageFromFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 800f;//
		float ww = 480f;//
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置采样率

		newOpts.inPreferredConfig = Config.ARGB_8888;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		// return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
		// 其实是无效的,大家尽管尝试
		return bitmap;
	}

	public String saveToSdCard(Bitmap bitmap) {
		String files = CacheUtils.getCacheDirectory(mContext, true, "pic")
				+ dateTime + "_11";
		File file = new File(files);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogUtils.i(TAG, file.getAbsolutePath());
		return file.getAbsolutePath();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		Log.w("oyx", "location = " + location.getLatitude() + "      "
				+ location.getLongitude());
		location.setLatitude(location.getLatitude() + Math.random() * 0.01);
		location.setLongitude(location.getLongitude() + Math.random() * 0.01);
		mGeoPoint = new BmobGeoPoint(location.getLongitude(),
				location.getLatitude());
		// 停止定位
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

}
