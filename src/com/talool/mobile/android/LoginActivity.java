package com.talool.mobile.android;

import java.util.Arrays;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.SocialNetwork_t;
import com.talool.mobile.android.activity.SettingsActivity;
import com.talool.mobile.android.tasks.FetchFavoriteMerchantsTask;
import com.talool.mobile.android.util.FacebookHelper;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

public class LoginActivity extends Activity
{
	public static final String TALOOL_FB_PASSCODE = "talool4";
	private static ThriftHelper client;
	private Customer_t facebookCostomer;
	private EditText username;
	private EditText password;
	private Exception exception;
	private String errorMessage;
	private UiLifecycleHelper lifecycleHelper;
	private boolean isResumed = false;

	private class CustomerServiceTask extends AsyncTask<String, Void, CTokenAccess_t>
	{
		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(getCurrentFocus().getContext());
            pd.setTitle("Logging in...");
            pd.setMessage("One moment, please.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
		}
		
		@Override
		protected void onPostExecute(CTokenAccess_t result)
		{	
			if (exception != null)
			{
				popupErrorMessage(exception, errorMessage);
			}
			else
			{
				TaloolUser.get().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");

				doPostLogin();

				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(myDealsIntent);
				
				if (pd != null && pd.isShowing())
				{
					pd.dismiss();
				}
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
				if (facebookCostomer != null && !client.getClient().customerEmailExists(facebookCostomer.getEmail()))
				{
					tokenAccess = client.getClient().createAccount(facebookCostomer, password.getText().toString());
				}
				else
				{
					tokenAccess = client.getClient().authenticate(username.getText().toString(), password.getText().toString());
				}
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
		lifecycleHelper = new UiLifecycleHelper(this, statusCallback);
		lifecycleHelper.onCreate(savedInstanceState);

		setContentView(R.layout.login_layout);

		LoginButton authButton = (LoginButton) this.findViewById(R.id.login_button);
		authButton.setReadPermissions(Arrays.asList("email", "user_birthday"));

		username = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		username.setText("chris@talool.com");
		password.setText("pass123");

		ClipDrawable username_bg = (ClipDrawable) username.getBackground();
		username_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) password.getBackground();
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

		setTitle(R.string.welcome);
		
	}

	public void onLoginClick(View view)
	{
		exception = null;
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
		
		CustomerServiceTask task = new CustomerServiceTask();
		task.execute(new String[] {});
	}

	public void popupErrorMessage(Exception exception, String errorMessage)
	{

		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
				.build()
				);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Logging In");

		alertDialogBuilder.setMessage(errorMessage == null ? exception.getMessage() : errorMessage);
		alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton("Register", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				onRegistrationClicked(null);
			}
		});
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		lifecycleHelper.onPause();
		isResumed = false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		lifecycleHelper.onResume();
		isResumed = true;

		Session session = Session.getActiveSession();

		if (session != null && session.isOpened())
		{
			// user doesn't need to login
		}
		else
		{
			// otherwise present the splash screen
			// and ask the person to login.
			// showLogin();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		lifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		lifecycleHelper.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		lifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}
	private Session.StatusCallback statusCallback = new Session.StatusCallback()
	{
		@Override
		public void call(Session session, SessionState state, Exception exception)
		{
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state, Exception exception)
	{
		if (isResumed)
		{
			if (state.isOpened() && TaloolUser.get().getAccessToken() == null)
			{
				Request request = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback()
				{
					@Override
					public void onCompleted(GraphUser user, Response response)
					{
						facebookCostomer = FacebookHelper.createCostomerFromFacebook(user);
						username.setText(facebookCostomer.getEmail());
						password.setText(TALOOL_FB_PASSCODE + facebookCostomer.getSocialAccounts().get(SocialNetwork_t.Facebook).getLoginId());
						CustomerServiceTask task = new CustomerServiceTask();
						task.execute(new String[] {});
					}
				});
				request.executeAsync();
			}
		}
	}
}
