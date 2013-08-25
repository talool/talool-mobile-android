package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import android.view.MenuItem;
import com.talool.mobile.android.activity.SettingsActivity;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.DealActivity;
import com.talool.mobile.android.adapters.DealsAcquiredAdapter;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.util.TaloolSmartImageView;
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
	private TaloolSmartImageView imageView;
	private ListView dealsAcquiredList;
	private DealsAcquiredAdapter dealAcquiredAdapter;
	private Exception exception;
	private List<DealAcquire_t> dealAcquires;
	private Merchant_t merchant;
	private Menu menu;
	private DialogFragment df;
	private String errorMessage;

	public boolean onCreateOptionsMenu(final Menu menu)
	{
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.heart_action_bar, menu);
        inflater.inflate(R.menu.main, menu);

		menu.getItem(0).setActionProvider(new FavoriteMerchantProvider(merchant, getApplicationContext()));

		this.menu = menu;

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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_acquires_activity_layout);
		createThriftClient();
		setIcons();
		dealsAcquiredList = (ListView) findViewById(R.id.dealsAcquiredList);
		imageView = (TaloolSmartImageView) findViewById(R.id.dealsMerchantImage);
		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
		try
		{
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");
			merchant = new Merchant_t();
			ThriftUtil.deserialize(merchantBytes, merchant);

			setTitle(merchant.getName());
			//reloadData();

			if (merchant.locations.get(0).merchantImageUrl != null)
			{
				imageView.setImageUrl(merchant.locations.get(0).merchantImageUrl);
			}
		}
		catch (TException e)
		{
			e.printStackTrace();
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(),exception),true)                                              
					.build()
					);
		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		reloadData();
	}

	public void onCallClicked(View view)
	{

		String uri = "tel:" + merchant.locations.get(0).phone.trim();
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
		Intent myIntent = new Intent(this.getApplicationContext(), BasicWebViewActivity.class);
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
			popupErrorMessage(exception,errorMessage);
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
			exception = e;
			errorMessage = "Make sure you have a network connection";
			popupErrorMessage(e, errorMessage);
		}
	}
	
	public void popupErrorMessage(Exception exception, String errorMessage)
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
		protected void onPreExecute() {
			if (dealAcquires==null || dealAcquires.isEmpty())
			{
				df = DialogFactory.getProgressDialog();
				df.show(getFragmentManager(), "dialog");
			}
		}

		@Override
		protected void onPostExecute(List<DealAcquire_t> results)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
			dealAcquires = results;
			loadListView();
		}

		@Override
		protected List<DealAcquire_t> doInBackground(String... arg0)
		{
			List<DealAcquire_t> results = new ArrayList<DealAcquire_t>();

			try
			{
				exception = null;
				errorMessage = null;
				client.setAccessToken(TaloolUser.get().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("deal.dealId").setAscending(true);
				results = client.getClient().getDealAcquires(merchant.merchantId, searchOptions);
			}
			catch (ServiceException_t e)
			{
				errorMessage = e.getErrorDesc();
				exception = e;
			}
			catch (TTransportException e)
			{
				errorMessage = "Make sure you have a network connection";
				exception = e;
			}
			catch (TException e)
			{
				exception = e;
			}
			catch (Exception e)
			{
				exception = e;
			}

			return results;
		}

	}
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker easyTracker = EasyTracker.getInstance(this);

	    easyTracker.activityStart(this);  // Add this method.

	    // MapBuilder.createEvent().build() returns a Map of event fields and values
	    // that are set and sent with the hit.
	    easyTracker.send(MapBuilder
	        .createEvent("deal_acquire_action","selected",merchant.merchantId,null)           
	        .build()
	    );
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
}
