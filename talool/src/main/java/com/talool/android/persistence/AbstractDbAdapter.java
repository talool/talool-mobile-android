package com.talool.android.persistence;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.talool.android.persistence.TaloolDbHelper.ActivityColumn;
import com.talool.android.persistence.TaloolDbHelper.MerchantColumn;
import com.talool.android.util.Constants;

public abstract class AbstractDbAdapter
{
	protected DatabaseHelper mDbHelper;
	protected static SQLiteDatabase mDb;

	protected final Context mCtx;

	protected static class DatabaseHelper extends SQLiteOpenHelper
	{

		DatabaseHelper(Context context)
		{
			super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			final StringBuilder query = new StringBuilder();
			query.append("CREATE TABLE activity (");
			query.append(ActivityColumn._id + " string primary key,");
			query.append(ActivityColumn.activity_date + " int not null,");
			query.append(ActivityColumn.activity_type + " int not null,");
			query.append(ActivityColumn.activity_obj + " blob not null);");

			db.execSQL(query.toString());
			query.setLength(0);

			query.append("CREATE INDEX activity_type_idx ON activity (").
					append(ActivityColumn.activity_type).append(");");

			db.execSQL(query.toString());
			query.setLength(0);

			query.append("CREATE TABLE merchant (");
			query.append(MerchantColumn._id + " string primary key,");
			query.append(MerchantColumn.name + " string not null,");
			query.append(MerchantColumn.category + " int not null,");
			query.append(MerchantColumn.merchant_obj + " blob not null);");

			db.execSQL(query.toString());
			query.setLength(0);

			query.append("CREATE INDEX name_idx ON merchant (").
					append(MerchantColumn.name).append(");");

			db.execSQL(query.toString());
			query.setLength(0);

			query.append("CREATE INDEX category_idx ON merchant (").
					append(MerchantColumn.category).append(");");

			db.execSQL(query.toString());
			query.setLength(0);

            query.append("CREATE TABLE favorite (");
            query.append(MerchantColumn._id + " string primary key,");
            query.append(MerchantColumn.name + " string not null,");
            query.append(MerchantColumn.category + " int not null,");
            query.append(MerchantColumn.merchant_obj + " blob not null);");

            db.execSQL(query.toString());
            query.setLength(0);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w("TaloolDbAdapter", "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			dropTables();
			onCreate(db);
		}

		public void dropTables()
		{
			mDb.execSQL("DROP TABLE IF EXISTS merchant");
			mDb.execSQL("DROP TABLE IF EXISTS activity");
            mDb.execSQL("DROP TABLE IF EXISTS favorite");

        }
	}

	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx
	 *          the Context within which to work
	 */
	public AbstractDbAdapter(Context ctx)
	{
		this.mCtx = ctx;
	}

	/**
	 * Open or create the routes database.
	 * 
	 * @return this
	 * @throws SQLException
	 *           if the database could be neither opened or created
	 */
	public AbstractDbAdapter open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		mDbHelper.close();
	}

}
