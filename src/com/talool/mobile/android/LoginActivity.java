package com.talool.mobile.android;

import java.util.Arrays;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.SettingsActivity;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.tasks.FetchFavoriteMerchantsTask;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

public class LoginActivity extends Activity
{
	private static ThriftHelper client;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private String username;
	private String password;
	private Exception exception;
	private String errorMessage;
	private boolean isResumed = false;
	private DialogFragment df;

	private class CustomerServiceTask extends AsyncTask<String, Void, CTokenAccess_t>
	{
		private Context context;

		public CustomerServiceTask(Context context)
		{
			super();
			this.context = context;
		}

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
				if(exception instanceof ServiceException_t)
				{
					EasyTracker easyTracker = EasyTracker.getInstance(context);
					easyTracker.send(MapBuilder
							.createEvent("login", "failure", ((ServiceException_t) exception).getErrorDesc(), null)
							.build());
				}
				else
				{
					popupErrorMessage(exception, errorMessage);

				}
			}
			else
			{
				TaloolUser.get().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");

				doPostLogin();

				final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}

		private void doPostLogin()
		{
			// load favorite merchants
			final FetchFavoriteMerchantsTask favMerchantTask = new FetchFavoriteMerchantsTask(getApplicationContext());

			favMerchantTask.execute(new String[] {});
		}

		@Override
		protected CTokenAccess_t doInBackground(String... arg0)
		{
			CTokenAccess_t tokenAccess = null;

			try
			{
				tokenAccess = client.getClient().authenticate(username, password);
			}
			catch (ServiceException_t e)
			{
				errorMessage = e.getErrorDesc();
				exception = e;						
			}
			catch (TTransportException e)
			{
				errorMessage = "Make sure you have a network connection";
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

		setContentView(R.layout.login_layout);

		usernameEditText = (EditText) findViewById(R.id.email);
		passwordEditText = (EditText) findViewById(R.id.password);

		ClipDrawable username_bg = (ClipDrawable) usernameEditText.getBackground();
		username_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) passwordEditText.getBackground();
		password_bg.setLevel(1500);

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			popupErrorMessage(e, errorMessage);
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}

		setTitle(R.string.login_with_talool);

	}

	public void onLoginClick(View view)
	{
		exception = null;
		errorMessage = null;
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
		username = usernameEditText.getText().toString();
		password = passwordEditText.getText().toString();
		CustomerServiceTask task = new CustomerServiceTask(this);
		task.execute(new String[] {});
	}

	public void popupErrorMessage(Exception exception, String errorMessage)
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
		String message = errorMessage == null ? exception.getMessage() : errorMessage;
		String title = getResources().getString(R.string.error_login);
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");

	}

	public void onForgotPasswordClicked(View view)
	{
		Intent myIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
		startActivity(myIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean ret;
		if (item.getItemId() == R.id.menu_settings)
		{
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			ret = true;
		}
		else
		{
			ret = super.onOptionsItemSelected(item);
		}
		return ret;
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
	protected void onPause()
	{
		super.onPause();
		isResumed = false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		isResumed = true;

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}

	}




}
