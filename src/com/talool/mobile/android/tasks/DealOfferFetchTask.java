package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import android.os.AsyncTask;

import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * 
 */
public class DealOfferFetchTask extends AsyncTask<String, Void, DealOffer_t>
{
	private ThriftHelper thriftHelper;
	private String dealOfferId;

	public DealOfferFetchTask(final ThriftHelper thriftHelper, final String dealOfferId)
	{
		this.thriftHelper = thriftHelper;
		this.dealOfferId = dealOfferId;
	}

	@Override
	protected void onPostExecute(final DealOffer_t result)
	{
		super.onPostExecute(result);
	}

	@Override
	protected DealOffer_t doInBackground(String... params)
	{
		DealOffer_t dealOffer = null;

		try
		{
			thriftHelper.setAccessToken(TaloolUser.getInstance().getAccessToken());
			dealOffer = thriftHelper.getClient().getDealOffer(dealOfferId);
		}
		catch (ServiceException_t e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dealOffer;
	}

}
