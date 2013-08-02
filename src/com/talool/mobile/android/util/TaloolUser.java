package com.talool.mobile.android.util;

import android.location.Location;

import com.talool.api.thrift.CTokenAccess_t;

/**
 * 
 * @author czachman,clintz
 * 
 *         TODO - put real location in soon
 *         http://stackoverflow.com/questions/2227292
 *         /how-to-get-latitude-and-longitude-of-the-mobiledevice-in-android
 */
public final class TaloolUser
{
	private static final boolean IS_DEVELOPMENT = true;
	private static final Location DENVER_LOCATION;
	private static final TaloolUser instance = new TaloolUser();

	private CTokenAccess_t accessToken;
	private Location location;

	static
	{
		DENVER_LOCATION = new Location("Talool");
		DENVER_LOCATION.setLatitude(39.766169);
		DENVER_LOCATION.setLongitude(-104.979033);
	}

	private TaloolUser()
	{
		if (IS_DEVELOPMENT)
		{
			location = DENVER_LOCATION;
		}
	}

	public static TaloolUser getInstance()
	{
		return instance;
	}

	public CTokenAccess_t getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(final CTokenAccess_t accessToken)
	{
		this.accessToken = accessToken;
	}

	public Location getLocation()
	{
		return location;
	}

	public void setLocation(final Location location)
	{
		this.location = location;
	}
	
	public void logoutUser()
	{
		this.accessToken = null;
	}

}
