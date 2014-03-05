package com.talool.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.talool.android.activity.GiftActivity;
import com.talool.android.activity.WelcomeActivity;
import com.talool.android.util.TaloolUser;

public class SplashScreen extends Activity
{

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 3000;
	
	// Deep links hosts
	private static String DEEPLINK_SCHEME = "talool";
	private static String DEEPLINK_HOST_GIFT = "gift";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final boolean hasToken = TaloolUser.get().getAccessToken() != null;

		if (hasToken)
		{
			if (getCallingActivity() == null)
			{
				if (checkForDeepLinks())
				{
					final Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
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
				final Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
	
	/* 
	 * This is how Facebook links to us from it's native app.
	 *
	 * The uri will come in as our FB action link, not a 
	 * talool:// style uri.  This method will rebuild the uri
	 * to use our talool scheme, so the GiftActivity can parse  
	 * web and native deep links the same way.
	 * 
	 */
	private boolean checkForDeepLinks()
	{
		Uri targetUri = getIntent().getData();
		if (targetUri != null) {
			
	        Log.d("IncomingDeepLink", targetUri.toString());
	        
	        String linkAction = targetUri.getPathSegments().get(0);
	        if (linkAction.equalsIgnoreCase(DEEPLINK_HOST_GIFT))
	        {
	        	final Intent intent = new Intent(SplashScreen.this, GiftActivity.class);
	        	
	        	// rewrite the uri to the talool:// scheme
	        	Uri.Builder taloolGift = new Uri.Builder();
	        	taloolGift.scheme(DEEPLINK_SCHEME);
	        	taloolGift.authority(DEEPLINK_HOST_GIFT);
	        	taloolGift.appendPath(targetUri.getPathSegments().get(1));
	        	intent.setData(taloolGift.build());
	        	
				startActivity(intent);
				
				return false;
	        }
	        
	    }
		
		return true;
	}

}
