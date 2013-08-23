package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.TaloolApplication;
import com.talool.mobile.android.persistence.ActivityDao;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * 
 */
public class GiftAcceptanceTask extends AsyncTask<String, Void, DealAcquire_t>
{
	private ThriftHelper client;
	private Activity_t activity;
	private boolean accept = false;
	private Context context;

	public GiftAcceptanceTask(final ThriftHelper client, final Activity_t activity, boolean accept, final Context context)
	{
		this.client = client;
		this.accept = accept;
		this.activity = activity;
		this.context = context;
	}

	public String getGiftId()
	{
		return activity.getActivityLink().getLinkElement();
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
			if (accept)
			{
				dealAcquire = client.client.acceptGift(getGiftId());
			}
			else
			{
				client.client.rejectGift(getGiftId());
			}

			// make sure we update our cache
			activity.setActionTaken(true);
			ActivityDao dao = new ActivityDao(TaloolApplication.getAppContext());
			dao.open();
			dao.saveActivity(activity);

		}
		catch (ServiceException_t e)
		{
			TaloolUtil.sendException(e, context);
		}
		catch (TException e)
		{
			TaloolUtil.sendException(e, context);
		}
		catch (Exception e)
		{
			TaloolUtil.sendException(e, context);
		}

		return dealAcquire;
	}
};