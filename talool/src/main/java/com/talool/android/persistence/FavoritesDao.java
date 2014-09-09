package com.talool.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.talool.api.thrift.Merchant_t;
import com.talool.thrift.util.ThriftUtil;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zachmanc on 3/7/14.
 */
public class FavoritesDao extends AbstractDbAdapter {

    public FavoritesDao(Context ctx)
    {
        super(ctx);
    }

    public void saveMerchant(final Merchant_t merchant)
    {
        ContentValues values = new ContentValues();
        values.put(TaloolDbHelper.MerchantColumn._id.name(), merchant.getMerchantId());
        values.put(TaloolDbHelper.MerchantColumn.name.name(), merchant.getName());
        values.put(TaloolDbHelper.MerchantColumn.category.name(), merchant.getCategory().getCategoryId());

        values.put(TaloolDbHelper.MerchantColumn.merchant_obj.name(), ThriftUtil.serialize(merchant));

        mDb.replace(TaloolDbHelper.FAVORITE_TBL, null, values);
        Log.i(this.getClass().getSimpleName(),"Merchant " + merchant.getMerchantId() + " saved to favorite db");

    }

    public List<Merchant_t> getMerchants(final Integer categoryId)
    {
        try {
            final List<Merchant_t> merchants = new ArrayList<Merchant_t>();
            Cursor cursor = null;

            if (categoryId == null) {
                // return all
                cursor = mDb.query(TaloolDbHelper.FAVORITE_TBL,
                        TaloolDbHelper.MerchantColumn.getColumnArray(), null, null, null, null, TaloolDbHelper.MerchantColumn.name + " ASC");
            } else {
                cursor = mDb.query(TaloolDbHelper.FAVORITE_TBL,
                        TaloolDbHelper.MerchantColumn.getColumnArray(), TaloolDbHelper.MerchantColumn.category + "=" +
                                categoryId, null, null, null, TaloolDbHelper.MerchantColumn.name + " ASC");
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Merchant_t activity = cursorToMerchant(cursor);
                merchants.add(activity);
                cursor.moveToNext();
            }
            // Make sure to close the cursor
            cursor.close();
            Log.i(this.getClass().getSimpleName(), "Get Merchants called");
            return merchants;
        }
        catch (SQLiteException e)
        {
            return new ArrayList<Merchant_t>();
        }
    }

    public void deleteRows(String merchantId)
    {
        try
        {
            if (merchantId != null)
            {
                mDb.delete(TaloolDbHelper.FAVORITE_TBL,TaloolDbHelper.MerchantColumn._id + "="+ merchantId,null);
            }else{
                mDb.delete(TaloolDbHelper.FAVORITE_TBL,null,null);
            }
            Log.i(this.getClass().getSimpleName(),"Rows Deleted");

        }
        catch (Exception ex)
        {
            Log.e(this.getClass().getSimpleName(), "Problem deleting rows from favorites table", ex);
        }
        finally {
        }
    }

    public boolean saveMerchants(final List<Merchant_t> merchants)
    {
        boolean success = false;
        mDb.beginTransaction();

        try
        {
            final ContentValues values = new ContentValues();
            for (final Merchant_t merchant : merchants)
            {
                values.put(TaloolDbHelper.MerchantColumn._id.name(), merchant.getMerchantId());
                values.put(TaloolDbHelper.MerchantColumn.name.name(), merchant.getName());
                values.put(TaloolDbHelper.MerchantColumn.category.name(), merchant.getCategory().getCategoryId());
                values.put(TaloolDbHelper.MerchantColumn.merchant_obj.name(), ThriftUtil.serialize(merchant));
                mDb.replace(TaloolDbHelper.FAVORITE_TBL, null, values);
                values.clear();
            }

            mDb.setTransactionSuccessful();
            Log.i(this.getClass().getSimpleName(),"Merchant list saved to favorite db");
            success = true;
        }
        catch (Exception ex)
        {
            Log.e(this.getClass().getSimpleName(), "Problem saving merchant to favorite table", ex);
        }
        finally
        {
            mDb.endTransaction();
            return success;
        }

    }

    private Merchant_t cursorToMerchant(final Cursor cursor)
    {
        final Merchant_t merchant = new Merchant_t();

        final byte[] objBytes = cursor.getBlob(TaloolDbHelper.MerchantColumn.merchant_obj.ordinal());

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
