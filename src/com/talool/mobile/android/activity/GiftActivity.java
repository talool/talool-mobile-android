package com.talool.mobile.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Gift_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.cache.DealOfferCache;
import com.talool.mobile.android.tasks.DealOfferFetchTask;
import com.talool.mobile.android.util.TaloolUser;
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
	private SmartImageView dealImageView;
	private SmartImageView logoImageView;
	private SmartImageView dealCreatorImageView;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gift_activity);

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		giftId = (String) getIntent().getSerializableExtra(GIFT_ID_PARAM);

		final GiftActivityTask dealsTask = new GiftActivityTask();
		dealsTask.execute(new String[] {});

		byte[] activityObjBytes = (byte[]) getIntent().getSerializableExtra(ACTIVITY_OBJ_PARAM);
		final Activity_t activity = new Activity_t();
		try
		{
			ThriftUtil.deserialize(activityObjBytes, activity);
		}
		catch (TException e)
		{
			e.printStackTrace();
		}

		final TextView thumbsDownIcon = (TextView) findViewById(R.id.thumbsUpIcon);
		thumbsDownIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		final TextView thumbsUpIcon = (TextView) findViewById(R.id.thumbsDownIcon);
		thumbsUpIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		/**
		 * if (activity.getSubtitle() != null) { final TextView activitySubTitle =
		 * (TextView) findViewById(R.id.activitySubtitle);
		 * activitySubTitle.setText(activity.getSubtitle()); }
		 **/

	}

	private void setDealCreatorImageView(final DealOffer_t dealOffer)
	{
		dealCreatorImageView = (SmartImageView) findViewById(R.id.dealCreatorLogo);
		dealCreatorImageView.setImageUrl(dealOffer.getMerchant().getLocations().get(0).getLogoUrl());
	}

	private class GiftActivityTask extends AsyncTask<String, Void, Gift_t>
	{
		@Override
		protected void onPostExecute(final Gift_t gift)
		{
			final DealOffer_t dealOffer = DealOfferCache.get().getDealOffer(gift.getDeal().getDealOfferId());
			if (dealOffer == null)
			{
				final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, gift.getDeal().getDealOfferId())
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
			else
			{
				setDealCreatorImageView(dealOffer);
			}

			final TextView summary = (TextView) findViewById(R.id.summary);
			final TextView details = (TextView) findViewById(R.id.details);

			setTitle("A Gift to " + gift.getDeal().getMerchant().getName());

			summary.setText(gift.getDeal().getSummary());

			details.setText(gift.getDeal().getDetails());

			dealImageView = (SmartImageView) findViewById(R.id.dealImage);
			dealImageView.setImageUrl(gift.getDeal().getImageUrl());

			logoImageView = (SmartImageView) findViewById(R.id.merchantLogo);
			logoImageView.setImageUrl(gift.getDeal().getMerchant().getLocations().get(0).getLogoUrl());

			final TextView address1 = (TextView) findViewById(R.id.address1);
			final TextView address2 = (TextView) findViewById(R.id.address2);
			final TextView cityStateZip = (TextView) findViewById(R.id.cityStateZip);

			final MerchantLocation_t location = gift.getDeal().getMerchant().getLocations().get(0);
			address1.setText(location.getAddress().getAddress1());
			if (location.getAddress().getAddress2() == null)
			{
				address2.setVisibility(View.GONE);
			}
			else
			{
				address2.setText(location.getAddress().getAddress2());
			}

			cityStateZip.setText(getCityStateZip(location));

		}

		@Override
		protected Gift_t doInBackground(final String... arg0)
		{
			Gift_t gift = null;

			try
			{
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				gift = client.getClient().getGift(giftId);

			}
			catch (ServiceException_t e)
			{
				e.printStackTrace();
			}
			catch (TException e)
			{
				e.printStackTrace();

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return gift;
		}
	}

	private static String getCityStateZip(final MerchantLocation_t location)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(location.getAddress().getCity()).append(", ").append(location.getAddress().getStateProvinceCounty());
		sb.append(" ").append(location.getAddress().getZip());

		return sb.toString();
	}

}
