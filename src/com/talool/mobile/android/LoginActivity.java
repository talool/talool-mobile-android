package com.talool.mobile.android;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static ThriftHelper client;
	private EditText username;
	private EditText password;
	private Exception exception;

	private class CustomerServiceTask extends AsyncTask<String,Void,CTokenAccess_t>{
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
				tokenAccess = client.getClient().authenticate(username.getText().toString(), password.getText().toString());
			} catch (ServiceException_t e) {
				// TODO Auto-generated catch block
				exception = e;
			} catch (TException e) {
				// TODO Auto-generated catch block
				exception = e;
			}
			return tokenAccess;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		username = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		username.setText("zachmanc@colorado.edu");
		password.setText("test123");
		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
			popupErrorMessage(e.getMessage());
		}

	}

	public void onLoginClick(View view)
	{
		CustomerServiceTask task = new CustomerServiceTask();
		task.execute(new String[]{});
	}
	
	public void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Logging In");
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	 dialog.dismiss();
		    } });
		alertDialogBuilder.setNegativeButton("Register", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	 onRegistrationClicked(null);
		    } });
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
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
