package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

public class DealsActivity extends Activity {
	private static ThriftHelper client;
	private ImageView imageView;
	private MapView mapView;
	private ListView dealsAcquiredList;
	private DealsAcquiredAdapter dealAcquiredAdapter;
	private Exception exception;
	private List<DealAcquire_t> dealAcquires;
	private String merchantId;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deals_activity_layout);
		createThriftClient();

		dealsAcquiredList = (ListView) findViewById(R.id.dealsAcquiredList);
		imageView = (ImageView) findViewById(R.id.dealsMerchantImage);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		reloadData();

		String lat = (String) getIntent().getSerializableExtra("Lat");
		String lon = (String) getIntent().getSerializableExtra("Lon");
		String address1 = (String) getIntent().getSerializableExtra("address1");
		String address2 = (String) getIntent().getSerializableExtra("address2");
		String city = (String) getIntent().getSerializableExtra("city");
		String zip = (String) getIntent().getSerializableExtra("zip");
		String state = (String) getIntent().getSerializableExtra("state");
		String merchantName = (String) getIntent().getSerializableExtra("merchantName");
		merchantId = (String) getIntent().getSerializableExtra("merchantId");
		String imageUrl = (String) getIntent().getSerializableExtra("imageUrl");

		if(imageUrl != null){
			ImageDownloader imageTask = new ImageDownloader(this.imageView);
			imageTask.execute(new String[]{imageUrl});			
		}
		try {
			MapsInitializer.initialize(this);
			LatLng location = new LatLng(Double.valueOf(lat),Double.valueOf(lon));
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);
			mapView.getMap().moveCamera(update);
			mapView.getMap().addMarker(new MarkerOptions()
			.position(location)
			.title(merchantName));
		} catch (GooglePlayServicesNotAvailableException e1) {
			Log.e(DealsActivity.class.toString(),"Maps was not loaded on this device. Please install");
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

	private void loadListView()
	{
		if(exception == null)
		{
			DealsAcquiredAdapter adapter = new DealsAcquiredAdapter(this, 
					R.layout.deals_acquired_item_row, dealAcquires);
			dealAcquiredAdapter = adapter;
			dealsAcquiredList.setAdapter(dealAcquiredAdapter);
		}
		else
		{
			popupErrorMessage(exception.getMessage());
		}
	}

	private void reloadData()
	{
		DealAcquiresTask dealAcquiresTask = new DealAcquiresTask();
		dealAcquiresTask.execute(new String[]{});
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


	private class DealAcquiresTask extends AsyncTask<String,Void,List<DealAcquire_t>>{

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
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("deal.dealId").setAscending(true);
				results = client.getClient().getDealAcquires(merchantId, searchOptions);
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
}
