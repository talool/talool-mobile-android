package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.DealActivity;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author czachman,clintz
 * 
 */
public class DealAcquiresActivity extends Activity
{
	private static ThriftHelper client;
	private ImageView imageView;
	private ListView dealsAcquiredList;
	private DealsAcquiredAdapter dealAcquiredAdapter;
	private Exception exception;
	private List<DealAcquire_t> dealAcquires;
	private Merchant_t merchant;

	public boolean onCreateOptionsMenu(final Menu menu)
	{
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.heart_action_bar, menu);

		menu.getItem(0).setActionProvider(new FavoriteMerchantProvider(getApplicationContext()));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_acquires_activity_layout);
		createThriftClient();
		setIcons();
		dealsAcquiredList = (ListView) findViewById(R.id.dealsAcquiredList);
		imageView = (ImageView) findViewById(R.id.dealsMerchantImage);

		try
		{
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			ThriftUtil.deserialize(merchantBytes, merchant);

			setTitle(merchant.getName());
			reloadData();

			if (merchant.locations.get(0).merchantImageUrl != null)
			{
				ImageDownloader imageTask = new ImageDownloader(this.imageView);
				imageTask.execute(new String[] { merchant.locations.get(0).merchantImageUrl });
			}
		}
		catch (TException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		reloadData();
	}

	public void onCallClicked(View view)
	{
		
		 String uri = "tel:" + merchant.locations.get(0).phone.trim() ;
		 Intent intent = new Intent(Intent.ACTION_DIAL);
		 intent.setData(Uri.parse(uri));
		 startActivity(intent);	
	}
	
	public void mapClick(View view)
	{
		Intent myIntent = new Intent(view.getContext(), MapActivity.class);
		myIntent.putExtra("merchant", ThriftUtil.serialize(merchant));
		startActivity(myIntent);
	}
	
	public void onWebsiteClick(View view)
	{
		Intent myIntent = new Intent(view.getContext(),BasicWebViewActivity.class);
		myIntent.putExtra(BasicWebViewActivity.TARGET_URL_PARAM, merchant.locations.get(0).websiteUrl);
		myIntent.putExtra(BasicWebViewActivity.TITLE_PARAM, merchant.name);
		startActivity(myIntent);
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
		if (exception == null)
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

	private void reloadData()
	{
		DealAcquiresTask dealAcquiresTask = new DealAcquiresTask();
		dealAcquiresTask.execute(new String[] {});
	}

	private void createThriftClient()
	{
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
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

	private class DealAcquiresTask extends AsyncTask<String, Void, List<DealAcquire_t>>
	{

		@Override
		protected void onPostExecute(List<DealAcquire_t> results)
		{
			dealAcquires = results;
			Log.i(MyDealsFragment.class.toString(), "Number of Deals: " + results.size());
			loadListView();
		}

		@Override
		protected List<DealAcquire_t> doInBackground(String... arg0)
		{
			List<DealAcquire_t> results = new ArrayList<DealAcquire_t>();

			try
			{
				exception = null;
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("deal.dealId").setAscending(true);
				results = client.getClient().getDealAcquires(merchant.merchantId, searchOptions);
			}
			catch (ServiceException_t e)
			{
				// TODO Auto-generated catch block
				exception = e;
				e.printStackTrace();

			}
			catch (TException e)
			{
				// TODO Auto-generated catch block
				exception = e;
				e.printStackTrace();

			}
			catch (Exception e)
			{
				exception = e;
				e.printStackTrace();

			}

			return results;
		}

	}
}
