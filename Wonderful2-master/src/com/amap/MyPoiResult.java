package com.amap;

import java.io.Serializable;

public class MyPoiResult  implements Serializable{

	private static final long serialVersionUID = 1L;
    private String poiName;
    private String poiAddress;
    private double end_latitude;
    private double end_longitude;
	public String getPoiName() {
		return poiName;
	}
	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}
	public String getPoiAddress() {
		return poiAddress;
	}
	public void setPoiAddress(String poiAddress) {
		this.poiAddress = poiAddress;
	}
	public double getEnd_latitude() {
		return end_latitude;
	}
	public void setEnd_latitude(double end_latitude) {
		this.end_latitude = end_latitude;
	}
	public double getEnd_longitude() {
		return end_longitude;
	}
	public void setEnd_longitude(double end_longitude) {
		this.end_longitude = end_longitude;
	}
    
	
}
