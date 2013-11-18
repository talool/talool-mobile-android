package com.talool.android.tasks;

import org.apache.thrift.TException;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.persistence.ActivityDao;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.ServiceException_t;

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
	private ActivityDao activityDao;
	private Activity_t activity;

	public ActivityActionTakenTask(final ThriftHelper client, final String activityId, final Context context,
			final ActivityDao activityDao, final Activity_t activity)
	{
		this.client = client;
		this.activityId = activityId;
		this.context = context;
		this.activityDao = activityDao;
		this.activity = activity;
	}

	@Override
	protected Void doInBackground(final Void... params)
	{
		try
		{
			client.client.activityAction(activityId);
			activity.setActionTaken(true);
			activityDao.saveActivity(activity);
			ActivitySupervisor.get().refreshFromPersistence();
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

		return null;
	}
}
