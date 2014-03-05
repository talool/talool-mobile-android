package com.talool.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.tasks.FetchFavoriteMerchantsTask;
import com.talool.android.util.FacebookHelper;
import com.talool.android.util.TaloolUser;
import com.talool.api.thrift.CTokenAccessResponse_t;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.SocialNetwork_t;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.util.Arrays;

public class WelcomeActivity extends TaloolActivity {
	
	private UiLifecycleHelper lifecycleHelper;
	public static final String TALOOL_FB_PASSCODE = "talool4";
	private Customer_t facebookCustomer;
	private String username;
	private String password;
	private boolean isResumed = false;
	
	private class CustomerServiceTask extends AsyncTask<String, Void, CTokenAccess_t>
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
				popupErrorMessage(exception, errorMessage);
			}
			else
			{
				TaloolUser.get().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");

				doPostLogin();

				final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
			CTokenAccessResponse_t tokenResponse = null;
			CTokenAccess_t tokenAccess = null;
						
			try
			{				
				if(facebookCustomer != null)		
				{
					if(facebookCustomer.getEmail() == null ||facebookCustomer.getEmail().isEmpty())
					{	
						ServiceException_t e = new ServiceException_t();
						e.errorDesc = "Facebook User Must have an email. Please verify facebook email";
						e.errorCode = 7777;
						exception = e;
						errorMessage = e.errorDesc;
					}
					else
					{
						String faceBookId = facebookCustomer.getSocialAccounts().get(SocialNetwork_t.Facebook).loginId;
						tokenResponse = client.getClient().loginFacebook(faceBookId, Session.getActiveSession().getAccessToken());
						if(tokenResponse.tokenAccess == null)
						{
							tokenAccess = client.getClient().createAccount(facebookCustomer, password);
						}
						else
						{
							tokenAccess = tokenResponse.tokenAccess;
						}
					}
				}
				else
				{
					tokenAccess = client.getClient().authenticate(username, password);
				}
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		lifecycleHelper = new UiLifecycleHelper(this, statusCallback);
		lifecycleHelper.onCreate(savedInstanceState);
		
		setContentView(R.layout.welcome_activity);
		
		LoginButton authButton = (LoginButton) this.findViewById(R.id.login_button);
		authButton.setReadPermissions(Arrays.asList("email", "user_birthday"));
		setTitle(R.string.welcome);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}
	
	public void onLoginClick(View view)
	{
		Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(myIntent);
        finish();
    }
	
	public void onRegistrationClicked(View view)
	{
		Intent myIntent = new Intent(getApplicationContext(), RegistrationActivity.class);
		startActivity(myIntent);
        finish();
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
						if(user != null)
						{
							facebookCustomer = FacebookHelper.createCustomerFromFacebook(user);
							username = facebookCustomer.getEmail();
							password = TALOOL_FB_PASSCODE + facebookCustomer.getEmail();
							CustomerServiceTask task = new CustomerServiceTask();
							task.execute(new String[] {});
						}
						else
						{
							if (df != null && !df.isHidden())
							{
								df.dismiss();
							}
							String message = "Error Logging in with facebook";
							String title = getResources().getString(R.string.error_login);
							String label = getResources().getString(R.string.retry);
							df = DialogFactory.getAlertDialog(title, message, label);
							df.show(getFragmentManager(), "dialog");						}
					}
				});
				request.executeAsync();
			}
            else if (state.isOpened() && TaloolUser.get().getAccessToken() != null)
            {
                final FetchFavoriteMerchantsTask favMerchantTask = new FetchFavoriteMerchantsTask(getApplicationContext());

                favMerchantTask.execute(new String[] {});
                final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
		}
	}

}
