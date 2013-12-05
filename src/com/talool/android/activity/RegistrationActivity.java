package com.talool.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.R.id;
import com.talool.android.R.layout;
import com.talool.android.R.string;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;

public class RegistrationActivity extends Activity
{
	private static ThriftHelper client;
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText password;
	private Exception exception;
	private DialogFragment df;

	private class RegisterTask extends AsyncTask<String, Void, CTokenAccess_t>
	{

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(CTokenAccess_t result)
		{
			if (exception != null)
			{
				popupErrorMessage(exception);
			}
			else
			{
				TaloolUser.get().setAccessToken(result);
				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				myDealsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
				exception = e;
			}
			catch (TException e)
			{
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
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void popupErrorMessage(Exception exception)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String title = getResources().getString(R.string.error_reg);
		String label = getResources().getString(R.string.retry);
		String message;
		if (exception instanceof ServiceException_t)
		{
			message = ((ServiceException_t) exception).errorDesc;
			EasyTracker easyTracker = EasyTracker.getInstance(this);
			easyTracker.send(MapBuilder
					.createEvent("registration", "failure", ((ServiceException_t) exception).getErrorDesc(), null)
					.build());
		}
		else
		{
			message = exception.getMessage();
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
					.build()
					);
		}
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
		this.exception = null;
	}

	public void popupErrorMessage(String message)
	{
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(message, true)
				.build()
				);

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String title = getResources().getString(R.string.error_reg);
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
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

	@Override
	protected void onResume()
	{
		super.onResume();
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
	}
}
