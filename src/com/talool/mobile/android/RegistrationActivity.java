package com.talool.mobile.android;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.CustomerService_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class RegistrationActivity extends Activity {
	private static THttpClient tHttpClient;
	private static TProtocol protocol;
	private static CustomerService_t.Client client;
	private static CTokenAccess_t tokenAccess;
	private static EditText firstName;
	private static EditText lastName;
	private static EditText email;
	private static EditText password;
	
	private class RegisterTask extends AsyncTask<String,Void,CTokenAccess_t>{

		@Override
		protected void onPostExecute(CTokenAccess_t result) {
			tokenAccess = result;
			loginAttemptComplete();
		}

		@Override
		protected CTokenAccess_t doInBackground(String... arg0) {
			CTokenAccess_t tokenAccess = null;

			try {
				Customer_t customer = new Customer_t(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString());
				tokenAccess = client.createAccount(customer, password.getText().toString());
			} catch (ServiceException_t e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return tokenAccess;
		}

	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.registrationEmail);
        password = (EditText) findViewById(R.id.registrationPassword);
        
		try {
			tHttpClient = new THttpClient("http://dev-api.talool.com/1.1");
			protocol = new TBinaryProtocol(tHttpClient);
			client = new CustomerService_t.Client(protocol);
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

    public void loginAttemptComplete()
    {
    	if(tokenAccess != null)
    	{
			Log.i(RegistrationActivity.class.toString(), "Login Complete");
			Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(myDealsIntent);
    	}
    }
    
    public void onRegistrationClick(View view)
    {
    	RegisterTask registerTask = new RegisterTask();
    	registerTask.execute(new String[]{});
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	public void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Registering");
		alertDialogBuilder.setMessage(message);
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}
