package com.talool.mobile.android.activity;

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

}
