package com.talool.mobile.android;

import org.apache.thrift.TException;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.talool.api.thrift.Merchant_t;
import com.talool.thrift.util.ThriftUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MapActivity extends Activity {
	private MapView mapView;
	private Merchant_t merchant;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity_layout);


		try {
			MapsInitializer.initialize(this);
			LatLng location = new LatLng(Double.valueOf(merchant.locations.get(0).location.latitude),Double.valueOf(merchant.locations.get(0).location.longitude));
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);
			mapView.getMap().moveCamera(update);
			mapView.getMap().addMarker(new MarkerOptions()
			.position(location)
			.title(merchant.name));
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			ThriftUtil.deserialize(merchantBytes,merchant);
			mapView = (MapView) findViewById(R.id.map);
			mapView.onCreate(savedInstanceState);
		} catch (GooglePlayServicesNotAvailableException e1) {
			Log.e(DealsActivity.class.toString(),"Maps was not loaded on this device. Please install");
		} catch (TException e) {
			// TODO Auto-generated catch block
			Log.e(DealsActivity.class.toString(),"Error Loading Merchant. Please go back and try again");
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
}
