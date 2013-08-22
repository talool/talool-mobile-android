package com.talool.mobile.android.tasks;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUtil;
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
	private Context context;

	public ActivityActionTakenTask(final ThriftHelper client, final String activityId, Context context)
	{
		this.client = client;
		this.activityId = activityId;
		this.context = context;
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

		return null;
	}
}
