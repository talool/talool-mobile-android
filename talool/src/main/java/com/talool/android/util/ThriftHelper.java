package com.talool.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.CustomerServiceConstants;
import com.talool.api.thrift.CustomerService_t;

public class ThriftHelper
{
	public THttpClient tHttpClient;
	public TProtocol protocol;
	public CustomerService_t.Client client;

	public ThriftHelper() throws TTransportException
	{
		tHttpClient = new THttpClient(Constants.getApiUrl());
		protocol = new TBinaryProtocol(tHttpClient);
		client = new CustomerService_t.Client(protocol);

		if (TaloolUser.get().getAccessToken() != null){
			tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME,TaloolUser.get().getAccessToken().getToken());
		}

        setCustomHeaders();

	}

	public ThriftHelper(CTokenAccess_t accessToken) throws TTransportException
	{
		tHttpClient = new THttpClient(Constants.getApiUrl());
		protocol = new TBinaryProtocol(tHttpClient);
		client = new CustomerService_t.Client(protocol);
		tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME, accessToken.getToken());
		tHttpClient.setCustomHeader("User-Agent", AndroidUtils.getUserAgent());

        setCustomHeaders();

    }

    private void setCustomHeaders(){
        if(TaloolUser.get().getGcmDeviceToken() != null){
            tHttpClient.setCustomHeader(Constants.GCM_TOKEN_HEADER,Base64.encodeToString(TaloolUser.get().getGcmDeviceToken().getBytes(),Base64.NO_WRAP));
        }

        if(TaloolUser.get().getDeviceToken() != null){
            tHttpClient.setCustomHeader(Constants.DEVICE_TOKEN_HEADER,TaloolUser.get().getDeviceToken());
        }

        tHttpClient.setCustomHeader("User-Agent", AndroidUtils.getUserAgent());


    }

	public TProtocol getProtocol()
	{
		return this.protocol;
	}

	public THttpClient gettHttpClient()
	{
		return tHttpClient;
	}

	public void settHttpClient(THttpClient tHttpClient)
	{
		this.tHttpClient = tHttpClient;
	}

	public CustomerService_t.Client getClient()
	{
		return client;
	}

	public void setClient(CustomerService_t.Client client)
	{
		this.client = client;
	}

	public void setProtocol(TProtocol protocol)
	{
		this.protocol = protocol;
	}

	public void setAccessToken(final CTokenAccess_t tokenAccess)
	{
		tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME, tokenAccess.getToken());
	}
}
