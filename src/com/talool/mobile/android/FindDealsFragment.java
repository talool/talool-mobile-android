package com.talool.mobile.android;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.Location;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.image.SmartImageView;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Deal_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.activity.LocationSelectActivity;
import com.talool.mobile.android.adapters.FindDealsAdapter;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

public class FindDealsFragment extends Fragment {
	private ThriftHelper client;
	private View view;
	private Exception exception;
	private List<Deal_t> dealOffers;
	private ListView dealOffersListView;
	private FindDealsAdapter dealOffersAdapter;
	private static final String DEAL_OFFER_ID_PAYBACK_BOULDER = "4d54d8ef-febb-4719-b9f0-a73578a41803";
	private static final String DEAL_OFFER_ID_PAYBACK_VANCOUVER = "a067de54-d63d-4613-8d60-9d995765cd52";
	private DealOffer_t boulderBook;
	private DealOffer_t vancouverBook;
	private SmartImageView bookImage;
	private DealOffer_t closestBook;
	private MapView mapView;
	private LinearLayout listViewLinearLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.view = inflater.inflate(R.layout.find_deals_fragment, container,false);
		bookImage = (SmartImageView) view.findViewById(R.id.bookImageView);
		dealOffersListView = (ListView) view.findViewById(R.id.dealOffersListView);
		listViewLinearLayout = (LinearLayout) view.findViewById(R.id.listViewLinearLayout);
		try {
			MapsInitializer.initialize(view.getContext());
			mapView = (MapView) view.findViewById(R.id.find_deals_map);
			mapView.onCreate(savedInstanceState);
		} catch (GooglePlayServicesNotAvailableException e1) {
			Log.e(DealAcquiresActivity.class.toString(),"Maps was not loaded on this device. Please install");
		} 
		createThriftClient();
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if(TaloolUser.get().getLocation() == null)
		{
			Intent intent = new Intent(this.view.getContext(), LocationSelectActivity.class);
			startActivity(intent);
		}
		else
		{
			reloadData();
		}

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
		easyTracker.set(Fields.SCREEN_NAME, "Find Deals");

		easyTracker.send(MapBuilder.createAppView().build());
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
			EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(),e),true)                                              
					.build()
					);
		}
	}

	private void reloadData()
	{
		loadBooks();
	}

	private void getBoulderBook()
	{
		final DealOffer_t dealOffer = DealOfferCache.get().getDealOffer(DEAL_OFFER_ID_PAYBACK_BOULDER);
		if (dealOffer == null)
		{
			final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, DEAL_OFFER_ID_PAYBACK_BOULDER)
			{
				@Override
				protected void onPostExecute(final DealOffer_t dealOffer)
				{
					DealOfferCache.get().setDealOffer(dealOffer);
					boulderBookCompletion(dealOffer);
				}
			};
			dealOfferFetchTask.execute(new String[] {});
		}
		else
		{
			boulderBookCompletion(dealOffer);
		}
	}

	private void getVancouverBook()
	{
		final DealOffer_t dealOffer = DealOfferCache.get().getDealOffer(DEAL_OFFER_ID_PAYBACK_VANCOUVER);
		if (dealOffer == null)
		{
			final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, DEAL_OFFER_ID_PAYBACK_VANCOUVER)
			{
				@Override
				protected void onPostExecute(final DealOffer_t dealOffer)
				{
					DealOfferCache.get().setDealOffer(dealOffer);
					vancouverBookCompletion(dealOffer);
				}
			};
			dealOfferFetchTask.execute(new String[] {});
		}
		else
		{
			vancouverBookCompletion(dealOffer);
		}
	}

	private void vancouverBookCompletion(DealOffer_t dealOffer)
	{
		vancouverBook = dealOffer;
		determineClosestBook();
	}

	private void boulderBookCompletion(DealOffer_t dealOffer)
	{
		boulderBook = dealOffer;
		determineClosestBook();
	}

	private void findDealOfferBooks()
	{
		getBoulderBook();
		getVancouverBook();
	}

	private void loadBookImages()
	{
		if(closestBook.imageUrl != null)
		{
			bookImage.setImageUrl(closestBook.imageUrl);
		}
	}

	private void determineClosestBook()
	{
		if(boulderBook == null || vancouverBook == null)
		{
			//return and wait for the other book to be loaded
			return;
		}
		else
		{
			Location userLocation = TaloolUser.get().getLocation();
			float distanceToBoulder = userLocation.distanceTo(TaloolUser.get().BOULDER_LOCATION);
			float distanceToVan = userLocation.distanceTo(TaloolUser.get().VANCOUVER_LOCATION);

			if(distanceToBoulder <= distanceToVan)
			{
				closestBook = boulderBook;
			}
			else
			{
				closestBook = vancouverBook;
			}
			loadBookImages();
			loadBookDeals();
		}
	}
	
	private void loadBookDeals()
	{		
//		LatLng location = new LatLng(Double.valueOf(merchant.locations.get(0).location.latitude),Double.valueOf(merchant.locations.get(0).location.longitude));
//		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 13);	
//		mapView.getMap().moveCamera(update);
//		mapView.getMap().addMarker(new MarkerOptions()
//		.position(location)
//		.title(merchant.name));
		FindDealsTask dealsTask = new FindDealsTask();
		dealsTask.execute(new String[]{});
	}

	private void loadBooks()
	{
		if (exception == null)
		{
			findDealOfferBooks();
			determineClosestBook();
		}
		else
		{
			popupErrorMessage(exception.getMessage());
		}
	}

	private void popupErrorMessage(final String message)
	{
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
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

	private void loadListView()
	{
		FindDealsAdapter adapter = new FindDealsAdapter(view.getContext(),
				R.layout.find_deal_row, dealOffers);
		dealOffersAdapter = adapter;
		dealOffersListView.setAdapter(dealOffersAdapter);
		setListViewHeightBasedOnChildren(dealOffersListView);
	}
	
	private class FindDealsTask extends AsyncTask<String, Void, List<Deal_t>>
	{

		@Override
		protected void onPostExecute(final List<Deal_t> results)
		{
			if(exception != null)
			{
				popupErrorMessage(exception.getMessage());
			}
			else
			{
				dealOffers = results;
				Log.i(MyDealsFragment.class.toString(), "Number of Merchants: " + results.size());
				loadListView();
			}
		}

		@Override
		protected List<Deal_t> doInBackground(String... arg0)
		{
			List<Deal_t> results = null;

			try
			{
				exception = null;
				final SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("title").setAscending(true);
				results = client.getClient().getDealsByDealOfferId(closestBook.dealOfferId, searchOptions);
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
			}
			catch (Exception e)
			{
				exception = e;
			}

			return results;
		}
	}
	
	   public void setListViewHeightBasedOnChildren(ListView listView) {
	        ListAdapter listAdapter = listView.getAdapter(); 
	        if (listAdapter == null) {
	            // pre-condition
	            return;
	        }

	        int totalHeight = 0;
	        for (int i = 0; i < listAdapter.getCount(); i++) {
	            View listItem = listAdapter.getView(i, null, listView);
	            listItem.measure(0, 0);
	            totalHeight += listItem.getMeasuredHeight();
	        }

	        ViewGroup.LayoutParams params = listView.getLayoutParams();
	        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	        listView.setLayoutParams(params);
	        listView.setBackgroundColor(view.getResources().getColor(R.color.dark_tan));

	    }

}
