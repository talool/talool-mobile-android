package com.talool.mobile.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.analytics.tracking.android.EasyTracker;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.dialog.DialogFactory.ConfirmDialogListener;
import com.talool.mobile.android.util.ThriftHelper;

public class ForgotPasswordActivity extends Activity implements ConfirmDialogListener
{
	private EditText email;
	private String errorMessage;
	private DialogFragment df;
	private Button sendEmailBtn;

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
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

			if (errorMessage != null)
			{
				popupErrorMessage(errorMessage);
				return;
			}

			sendEmailBtn.setVisibility(View.GONE);
			final String title = getResources().getString(R.string.alert_sent_password_reset_title);
			final String message = getResources().getString(R.string.alert_sent_password_reset_message);
			df = DialogFactory.getAlertDialog(title, message, "Ok", ForgotPasswordActivity.this);
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected Void doInBackground(String... arg0)
		{
			final ThriftHelper client;

			try
			{
				client = new ThriftHelper();
				client.getClient().sendResetPasswordEmail(email.getText().toString());
			}
			catch (ServiceException_t e)
			{
				errorMessage = e.getErrorDesc();
				return null;
			}
			catch (Exception e)
			{
				popupErrorMessage("There was a problem generating your password reset request");
				return null;
			}

			return null;

		}
	}

	public void onForgotPasswordClick(final View view)
	{
		final String emailStr = email.getText().toString();

		if (TextUtils.isEmpty(emailStr))
		{
			popupErrorMessage("Your email is required");
		}
		else
		{
			final ForgotPasswordTask task = new ForgotPasswordTask();
			task.execute(new String[] {});
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password_layout);

		email = (EditText) findViewById(R.id.forgot_password_email);
		sendEmailBtn = (Button) findViewById(R.id.sendEmailBtn);

		ClipDrawable username_bg = (ClipDrawable) email.getBackground();
		username_bg.setLevel(1500);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void popupErrorMessage(String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String title = "";
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");

		errorMessage = null;
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

	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
		Intent myDealsIntent = new Intent(getApplicationContext(), LoginActivity.class);
		myDealsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(myDealsIntent);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog)
	{

	}
}
