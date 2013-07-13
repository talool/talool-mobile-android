package com.talool.mobile.android;

import com.talool.mobile.android.util.TaloolUser;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private final LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        TaloolUser.getInstance().setLocation(location);
	    }

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );
		actionBar.setDisplayShowTitleEnabled(false); 
		actionBar.setDisplayShowHomeEnabled(false);
		Tab tab = actionBar.newTab().setText("My Deals").setTabListener( 
	            new MyTabListener( this, MyDealsFragment.class.getName() ) );
		Tab tab2 = actionBar.newTab().setText("Find Deals").setTabListener( 
	            new MyTabListener( this, FindDealsFragment.class.getName()) );
		Tab tab3 = actionBar.newTab().setText("My Activity").setTabListener( 
	            new MyTabListener( this, TaloolActivityFragment.class.getName()) );
		actionBar.addTab(tab);
		actionBar.addTab(tab2);
		actionBar.addTab(tab3);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		boolean ret;
		if (item.getItemId() == R.id.menu_settings)
		{
			// Handle Settings
			ret = true;
		} else
		{
			ret = super.onOptionsItemSelected( item );
		}
		return ret;
	}
	
	private class MyTabListener implements ActionBar.TabListener
	{
    private Fragment mFragment;
    private final Activity mActivity;
    private final String mFragName;
 
    public MyTabListener( Activity activity, 
        String fragName )
    {
        mActivity = activity;
        mFragName = fragName;
    }
 
    @Override
    public void onTabReselected( Tab tab, 
        FragmentTransaction ft )
    {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void onTabSelected( Tab tab, 
        FragmentTransaction ft )
    {
        mFragment = Fragment.instantiate( mActivity, 
            mFragName );
        ft.add( android.R.id.content, mFragment );
    }
 
    @Override
    public void onTabUnselected( Tab tab, 
        FragmentTransaction ft )
    {
        ft.remove( mFragment );
        mFragment = null;
    }
}
}
