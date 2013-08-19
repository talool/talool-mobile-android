package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.DealActivity;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.thrift.util.ThriftUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity_layout);
		createThriftClient();
		dealsAcquiredList = (ListView) findViewById(R.id.mapActivityDealAcquiresList);

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
			mapView.getMap().addMarker(new MarkerOptions()
			.position(location)
			.title(merchant.name));
			


		} catch (GooglePlayServicesNotAvailableException e1) {
			Log.e(DealAcquiresActivity.class.toString(),"Maps was not loaded on this device. Please install");
		} catch (TException e) {
			// TODO Auto-generated catch block
			Log.e(DealAcquiresActivity.class.toString(),"Error Loading Merchant. Please go back and try again");
		}
	}
	
	private void createThriftClient()
	{

		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private class MapAcquiresTask extends AsyncTask<String,Void,List<DealAcquire_t>>{

		@Override
		protected void onPostExecute(List<DealAcquire_t> results) {
			dealAcquires = results;
			Log.i(MyDealsFragment.class.toString(), "Number of Deals: " + results.size());
			loadListView();
		}

		@Override
		protected List<DealAcquire_t> doInBackground(String... arg0) {
			List<DealAcquire_t> results = new ArrayList<DealAcquire_t>();

			try {
				exception = null;
				client.setAccessToken(TaloolUser.get().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("deal.dealId").setAscending(true);
				results = client.getClient().getDealAcquires(merchant.merchantId, searchOptions);
			} catch (ServiceException_t e) {
				// TODO Auto-generated catch block
				exception = e;
				e.printStackTrace();

			} catch (TException e) {
				// TODO Auto-generated catch block
				exception = e;
				e.printStackTrace();

			} catch (Exception e)
			{
				exception = e;
				e.printStackTrace();

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
			popupErrorMessage(exception.getMessage());
		}
	}
	
	private void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Error Loading Deals");
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				createThriftClient();
				reloadData();
			}
		});
		alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	private void reloadData()
	{
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

