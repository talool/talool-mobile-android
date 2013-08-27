package com.talool.mobile.android.activity;

import java.util.Date;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.loopj.android.image.SmartImageView;
import com.talool.api.thrift.AcquireStatus_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.LoginActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.tasks.DealRedemptionTask;
import com.talool.mobile.android.util.AlertMessage;
import com.talool.mobile.android.util.AndroidUtils;
import com.talool.mobile.android.util.Constants;
import com.talool.mobile.android.util.SafeSimpleDateFormat;
import com.talool.mobile.android.util.TaloolSmartImageView;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

public class DealActivity extends Activity
{
	private static ThriftHelper client;
	private DealAcquire_t deal;
	private Merchant_t merchant;
	private TaloolSmartImageView dealMerchantImage;
	private SmartImageView logoImageView;
	private SmartImageView dealOfferCreatorImage;
	private TextView dealAddressText;
	private TextView dealSummaryText;
	private TextView dealValidText;
	private TextView dealExpirationText;
	private LinearLayout dealActivityButtonLayout;
	private String redemptionCode;
	private String email;
	private Exception exception;
	private String name;
	private DialogFragment df;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_activity_layout);
		createThriftClient();
		logoImageView = (SmartImageView) findViewById(R.id.dealLogoImage);
		dealMerchantImage = (TaloolSmartImageView) findViewById(R.id.dealMerchantImage);
		dealAddressText = (TextView) findViewById(R.id.dealAddressText);
		dealValidText = (TextView) findViewById(R.id.dealValidText);
		dealSummaryText = (TextView) findViewById(R.id.dealSummaryText);
		dealOfferCreatorImage = (SmartImageView) findViewById(R.id.dealActivityCreatorImage);
		dealExpirationText = (TextView) findViewById(R.id.dealActivityExpires);
		dealActivityButtonLayout = (LinearLayout) findViewById(R.id.dealActivityButtonLayout);
		redemptionCode = null;
		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
		try
		{
			byte[] dealBytes = (byte[]) getIntent().getSerializableExtra("deal");
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");

			deal = new DealAcquire_t();
			merchant = new Merchant_t();
			ThriftUtil.deserialize(dealBytes, deal);
			ThriftUtil.deserialize(merchantBytes, merchant);

			setText();
			loadImages();
			checkDealRedeemed();
			setDealCreatorImage();

		}
		catch (TException e)
		{
			e.printStackTrace();
			EasyTracker easyTracker = EasyTracker.getInstance(this);

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}

	}

	private void checkDealRedeemed()
	{

		if (deal.status == AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE)
		{
			dealActivityButtonLayout.removeAllViewsInLayout();
			dealActivityButtonLayout.setBackgroundDrawable(null);
			TextView redemptionCodeTextView = new TextView(DealActivity.this);
			redemptionCodeTextView.setText("Gifted on " + new Date(deal.getUpdated()).toString());
			redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
			redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
			redemptionCodeTextView.setPadding(30, 0, 30, 0);
			dealActivityButtonLayout.addView(redemptionCodeTextView);
			dealActivityButtonLayout.setGravity(Gravity.CENTER);
			return;
		}
		else if (deal.redeemed == 0)
		{
			return;
		}
		else
		{
			dealActivityButtonLayout.removeAllViewsInLayout();
			dealActivityButtonLayout.setBackgroundDrawable(null);
			TextView redemptionCodeTextView = new TextView(DealActivity.this);
			redemptionCodeTextView.setText("Redeemed on " +
					new SafeSimpleDateFormat(Constants.GENERAL_DATE_TIME_FORMAT).format(deal.redeemed));

			redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
			redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
			redemptionCodeTextView.setPadding(30, 0, 30, 0);
			dealActivityButtonLayout.addView(redemptionCodeTextView);
			dealActivityButtonLayout.setGravity(Gravity.CENTER);
			return;
		}

	}

	private void setDealCreatorImage()
	{
		final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, deal.getDeal().getDealOfferId(), DealActivity.this)
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

	private void setDealCreatorImageView(DealOffer_t dealOffer)
	{
		dealOfferCreatorImage.setImageUrl(dealOffer.getImageUrl());
	}

	private void loadImages()
	{
		if (deal.deal.imageUrl != null)
		{
			dealMerchantImage.setImageUrl(deal.deal.imageUrl);
		}

		if (merchant.locations.get(0).logoUrl != null)
		{
			logoImageView.setImageUrl(merchant.locations.get(0).logoUrl);
		}

	}

	private void setText()
	{
		setAddressText();
		dealSummaryText.setText(deal.deal.summary);
		dealSummaryText.setTypeface(TypefaceFactory.get().getMarkerFeltWide());
		dealValidText.setText(deal.deal.details);
		dealValidText.setTypeface(TypefaceFactory.get().getMarkerFelt());
		dealExpirationText.setText(TaloolUtil.getExpirationText(deal.deal.expires));
		setTitle(merchant.name);

		final TextView useDealIcon = (TextView) findViewById(R.id.useDealIcon);
		if (useDealIcon != null)
		{
			useDealIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		}

		final TextView giftIcon = (TextView) findViewById(R.id.giftIcon);
		if (giftIcon != null)
		{
			giftIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		}

	}

	public void onUseDealNowClick(final View view)
	{
		DealRedemptionTask dealAcceptanceTask = new DealRedemptionTask(client, deal.dealAcquireId, view.getContext())
		{

			@Override
			protected void onPostExecute(String result)
			{
				redemptionCode = result;
				dealActivityButtonLayout.removeAllViewsInLayout();
				dealActivityButtonLayout.setBackgroundDrawable(null);
				TextView redemptionCodeTextView = new TextView(DealActivity.this);
				redemptionCodeTextView.setText("Redemption code " + redemptionCode);
				redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
				redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
				redemptionCodeTextView.setPadding(30, 0, 30, 0);
				dealActivityButtonLayout.addView(redemptionCodeTextView);
				dealActivityButtonLayout.setGravity(Gravity.CENTER);

				EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());
				easyTracker.send(MapBuilder
						.createEvent("redeem", "selected", deal.dealAcquireId, null)
						.build());
			}

		};
		dealAcceptanceTask.execute(new String[] {});
	}

	private void setAddressText()
	{
		MerchantLocation_t location = merchant.locations.get(0);
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

		dealAddressText.setText(sb.toString());
	}

	private void createThriftClient()
	{
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
	}

	public void onGiftViaEmail(View view)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, 100);
		}
		catch (Exception e)
		{
			AlertMessage alertMessage = new AlertMessage("Gift Via Email Picker", "Error on Picker ", e);
			AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Cursor cursor = null;

		try
		{
			if (resultCode == RESULT_OK)
			{
				switch (requestCode)
				{
					case 100:

						Uri result = data.getData();

						// get the contact id from the Uri
						String id = result.getLastPathSegment();

						// query for everything email
	                    cursor = getContentResolver().query(Email.CONTENT_URI,  null, Email.CONTACT_ID + "=?", new String[] { id }, null);
	                    
						int emailIdx = cursor.getColumnIndex(Email.DATA);

						// let's just get the first email
						if (cursor.moveToFirst())
						{
							email = cursor.getString(emailIdx);
						}

						if (cursor != null)
						{
							cursor.close();
						}
						sendGift();
						break;
				}
			}

		}
		catch (Exception e)
		{
			if (cursor != null)
			{
				cursor.close();
			}
			AlertMessage alertMessage = new AlertMessage("Gift Via Email Picker Results", "Error on Picker Results ", e);
			AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
		}
	}

	private void sendGift()
	{

		if (this.email == null || this.email.isEmpty())
		{
			Toast.makeText(DealActivity.this, "Please select a contact with an email address", Toast.LENGTH_LONG).show();
		}
		else
		{
			GiftTask giftTask = new GiftTask();
			giftTask.execute(new String[] {});
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.activityStart(this); // Add this method.

		// MapBuilder.createEvent().build() returns a Map of event fields and values
		// that are set and sent with the hit.
		easyTracker.send(MapBuilder
				.createEvent("deal_activity", "selected", deal.deal.dealId, null)
				.build()
				);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean ret;
		if (item.getItemId() == R.id.menu_settings)
		{
			Intent intent = new Intent(DealActivity.this, SettingsActivity.class);
			startActivity(intent);
			ret = true;
		}
		else
		{
			ret = super.onOptionsItemSelected(item);
		}
		return ret;
	}

	private class GiftTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected void onPostExecute(final String results)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}
			try
			{
				if (exception == null)
				{
					dealActivityButtonLayout.removeAllViewsInLayout();
					dealActivityButtonLayout.setBackgroundDrawable(null);
					TextView redemptionCodeTextView = new TextView(DealActivity.this);
					redemptionCodeTextView.setText("Gifted on " + new Date());
					redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
					redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
					redemptionCodeTextView.setPadding(30, 0, 30, 0);
					dealActivityButtonLayout.addView(redemptionCodeTextView);
					dealActivityButtonLayout.setGravity(Gravity.CENTER);

					EasyTracker easyTracker = EasyTracker.getInstance(DealActivity.this);
					easyTracker.send(MapBuilder
							.createEvent("gift", "selected", deal.dealAcquireId, null)
							.build());
				}
				else
				{
					AlertMessage alertMessage = new AlertMessage("An exception has occured", "Please try again", exception);
					AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
				}
			}
			catch (Exception e)
			{
				AlertMessage alertMessage = new AlertMessage("An exception has occured", "Please try again", e);
				AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
			}
		}

		@Override
		protected String doInBackground(String... arg0)
		{
			String results = null;

			try
			{
				exception = null;
				results = client.getClient().giftToEmail(deal.dealAcquireId, email, name);
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
	protected void onResume()
	{
		super.onResume();
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
	}

}
