package com.talool.android.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.talool.android.R;
import com.talool.android.util.ApiUtil;
import com.talool.android.util.TypefaceFactory;
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
			holder.dealImage = (SmartImageView) row.findViewById(R.id.discoverDealsImage);
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

//		holder.dealIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
//		holder.dealIcon.setText(ApiUtil.getIcon(dealOffer.merchant.category));
//		holder.dealIcon.setTextColor(row.getResources().getColor(ApiUtil.getIconColor(dealOffer.merchant.category)));
//		
//		holder.dealTitle.setText(dealOffer.getTitle());
//		holder.dealMerchant.setText(dealOffer.merchant.name);


		return row;
	}
}