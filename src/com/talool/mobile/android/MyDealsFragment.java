package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

public class MyDealsFragment extends Fragment
{
	private ListView myDealsListView;
	private MyDealsAdapter myDealsAdapter;
	private ThriftHelper client;
	private View view;
	private Context context;
	private Exception exception;
	private List<Merchant_t> merchants;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{

		this.view = inflater.inflate(R.layout.my_deals_fragment, container, false);
		final TextView txt = (TextView) view.findViewById(R.id.myDealsFoodFilterButton);
		txt.setTypeface(TypefaceFactory.get().getFontAwesome());
		myDealsListView = (ListView) view.findViewById(R.id.myDealsListView);

		this.context = view.getContext();
		createThriftClient();
		reloadData();
		return view;
	}

	public void createThriftClient()
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

	public void setButtonListeners()
	{
		Button allButton = (Button) view.findViewById(R.id.myDealsAllFilterButton);
		Button foodButton = (Button) view.findViewById(R.id.myDealsFoodFilterButton);
		Button funButton = (Button) view.findViewById(R.id.myDealsFunFilterButton);
		Button nightButton = (Button) view.findViewById(R.id.myDealsNightFilterButton);
		Button shopButton = (Button) view.findViewById(R.id.myDealsShopFilterButton);

		allButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				myDealsAdapter.getFilter().filter("");
			}
		});

		foodButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				myDealsAdapter.getFilter().filter("Food");
			}
		});

		funButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				myDealsAdapter.getFilter().filter("Fun");
			}
		});

		nightButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				myDealsAdapter.getFilter().filter("Night");
			}
		});

		shopButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				myDealsAdapter.getFilter().filter("Shop");
			}
		});
	}

	private void reloadData()
	{
		MyDealsTask dealsTask = new MyDealsTask();
		dealsTask.execute(new String[] {});
	}

	private void loadListView()
	{
		if (exception == null)
		{
			MyDealsAdapter adapter = new MyDealsAdapter(view.getContext(),
					R.layout.mydeals_item_row, merchants);
			myDealsAdapter = adapter;
			myDealsListView.setAdapter(myDealsAdapter);
			myDealsListView.setOnItemClickListener(onClickListener);
			setButtonListeners();
		}
		else
		{
			popupErrorMessage(exception.getMessage());
		}
	}

	private void popupErrorMessage(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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

	private class MyDealsTask extends AsyncTask<String, Void, List<Merchant_t>>
	{

		@Override
		protected void onPostExecute(List<Merchant_t> results)
		{
			merchants = results;
			Log.i(MyDealsFragment.class.toString(), "Number of Merchants: " + results.size());
			loadListView();
		}

		@Override
		protected List<Merchant_t> doInBackground(String... arg0)
		{
			List<Merchant_t> results = new ArrayList<Merchant_t>();

			try
			{
				exception = null;
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("merchant.name").setAscending(true);
				results = client.getClient().getMerchantAcquires(searchOptions);
			}
			catch (ServiceException_t e)
			{
				// TODO Auto-generated catch block
				exception = e;
				Log.e(MyDealsFragment.class.toString(), e.getMessage());
			}
			catch (TException e)
			{
				// TODO Auto-generated catch block
				exception = e;
				Log.e(MyDealsFragment.class.toString(), e.getMessage());

			}
			catch (Exception e)
			{
				exception = e;
				Log.e(MyDealsFragment.class.toString(), e.getMessage());

			}

			return results;
		}

	}

	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3)
		{
			MyDealsAdapter myDealsAdapter = (MyDealsAdapter) arg0.getAdapter();
			Merchant_t merchant = (Merchant_t) myDealsAdapter.getItem(position);

			Intent myIntent = new Intent(arg1.getContext(), DealsActivity.class);

			try
			{
				myIntent.putExtra("merchant", ThriftUtil.serialize(merchant));

				// deserialize example
				// Merchant_t merc = new Merchant_t();
				// ThriftUtil.deserialize(whatEverBytes,merc);

			}
			catch (TException e)
			{
				Log.e(this.getClass().getSimpleName(), "Cannot serialize merchant", e);
			}

			myIntent.putExtra("Lat", String.valueOf(merchant.getLocations().get(0).location.latitude));
			myIntent.putExtra("Lon", String.valueOf(merchant.getLocations().get(0).location.longitude));
			myIntent.putExtra("address1", merchant.getLocations().get(0).address.address1);
			myIntent.putExtra("address2", merchant.getLocations().get(0).address.address2);
			myIntent.putExtra("city", merchant.getLocations().get(0).address.city);
			myIntent.putExtra("zip", merchant.getLocations().get(0).address.zip);
			myIntent.putExtra("state", merchant.getLocations().get(0).address.stateProvinceCounty);
			myIntent.putExtra("merchantId", merchant.merchantId);
			myIntent.putExtra("merchantName", merchant.name);
			myIntent.putExtra("imageUrl", merchant.locations.get(0).merchantImageUrl);
			startActivity(myIntent);

		}
	};

}
