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
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.DealActivity;
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
	private ListView dealsAcquiredList;
	private DealsAcquiredAdapter dealAcquiredAdapter;
	private Exception exception;
	private List<DealAcquire_t> dealAcquires;
	private DialogFragment df;
	private String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity_layout);
		createThriftClient();
		dealsAcquiredList = (ListView) findViewById(R.id.mapActivityDealAcquiresList);
		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
		try {
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			MapsInitializer.initialize(this);
			ThriftUtil.deserialize(merchantBytes,merchant);
			mapView = (MapView) findViewById(R.id.map);
			mapView.onCreate(savedInstanceState);
			reloadData();

			LatLng location = new LatLng(Double.valueOf(merchant.locations.get(0).location.latitude),Double.valueOf(merchant.locations.get(0).location.longitude));
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);


			mapView.getMap().moveCamera(update);
			MarkerOptions marker = new MarkerOptions().position(location).title(merchant.name);
			mapView.getMap().addMarker(marker);
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


		} catch (GooglePlayServicesNotAvailableException e1) {
			exception = e1;
			errorMessage = "Google maps is not installed on this device. Please install";
			popupErrorMessage(exception, errorMessage);
		} catch (TException e) {
			// TODO Auto-generated catch block
			exception = e;
			errorMessage = "Error loading Map, please try again";
			popupErrorMessage(exception, errorMessage);		}
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

	private class MapAcquiresTask extends AsyncTask<String,Void,List<DealAcquire_t>>{

		@Override
		protected void onPreExecute() {
			if (dealAcquires==null || dealAcquires.isEmpty())
			{
				df = DialogFactory.getProgressDialog();
				df.show(getFragmentManager(), "dialog");
			}
		}

		@Override
		protected void onPostExecute(List<DealAcquire_t> results) {
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
			dealAcquires = results;
			loadListView();
		}

		@Override
		protected List<DealAcquire_t> doInBackground(String... arg0) {
			List<DealAcquire_t> results = new ArrayList<DealAcquire_t>();

			try {
				exception = null;
				errorMessage = null;
				client.setAccessToken(TaloolUser.get().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("deal.dealId").setAscending(true);
				results = client.getClient().getDealAcquires(merchant.merchantId, searchOptions);
			} catch (ServiceException_t e) {
				// TODO Auto-generated catch block
				exception = e;
			}
			catch (TTransportException e)
			{
				errorMessage = "Make sure you have a network connection";
				exception = e;
			} catch (TException e) {
				// TODO Auto-generated catch block
				exception = e;
			} catch (Exception e){
				exception = e;
			}

			return results;
		}

	}

	private void loadListView()
	{
		if(exception == null)
		{
			DealsAcquiredAdapter adapter = new DealsAcquiredAdapter(this, 
					R.layout.deals_acquired_item_row, dealAcquires);
			dealAcquiredAdapter = adapter;
			dealsAcquiredList.setAdapter(dealAcquiredAdapter);
			dealsAcquiredList.setOnItemClickListener(onClickListener);
		}
		else
		{
			popupErrorMessage(exception, errorMessage);
		}
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
		MapAcquiresTask dealAcquiresTask = new MapAcquiresTask();
		dealAcquiresTask.execute(new String[]{});
	}

	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3)
		{
			DealsAcquiredAdapter dealAcquiredAdapter = (DealsAcquiredAdapter) arg0.getAdapter();
			DealAcquire_t deal = (DealAcquire_t) dealAcquiredAdapter.getItem(position);

			Intent myIntent = new Intent(arg1.getContext(), DealActivity.class);

			myIntent.putExtra("deal", ThriftUtil.serialize(deal));
			myIntent.putExtra("merchant", ThriftUtil.serialize(merchant));

			startActivity(myIntent);

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

