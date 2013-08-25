package com.talool.mobile.android;

import android.app.Application;
import android.content.Context;

import com.talool.mobile.android.util.TypefaceFactory;

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

		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			@Override
			public void run()
			{
				System.out.println("Got here");
			}

		});

	}

	public static Context getAppContext()
	{
		return TaloolApplication.context;
	}

}
