package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class DealAcceptanceTask extends AsyncTask<String, Void, String> {
	private ThriftHelper client;
	private String dealAcquireId;

	public DealAcceptanceTask(final ThriftHelper client, final String dealAcquireId)
	{
		this.client = client;
		this.dealAcquireId = dealAcquireId;
	}

	@Override
	protected void onPostExecute(final String result)
	{
		// void
	}

	@Override
	protected String doInBackground(final String... params)
	{
		String code = null;

		try
		{
			Location loc = TaloolUser.get().getLocation();
			if(loc == null)
			{
				code = client.client.redeem(dealAcquireId,null);
			}
			else
			{
				Location_t location = new Location_t(loc.getLongitude(), loc.getLatitude());
				code = client.client.redeem(dealAcquireId,location);
			}
		}
		catch (ServiceException_t e)
		{
			Log.e(this.getClass().getSimpleName(), "Service API problem accepting gift", e);
			
		}
		catch (TException e)
		{
			Log.e(this.getClass().getSimpleName(), "Thrift protocol problem accepting gift", e);
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), "Problem accepting gift", e);
		}

		return code;
	}
}
