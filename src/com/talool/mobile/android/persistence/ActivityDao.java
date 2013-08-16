package com.talool.mobile.android.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.mobile.android.persistence.TaloolDbHelper.ActivityColumn;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz
 * 
 */
public final class ActivityDao extends AbstractDbAdapter
{
	public ActivityDao(Context ctx)
	{
		super(ctx);
	}

	public void saveActivity(final Activity_t activity)
	{
		ContentValues values = new ContentValues();
		values.put(ActivityColumn._id.name(), activity.getActivityId());
		values.put(ActivityColumn.activity_date.name(), activity.getActivityDate());
		values.put(ActivityColumn.activity_type.name(), activity.getActivityEvent().ordinal());

		values.put(ActivityColumn.activity_obj.name(), ThriftUtil.serialize(activity));

		mDb.replace(TaloolDbHelper.ACTIVITY_TBL, null,
				values);

	}

	public List<Activity_t> getAllActivities(final List<ActivityEvent_t> filterEvents)
	{
		Cursor cursor = null;
		final List<Activity_t> activities = new ArrayList<Activity_t>();

		if (filterEvents == null || filterEvents.size() == 0)
		{
			cursor = mDb.query(TaloolDbHelper.ACTIVITY_TBL,
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

			cursor = mDb.query(TaloolDbHelper.ACTIVITY_TBL,
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

	public void saveActivities(final List<Activity_t> activities)
	{
		mDb.beginTransaction();

		try
		{
			final ContentValues values = new ContentValues();
			for (final Activity_t activity : activities)
			{
				values.put(ActivityColumn._id.name(), activity.getActivityId());
				values.put(ActivityColumn.activity_date.name(), activity.getActivityDate());
				values.put(ActivityColumn.activity_type.name(), activity.getActivityEvent().ordinal());

				values.put(ActivityColumn.activity_obj.name(), ThriftUtil.serialize(activity));

				mDb.replace(TaloolDbHelper.ACTIVITY_TBL, null, values);
				values.clear();
			}

			mDb.setTransactionSuccessful();
		}
		catch (Exception ex)
		{
			Log.e(this.getClass().getSimpleName(), "Problem saving activities", ex);
		}
		finally
		{
			mDb.endTransaction();
		}
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