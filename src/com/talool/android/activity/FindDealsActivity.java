package com.talool.android.activity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.R;
import com.talool.android.adapters.FindDealsAdapter;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.dialog.DialogFactory.DialogClickListener;
import com.talool.android.util.Constants;
import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.SafeSimpleDecimalFormat;
import com.talool.android.util.TaloolSmartImageView;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Deal_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.thrift.util.ThriftUtil;

public class FindDealsActivity extends TaloolActivity implements DialogClickListener
{
	private List<Deal_t> dealOffers;
	private ListView dealOffersListView;
	private LinearLayout purchaseClickLayout;
	private FindDealsAdapter dealOffersAdapter;
	private TaloolSmartImageView bookImage;
	private TextView bookDescription;
	private DealOffer_t closestBook;
	private LinearLayout listViewLinearLayout;
	private String accessCode;
	private Button loadDealsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.find_deals_activity_layout);
		bookImage = (TaloolSmartImageView) findViewById(R.id.bookImageView);
		dealOffersListView = (ListView) findViewById(R.id.dealOffersListView);
		bookDescription = (TextView) findViewById(R.id.dealOfferDescription);
		final EditText accessCodeEditText = (EditText) findViewById(R.id.accessCode);
		ClipDrawable accessCode_bg = (ClipDrawable) accessCodeEditText.getBackground();
		accessCode_bg.setLevel(1500);


		try {
			byte[] dealOfferBytes = (byte[]) getIntent().getSerializableExtra("dealOffer");
			closestBook = new DealOffer_t();
			ThriftUtil.deserialize(dealOfferBytes, closestBook);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		loadDealsButton = (Button) findViewById(R.id.loadDealsButton);
		loadDealsButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				EditText editText = (EditText) findViewById(R.id.accessCode);
				accessCode = editText.getText().toString();
				if (accessCode == null || accessCode == "" || accessCode.isEmpty())
				{
					popupErrorMessage("Access Code Must Not Be Empty");
				}
				else
				{				

					RedeemBook redeemBookTask = new RedeemBook()
					{
						@Override
						protected void onPostExecute(Void results)
						{
							super.onPostExecute(results);
							handleRedeemBookResponse(results);
						}
					};
					redeemBookTask.execute(new Void[] {});
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(accessCodeEditText.getWindowToken(), 0);
				}
			}
		});
		listViewLinearLayout = (LinearLayout) findViewById(R.id.listViewLinearLayout);
		listViewLinearLayout.setVisibility(View.INVISIBLE);

		purchaseClickLayout = (LinearLayout) findViewById(R.id.purchaseClickLayout);

		purchaseClickLayout.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View view)
			{
				final Intent myIntent = new Intent(view.getContext(), PaymentActivity.class);
				myIntent.putExtra("dealOffer", ThriftUtil.serialize(closestBook));
				startActivity(myIntent);
			}
		});

		purchaseClickLayout.setVisibility(View.GONE);
	}

	protected void handleRedeemBookResponse(Void results)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		if (exception != null)
		{
			if (exception instanceof ServiceException_t)
			{
				EasyTracker easyTracker = EasyTracker.getInstance(this);
				easyTracker.send(MapBuilder
						.createEvent("redeemBook", "failure", ((ServiceException_t) exception).getErrorDesc(), null)
						.build());

				popupErrorMessage(ErrorMessageCache.getMessage(((ServiceException_t) exception).getErrorCode()));

			}
			else
			{
				EasyTracker easyTracker = EasyTracker.getInstance(this);

				easyTracker
				.send(MapBuilder
						.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception),
								true)
								.build()
						);
				popupErrorMessage(exception.getMessage());

			}
		}
		else
		{
			EasyTracker easyTracker = EasyTracker.getInstance(this);
			easyTracker.send(MapBuilder
					.createEvent("redeem_book", "selected", closestBook.dealOfferId, null)
					.build()
					);

			String title = getResources().getString(R.string.alert_new_deals_title);
			String message = getResources().getString(R.string.alert_new_deals_message);
			df = DialogFactory.getConfirmDialog(title, message, FindDealsActivity.this);
			df.show(getFragmentManager(), "dialog");

		}
	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}

		if (TaloolUser.get().getLocation() == null)
		{
			Intent intent = new Intent(this, LocationSelectActivity.class);
			startActivity(intent);
		}
		else
		{
			reloadData();
		}

	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.set(Fields.SCREEN_NAME, "Find Deals");
		easyTracker.send(MapBuilder.createAppView().build());
	}

	@Override
	public void onStop()
	{
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
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}
	}

	private void reloadData()
	{
		loadBookDetails();
		loadBookDeals();
	}

	private void loadBookDetails()
	{
		if (closestBook.imageUrl != null)
		{
			bookImage.setImageUrl(closestBook.imageUrl);
		}

		if (closestBook.summary != null)
		{
			bookDescription.setText(closestBook.summary);
		}
	}

	private void loadBookDeals()
	{
		FindDealsTask dealsTask = new FindDealsTask();
		dealsTask.execute(new String[] {});
	}

	private int getNumberOfMerchants()
	{
		int numberOfMerchants = 0;
		Map<String, String> map = new HashMap<String, String>();
		for (Deal_t deal : dealOffers)
		{
			if (map.get(deal.merchant.merchantId) == null)
			{
				map.put(deal.merchant.merchantId, deal.dealOfferId);
			}
		}
		numberOfMerchants = map.size();

		return numberOfMerchants;
	}

	private void loadListView(int numMerchants)
	{
		FindDealsAdapter adapter = new FindDealsAdapter(this,
				R.layout.find_deal_row, dealOffers);
		dealOffersAdapter = adapter;
		dealOffersListView.setAdapter(dealOffersAdapter);
		setListViewHeightBasedOnChildren(dealOffersListView);
		listViewLinearLayout.setVisibility(View.VISIBLE);

		Date now = new Date();
		if (closestBook.expires > now.getTime())
		{
			purchaseClickLayout.setVisibility(View.VISIBLE);
			final TextView buyNowView = (TextView) findViewById(R.id.buy_now_text);
			buyNowView.setText(getBuyNowText());
		}
		else
		{
			purchaseClickLayout.setVisibility(View.GONE);
		}

		final TextView textView = (TextView) findViewById(R.id.summaryText);
		textView.setText(dealOffers.size() + " Deals from " + numMerchants + " Merchants");
	}

	private String getBuyNowText()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.find_deal_buy_now)).append(" ").
		append(new SafeSimpleDecimalFormat(Constants.FORMAT_DECIMAL_MONEY).format(closestBook.getPrice()));
		return sb.toString();
	}

	private class FindDealsTask extends AsyncTask<String, Void, List<Deal_t>>
	{

		@Override
		protected void onPreExecute()
		{
			if (dealOffers == null || dealOffers.isEmpty())
			{
				df = DialogFactory.getProgressDialog();
				df.show(getFragmentManager(), "dialog");
			}
		}

		@Override
		protected void onPostExecute(final List<Deal_t> results)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
			if (exception != null)
			{
				popupErrorMessage(exception.getMessage());
			}
			else
			{
				dealOffers = results;
				loadListView(getNumberOfMerchants());
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
			catch (Exception e)
			{
				exception = e;
			}

			return results;
		}
	}

	private void redirectToMyDeals()
	{
		//TODO CZ figure out what to do here
		//		ActionBar bar = this.getActionBar();
		//		bar.setSelectedNavigationItem(0);
		super.onBackPressed();   

	}

	private class RedeemBook extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... arg0)
		{
			try
			{
				exception = null;
				client.getClient().activateCode(closestBook.dealOfferId, accessCode);
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
			return null;
		}
	}

	public void setListViewHeightBasedOnChildren(ListView listView)
	{
		ListAdapter listAdapter = listView.getAdapter();		
		int totalHeight = 0;

		if (listAdapter == null)
		{
			// pre-condition
			return;
		}
		for (int i = 0; i < listAdapter.getCount(); i++)
		{
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.setBackgroundColor(this.getResources().getColor(R.color.dark_tan));

	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
		redirectToMyDeals();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog)
	{
	}

}
