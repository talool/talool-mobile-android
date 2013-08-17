package com.talool.mobile.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.talool.mobile.android.LoginActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUser;

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

		final TextView userName = (TextView) findViewById(R.id.settingsUserNameText);
		userName.setText(TaloolUser.get().getAccessToken().getCustomer().getEmail());

		final Button button = (Button) findViewById(R.id.logoutButton);
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
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
}
