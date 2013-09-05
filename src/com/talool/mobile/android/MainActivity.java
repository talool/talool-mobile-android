package com.talool.mobile.android;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.talool.mobile.android.activity.MyActivityFragment;
import com.talool.mobile.android.activity.SettingsActivity;
import com.talool.mobile.android.tasks.ActivitySupervisor;
import com.talool.mobile.android.util.NotificationHelper;
import com.talool.mobile.android.util.TaloolUser;

public class MainActivity extends Activity
{

	private PullToRefreshAttacher mPullToRefreshAttacher;
	NotificationHelper notificationHelper;

	private final LocationListener locationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			if (isBetterLocation(location, TaloolUser.get().getLocation()))
			{
				TaloolUser.get().setLocation(location, true);
				LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				lm.removeUpdates(locationListener);
			}
		}

		@Override
		public void onProviderDisabled(String arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2)
		{
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onPause()
	{
		super.onPause();

		if (ActivitySupervisor.get() != null)
		{
			ActivitySupervisor.get().pause();
		}
	}

	private void getLastKnownLocation()
	{
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null)
		{
			TaloolUser.get().setLocation(location, true);
		}
	}

	private void subscribeForLocationUpdates()
	{
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getLastKnownLocation();
		subscribeForLocationUpdates();
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar.newTab().setText("My Deals").setTabListener(
				new MyTabListener(this, MyDealsFragment.class.getName()));
		Tab tab2 = actionBar.newTab().setText("Find Deals").setTabListener(
				new MyTabListener(this, FindDealsFragment.class.getName()));
		Tab tab3 = actionBar.newTab().setCustomView(R.layout.activity_tab_layout).setTabListener(
				new MyTabListener(this, MyActivityFragment.class.getName()));

		actionBar.addTab(tab);
		actionBar.addTab(tab2);
		actionBar.addTab(tab3);

		if (TaloolUser.get().getAccessToken() == null)
		{
			showLogin();
		}

		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (TaloolUser.get().getAccessToken() != null && notificationHelper == null)
		{
			notificationHelper = new NotificationHelper(getActionBar().getTabAt(2), getApplicationContext());
		}

		if (ActivitySupervisor.get() != null)
		{
			ActivitySupervisor.get().resume();
		}

	}

	private void showLogin()
	{
		final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			Log.d(this.getClass().getName(), "back button pressed");
			finish();
			return true;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	private class MyTabListener implements ActionBar.TabListener
	{
		private Fragment mFragment;
		private final Activity mActivity;
		private final String mFragName;

		public MyTabListener(Activity activity,
				String fragName)
		{
			mActivity = activity;
			mFragName = fragName;
		}

		@Override
		public void onTabReselected(Tab tab,
				FragmentTransaction ft)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void onTabSelected(Tab tab,
				FragmentTransaction ft)
		{
			// previous Fragment management
			Fragment prevFragment;
			FragmentManager fm = mActivity.getFragmentManager();
			prevFragment = fm.findFragmentByTag(mFragName);
			if (prevFragment != null)
			{
				mFragment = prevFragment;
			} // \previous Fragment management

			// Check if the fragment is already initialized
			if (mFragment == null)
			{
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mFragName);
				ft.add(android.R.id.content, mFragment, mFragName);
			}
			else
			{
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab,
				FragmentTransaction ft)
		{
			ft.remove(mFragment);
			mFragment = null;
		}
	}

	public PullToRefreshAttacher getPullToRefreshAttacher()
	{
		return mPullToRefreshAttacher;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(locationListener);
	}

	protected boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > 30000;
		boolean isSignificantlyOlder = timeDelta < -30000;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the
		// new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder)
		{
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
