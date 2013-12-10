package com.talool.android.adapters;

import java.util.List;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.Log;
import com.talool.android.R;
import com.talool.android.util.ImageDownloader;
import com.talool.android.util.TaloolSmartImageView;
import com.talool.android.util.TaloolUtil;
import com.talool.api.thrift.CoreConstants;
import com.talool.api.thrift.DealOfferGeoSummary_t;

public class DiscoverDealsAdapter extends ArrayAdapter<DealOfferGeoSummary_t> {

	Context context;
	int layoutResourceId;
	List<DealOfferGeoSummary_t> data = null;

	public DiscoverDealsAdapter(Context context, int layoutResourceId, List<DealOfferGeoSummary_t> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		DiscoverDealsRow holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DiscoverDealsRow();
			holder.dealImage = (TaloolSmartImageView) row.findViewById(R.id.discoverDealsImage);
			holder.dealLayout = (LinearLayout) row.findViewById(R.id.discoverDealsLinearLayout);
			holder.dealTitle = (TextView) row.findViewById(R.id.discoverDealsTitle);
			holder.dealSubtitle = (TextView) row.findViewById(R.id.discoverDealsSubtitle);
			holder.dealPrice = (TextView) row.findViewById(R.id.discoverDealsPrice);
			holder.dealSummary = (TextView) row.findViewById(R.id.discoverDealsSummary);

			row.setTag(holder);
		}
		else
		{
			holder = (DiscoverDealsRow) row.getTag();
		}

		DealOfferGeoSummary_t dealOffer = data.get(position);				
		
		holder.dealTitle.setText(dealOffer.dealOffer.title);
		holder.dealPrice.setText(TaloolUtil.moneyFormattedString(dealOffer.dealOffer.price));
		holder.dealSubtitle.setText(dealOffer.dealOffer.summary);
		holder.dealImage.setImageUrl(dealOffer.dealOffer.dealOfferMerchantLogo);
		holder.dealLayout.setBackgroundResource(0);
		holder.dealSummary.setText(getSummaryText(dealOffer));

		if(dealOffer.dealOffer.dealOfferBackgroundImage != null)
		{
			ImageDownloader imageDownloader = new ImageDownloader(holder.dealLayout);
			imageDownloader.execute(dealOffer.dealOffer.dealOfferBackgroundImage);
		}
		else
		{
			holder.dealLayout.setBackgroundResource(R.drawable.deal_offer_bg);
		}
		Log.i(dealOffer.dealOffer.dealOfferBackgroundImage);

		return row;
	}

	public String getSummaryText(DealOfferGeoSummary_t dealOffer)
	{
		String summaryString = String.valueOf(dealOffer.getLongMetrics().get(CoreConstants.METRIC_TOTAL_DEALS));
		summaryString = summaryString + " deals from " + String.valueOf(dealOffer.getLongMetrics().get(CoreConstants.METRIC_TOTAL_MERCHANTS)) +" merchants";
		return summaryString;
	}
}