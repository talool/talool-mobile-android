package com.talool.mobile.android;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.talool.api.thrift.Customer_t;
import com.talool.mobile.android.util.FacebookHelper;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.tasks.FetchFavoriteMerchantsTask;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import java.util.Arrays;

public class LoginActivity extends Activity
{
    public static final String TALOOL_FB_PASSCODE = "";
    private static ThriftHelper client;
    private Customer_t facebookCostomer;
	private EditText username;
	private EditText password;
	private Exception exception;
    private UiLifecycleHelper lifecycleHelper;
    private boolean isResumed = false;

	private class CustomerServiceTask extends AsyncTask<String, Void, CTokenAccess_t>
	{
        @Override
		protected void onPostExecute(CTokenAccess_t result)
		{
			if (exception != null)
			{
				popupErrorMessage(exception.getMessage());
			}
			else
			{
				TaloolUser.getInstance().setAccessToken(result);
				Log.i(LoginActivity.class.toString(), "Login Complete");

				doPostLogin();

				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(myDealsIntent);
			}
		}

		private void doPostLogin()
		{
			// load favorite merchants
			final FetchFavoriteMerchantsTask favMerchantTask = new FetchFavoriteMerchantsTask();

			favMerchantTask.execute(new String[] {});


		}



		@Override
		protected CTokenAccess_t doInBackground(String... arg0)
		{
			CTokenAccess_t tokenAccess = null;

			try
			{
				tokenAccess = client.getClient().authenticate(username.getText().toString(), password.getText().toString());
			}
			catch (ServiceException_t e)
			{
				Log.i(LoginActivity.class.toString(), e.getMessage());
				exception = e;
			}
			catch (TException e)
			{
				Log.i(LoginActivity.class.toString(), e.getMessage());
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
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			popupErrorMessage(e.getMessage());
		}
	}

	public void onLoginClick(View view)
	{
		exception = null;
		CustomerServiceTask task = new CustomerServiceTask();
		task.execute(new String[] {});
	}

	public void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Logging In");
		alertDialogBuilder.setMessage(message);
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
    protected void onPause() {
        super.onPause();
        lifecycleHelper.onPause();
        isResumed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleHelper.onResume();
        isResumed = true;

        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // user doesn't need to login
        } else {
            // otherwise present the splash screen
            // and ask the person to login.
            //showLogin();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lifecycleHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };


    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        if (isResumed) {
            if (state.isOpened() && TaloolUser.getInstance().getAccessToken() == null ) {
                Request request = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        facebookCostomer = FacebookHelper.createCostomerFromFacebook(user);

                        //CustomerServiceTask task = new CustomerServiceTask();
                        FacebookServiceTask task = new FacebookServiceTask();
                        task.execute(new String[] {});
                    }
                });
                request.executeAsync();
            } else if (state.isClosed()) {
                //show login
                //showLogin();
            }
        }
    }

    private class FacebookServiceTask extends AsyncTask<String,Void,CTokenAccess_t>{

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
                if (client.getClient().customerEmailExists(facebookCostomer.getEmail())){
                    //todo better password for facebook accounts.
                    tokenAccess = client.getClient().authenticate(facebookCostomer.getEmail(), TALOOL_FB_PASSCODE + facebookCostomer.getEmail());
                }
                else {
                    tokenAccess = client.getClient().createAccount(facebookCostomer, TALOOL_FB_PASSCODE + facebookCostomer.getEmail());
                }
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

}
