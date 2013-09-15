package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import com.talool.mobile.android.activity.SettingsActivity;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.talool.api.thrift.Address_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.DealActivity;
import com.talool.mobile.android.adapters.MerchantLocationAdapter;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.thrift.util.ThriftUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MapActivity extends Activity {
	private static ThriftHelper client;
	private MapView mapView;
	private Merchant_t merchant;
	private ListView addressList;
	private MerchantLocationAdapter addressAdapter;
	private Exception exception;
	private List<MerchantLocation_t> merchantLocations;
	private DialogFragment df;
	private String errorMessage;
	private Boolean mapsConfigured = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity_layout);
		createThriftClient();
		addressList = (ListView) findViewById(R.id.mapActivityAddressList);
		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
		try {
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			ThriftUtil.deserialize(merchantBytes,merchant);
			reloadData();
			MapsInitializer.initialize(this);
			mapView = (MapView) findViewById(R.id.map);
			mapView.onCreate(savedInstanceState);

			plotLocationsOnMap();
			mapsConfigured = true;
		} catch (GooglePlayServicesNotAvailableException e1) {
			exception = e1;
			errorMessage = "Google maps is not installed on this device. Please install";
			popupErrorMessage(exception, errorMessage);
		} catch (TException e) {
			// TODO Auto-generated catch block
			exception = e;
			errorMessage = "Error loading Map, please try again";
			popupErrorMessage(exception, errorMessage);		
		}
	}

	private void plotLocationsOnMap()
	{
		for(MerchantLocation_t loc : merchant.locations)
		{
			LatLng location = new LatLng(Double.valueOf(loc.location.latitude),Double.valueOf(loc.location.longitude));
			MarkerOptions marker = new MarkerOptions().position(location).title(merchant.name);
			mapView.getMap().addMarker(marker);
		}
		mapView.getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				double lat = marker.getPosition().latitude;
				double lon = marker.getPosition().longitude;
				String url = "geo:"+lat+","+lon+"?q="+lat+","+lon+"("+marker.getTitle()+")";
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		LatLng location = new LatLng(Double.valueOf(merchant.locations.get(0).location.latitude),Double.valueOf(merchant.locations.get(0).location.longitude));
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);
		mapView.getMap().moveCamera(update);

	}

	private void createThriftClient()
	{

		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			exception = e;
			errorMessage = "Make sure you have a network connection";
			popupErrorMessage(exception, errorMessage);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onResume();
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


	private void loadListView()
	{
		merchantLocations = merchant.locations;
		MerchantLocationAdapter adapter = new MerchantLocationAdapter(this, 
				R.layout.address_row_layout, merchantLocations);
		addressAdapter = adapter;
		addressList.setAdapter(addressAdapter);
		addressList.setOnItemClickListener(onClickListener);
	}

	private void popupErrorMessage(Exception exception, String errorMessage)
	{
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
				.build()
				);

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String message = errorMessage == null ? exception.getMessage() : errorMessage;
		String title = getResources().getString(R.string.error_loading_deals);
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
		/*
        dialog.dismiss();
		createThriftClient();
		reloadData();
		 */
	}

	private void reloadData()
	{
		exception = null;
		errorMessage = null;
		loadListView();
	}

	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3)
		{
			if(mapsConfigured)
			{
				MerchantLocationAdapter merchantLocationAdapter = (MerchantLocationAdapter) arg0.getAdapter();
				MerchantLocation_t merchantLocation = (MerchantLocation_t) merchantLocationAdapter.getItem(position);

				LatLng location = new LatLng(Double.valueOf(merchantLocation.location.latitude),Double.valueOf(merchantLocation.location.longitude));
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);
				mapView.getMap().moveCamera(update);
			}

		}
	};

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

