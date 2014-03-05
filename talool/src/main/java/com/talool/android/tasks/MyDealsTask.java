package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.persistence.MerchantDao;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;

import org.apache.thrift.TException;

import java.util.List;

/**
 * Created by zachmanc on 1/13/14.
 */
public class MyDealsTask extends AsyncTask<String, Void, List<Merchant_t>>
{
    private ThriftHelper client;
    private Context context;
    private MerchantDao merchantDao;

    public MyDealsTask(ThriftHelper client, Context context, MerchantDao merchantDao)
    {
        this.client = client;
        this.context = context;
        this.merchantDao = merchantDao;
    }

    @Override
    protected void onPostExecute(final List<Merchant_t> results)
    {
        if(results != null)
        {
            merchantDao.saveMerchants(results);
        }
    }

    @Override
    protected List<Merchant_t> doInBackground(String... arg0)
    {
        List<Merchant_t> results = null;

        try
        {
            final SearchOptions_t searchOptions = new SearchOptions_t();
            searchOptions.setMaxResults(1000).setPage(0).setSortProperty("merchant.name").setAscending(true);
            if (TaloolUser.get().getLocation() != null)
            {
                Location_t location = new Location_t();
                location.latitude = TaloolUser.get().getLocation().getLatitude();
                location.longitude = TaloolUser.get().getLocation().getLongitude();
                results = client.getClient().getMerchantAcquiresWithLocation(searchOptions, location);
            }
            else
            {
                results = client.getClient().getMerchantAcquiresWithLocation(searchOptions, null);
            }
        }
        catch (ServiceException_t e)
        {
            TaloolUtil.sendException(e,context);
        }
        catch (TException e)
        {
            TaloolUtil.sendException(e,context);
        }
        catch (Exception e)
        {
            TaloolUtil.sendException(e,context);
        }

        return results;
    }
}
