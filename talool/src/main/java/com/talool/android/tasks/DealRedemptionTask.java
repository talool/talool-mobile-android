package com.talool.android.tasks;

import android.content.Context;
import android.location.Location;

import com.talool.android.util.AlertMessage;
import com.talool.android.util.AndroidUtils;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.ServiceException_t;

import org.apache.thrift.TException;

/**
 * 
 * @author clintz,czachman
 * 
 */
public class DealRedemptionTask extends AbstractTaloolTask<String, Void, String>
{
	private ThriftHelper client;
	private String dealAcquireId;

	public DealRedemptionTask(final ThriftHelper client, final String dealAcquireId, final Context context)
	{
		super(context);
		this.client = client;
		this.dealAcquireId = dealAcquireId;
		this.context = context;
	}

	@Override
	protected void onCancelled(String result)
	{
		super.onCancelled(result);

		if (alertMessage != null)
		{
			AndroidUtils.popupMessageWithOk(alertMessage, context);
		}
	}

	@Override
	protected String doInBackground(final String... params)
	{
		if (!AndroidUtils.hasNetworkConnection())
		{
			alertMessage = new AlertMessage("You have no network connection", null);
			cancel(true);
			return null;
		}

		String code = null;

		try
		{
			Location loc = TaloolUser.get().getLocation();
			if (loc == null)
			{
				code = client.client.redeem(dealAcquireId, null);
			}
			else
			{
				Location_t location = new Location_t(loc.getLongitude(), loc.getLatitude());
				code = client.client.redeem(dealAcquireId, location);
			}
		}
		catch (ServiceException_t e)
		{
			cancel(true);
			alertMessage = new AlertMessage("Service Exception", "Service Exception");
			TaloolUtil.sendException(e, context);
		}
		catch (TException e)
		{
			cancel(true);
			alertMessage = new AlertMessage("Connection error", "Make sure you have a network connection");
			TaloolUtil.sendException(e, context);
		}
		catch (Exception e)
		{
			cancel(true);
			alertMessage = new AlertMessage("An error has occured", "Please try again", e);
			TaloolUtil.sendException(e, context);
		}

		return code;
	}
}
