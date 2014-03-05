package com.talool.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.talool.android.persistence.TaloolDbHelper.MerchantColumn;
import com.talool.api.thrift.Merchant_t;
import com.talool.thrift.util.ThriftUtil;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author clintz
 * 
 */
public final class MerchantDao extends AbstractDbAdapter
{
	public MerchantDao(Context ctx)
	{
		super(ctx);
	}

	public void saveMerchant(final Merchant_t merchant)
	{
		ContentValues values = new ContentValues();
		values.put(MerchantColumn._id.name(), merchant.getMerchantId());
		values.put(MerchantColumn.name.name(), merchant.getName());
		values.put(MerchantColumn.category.name(), merchant.getCategory().getCategoryId());

		values.put(MerchantColumn.merchant_obj.name(), ThriftUtil.serialize(merchant));

		mDb.replace(TaloolDbHelper.MERCHANT_TBL, null, values);

	}

	public List<Merchant_t> getMerchants(final Integer categoryId)
	{
		final List<Merchant_t> merchants = new ArrayList<Merchant_t>();
		Cursor cursor = null;

		if (categoryId == null)
		{
			// return all
			cursor = mDb.query(TaloolDbHelper.MERCHANT_TBL,
					MerchantColumn.getColumnArray(), null, null, null, null, MerchantColumn.name + " ASC");
		}
		else
		{
			cursor = mDb.query(TaloolDbHelper.MERCHANT_TBL,
					MerchantColumn.getColumnArray(), MerchantColumn.category + "=" +
							categoryId, null, null, null, MerchantColumn.name + " ASC");
		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			Merchant_t activity = cursorToActivity(cursor);
			merchants.add(activity);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return merchants;
	}

	public void saveMerchants(final List<Merchant_t> merchants)
	{
		mDb.beginTransaction();

		try
		{
			final ContentValues values = new ContentValues();
			for (final Merchant_t merchant : merchants)
			{
				values.put(MerchantColumn._id.name(), merchant.getMerchantId());
				values.put(MerchantColumn.name.name(), merchant.getName());
				values.put(MerchantColumn.category.name(), merchant.getCategory().getCategoryId());
				values.put(MerchantColumn.merchant_obj.name(), ThriftUtil.serialize(merchant));
				mDb.replace(TaloolDbHelper.MERCHANT_TBL, null, values);
				values.clear();
			}

			mDb.setTransactionSuccessful();
		}
		catch (Exception ex)
		{
			Log.e(this.getClass().getSimpleName(), "Problem saving merchants", ex);
		}
		finally
		{
			mDb.endTransaction();
		}

	}

	private Merchant_t cursorToActivity(final Cursor cursor)
	{
		final Merchant_t merchant = new Merchant_t();

		final byte[] objBytes = cursor.getBlob(MerchantColumn.merchant_obj.ordinal());

		try
		{
			ThriftUtil.deserialize(objBytes, merchant);
		}
		catch (TException e)
		{
		}

		return merchant;
	}
}