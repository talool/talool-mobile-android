package com.talool.mobile.android;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class RegistrationActivity extends Activity {
	private static ThriftHelper client;
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText password;
	private Exception exception;
	
	private class RegisterTask extends AsyncTask<String,Void,CTokenAccess_t>{

		@Override
		protected void onPostExecute(CTokenAccess_t result) {
			if(exception != null)
			{
				popupErrorMessage(exception.getMessage());
			}
			else
			{
				TaloolUser.getInstance().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");
				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(myDealsIntent);
			}
		}

		@Override
		protected CTokenAccess_t doInBackground(String... arg0) {
			CTokenAccess_t tokenAccess = null;

			try {
				Customer_t customer = new Customer_t(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString());
				tokenAccess = client.getClient().createAccount(customer, password.getText().toString());
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
			client = new ThriftHelper();
		} catch (TTransportException e) {
			popupErrorMessage(e.getMessage());
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
