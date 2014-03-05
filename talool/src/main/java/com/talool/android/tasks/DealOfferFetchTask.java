package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.ServiceException_t;

import org.apache.thrift.TException;

/**
 * 
 * @author clintz
 * 
 */
public class DealOfferFetchTask extends AsyncTask<String, Void, DealOffer_t>
{
	private ThriftHelper thriftHelper;
	private String dealOfferId;
	private Context context;

	public DealOfferFetchTask(final ThriftHelper thriftHelper, final String dealOfferId, final Context context)
	{
		this.thriftHelper = thriftHelper;
		this.dealOfferId = dealOfferId;
		this.context = context;
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
			thriftHelper.setAccessToken(TaloolUser.get().getAccessToken());
			dealOffer = thriftHelper.getClient().getDealOffer(dealOfferId);
		}
		catch (ServiceException_t e)
		{
			TaloolUtil.sendException(e,context);
		}
		catch (TException e)
		{
			TaloolUtil.sendException(e,context);
		}

		return dealOffer;
	}

}
