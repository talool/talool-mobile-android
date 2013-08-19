package com.talool.mobile.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
		final Intent intent = new Intent(view.getContext(), BasicWebViewActivity.class);

		intent.putExtra(BasicWebViewActivity.TARGET_URL_PARAM, "http://www.talool.com/privacy"));
		intent.putExtra(BasicWebViewActivity.TITLE_PARAM, "Talool");
		startActivity(intent);
	}
}
