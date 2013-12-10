package com.talool.android.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.TaloolApplication;
import com.talool.android.activity.DealAcquiresActivity;
import com.talool.android.adapters.MyDealsAdapter;
import com.talool.android.cache.FavoriteMerchantCache;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.persistence.MerchantDao;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.ThriftHelper;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
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
	private TextView noResultsMessage;
	private Exception exception;
	private List<Merchant_t> merchants;
	private Menu menu;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private MerchantDao merchantDao;
	private DialogFragment df;
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

		noResultsMessage.setVisibility(View.GONE);
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

		noResultsMessage = (TextView) view.findViewById(R.id.activity_no_results_msg);
		noResultsMessage.setVisibility(View.GONE);

		createThriftClient();
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
			EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
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
			if (selectedFilter != FilterBy.All)
			{
				final StringBuilder sb = new StringBuilder();

				noResultsMessage.setVisibility(View.VISIBLE);
				if (selectedFilter.androidId == R.id.my_deals_filter_favorites)
				{
					sb.append(getResources().getString(selectedFilter.androidId == R.id.my_deals_filter_favorites ?
							R.string.my_deals_no_favorites_prefix : R.string.my_deals_no_favorites_prefix));
				}
				else
				{
					sb.append(getResources().getString(selectedFilter.androidId == R.id.my_deals_filter_favorites ?
							R.string.my_deals_no_favorites_prefix : R.string.my_deals_no_favorites_prefix)).append(" '").
							append(((MenuItem) menu.findItem(selectedFilter.androidId)).getTitle()).append("'");
				}
				noResultsMessage.setText(sb.toString());
			}
			else
			{
				// if there are no merchants and ALL is selected, then we have just
				// logged in
				refreshViaService();
			}

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
		}

		myDealsListView.setOnItemClickListener(onClickListener);
	}

	private void loadListView()
	{
		if (exception == null)
		{
			if (merchants.isEmpty())
			{
				showHelp();
			}
			else
			{
				MyDealsAdapter adapter = new MyDealsAdapter(view.getContext(),
						R.layout.mydeals_item_row, merchants);
				myDealsAdapter = adapter;
				myDealsListView.setAdapter(myDealsAdapter);
				myDealsListView.setOnItemClickListener(onClickListener);
			}
		}
		else
		{
			popupErrorMessage(exception.getMessage());
		}
	}

	private void popupErrorMessage(final String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String title = getResources().getString(R.string.error_loading_deals);
		String label = getResources().getString(R.string.ok);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
		/*
		 * dialog.dismiss(); createThriftClient(); reloadData();
		 */
	}

	private class MyDealsTask extends AsyncTask<String, Void, List<Merchant_t>>
	{

		private DialogFragment df;

		@Override
		protected void onPreExecute()
		{
			if (merchants != null && merchants.isEmpty())
			{
				df = DialogFactory.getProgressDialog();
				df.show(getFragmentManager(), "dialog");
			}
		}

		@Override
		protected void onPostExecute(final List<Merchant_t> results)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

			if(results != null)
			{
				merchants = results;
			}
			loadListView();
			mPullToRefreshAttacher.setRefreshComplete();

			if(results != null)
			{
				merchantDao.saveMerchants(results);
			}
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
				if (TaloolUser.get().getLocation() != null)
				{
					Location_t location = new Location_t();
					location.latitude = TaloolUser.get().getLocation().getLatitude();
					location.longitude = TaloolUser.get().getLocation().getLongitude();
					results = client.getClient().getMerchantAcquiresWithLocation(searchOptions, location);
				}
				else
				{
					results = client.getClient().getMerchantAcquiresWithLocation(searchOptions, null);
				}
			}
			catch (ServiceException_t e)
			{
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
	public void onResume()
	{
		super.onResume();
		reloadData();
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
	}

	@Override
	public void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
		easyTracker.set(Fields.SCREEN_NAME, "My Deals");

		easyTracker.send(MapBuilder.createAppView().build());
	}

	@Override
	public void onStop()
	{
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void showHelp()
	{
		TextView help = (TextView) view.findViewById(R.id.myDealsHelp);
		help.setTypeface(TypefaceFactory.get().getMarkerFelt());
		help.setText(R.string.helpLoadingDeals);
		Drawable arrowImage = getResources().getDrawable(R.drawable.help_arrow_brown);
		ImageView arrow2 = (ImageView) view.findViewById(R.id.arrow2);
		arrow2.setImageDrawable(arrowImage);
		ImageView arrow3 = (ImageView) view.findViewById(R.id.arrow3);
		arrow3.setImageDrawable(arrowImage);
	}

}
