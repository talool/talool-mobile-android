package com.talool.mobile.android;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import com.talool.mobile.android.activity.SettingsActivity;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean ret;
        if (item.getItemId() == R.id.menu_settings)
        {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            ret = true;
        }
        else
        {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

}
