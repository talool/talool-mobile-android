package com.talool.android.fragment;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.activity.FindDealsActivity;
import com.talool.android.adapters.DiscoverDealsAdapter;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.util.Constants;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.CustomerService_t;
import com.talool.api.thrift.DealOfferGeoSummariesResponse_t;
import com.talool.api.thrift.DealOfferGeoSummary_t;
import com.talool.api.thrift.Location_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.TServiceException_t;
import com.talool.thrift.util.ThriftUtil;

public class DiscoverDealsFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener {	
	private ThriftHelper client;
	private View view;
	private ListView discoverDealsListView;
	private List<DealOfferGeoSummary_t> discoverDealsOffers;
	private Exception exception;
	private DialogFragment df;
	private DiscoverDealsAdapter discoverDealsAdapter;
	private PullToRefreshAttacher mPullToRefreshAttacher;
    private static final String FIND_DEALS_TITLE="Find Deals";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.discover_deals_fragment, container, false);
		discoverDealsListView = (ListView) view.findViewById(R.id.discoverDealsListView);
		createThriftClient();

        getActivity().setTitle(FIND_DEALS_TITLE);


        mPullToRefreshAttacher = ((MainActivity) getActivity())
				.getPullToRefreshAttacher();
		mPullToRefreshAttacher.addRefreshableView(discoverDealsListView, this);
		
		return this.view;
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

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
		easyTracker.set(Fields.SCREEN_NAME, FIND_DEALS_TITLE);
		easyTracker.send(MapBuilder.createAppView().build());
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	private void reloadData()
	{
		DiscoverDealsTask dealsTask = new DiscoverDealsTask();
		dealsTask.execute(new String[] {});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		reloadData();


	}

	private void loadListView()
	{
		DiscoverDealsAdapter adapter = new DiscoverDealsAdapter(view.getContext(),
				R.layout.discover_deal_row, discoverDealsOffers);
		discoverDealsAdapter = adapter;
		discoverDealsListView.setAdapter(discoverDealsAdapter);
		discoverDealsListView.setOnItemClickListener(onClickListener);
	}
	
	private class DiscoverDealsTask extends AsyncTask<String, Void, List<DealOfferGeoSummary_t>>
	{

		@Override
		protected void onPreExecute()
		{
			if (discoverDealsOffers == null || discoverDealsOffers.isEmpty())
			{
				df = DialogFactory.getProgressDialog();
				df.show(getFragmentManager(), "dialog");
			}
		}

		@Override
		protected void onPostExecute(final List<DealOfferGeoSummary_t> results)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
			mPullToRefreshAttacher.setRefreshComplete();

			if (exception != null)
			{
				popupErrorMessage(exception.getMessage());
			}
			else if(results == null)
			{
				popupErrorMessage("No deal were found");
			}
			else
			{
				discoverDealsOffers = results;
				loadListView();
			}

		}

		@Override
		protected List<DealOfferGeoSummary_t> doInBackground(String... arg0)
		{
			List<DealOfferGeoSummary_t> results = null;
			try
			{
				exception = null;
				final SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("distanceInMeters").setAscending(true);
				
				final SearchOptions_t fallbackOptions = new SearchOptions_t();
				fallbackOptions.setMaxResults(1000).setPage(0).setSortProperty("price").setAscending(true);
				
				
				Location userLocation = TaloolUser.get().getLocation();
				Location_t location = null;
				if(userLocation != null)
				{
					location = new Location_t(userLocation.getLongitude(),userLocation.getLatitude());
				}
				CustomerService_t.Client helper = client.getClient();
				DealOfferGeoSummariesResponse_t response = helper.getDealOfferGeoSummariesWithin(location, Constants.MAX_DISCOVER_MILES, fallbackOptions, fallbackOptions);
				results = response.getDealOfferGeoSummaries();
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

	private void popupErrorMessage(String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		if (message == null)
		{
			message = getResources().getString(R.string.error_access_code_message);
		}
		String title = getResources().getString(R.string.error_loading_deals);
		String label = getResources().getString(R.string.ok);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
	}

	@Override
	public void onRefreshStarted(View view) {
		reloadData();
	}
	
	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3)
		{
			DiscoverDealsAdapter discoverDealsAdapter = (DiscoverDealsAdapter) arg0.getAdapter();
			DealOfferGeoSummary_t dealOfferSummary = (DealOfferGeoSummary_t) discoverDealsAdapter.getItem(position);

			Intent myIntent = new Intent(arg1.getContext(), FindDealsActivity.class);
			myIntent.putExtra("dealOffer", ThriftUtil.serialize(dealOfferSummary.dealOffer));
			startActivity(myIntent);
		}
	};


}
