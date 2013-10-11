package com.talool.mobile.android;

import org.apache.thrift.TException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.TNotFoundException_t;
import com.talool.api.thrift.TServiceException_t;
import com.talool.api.thrift.TUserException_t;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.util.ErrorMessageCache;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ResetPasswordActivity extends Activity
{
	private static final String LOG_TAG = ResetPasswordActivity.class.getSimpleName();

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private ResetPasswordTask mAuthTask = null;
	private String mConfirm;
	private String mPassword;
	private String customerId;
	private String resetCode;

	// UI references.
	private EditText mConfirmView;
	private EditText mPasswordView;
	private DialogFragment df;

	private String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reset_password);

		// get the limited use token from the URI
		final Uri uri;
		try
		{
			uri = getIntent().getData();
			customerId = uri.getPathSegments().get(0);
			resetCode = uri.getPathSegments().get(1);
		}
		catch (Exception e)
		{
			Log.e("ParseDeepLink", e.getLocalizedMessage());
		}

		// Set up the login form.
		mConfirmView = (EditText) findViewById(R.id.confirm);
		mPasswordView = (EditText) findViewById(R.id.password);
		ClipDrawable confirm_bg = (ClipDrawable) mConfirmView.getBackground();
		confirm_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) mPasswordView.getBackground();
		password_bg.setLevel(1500);

	}

	public void onResetClick(View view)
	{
		if (mAuthTask != null)
		{
			return;
		}

		// Reset errors.
		mConfirmView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mConfirm = mConfirmView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword))
		{
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		else if (mPassword.length() < 4)
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (!mConfirm.equals(mPassword))
		{
			mConfirmView.setError(getString(R.string.error_passwords_dont_match));
			focusView = mConfirmView;
			cancel = true;
		}

		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			mAuthTask = new ResetPasswordTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous task used to reset password and authenticate the
	 * user.
	 */
	public class ResetPasswordTask extends AsyncTask<Void, Void, CTokenAccess_t>
	{

		@Override
		protected CTokenAccess_t doInBackground(Void... params)
		{
			try
			{
				ThriftHelper client = new ThriftHelper();
				return client.getClient().resetPassword(customerId, resetCode, mPassword);
			}
			catch (TServiceException_t e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			catch (TUserException_t e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			catch (TNotFoundException_t e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			catch (TException e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(final CTokenAccess_t tokenAccess)
		{
			mAuthTask = null;
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

			if (errorMessage != null)
			{
				popupErrorMessage(errorMessage);
				return;
			}

			if (tokenAccess != null)
			{
				TaloolUser.get().setAccessToken(tokenAccess);
				Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
				myDealsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(myDealsIntent);
			}
			else
			{
				mPasswordView
						.setError(getString(R.string.error_password_reset_auth_failed));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled()
		{
			mAuthTask = null;
			if (df != null && !df.isHidden())
			{
				df.dismiss();
				errorMessage = null;
			}
		}
	}

	public void popupErrorMessage(String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}

		df = DialogFactory.getAlertDialog(ErrorMessageCache.Message.ResetPasswordFailureTitle.getText(),
				message, "Ok");

		df.show(getFragmentManager(), "dialog");

		errorMessage = null;
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
