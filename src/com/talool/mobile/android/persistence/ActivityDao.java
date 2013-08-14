package com.talool.mobile.android.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.mobile.android.persistence.ActivityDbHelper.ActivityColumn;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz
 * 
 */
public final class ActivityDao
{
	private static ActivityDao instance;
	private ActivityDbHelper activityDbHelper;

	public static synchronized ActivityDao createInstance(final Context context)
	{
		if (instance == null)
		{
			instance = new ActivityDao(context);
		}

		return instance;
	}

	public static ActivityDao get()
	{
		return instance;
	}

	private ActivityDao(final Context context)
	{
		activityDbHelper = new ActivityDbHelper(context);
	}

	public void close()
	{
		activityDbHelper.close();
	}

	public void saveActivity(final Activity_t activity)
	{
		final SQLiteDatabase database = this.activityDbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ActivityColumn._id.name(), activity.getActivityId());
		values.put(ActivityColumn.activity_date.name(), activity.getActivityDate());
		values.put(ActivityColumn.activity_type.name(), activity.getActivityEvent().ordinal());

		values.put(ActivityColumn.activity_obj.name(), ThriftUtil.serialize(activity));

		long insertId = database.replace(ActivityDbHelper.ACTIVITY_TBL, null,
				values);

	}

	public List<Activity_t> getActivities(final ActivityEvent_t activityEvent)
	{
		final SQLiteDatabase database = this.activityDbHelper.getReadableDatabase();
		final List<Activity_t> activities = new ArrayList<Activity_t>();

		final Cursor cursor = database.query(ActivityDbHelper.ACTIVITY_TBL,
				ActivityColumn.getColumnArray(), ActivityColumn.activity_type + "=" + activityEvent.ordinal(), null, null, null, ActivityColumn.activity_date
						+ " DESC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			Activity_t activity = cursorToActivity(cursor);
			activities.add(activity);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	public List<Activity_t> getAllActivities(final List<ActivityEvent_t> filterEvents)
	{
		Cursor cursor = null;
		final SQLiteDatabase database = this.activityDbHelper.getReadableDatabase();
		final List<Activity_t> activities = new ArrayList<Activity_t>();

		if (filterEvents == null || filterEvents.size() == 0)
		{
			cursor = database.query(ActivityDbHelper.ACTIVITY_TBL,
					ActivityColumn.getColumnArray(), null, null, null, null, ActivityColumn.activity_date + " DESC");
		}
		else
		{
			final StringBuilder sb = new StringBuilder();

			for (ActivityEvent_t evt : filterEvents)
			{
				if (sb.length() > 0)
				{
					sb.append(" OR ");
				}
				sb.append(ActivityColumn.activity_type).append("=").append(evt.ordinal());
			}

			cursor = database.query(ActivityDbHelper.ACTIVITY_TBL,
					ActivityColumn.getColumnArray(), sb.toString(), null, null, null, ActivityColumn.activity_date + " DESC");

		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			Activity_t activity = cursorToActivity(cursor);
			activities.add(activity);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	public void saveActivities(List<Activity_t> activities)
	{
		long before = System.currentTimeMillis();
		for (final Activity_t activity : activities)
		{
			saveActivity(activity);
		}

		long total = System.currentTimeMillis() - before;
		System.out.println(total);
	}

	private Activity_t cursorToActivity(final Cursor cursor)
	{
		final Activity_t activity = new Activity_t();

		final byte[] objBytes = cursor.getBlob(ActivityColumn.activity_obj.ordinal());

		try
		{
			ThriftUtil.deserialize(objBytes, activity);
		}
		catch (TException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return activity;
	}
}