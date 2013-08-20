package com.talool.mobile.android.util;

import android.location.Location;

import com.facebook.Session;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.mobile.android.TaloolApplication;
import com.talool.mobile.android.cache.FavoriteMerchantCache;

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
	public final Location BOULDER_LOCATION;
	public final Location VANCOUVER_LOCATION;
	private static final TaloolUser instance = new TaloolUser();

	private CTokenAccess_t accessToken;
	private Location location;

	private TaloolUser()
	{
		BOULDER_LOCATION = new Location("Boulder");
		BOULDER_LOCATION.setLatitude(40.0176);
		BOULDER_LOCATION.setLongitude(-105.2797);

		VANCOUVER_LOCATION = new Location("Vancouver");
		VANCOUVER_LOCATION.setLatitude(45.6389);
		VANCOUVER_LOCATION.setLongitude(-122.6028);
	}

	public static TaloolUser get()
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
		TaloolApplication.getAppContext().deleteDatabase(Constants.DATABASE_NAME);
		FavoriteMerchantCache.get().clear();

		if (Session.getActiveSession().isOpened())
		{
			Session.getActiveSession().closeAndClearTokenInformation();
		}

	}

}
