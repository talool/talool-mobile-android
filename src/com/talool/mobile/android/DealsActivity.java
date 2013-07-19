package com.talool.mobile.android;

import org.apache.thrift.transport.TTransportException;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class DealsActivity extends Activity {
	private static ThriftHelper client;
	private Merchant_t merchant;
	private ImageView imageView;
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deals_activity_layout);
		imageView = (ImageView) findViewById(R.id.dealsMerchantImage);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		String lat = (String) getIntent().getSerializableExtra("Lat");
		String lon = (String) getIntent().getSerializableExtra("Lon");
		String address1 = (String) getIntent().getSerializableExtra("address1");
		String address2 = (String) getIntent().getSerializableExtra("address2");
		String city = (String) getIntent().getSerializableExtra("city");
		String zip = (String) getIntent().getSerializableExtra("zip");
		String state = (String) getIntent().getSerializableExtra("state");
		String merchantName = (String) getIntent().getSerializableExtra("merchantName");
		String merchantId = (String) getIntent().getSerializableExtra("merchantId");
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


		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
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
