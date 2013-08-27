package com.talool.mobile.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.loopj.android.image.SmartImageView;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Gift_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.LoginActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.tasks.GiftAcceptanceTask;
import com.talool.mobile.android.util.TaloolSmartImageView;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz
 * 
 * @TODO Wire up proper exception handling/logging
 */
public class GiftActivity extends Activity
{
	public static String GIFT_ID_PARAM = "giftId";
	public static String ACTIVITY_OBJ_PARAM = "activityObj";

	private ThriftHelper client;
	private String giftId;
	private Activity_t activity;
	private TaloolSmartImageView dealImageView;
	private SmartImageView logoImageView;
	private SmartImageView dealCreatorImageView;
	private TextView fromFriend;
	private DialogFragment df;
	private Exception exception;
	private String errorMessage;
	private LinearLayout linearLayout;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gift_activity);
		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}

		byte[] activityObjBytes = (byte[]) getIntent().getSerializableExtra(ACTIVITY_OBJ_PARAM);
		activity = new Activity_t();
		try
		{
			ThriftUtil.deserialize(activityObjBytes, activity);
		}
		catch (TException e)
		{
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}

		giftId = activity.getActivityLink().getLinkElement();
		linearLayout = (LinearLayout) findViewById(R.id.giftLinearLayout);
		final GiftActivityTask dealsTask = new GiftActivityTask();
		dealsTask.execute(new String[] {});

		final TextView thumbsDownIcon = (TextView) findViewById(R.id.thumbsUpIcon);
		thumbsDownIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		final TextView thumbsUpIcon = (TextView) findViewById(R.id.thumbsDownIcon);
		thumbsUpIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		fromFriend = (TextView) findViewById(R.id.fromFriend);

	}

	private void setDealCreatorImageView(final DealOffer_t dealOffer)
	{
		dealCreatorImageView = (SmartImageView) findViewById(R.id.dealCreatorLogo);
		dealCreatorImageView.setImageUrl(dealOffer.getImageUrl());
	}

	public void acceptGiftClick(final View view)
	{
		// accept the gift and redirect to "My Deals"
		final GiftAcceptanceTask task = new GiftAcceptanceTask(client, activity, true, view.getContext())
		{
			@Override
			protected void onPostExecute(DealAcquire_t result)
			{
				finish();
			}

		};

		task.execute(new String[] {});
	}

	public void rejectGiftClick(final View view)
	{
		// accept the gift and redirect to "My Deals"
		final GiftAcceptanceTask task = new GiftAcceptanceTask(client, activity, false, view.getContext())
		{
			@Override
			protected void onPostExecute(DealAcquire_t result)
			{
				finish();
			}

		};

		task.execute(new String[] {});
	}

	private class GiftActivityTask extends AsyncTask<String, Void, Gift_t>
	{

		@Override
		protected void onPreExecute() {
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}
		
		@Override
		protected void onPostExecute(final Gift_t gift)
		{
			if(gift == null)
			{
				popupErrorMessage(exception, errorMessage);
				return;
			}
			linearLayout.setVisibility(View.VISIBLE);
			
			final DealOffer_t dealOffer = DealOfferCache.get().getDealOffer(gift.getDeal().getDealOfferId());
			if (dealOffer == null)
			{
				final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, gift.getDeal().getDealOfferId(), getApplicationContext())
				{

					@Override
					protected void onPostExecute(final DealOffer_t dealOffer)
					{
						if (dealOffer != null)
						{
							setDealCreatorImageView(dealOffer);
							// make sure we cache the dealOffer
							DealOfferCache.get().setDealOffer(dealOffer);
						}
					}

				};

				dealOfferFetchTask.execute(new String[] {});
			}
			else
			{
				setDealCreatorImageView(dealOffer);
			}

			final TextView summary = (TextView) findViewById(R.id.summary);
			final TextView details = (TextView) findViewById(R.id.details);

			setTitle("A Gift to " + gift.getDeal().getMerchant().getName());

			String fn = gift.getFromCustomer().firstName;
			if (fn.length() > 10)
			{
				fn = (new StringBuilder(fn.substring(0, 7)).append("...")).toString();
			}
			StringBuilder fromFriendLabel = new StringBuilder("From\n");
			fromFriend.setText(fromFriendLabel.append(fn).toString());

			summary.setText(gift.getDeal().getSummary());
			summary.setTypeface(TypefaceFactory.get().getMarkerFeltWide());
			details.setText(gift.getDeal().getDetails());
			details.setTypeface(TypefaceFactory.get().getMarkerFelt());

			dealImageView = (TaloolSmartImageView) findViewById(R.id.dealImage);
			dealImageView.setImageUrl(gift.getDeal().getImageUrl());

			logoImageView = (SmartImageView) findViewById(R.id.merchantLogo);
			logoImageView.setImageUrl(gift.getDeal().getMerchant().getLocations().get(0).getLogoUrl());

			final TextView address1 = (TextView) findViewById(R.id.address1);

			final MerchantLocation_t location = gift.getDeal().getMerchant().getLocations().get(0);

			final TextView expires = (TextView) findViewById(R.id.expires);
			expires.setText(TaloolUtil.getExpirationText(gift.getDeal().getExpires()));

			StringBuilder sb = new StringBuilder(location.address.address1);
			if (location.address.address2 != null)
			{
				sb.append("\n").append(location.address.address2);
			}
			sb.append("\n")
					.append(location.address.city)
					.append(", ")
					.append(location.address.stateProvinceCounty)
					.append(" ")
					.append(location.address.zip);

			address1.setText(sb.toString());
			
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

		}

		@Override
		protected Gift_t doInBackground(final String... arg0)
		{
			Gift_t gift = null;

			try
			{
				client.setAccessToken(TaloolUser.get().getAccessToken());
				gift = client.getClient().getGift(giftId);

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
			return gift;
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
		String title = getResources().getString(R.string.error_loading_gifts);
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
        
        
	}

	private static String getCityStateZip(final MerchantLocation_t location)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(location.getAddress().getCity()).append(", ").append(location.getAddress().getStateProvinceCounty());
		sb.append(" ").append(location.getAddress().getZip());

		return sb.toString();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
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
}
