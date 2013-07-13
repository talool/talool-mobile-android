package com.talool.mobile.android.util;


import android.location.Location;

import com.talool.api.thrift.CTokenAccess_t;

public class TaloolUser {
	private static TaloolUser instance = null;
	private CTokenAccess_t accessToken;
	private Location location;

	protected TaloolUser() {
		location = new Location("Talool");
		location.setLatitude(39.766169);
		location.setLongitude(-104.979033);
	}
	
	public synchronized static TaloolUser getInstance() {
		if(instance == null) {
			instance = new TaloolUser();
		}
		return instance;
	}

	public synchronized CTokenAccess_t getAccessToken() {
		return accessToken;
	}
	public synchronized void  setAccessToken(CTokenAccess_t accessToken) {
		this.accessToken = accessToken;
	}
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}


}
