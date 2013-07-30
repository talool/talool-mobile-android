package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import android.os.AsyncTask;
import android.util.Log;

import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * 
 */
public class GiftAcceptanceTask extends AsyncTask<String, Void, DealAcquire_t>
{
	private ThriftHelper client;
	private String giftId;
	private boolean accept = false;

	public GiftAcceptanceTask(final ThriftHelper client, final String giftId, boolean accept)
	{
		this.client = client;
		this.giftId = giftId;
		this.accept = accept;
	}

	@Override
	protected void onPostExecute(final DealAcquire_t result)
	{
		// void
	}

	@Override
	protected DealAcquire_t doInBackground(final String... params)
	{
		DealAcquire_t dealAcquire = null;

		try
		{
			// TODO add the returned dealAcquire to cache, and then set the Intent
			if (accept)
			{
				dealAcquire = client.client.acceptGift(giftId);

			}
			else
			{
				client.client.rejectGift(giftId);
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

		return dealAcquire;
	}
};