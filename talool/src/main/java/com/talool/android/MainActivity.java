package com.talool.android;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.talool.android.activity.LoginActivity;
import com.talool.android.activity.SettingsActivity;
import com.talool.android.fragment.DiscoverDealsFragment;
import com.talool.android.fragment.MyActivityFragment;
import com.talool.android.fragment.MyDealsFragment;
import com.talool.android.tasks.ActivitySupervisor;
import com.talool.android.util.Constants;
import com.talool.android.util.NotificationHelper;
import com.talool.android.util.TaloolUser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends Activity
{
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    private String SENDER_ID = "447579835837";
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private Context context;
    private String regid;

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
                actionBar.setIcon( android.R.color.transparent);

		Tab tab = actionBar.newTab().setText("My Deals").setTabListener(
				new MyTabListener(this, MyDealsFragment.class.getName()));
		Tab tab2 = actionBar.newTab().setText("Find Deals").setTabListener(
				new MyTabListener(this, DiscoverDealsFragment.class.getName()));
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

        context = getApplicationContext();
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }else{
                TaloolUser.get().setGcmDeviceToken(regid);
            }

            String deviceId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            if(deviceId != null){
                TaloolUser.get().setDeviceToken(deviceId);
            }
        }


	}

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }

        Log.i("GCM",registrationId);
        return registrationId;
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    Log.i("GCM","Device registered, registration ID=" + regid);

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
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

        selectPassedInTab();
        checkPlayServices();

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void selectPassedInTab()
    {
        Bundle b = getIntent().getExtras();
        if(b!=null)
        {
            int tabSelectedValue = b.getInt(Constants.TAB_SELECTED_KEY);
            if(tabSelectedValue < 3){
                getActionBar().setSelectedNavigationItem(tabSelectedValue);
            }else{
                getActionBar().setSelectedNavigationItem(0);
            }
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
			} // previous Fragment management

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
