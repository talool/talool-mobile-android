package com.talool.mobile.android;

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
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.util.ThriftHelper;

public class ForgotPasswordActivity extends Activity
{
	private EditText email;
	private Exception exception;
	private DialogFragment df;

	private class ForgotPasswordTask extends AsyncTask<String, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(Void nothing)
		{
			if (exception != null)
			{
				popupErrorMessage(exception);
			}
			else
			{

				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				myDealsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(myDealsIntent);
			}
		}

		@Override
		protected Void doInBackground(String... arg0)
		{
			ThriftHelper client = null;

			try
			{
				client = new ThriftHelper();
			}
			catch (TTransportException e)
			{
				popupErrorMessage(e);
				return null;
			}

			return null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password_layout);

		email = (EditText) findViewById(R.id.forgot_password_email);

		ClipDrawable username_bg = (ClipDrawable) email.getBackground();
		username_bg.setLevel(1500);

	}

	public void onRegistrationClick(View view)
	{
		if (TextUtils.isEmpty(email.getText().toString()))
		{
			popupErrorMessage("Your email is required");
		}
		else
		{
			ForgotPasswordTask registerTask = new ForgotPasswordTask();
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

		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
				.build()
				);

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
		}
		else
		{
			message = exception.getMessage();
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
