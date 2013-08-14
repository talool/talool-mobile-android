package com.talool.mobile.android.cache;

import java.util.HashMap;
import java.util.Map;

import com.talool.api.thrift.Activity_t;

/**
 * Activity Cache
 * 
 * @author clintz
 * 
 */
public final class ActivityCache
{
	private Map<String, Activity_t> activities = new HashMap<String, Activity_t>();

	private static ActivityCache instance = new ActivityCache();

	public static ActivityCache get()
	{
		return instance;
	}

	public Activity_t getActivity(final String activityId)
	{
		return activities.get(activityId);
	}

	public void setActivity(final Activity_t activity)
	{
		activities.put(activity.getActivityId(), activity);
	}
}
