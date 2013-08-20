package com.talool.mobile.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.talool.mobile.android.BasicWebViewActivity;
import com.talool.mobile.android.LoginActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.TypefaceFactory;

/**
 * 
 * @author clintz
 * 
 */
public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		final TextView userName = (TextView) findViewById(R.id.settings_username_label);
		userName.setText(TaloolUser.get().getAccessToken().getCustomer().getEmail());

		final TextView accountLabel = (TextView) findViewById(R.id.settings_account);
		accountLabel.setTypeface(TypefaceFactory.get().getMarkerFeltWide());

		final TextView aboutLabel = (TextView) findViewById(R.id.settings_about);
		aboutLabel.setTypeface(TypefaceFactory.get().getMarkerFeltWide());

		final Button button = (Button) findViewById(R.id.settings_logout_button);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(final View view)
			{
				TaloolUser.get().logoutUser();
				final Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
				startActivity(myIntent);
			}
		});

	}

	public void openUrl(final View view)
	{
		String url = null;
		String title = null;

		final Intent intent = new Intent(view.getContext(), BasicWebViewActivity.class);

		switch (view.getId())
		{
			case R.id.settings_privacy_layout:
				url = getResources().getString(R.string.settings_privacy_policy_url);
				title = getResources().getString(R.string.settings_privacy_policy);
				break;

			case R.id.settings_terms_of_use_layout:
				url = getResources().getString(R.string.settings_terms_of_service_url);
				title = getResources().getString(R.string.settings_terms_of_service);
				break;

			case R.id.settings_merchant_services_layout:
				url = getResources().getString(R.string.settings_merchant_services_url);
				title = getResources().getString(R.string.settings_merchant_services);
				break;

			case R.id.settings_publisher_services_layout:
				url = getResources().getString(R.string.settings_publisher_services_url);
				title = getResources().getString(R.string.settings_publisher_services);
				break;

		}

		intent.putExtra(BasicWebViewActivity.TARGET_URL_PARAM, url);
		intent.putExtra(BasicWebViewActivity.TITLE_PARAM, title);
		startActivity(intent);
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
