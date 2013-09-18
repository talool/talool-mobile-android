package com.talool.mobile.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.drawable.ClipDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ResetPasswordActivity extends Activity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private ResetPasswordTask mAuthTask = null;
	
	private static ThriftHelper client;

	private String mConfirm;
	private String mPassword;
	private String mToken;

	// UI references.
	private EditText mConfirmView;
	private EditText mPasswordView;
	private DialogFragment df;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reset_password);
		
		// get the limited use token from the URI
		final Uri uri;
		try {
			uri = getIntent().getData();
			mToken = uri.getPathSegments().get(0);
		}
		catch(Exception e)
		{
			Log.e("ParseDeepLink", e.getLocalizedMessage());
		}
		Log.d("ParseDeepLink", mToken);

		// Set up the login form.
		mConfirmView = (EditText) findViewById(R.id.confirm);
		mPasswordView = (EditText) findViewById(R.id.password);
		ClipDrawable confirm_bg = (ClipDrawable) mConfirmView.getBackground();
		confirm_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) mPasswordView.getBackground();
		password_bg.setLevel(1500);

	}

	public void onResetClick(View view) {
		if (mAuthTask != null) {
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
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mConfirm)) {
			mConfirmView.setError(getString(R.string.error_field_required));
			focusView = mConfirmView;
			cancel = true;
		} else if (!mConfirm.equals(mPassword)) {
			mConfirmView.setError(getString(R.string.error_passwords_dont_match));
			focusView = mConfirmView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			mAuthTask = new ResetPasswordTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous task used to reset password and authenticate
	 * the user.
	 */
	public class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: reset password
			// TODO: authenticate
			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

			if (success) {
				// TODO: post login stuff
				//finish();
			} else {
				mPasswordView
						.setError(getString(R.string.error_password_reset_auth_failed));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
		}
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
