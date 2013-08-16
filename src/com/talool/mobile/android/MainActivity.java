package com.talool.mobile.android;

import android.app.*;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.*;
import com.talool.mobile.android.activity.MyActivityFragment;
import com.talool.mobile.android.activity.SettingsActivity;
import com.talool.mobile.android.tasks.ActivitySupervisor;
import com.talool.mobile.android.util.TaloolUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends Activity
{
    private UiLifecycleHelper lifecycleHelper;
    private boolean isResumed = false;

    private final LocationListener locationListener = new LocationListener()
	{
		public void onLocationChanged(Location location)
		{
			TaloolUser.getInstance().setLocation(location);
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
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        // Add code to print out the key hash
        try {
            //todo remove, this will print your keyhash
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.talool.mobile.android",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("bug","bug");

        } catch (NoSuchAlgorithmException e) {

        }

        lifecycleHelper = new UiLifecycleHelper(this, statusCallback);
        lifecycleHelper.onCreate(savedInstanceState);
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar.newTab().setText("My Deals").setTabListener(
				new MyTabListener(this, MyDealsFragment.class.getName()));
		Tab tab2 = actionBar.newTab().setText("Find Deals").setTabListener(
				new MyTabListener(this, FindDealsFragment.class.getName()));
		Tab tab3 = actionBar.newTab().setCustomView(R.layout.activity_tab_layout).setTabListener(
                new MyTabListener( this, MyActivityFragment.class.getName()) );

        actionBar.addTab(tab);
		actionBar.addTab(tab2);
		actionBar.addTab(tab3);

		if (TaloolUser.getInstance().getAccessToken() == null)
		{
            showLogin();
		}
	}


    @Override
    protected void onResume() {
        super.onResume();
        lifecycleHelper.onResume();
        isResumed = true;

        /*if (TaloolUser.getInstance().getAccessToken() != null){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActivitySupervisor.createInstance(notificationCallback);
                }
            });
        } */
    }

    ActivitySupervisor.NotificationCallback notificationCallback = new ActivitySupervisor.NotificationCallback() {
        @Override
        public void handleNotificationCount(int totalNotifications) {
            if (totalNotifications > 0){
                newNotification(NOTIFICATION_ID, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, totalNotifications, null);
            }
        }
    };

    private void showLogin(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
			return false;
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

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        if (isResumed) {
            if (state.isOpened()) {
                Log.i("Pauk","facebook login success");
               //user is logged in... welcome back screen?
            } else if (state.isClosed()) {
                //show login
                showLogin();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lifecycleHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    //todo add dynamic messages
    public static final String NOTIFICATION_MESSAGE = "Received gift \"2 for 1 lunch\" at The Kitchen";
    private final static String NOTIFICATION_TITLE = "Update Test Message";
    private final static int NOTIFICATION_ID = 001;

    public void updateNotificationTab(final int count){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //todo clean clean clean
                Tab notificationTab = getActionBar().getTabAt(2);

                if (count > 0){
                    ((TextView)notificationTab.getCustomView().findViewById(R.id.actionbar_notifcation_textview)).setText(Integer.toString(count));
                    notificationTab.getCustomView().findViewById(R.id.activity_text_count).setVisibility(RelativeLayout.VISIBLE);
                }
                else {
                    notificationTab.getCustomView().findViewById(R.id.activity_text_count).setVisibility(RelativeLayout.GONE);
                }

            }
        });
    }

    private void newNotification(int notificationId, String notificationTitle, String notificationMessage, int totalNotifications, Object o) {
        updateNotificationTab(totalNotifications);

        //todo add intent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.icon_teal)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_MESSAGE)
                .setNumber(totalNotifications)
                .setContentIntent(null);

        NotificationManager mNotifyMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, builder.build());
    }






}
