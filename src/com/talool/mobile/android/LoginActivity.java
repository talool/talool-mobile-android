package com.talool.mobile.android;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CustomerService_t;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class LoginActivity extends Activity {
	private static THttpClient tHttpClient;
	private static TProtocol protocol;
	private static CustomerService_t.Client client;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
		try {
			tHttpClient = new THttpClient("http://dev-api.talool.com/1.1");
			protocol = new TBinaryProtocol(tHttpClient);
			client = new CustomerService_t.Client(protocol);
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    public void onRegistrationClicked(View view)
    {
    
    	Intent myIntent = new Intent(getApplicationContext(), RegistrationActivity.class);
    	startActivity(myIntent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
