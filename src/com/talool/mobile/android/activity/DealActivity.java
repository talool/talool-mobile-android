package com.talool.mobile.android.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.MyDealsFragment;
import com.talool.mobile.android.R;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.tasks.DealAcceptanceTask;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.util.TaloolSmartImageView;
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
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(),e),true)                                              
					.build()
					);
		}

	}

	private void checkDealRedeemed()
	{

		if(deal.status == AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE)
		{
			dealActivityButtonLayout.removeAllViewsInLayout();
			dealActivityButtonLayout.setBackground(null);
			TextView redemptionCodeTextView = new TextView(getBaseContext());
			redemptionCodeTextView.setText("Gifted on " + new Date(deal.getUpdated()).toString());
			redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
			redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
			redemptionCodeTextView.setPadding(30, 0, 30, 0);
			dealActivityButtonLayout.addView(redemptionCodeTextView);
			dealActivityButtonLayout.setGravity(Gravity.CENTER);
			return;
		}
		else if(deal.redeemed == 0)
		{
			return;
		}
		else
		{
			dealActivityButtonLayout.removeAllViewsInLayout();
			dealActivityButtonLayout.setBackground(null);
			TextView redemptionCodeTextView = new TextView(getBaseContext());
			redemptionCodeTextView.setText("Redeemed on " + new Date(deal.redeemed).toString());
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
		final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, deal.getDeal().getDealOfferId())
		{

			@Override
			protected void onPostExecute(final DealOffer_t dealOffer)
			{
				setDealCreatorImageView(dealOffer);
				// make sure we cache the dealOffer
				DealOfferCache.get().setDealOffer(dealOffer);
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

	public void onUseDealNowClick(View view)
	{
		DealAcceptanceTask dealAcceptanceTask = new DealAcceptanceTask(client, deal.dealAcquireId)
		{

			@Override
			protected void onPostExecute(String result)
			{
				redemptionCode = result;
				dealActivityButtonLayout.removeAllViewsInLayout();
				dealActivityButtonLayout.setBackground(null);
				TextView redemptionCodeTextView = new TextView(getBaseContext());
				redemptionCodeTextView.setText("Redemption code " + redemptionCode);
				redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
				redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
				redemptionCodeTextView.setPadding(30, 0, 30, 0);
				dealActivityButtonLayout.addView(redemptionCodeTextView);
				dealActivityButtonLayout.setGravity(Gravity.CENTER);
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
					.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(),e),true)                                              
					.build()
					);
		}
	}

	public void onGiftViaEmail(View view)
	{
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  ContactsContract.CommonDataKinds.Email.CONTENT_URI);  
		startActivityForResult(contactPickerIntent, 100);  
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if (resultCode == RESULT_OK) {  
			switch (requestCode) {  
			case 100:  
				Cursor emailCur = null;
				String email = "";  
				try {  
					Uri result = data.getData();  
					// get the contact id from the Uri  
					String id = result.getLastPathSegment();  
					// query for everything email  
					emailCur = getContentResolver().query( 
							ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
							null,
							ContactsContract.CommonDataKinds.Email._ID + " = ?", 
									new String[]{id}, null); 
					while (emailCur.moveToNext()) { 
						email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						name = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY));

					}
				} catch (Exception e) {  
				} finally {  
					if (emailCur != null) {  
						emailCur.close();  
					}  
					this.email = email;
				}  
				break;  
			}  
		} else {  
		}
		sendGift();
	}

	private void sendGift()
	{
		if(this.email ==  null || this.email.isEmpty())
		{
			Toast.makeText(getApplicationContext(), "Please select a contact with an email address", Toast.LENGTH_LONG).show();
		}
		else		
		{
			GiftTask giftTask = new GiftTask();
			giftTask.execute(new String[]{});
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
				.createEvent("deal_activity","selected",deal.deal.dealId,null)           
				.build()
				);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
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

    
	private class GiftTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected void onPostExecute(final String results)
		{
			if(exception == null)
			{
				dealActivityButtonLayout.removeAllViewsInLayout();
				dealActivityButtonLayout.setBackground(null);
				TextView redemptionCodeTextView = new TextView(getBaseContext());
				redemptionCodeTextView.setText("Gifted on " + new Date(deal.getUpdated()).toString());
				redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
				redemptionCodeTextView.setTypeface(TypefaceFactory.get().getMarkerFelt(), Typeface.NORMAL);
				redemptionCodeTextView.setPadding(30, 0, 30, 0);
				dealActivityButtonLayout.addView(redemptionCodeTextView);
				dealActivityButtonLayout.setGravity(Gravity.CENTER);
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
}
