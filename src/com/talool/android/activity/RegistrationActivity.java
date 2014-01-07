package com.talool.android.activity;

import org.apache.thrift.TException;

import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.util.TaloolUser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;

public class RegistrationActivity extends TaloolActivity
{
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText password;

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
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Log.d(this.getClass().getName(), "back button pressed");
            final Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
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

	protected void popupErrorMessage(Exception exception)
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
			sendExceptionToAnalytics(exception);
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
}
