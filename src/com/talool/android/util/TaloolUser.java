package com.talool.android.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.thrift.TException;

import android.content.Context;
import android.location.Location;

import com.facebook.Session;
import com.talool.android.TaloolApplication;
import com.talool.android.cache.FavoriteMerchantCache;
import com.talool.android.tasks.ActivitySupervisor;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.SocialNetwork_t;
import com.talool.thrift.util.ThriftUtil;

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
	private static final String TOKEN_FILE = "talool_token";

	private Location location;
	private boolean realLocation = false;

	private CTokenAccess_t accessToken;

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

	public boolean isFacebookLogin()
	{
		if (accessToken == null)
		{
			return false;
		}
		return accessToken.getCustomer().getSocialAccounts().get(SocialNetwork_t.Facebook) != null;
	}

	public CTokenAccess_t getAccessToken()
	{
		if (accessToken != null)
		{
			return accessToken;
		}

		try
		{
			final FileInputStream fis = TaloolApplication.getAppContext().openFileInput(TOKEN_FILE);
			if (fis == null)
			{
				return null;
			}

			accessToken = new CTokenAccess_t();
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final DataInputStream dis = new DataInputStream(bis);
			byte fileContent[] = toByteArray(dis);
			ThriftUtil.deserialize(fileContent, accessToken);

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (TException e)
		{
			e.printStackTrace();
		}

		return accessToken;
	}

	public static byte[] toByteArray(InputStream in) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		return out.toByteArray();
	}

	public static long copy(InputStream from, OutputStream to) throws IOException
	{
		byte[] buf = new byte[256];
		long total = 0;
		while (true)
		{
			int r = from.read(buf);
			if (r == -1)
			{
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}

	public void setAccessToken(final CTokenAccess_t accessToken)
	{
		try
		{
			// persist to memory
			this.accessToken = accessToken;
			// persist to disk
			final FileOutputStream fos = TaloolApplication.getAppContext().openFileOutput(TOKEN_FILE, Context.MODE_PRIVATE);
			fos.write(ThriftUtil.serialize(accessToken));
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// this.accessToken = accessToken;
	}

	public Location getLocation()
	{
		return location;
	}

	public void setLocation(final Location location, boolean realLocation)
	{
		this.location = location;
		this.realLocation = realLocation;
	}

	public void logoutUser(Context context)
	{
		// clear token
		TaloolApplication.getAppContext().deleteFile(TOKEN_FILE);
		accessToken = null;

		TaloolApplication.getAppContext().deleteDatabase(Constants.DATABASE_NAME);

		ActivitySupervisor.get().shutdown();

		FavoriteMerchantCache.get().clear();

		// pull the active session from cache if needed
		Session.openActiveSessionFromCache(context);
		if (Session.getActiveSession() != null)
		{
			Session.getActiveSession().closeAndClearTokenInformation();
		}

	}

	public boolean isRealLocation()
	{
		return this.realLocation;
	}

}
