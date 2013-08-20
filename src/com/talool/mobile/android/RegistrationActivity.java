package com.talool.mobile.android;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

public class RegistrationActivity extends Activity
{
	private static ThriftHelper client;
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText password;
	private Exception exception;

	private class RegisterTask extends AsyncTask<String, Void, CTokenAccess_t>
	{

		@Override
		protected void onPostExecute(CTokenAccess_t result)
		{
			if (exception != null)
			{
				popupErrorMessage(exception);
			}
			else
			{
				System.out.println("Free memory (bytes): " +
						Runtime.getRuntime().freeMemory());

				/* This will return Long.MAX_VALUE if there is no preset limit */
				long maxMemory = Runtime.getRuntime().maxMemory();
				/* Maximum amount of memory the JVM will attempt to use */
				System.out.println("Maximum memory (bytes): " +
						(maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

				/* Total memory currently in use by the JVM */
				System.out.println("Total memory (bytes): " +
						Runtime.getRuntime().totalMemory());
				TaloolUser.get().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");
				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(myDealsIntent);
			}
		}

		@Override
		protected CTokenAccess_t doInBackground(String... arg0)
		{
			CTokenAccess_t tokenAccess = null;

			try
			{
				Customer_t customer = new Customer_t(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString());
				tokenAccess = client.getClient().createAccount(customer, password.getText().toString());
			}
			catch (ServiceException_t e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				exception = e;
			}
			catch (TException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				exception = e;
			}
			return tokenAccess;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_layout);

		firstName = (EditText) findViewById(R.id.firstName);
		lastName = (EditText) findViewById(R.id.lastName);
		email = (EditText) findViewById(R.id.registrationEmail);
		password = (EditText) findViewById(R.id.registrationPassword);

		ClipDrawable firstName_bg = (ClipDrawable) firstName.getBackground();
		firstName_bg.setLevel(1500);
		ClipDrawable lastName_bg = (ClipDrawable) lastName.getBackground();
		lastName_bg.setLevel(1500);
		ClipDrawable username_bg = (ClipDrawable) email.getBackground();
		username_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) password.getBackground();
		password_bg.setLevel(1500);

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			popupErrorMessage(e);
		}

	}

	public void onRegistrationClick(View view)
	{
		if (TextUtils.isEmpty(firstName.getText().toString()) ||
				TextUtils.isEmpty(lastName.getText().toString()) ||
				TextUtils.isEmpty(email.getText().toString()) ||
				TextUtils.isEmpty(password.getText().toString()))
		{
			popupErrorMessage("All fields in the registration page are required");
		}
		else
		{
			RegisterTask registerTask = new RegisterTask();
			registerTask.execute(new String[] {});
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void popupErrorMessage(Exception exception)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Registering");
		alertDialogBuilder.setMessage(exception.getMessage());
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
				.build()
				);
		// show it
		alertDialog.show();
	}

	public void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Registering");
		alertDialogBuilder.setMessage(message);
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(message, true)
				.build()
				);
		// show it
		alertDialog.show();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
}
