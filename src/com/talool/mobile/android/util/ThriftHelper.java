package com.talool.mobile.android.util;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.CustomerServiceConstants;
import com.talool.api.thrift.CustomerService_t;

public class ThriftHelper
{
	// private final static String URL = "http://dev-api.talool.com/1.1";
	private final static String URL = "http://api.talool.com/1.1";
	public THttpClient tHttpClient;
	public TProtocol protocol;
	public CustomerService_t.Client client;

	public ThriftHelper() throws TTransportException
	{
		tHttpClient = new THttpClient(URL);
		protocol = new TBinaryProtocol(tHttpClient);
		client = new CustomerService_t.Client(protocol);

		if (TaloolUser.get().getAccessToken() != null)
		{
			tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME,
					TaloolUser.get().getAccessToken().getToken());
		}
	}

	public ThriftHelper(CTokenAccess_t accessToken) throws TTransportException
	{
		tHttpClient = new THttpClient(URL);
		protocol = new TBinaryProtocol(tHttpClient);
		client = new CustomerService_t.Client(protocol);
		tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME, accessToken.getToken());
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
