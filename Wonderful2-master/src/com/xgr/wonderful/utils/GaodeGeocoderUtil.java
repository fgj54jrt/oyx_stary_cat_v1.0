package com.xgr.wonderful.utils;

import android.content.Context;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class GaodeGeocoderUtil implements OnGeocodeSearchListener {

	public String addr;
	private IAddrCallBack mIAddrCallBack;
	private TextView mtv;
	private boolean needAllAddr = false;

	private GeocodeSearch geocoderSearch;
	private LatLonPoint latLonPoint;
	private double[] gaode;

	public GaodeGeocoderUtil(Context context, TextView tv, double lat,
			double lon, boolean needAllAddr, IAddrCallBack callback) {
		this(context, tv, lat, lon, callback);
		this.needAllAddr = needAllAddr;
	}

	public GaodeGeocoderUtil(Context context, TextView tv, double lat,
			double lon, IAddrCallBack callback) {

		geocoderSearch = new GeocodeSearch(context);
		geocoderSearch.setOnGeocodeSearchListener(this);

		mIAddrCallBack = callback;
		mtv = tv;
		gaode = Coordinate.wgtochina(lon, lat);
		latLonPoint = new LatLonPoint(gaode[1], gaode[0]);
		new Thread(new GetAddr()).start();
	}

	private class GetAddr implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (latLonPoint != null && geocoderSearch != null) {
				RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
						GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
				geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
			}
		}

	}

	public interface IAddrCallBack {
		public void addrLoad(TextView tv, String addr);
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null) {
				if (needAllAddr) {
					addr = result.getRegeocodeAddress().toString();
				} else {
					addr = result.getRegeocodeAddress().getFormatAddress();
				}
			}
			if (mIAddrCallBack != null) {
				mIAddrCallBack.addrLoad(mtv, addr);
			}
		}

	}
}
