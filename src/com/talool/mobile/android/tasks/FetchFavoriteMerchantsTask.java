package com.talool.mobile.android.tasks;

import java.util.List;

import org.apache.thrift.TException;

import android.os.AsyncTask;
import android.util.Log;

import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.cache.FavoriteMerchantCache;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * 
 */
public class FetchFavoriteMerchantsTask extends AsyncTask<String, Void, List<Merchant_t>>
{
	public FetchFavoriteMerchantsTask()
	{}

	@Override
	protected void onPostExecute(final List<Merchant_t> merchants)
	{
		if (merchants == null || merchants.size() == 0)
		{
			return;
		}

		for (final Merchant_t merchant : merchants)
		{
			FavoriteMerchantCache.get().addMerchant(merchant);
		}
	}

	@Override
	protected List<Merchant_t> doInBackground(final String... params)
	{
		List<Merchant_t> merchants = null;

		try
		{
			ThriftHelper client = new ThriftHelper();

			merchants = client.getClient().getFavoriteMerchants(null);
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

		return merchants;
	}
};