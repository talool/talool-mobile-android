package com.talool.android.tasks;

import java.util.List;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.cache.FavoriteMerchantCache;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;

/**
 * 
 * @author clintz
 * 
 */
public class FetchFavoriteMerchantsTask extends AsyncTask<String, Void, List<Merchant_t>>
{
	private Context context;
	public FetchFavoriteMerchantsTask(final Context context)
	{
		this.context = context;
	}

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

		return merchants;
	}
};