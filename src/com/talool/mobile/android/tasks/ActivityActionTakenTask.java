package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import android.os.AsyncTask;
import android.util.Log;

import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * 
 */
public class ActivityActionTakenTask extends AsyncTask<Void, Void, Void>
{
	private ThriftHelper client;
	private String activityId;

	public ActivityActionTakenTask(final ThriftHelper client, final String activityId)
	{
		this.client = client;
		this.activityId = activityId;
	}

	@Override
	protected Void doInBackground(final Void... params)
	{
		try
		{
			client.client.activityAction(activityId);
		}
		catch (ServiceException_t e)
		{
			Log.e(this.getClass().getSimpleName(), "Service API problem activity action", e);
		}
		catch (TException e)
		{
			Log.e(this.getClass().getSimpleName(), "Thrift protocol problem activity action", e);
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), "Problem activity action", e);
		}

		return null;

	}
}
