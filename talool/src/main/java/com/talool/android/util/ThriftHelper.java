package com.talool.android.util;

import android.util.Base64;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.CustomerServiceConstants;
import com.talool.api.thrift.CustomerService_t;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;

/**
* Created by clintz on 3/1/15.
*/
public class ThriftHelper {

  public THttpClient tHttpClient;
  public TProtocol protocol;
  public CustomerService_t.Client client;

  public ThriftHelper() throws TTransportException {
    tHttpClient = new THttpClient(Constants.getApiUrl());
    protocol = new TBinaryProtocol(tHttpClient);
    client = new CustomerService_t.Client(protocol);
    setBaseHeaders();
    setCustomHeaders();
  }

  /**
   * Headers that are set for ever flavor
   */
  private void setBaseHeaders() {
    if ((TaloolUser.get() != null) && (TaloolUser.get().getAccessToken() != null)) {
          tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME, TaloolUser.get().getAccessToken().getToken());
      }
    tHttpClient.setCustomHeader("User-Agent", AndroidUtils.getUserAgent());
    if (TaloolUser.get().getGcmDeviceToken() != null) {
      tHttpClient.setCustomHeader(Constants.GCM_TOKEN_HEADER, Base64
          .encodeToString(TaloolUser.get().getGcmDeviceToken().getBytes(), Base64.NO_WRAP));
    }

    if (TaloolUser.get().getDeviceToken() != null) {
      tHttpClient.setCustomHeader(Constants.DEVICE_TOKEN_HEADER, TaloolUser.get().getDeviceToken());
    }

    tHttpClient.setCustomHeader("User-Agent", AndroidUtils.getUserAgent());
    tHttpClient.setCustomHeader("X-Supports-Free-Books", "");
  }

  protected void setCustomHeaders() {
    if (Constants.getCustomHeaders() == null) {
      return;
    }

    for (Map.Entry<String, String> entrySet : Constants.getCustomHeaders().entrySet()) {
      tHttpClient.setCustomHeader(entrySet.getKey(), entrySet.getValue());
    }

  }

  public TProtocol getProtocol() {
    return this.protocol;
  }

  public THttpClient gettHttpClient() {
    return tHttpClient;
  }

  public void settHttpClient(THttpClient tHttpClient) {
    this.tHttpClient = tHttpClient;
  }

  public CustomerService_t.Client getClient() {
    return client;
  }

  public void setClient(CustomerService_t.Client client) {
    this.client = client;
  }

  public void setProtocol(TProtocol protocol) {
    this.protocol = protocol;
  }

  public void setAccessToken(final CTokenAccess_t tokenAccess) {
    tHttpClient.setCustomHeader(CustomerServiceConstants.CTOKEN_NAME, tokenAccess.getToken());
  }
}
