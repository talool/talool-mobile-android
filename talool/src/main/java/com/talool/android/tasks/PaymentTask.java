package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.TNotFoundException_t;
import com.talool.api.thrift.TServiceException_t;
import com.talool.api.thrift.TUserException_t;
import com.talool.api.thrift.TransactionResult_t;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmccuen on 7/31/14.
 */
public class PaymentTask extends AsyncTask<String, Void, TransactionResult_t>
{
    private ThriftHelper client;

    private String nonce;
    private String accessCode;
    private DealOffer_t dealOffer;
    private Context context;

    public String errorMessage;


    public PaymentTask(final DealOffer_t offer, final String nonce, final String code, final Context context, final ThriftHelper client)
    {
        this.nonce = nonce;
        this.dealOffer = offer;
        this.accessCode = code;
        this.context = context;
        this.client = client;

        this.errorMessage = null;
    }

    @Override
    protected TransactionResult_t doInBackground(final String... params)
    {
        TransactionResult_t transactionResult = null;
        Map<String,String> paymentProperties = new HashMap<String,String>(1);

        if(StringUtils.isNotEmpty(accessCode) )
        {
            paymentProperties.put("merchant_code",accessCode);
        }

        try
        {
            client.setAccessToken(TaloolUser.get().getAccessToken());
            transactionResult = client.getClient().purchaseWithNonce(dealOffer.getDealOfferId(), nonce, paymentProperties);
        }
        catch (TServiceException_t e)
        {
            TaloolUtil.sendException(e, context);
            errorMessage = ErrorMessageCache.getMessage(e.getErrorCode());
        }
        catch (TUserException_t e)
        {
            TaloolUtil.sendException(e,context);
            errorMessage = ErrorMessageCache.getMessage(e.getErrorCode());
        }
        catch (TNotFoundException_t e)
        {
            TaloolUtil.sendException(e,context);
            errorMessage = ErrorMessageCache.getNotFoundMessage(e.getIdentifier(), e.getKey());
        }
        catch (TException e)
        {
            TaloolUtil.sendException(e,context);
            errorMessage = ErrorMessageCache.getNetworkIssueMessage();
        }

        return transactionResult;
    }
}
