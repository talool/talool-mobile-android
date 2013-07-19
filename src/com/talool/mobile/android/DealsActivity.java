package com.talool.mobile.android;

import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.util.ThriftHelper;

public class DealsActivity extends Activity
{
	private static ThriftHelper client;
	private Merchant_t merchant;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deals_activity_layout);
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		String lat = (String) getIntent().getSerializableExtra("Lat");
		String lon = (String) getIntent().getSerializableExtra("Lon");
		try
		{
			MapsInitializer.initialize(this);
		}
		catch (GooglePlayServicesNotAvailableException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(lat), Double.valueOf(lon)), 15);
		mapView.getMap().moveCamera(update);
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{}
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onDestroy();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onPause();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		MapView mapView = (MapView) findViewById(R.id.map);
		mapView.onResume();
	}
}
