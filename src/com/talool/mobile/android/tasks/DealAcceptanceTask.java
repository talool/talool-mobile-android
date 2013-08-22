package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.ThriftHelper;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class DealAcceptanceTask extends AsyncTask<String, Void, String> {
	private ThriftHelper client;
	private String dealAcquireId;
	private Context context;
	public DealAcceptanceTask(final ThriftHelper client, final String dealAcquireId, final Context context)
	{
		this.client = client;
		this.dealAcquireId = dealAcquireId;
		this.context = context;
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
			TaloolUtil.sendException(e,context);			
		}
		catch (TException e)
		{
			TaloolUtil.sendException(e,context);
		}
		catch (Exception e)
		{
			TaloolUtil.sendException(e,context);
		}

		return code;
	}
}
