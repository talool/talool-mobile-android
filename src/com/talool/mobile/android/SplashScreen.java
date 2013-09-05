package com.talool.mobile.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.talool.mobile.android.util.TaloolUser;

public class SplashScreen extends Activity
{

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final boolean hasToken = TaloolUser.get().getAccessToken() != null;

		if (hasToken)
		{
			if (getCallingActivity() == null)
			{
				final Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				// close this activity
				finish();
				return;
			}
			else
			{
				finish();
				return;
			}
		}

		setContentView(R.layout.splash_screen_layout);

		new Handler().postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				// This method will be executed once the timer is over
				// Start your app main activity
				final Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}
