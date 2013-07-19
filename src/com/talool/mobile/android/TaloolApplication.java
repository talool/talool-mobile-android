package com.talool.mobile.android;

import android.app.Application;

import com.talool.mobile.android.util.TypefaceFactory;

/**
 * 
 * @author clintz
 * 
 */
public class TaloolApplication extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		init();
	}

	private void init()
	{
	    TypefaceFactory.createInstance(getAssets());
	}

}
