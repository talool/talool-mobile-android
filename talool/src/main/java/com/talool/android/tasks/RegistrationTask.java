package com.talool.android.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.talool.android.MainActivity;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.Sex_t;

import org.apache.thrift.TException;

import java.util.Date;

/**
 * Created by dmccuen on 8/1/14.
 */
public class RegistrationTask extends AsyncTask<String, Void, CTokenAccess_t> {

    private Customer_t customer;
    private String password;
    private ThriftHelper client;
    private Context context;

    public String errorMessage;

    public RegistrationTask(final Customer_t customer, final String password,
                            final Context context, final ThriftHelper client ) {
        this.customer = customer;
        this.password = password;
        this.client = client;
        this.context = context;
    }

    @Override
    protected CTokenAccess_t doInBackground(String... arg0)
    {
        CTokenAccess_t tokenAccess = null;

        try
        {
            tokenAccess = client.getClient().createAccount(customer, password);
        }
        catch (ServiceException_t e)
        {
            TaloolUtil.sendException(e, context);
            errorMessage = ((ServiceException_t) e).errorDesc;
        }
        catch (TException e)
        {
            TaloolUtil.sendException(e,context);
            errorMessage = ((ServiceException_t) e).errorDesc;
        }
        return tokenAccess;
    }
}
