package com.talool.mobile.android;

import android.app.Application;

import com.talool.mobile.android.persistence.ActivityDao;
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
		// cache font-awesome typeface
		TypefaceFactory.createInstance(getAssets());

		// initialize Dao
		ActivityDao.createInstance(this.getApplicationContext());
	}

}
