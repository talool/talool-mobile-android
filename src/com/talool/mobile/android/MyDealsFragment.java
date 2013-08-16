package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.cache.FavoriteMerchantCache;
import com.talool.mobile.android.persistence.MerchantDao;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz,czachman
 * 
 */
public class MyDealsFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener
{
	private ListView myDealsListView;
	private MyDealsAdapter myDealsAdapter;
	private ThriftHelper client;
	private View view;
	private Context context;
	private Exception exception;
	private List<Merchant_t> merchants;
	private Menu menu;

	private MerchantDao merchantDao;

	private FilterBy selectedFilter = FilterBy.All;

	private enum FilterBy
	{
		All(null, R.id.my_deals_filter_all), Food(1, R.id.my_deals_filter_food),
		Shopping(2, R.id.my_deals_filter_shopping), Fun(3, R.id.my_deals_filter_fun),
		Favorites(null, R.id.my_deals_filter_favorites);

		private Integer categoryId;
		private Integer androidId;

		private static SparseArray<FilterBy> filterByMap = new SparseArray<FilterBy>();

		static
		{
			for (FilterBy fb : FilterBy.values())
			{
				filterByMap.put(fb.androidId, fb);
			}
		}

		FilterBy(Integer categoryId, Integer androidId)
		{
			this.categoryId = categoryId;
			this.androidId = androidId;
		}

		public Integer getCategoryId()
		{
			return categoryId;
		}
	};

	private PullToRefreshAttacher mPullToRefreshAttacher;

	// TODO REMOVE FOR PRODUCTION!
	private static final Location_t DENVER_LOCATION = new Location_t(-104.9842, 39.7392);

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// menu.clear();

		this.menu = menu;

		inflater.inflate(R.menu.my_deals_action_bar, menu);

		final MenuItem menuItem = menu.findItem(R.id.my_deals_filter_all);
		menuItem.setChecked(true);

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.my_deals_filter_root)
		{
			return true;
		}

		selectedFilter = FilterBy.filterByMap.get(item.getItemId());

		item.setChecked(item.isChecked() ? false : true);

		reloadData();

		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		merchantDao = new MerchantDao(TaloolApplication.getAppContext());
		merchantDao.open();

		this.view = inflater.inflate(R.layout.my_deals_fragment, container, false);
		// final TextView txt = (TextView)
		// view.findViewById(R.id.myDealsFoodFilterButton);
		// txt.setTypeface(TypefaceFactory.get().getFontAwesome());
		myDealsListView = (ListView) view.findViewById(R.id.myDealsListView);

		mPullToRefreshAttacher = ((MainActivity) getActivity())
				.getPullToRefreshAttacher();
		mPullToRefreshAttacher.addRefreshableView(myDealsListView, this);

		this.context = view.getContext();
		createThriftClient();
		reloadData();

		setHasOptionsMenu(true);

		StringBuilder sb = new StringBuilder();
		Customer_t c = TaloolUser.get().getAccessToken().customer;
		sb.append(c.firstName).append(" ").append(c.lastName);
		getActivity().setTitle(sb.toString());

		return view;
	}

	public void createThriftClient()
	{
		try
		{
			client = new ThriftHelper();
			client.setAccessToken(TaloolUser.get().getAccessToken());
		}
		catch (TTransportException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void reloadData()
	{
		switch (selectedFilter)
		{
			case All:
				merchants = merchantDao.getMerchants(null);
				break;
			case Favorites:
				merchants = new ArrayList<Merchant_t>(FavoriteMerchantCache.get().getMerchants());
				break;
			default:
				merchants = merchantDao.getMerchants(selectedFilter.getCategoryId());
				break;

		}
		if (merchants == null)
		{
			merchants = new ArrayList<Merchant_t>();
		}

		if (merchants.size() > 0)
		{
			updateMerchantsList(merchants);
		}
		else
		{
			refreshViaService();
		}

	}

	private void refreshViaService()
	{
		final MyDealsTask dealsTask = new MyDealsTask();
		dealsTask.execute(new String[] {});
	}

	private void updateMerchantsList(final List<Merchant_t> results)
	{

		try
		{
			MyDealsAdapter adapter = new MyDealsAdapter(view.getContext(),
					R.layout.mydeals_item_row, merchants);
			myDealsAdapter = adapter;
			myDealsListView.setAdapter(myDealsAdapter);
			myDealsListView.setOnItemClickListener(onClickListener);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			Log.e("dealsFrag", e.getLocalizedMessage(), e);
		}

		myDealsListView.setOnItemClickListener(onClickListener);
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

		}
		else
		{
			popupErrorMessage(exception.getMessage());
		}
	}

	private void popupErrorMessage(final String message)
	{
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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
		protected void onPostExecute(final List<Merchant_t> results)
		{
			merchants = results;
			Log.i(MyDealsFragment.class.toString(), "Number of Merchants: " + results.size());
			loadListView();
			mPullToRefreshAttacher.setRefreshComplete();
			merchantDao.saveMerchants(results);
		}

		@Override
		protected List<Merchant_t> doInBackground(String... arg0)
		{
			List<Merchant_t> results = null;

			try
			{
				exception = null;
				final SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("merchant.name").setAscending(true);
				results = client.getClient().getMerchantAcquiresWithLocation(searchOptions, DENVER_LOCATION);
			}
			catch (ServiceException_t e)
			{
				// TODO Auto-generated catch block
				exception = e;
				// Log.e(MyDealsFragment.class.toString(), e.getMessage());
			}
			catch (TException e)
			{
				// TODO Auto-generated catch block
				exception = e;
				// Log.e(MyDealsFragment.class.toString(), e.getMessage());

			}
			catch (Exception e)
			{
				exception = e;
				// Log.e(MyDealsFragment.class.toString(), e.getMessage());

			}

			return results;
		}
	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		// reloadData();
	}

	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3)
		{
			MyDealsAdapter myDealsAdapter = (MyDealsAdapter) arg0.getAdapter();
			Merchant_t merchant = (Merchant_t) myDealsAdapter.getItem(position);
			Intent myIntent = new Intent(arg1.getContext(), DealAcquiresActivity.class);
			myIntent.putExtra("merchant", ThriftUtil.serialize(merchant));
			startActivity(myIntent);
		}
	};

	@Override
	public void onRefreshStarted(final View view)
	{
		refreshViaService();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (merchantDao != null)
		{
			merchantDao.close();
		}
	}

}
