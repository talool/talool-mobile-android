package com.talool.android.tasks;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.ServiceException_t;

public class FacebookGiftIdTask extends AsyncTask<String, String, String> {
	private ThriftHelper thriftHelper;
	private String dealOfferId;
	private Context context;
	private String receipientName;
	private String facebookId;

	public FacebookGiftIdTask(final ThriftHelper thriftHelper, final String dealOfferId, final String facebookId, final String receipientName, final Context context)
	{
		this.thriftHelper = thriftHelper;
		this.dealOfferId = dealOfferId;
		this.context = context;
		this.receipientName = receipientName;
		this.facebookId = facebookId;
	}

	@Override
	protected void onPostExecute(final String result)
	{
		super.onPostExecute(result);
	}

	@Override
	protected String doInBackground(String... params)
	{
		String giftId = "";

		try
		{
			thriftHelper.setAccessToken(TaloolUser.get().getAccessToken());
			giftId = thriftHelper.getClient().giftToFacebook(dealOfferId, facebookId, receipientName);
		}
		catch (ServiceException_t e)
		{
			TaloolUtil.sendException(e,context);
		}
		catch (TException e)
		{
			TaloolUtil.sendException(e,context);
		}

		return giftId;
	}
}
