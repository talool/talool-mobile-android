package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.persistence.MerchantDao;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.EmailMessageResponse_t;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;

import org.apache.thrift.TException;

import java.util.List;

/**
 * Created by zachmanc on 7/13/14.
 */
public class EmailBodyRequestTask extends AsyncTask<String, Void, EmailMessageResponse_t> {

    private ThriftHelper client;
    private Context context;
    private String dealOfferId;

    public EmailBodyRequestTask(ThriftHelper client, Context context, String dealOfferId)
    {
        this.client = client;
        this.context = context;
        this.dealOfferId = dealOfferId;
    }

    @Override
    protected EmailMessageResponse_t doInBackground(String... arg0)
    {
        try
        {
            EmailMessageResponse_t emailMessageResponse_t = client.getClient().getEmailMessage("1",this.dealOfferId);
            return emailMessageResponse_t;
        }
        catch (ServiceException_t e)
        {
            TaloolUtil.sendException(e, context);
        }
        catch (TException e)
        {
            TaloolUtil.sendException(e,context);
        }
        catch (Exception e)
        {
            TaloolUtil.sendException(e,context);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(EmailMessageResponse_t emailMessageResponse_t) {
        super.onPostExecute(emailMessageResponse_t);
    }

}
