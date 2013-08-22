package com.talool.mobile.android.tasks;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.thrift.transport.TTransportException;

import android.content.Context;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
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

	private NotificationCallback notificationCallback;

	private ActivityDao activityDao;

	private Activity_t mostCurrentActivity;

	private SearchOptions_t searchOptions;

	private static final ActivityObservable activityObservable = new ActivityObservable();

	public static class ActivityUpdateSummary
	{
		private Activity_t currentActivity;
		private int actionsPending;

		public ActivityUpdateSummary(final Activity_t currentActivity, final int actionsPending)
		{
			this.currentActivity = currentActivity;
			this.actionsPending = actionsPending;
		}

		public Activity_t getCurrentActivity()
		{
			return currentActivity;
		}

		public int getActionsPending()
		{
			return actionsPending;
		}

	}

	public static class ActivityObservable extends Observable
	{
		private final ActivityUpdateSummary activityUpdateSummary = new ActivityUpdateSummary(null, 0);

		public void updateActivitySummary(final Activity_t activity, int actionsPending)
		{
			activityUpdateSummary.currentActivity = activity;
			activityUpdateSummary.actionsPending = actionsPending;

			setChanged();
			notifyObservers();
		}

		public ActivityUpdateSummary getActivityUpdateSummary()
		{
			return activityUpdateSummary;
		}

	}

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

					activityObservable.updateActivitySummary(mostCurrentActivity, actionsPending);
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

	public static ActivitySupervisor get()
	{
		return instance;
	}

	public void addActivityObserver(final Observer observer)
	{
		activityObservable.addObserver(observer);
	}

	public void removeActivityObserver(final Observer observer)
	{
		activityObservable.deleteObserver(observer);
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
		}
	}
	

}
