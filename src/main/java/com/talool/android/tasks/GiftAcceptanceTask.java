package com.talool.android.tasks;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.TaloolApplication;
import com.talool.android.persistence.ActivityDao;
import com.talool.android.persistence.MerchantDao;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.ServiceException_t;

/**
 * 
 * @author clintz
 * 
 */
public class GiftAcceptanceTask extends AsyncTask<String, Void, DealAcquire_t>
{
	private ThriftHelper client;
	private Activity_t activity;
	private String giftId;
	private boolean accept = false;
	private Context context;

	public GiftAcceptanceTask(final ThriftHelper client, final Activity_t activity, boolean accept, final Context context)
	{
		this.client = client;
		this.accept = accept;
		this.activity = activity;
		this.context = context;
		this.giftId = activity.getActivityLink().getLinkElement();
	}
	
	public GiftAcceptanceTask(final ThriftHelper client, final String giftId, boolean accept, final Context context)
	{
		this.client = client;
		this.accept = accept;
		this.giftId = giftId;
		this.context = context;
	}

	public String getGiftId()
	{
		return giftId;
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

				// make sure we put the merchant acquire in the cache! This is right to
				// do in the first release because "My Deals pulls from the merchantDao
				// onResume
				final MerchantDao mDao = new MerchantDao(TaloolApplication.getAppContext());
				mDao.saveMerchant(dealAcquire.getDeal().getMerchant());
			}
			else
			{
				client.client.rejectGift(getGiftId());
			}

			// make sure we update our cache
			if (activity != null)
			{
				activity.setActionTaken(true);
				ActivityDao dao = new ActivityDao(TaloolApplication.getAppContext());
				dao.open();
				dao.saveActivity(activity);
				ActivitySupervisor.get().refreshFromPersistence();
			}
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