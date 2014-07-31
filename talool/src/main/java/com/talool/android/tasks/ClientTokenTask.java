package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;

/**
 * Created by dmccuen on 7/31/14.
 */
public class ClientTokenTask extends AsyncTask<String, Void, String>
{
    private ThriftHelper client;
    private Context context;

    public String errorMessage;

    public ClientTokenTask(final Context context, final ThriftHelper client)
    {
        this.context = context;
        this.client = client;
    }

    @Override
    protected String doInBackground(final String... params)
    {
        String token = null;
        try
        {
            client.setAccessToken(TaloolUser.get().getAccessToken());
            token = client.getClient().generateBraintreeClientToken();
        }
        catch (Exception e)
        {
            TaloolUtil.sendException(e, context);
            errorMessage = "We are unable to process your transaction at this time.  Please try again later.";
        }
        return token;
    }
}
