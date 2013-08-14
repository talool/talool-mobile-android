package com.talool.mobile.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author clintz
 * 
 */
public class ActivityDbHelper extends SQLiteOpenHelper
{
	public static final String ACTIVITY_TBL = "activity";

	private static final String DATABASE_NAME = "talool.db";
	private static final int DATABASE_VERSION = 1;

	public enum ActivityColumn
	{
		_id, activity_date, activity_type, activity_obj;

		private static String[] columnArray;

		static
		{
			columnArray = new String[ActivityColumn.values().length];
			int i = 0;
			for (final ActivityColumn ac : ActivityColumn.values())
			{
				columnArray[i++] = ac.name();
			}
		}

		public static String[] getColumnArray()
		{
			return columnArray;
		}
	};

	public ActivityDbHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		final StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE " + ACTIVITY_TBL + " (");
		query.append(ActivityColumn._id + " string primary key,");
		query.append(ActivityColumn.activity_date + " int not null,");
		query.append(ActivityColumn.activity_type + " int not null,");
		query.append(ActivityColumn.activity_obj + " blob not null);");

		query.append("CREATE INDEX activity_type_idx ON ").append(ACTIVITY_TBL).append("(").
				append(ActivityColumn.activity_type).append(");");

		database.execSQL(query.toString());

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(ActivityDbHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		onCreate(db);
	}

}