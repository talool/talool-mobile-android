package com.talool.mobile.android.tasks;

import java.util.List;

import org.apache.thrift.transport.TTransportException;

import android.content.Context;
import android.util.Log;

import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.mobile.android.persistence.ActivityDao;
import com.talool.mobile.android.util.ApiUtil;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * A polling thread that simply fetches the latest activity from the server and
 * saves via the ActivtiyDao.
 * 
 * It also publishes new activities notifications
 * 
 * 
 * 
 * @author clintz
 * 
 */
public final class ActivitySupervisor
{
	private static ActivitySupervisor instance;
	private static final long POLLER_SLEEP_TIME = 30000; // 30 seconds
	private ThriftHelper client = null;
	private ActivityPoller activityPoller = null;

	private volatile int actionsPending = 0;

	private NotificationCallback notificationCallback;

	private ActivityDao activityDao;

	private Activity_t mostCurrentActivity;

	private SearchOptions_t searchOptions;

	public static interface NotificationCallback
	{
		public void handleNotificationCount(final int totalNotifications);
	}

	private class ActivityPoller extends Thread
	{
		private boolean forceRefresh = false;
		private long lastActivityTime;

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					final List<Activity_t> acts = client.getClient().getActivities(searchOptions);
					handleActionsPending(acts);
				}
				catch (Exception e)
				{
					Log.e(this.getClass().getSimpleName(), "Problem polling for activities", e);
				}

				try
				{
					sleep(POLLER_SLEEP_TIME);
				}
				catch (InterruptedException ignoreException)
				{
					//
					if (forceRefresh)
					{
						handleActionsPending(activityDao.getAllActivities(null));
						forceRefresh = false;
					}
				}
			}

		}

		public void handleActionsPending(final List<Activity_t> acts)
		{
			if (acts != null)
			{
				mostCurrentActivity = acts.get(acts.size() - 1);

				if (mostCurrentActivity.getActivityDate() != lastActivityTime)
				{
					// only update dao of we know something has changed
					// blindy update everything , optimize in the next release
					lastActivityTime = mostCurrentActivity.getActivityDate();
					activityDao.saveActivities(acts);
				}

				if (notificationCallback != null)
				{
					int actionsPending = 0;
					for (final Activity_t act : acts)
					{
						if (ApiUtil.isClickableActivityLink(act) && act.actionTaken == false)
						{
							actionsPending++;
						}
					}

					notificationCallback.handleNotificationCount(actionsPending);

					instance.actionsPending = actionsPending;
				}
			}

		}
	}

	/**
	 * Creates an instance, or if one already exists, it simply re-assigns the
	 * notificationCallback and forces a refresh of the activityPoller (from local
	 * persistence).
	 * 
	 * It is written this way because we can't use a singleInstance activity and
	 * guarantee a singleton instance of the Notification callback, but lets
	 * guarantee a singleton still of our Activity supervisor
	 * 
	 * @param context
	 * @param notificationCallback
	 * @return
	 */

	public static synchronized ActivitySupervisor createInstance(final Context context, final NotificationCallback notificationCallback)
	{
		if (instance == null)
		{
			instance = new ActivitySupervisor(context, notificationCallback);

		}

		else
		{
			instance.notificationCallback = notificationCallback;
			instance.activityPoller.forceRefresh = true;
			instance.activityPoller.interrupt();
		}

		return instance;
	}

	public int getActionsPending()
	{
		return actionsPending;
	}

	public Activity_t getMostCurrentActivity()
	{
		return mostCurrentActivity;
	}

	public static ActivitySupervisor get()
	{
		return instance;
	}

	private ActivitySupervisor(final Context context, final NotificationCallback notificationCallback)
	{
		try
		{
			activityDao = new ActivityDao(context);
			activityDao.open();

			searchOptions = new SearchOptions_t();
			searchOptions.setSortProperty("activityDate");
			searchOptions.setAscending(true);

			client = new ThriftHelper();
			this.notificationCallback = notificationCallback;
			client.setAccessToken(TaloolUser.get().getAccessToken());
			activityPoller = new ActivityPoller();
			activityPoller.setDaemon(true);
			activityPoller.start();

		}
		catch (TTransportException e)
		{
			Log.e(this.getClass().getSimpleName(), "Problem creating thrift util", e);
		}
	}
}
