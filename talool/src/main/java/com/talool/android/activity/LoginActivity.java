package com.talool.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.tasks.FetchFavoriteMerchantsTask;
import com.talool.android.util.TaloolUser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.ServiceException_t;

public class LoginActivity extends TaloolActivity
{
	private EditText usernameEditText;
	private EditText passwordEditText;
	private String username;
	private String password;

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
				if (exception instanceof ServiceException_t)
				{
					EasyTracker easyTracker = EasyTracker.getInstance(context);
					easyTracker.send(MapBuilder
							.createEvent("login", "failure", ((ServiceException_t) exception).getErrorDesc(), null)
							.build());
				}

				popupErrorMessage(exception, errorMessage);

			}
			else
			{
				TaloolUser.get().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");

				doPostLogin();

				final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            catch (TException e)
            {
                if( e instanceof TTransportException)
                {
                    errorMessage = "Make sure you have a network connection";
                }
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
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

}
