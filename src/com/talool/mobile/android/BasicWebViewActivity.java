package com.talool.mobile.android;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * An activity that simply presents a WebView
 * 
 * @author clintz
 * 
 */
public class BasicWebViewActivity extends Activity
{
	public static String TARGET_URL_PARAM = "targetUrl";
	public static String TITLE_PARAM = "title";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_activity);

		final WebView webView = (WebView) findViewById(R.id.webview);
		final String targetUrl = (String) getIntent().getSerializableExtra(TARGET_URL_PARAM);
		final String title = (String) getIntent().getSerializableExtra(TITLE_PARAM);

		if (title != null)
		{
			setTitle(title);
		}

		webView.loadUrl(targetUrl);
		webView.getSettings().setJavaScriptEnabled(true);

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
