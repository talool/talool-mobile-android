package com.talool.mobile.android.activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class LocationSelectActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_select_activity);
	}
	
	public void onBoulderClicked(View view)
	{
		TaloolUser.get().setLocation(TaloolUser.get().BOULDER_LOCATION);
		onBackPressed();
	}
	
	public void onVancouverClicked(View view)
	{
		TaloolUser.get().setLocation(TaloolUser.get().VANCOUVER_LOCATION);
		onBackPressed();
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
