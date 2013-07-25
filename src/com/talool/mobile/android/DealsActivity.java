package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.google.android.gms.maps.MapView;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DealsActivity extends Activity {
	private static ThriftHelper client;
	private ImageView imageView;
	private MapView mapView;
	private ListView dealsAcquiredList;
	private DealsAcquiredAdapter dealAcquiredAdapter;
	private Exception exception;
	private List<DealAcquire_t> dealAcquires;
	private Merchant_t merchant;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deals_activity_layout);
		createThriftClient();
		setIcons();
		dealsAcquiredList = (ListView) findViewById(R.id.dealsAcquiredList);
		imageView = (ImageView) findViewById(R.id.dealsMerchantImage);
		


		try {
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			ThriftUtil.deserialize(merchantBytes,merchant);
			reloadData();

			if(merchant.locations.get(0).merchantImageUrl != null){
				ImageDownloader imageTask = new ImageDownloader(this.imageView);
				imageTask.execute(new String[]{merchant.locations.get(0).merchantImageUrl});			
			}
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setIcons()
	{
		final TextView callIcon = (TextView) findViewById(R.id.callIcon);
		callIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		final TextView visitIcon = (TextView) findViewById(R.id.visitIcon);
		visitIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		final TextView mapIcon = (TextView) findViewById(R.id.mapIcon);
		mapIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
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
}
