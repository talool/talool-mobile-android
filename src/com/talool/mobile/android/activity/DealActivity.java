package com.talool.mobile.android.activity;

import java.util.Date;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.loopj.android.image.SmartImageView;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.tasks.DealAcceptanceTask;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

public class DealActivity extends Activity
{
	private static ThriftHelper client;
	private DealAcquire_t deal;
	private Merchant_t merchant;
	private SmartImageView dealMerchantImage;
	private SmartImageView logoImageView;
	private SmartImageView dealOfferCreatorImage;
	private TextView dealAddressText;
	private TextView dealSummaryText;
	private TextView dealValidText;
	private TextView dealExpirationText;
	private LinearLayout dealActivityButtonLayout;
	private String redemptionCode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_activity_layout);
		createThriftClient();
		logoImageView = (SmartImageView) findViewById(R.id.dealLogoImage);
		dealMerchantImage = (SmartImageView) findViewById(R.id.dealMerchantImage);
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
		if (deal.redeemed == 0)
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
			redemptionCodeTextView.setTextColor(getResources().getColor(R.color.orange));
			redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
			dealActivityButtonLayout.addView(redemptionCodeTextView);
			dealActivityButtonLayout.setGravity(Gravity.CENTER);

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
				redemptionCodeTextView.setTextColor(getResources().getColor(R.color.orange));
				redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
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
}
