package com.talool.android;

import android.app.Application;
import android.content.Context;

import com.talool.android.util.TypefaceFactory;

/**
 * 
 * @author clintz
 * 
 */
public class TaloolApplication extends Application
{
	private static Context context;

	@Override
	public void onCreate()
	{
		super.onCreate();
		TaloolApplication.context = getApplicationContext();
		init();
	}

	private void init()
	{
		// cache font-awesome typeface
		TypefaceFactory.createInstance(getAssets());

	}

	public static Context getAppContext()
	{
		return TaloolApplication.context;
	}

}
