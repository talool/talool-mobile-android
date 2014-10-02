package com.talool.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.talool.android.R;
import com.talool.android.util.ApiUtil;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;

import java.util.List;

public class MyDealsAdapter extends ArrayAdapter<Merchant_t>
{
	private Context context;
	private int layoutResourceId;
	private List<Merchant_t> data = null;

	public MyDealsAdapter(Context context, int layoutResourceId, List<Merchant_t> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount()
	{
		return data.size();
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		MyDealsRow holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new MyDealsRow();
			holder.myDealsMerchantIcon = (TextView) row.findViewById(R.id.iconView);
			holder.myDealsMerchantTitle = (TextView) row.findViewById(R.id.myDealsMerchantTitle);
			holder.myDealsMerchantLocation = (TextView) row.findViewById(R.id.myDealsMerchantLocation);
			holder.myDealsMerchantDistance = (TextView) row.findViewById(R.id.myDealsMerchantDistance);
			holder.myDealsMerchantArrow = (SmartImageView) row.findViewById(R.id.myDealsMerchantArrow);

			row.setTag(holder);
		}
		else
		{
			holder = (MyDealsRow) row.getTag();
		}

		Merchant_t merchant = data.get(position);
		holder.merchant = merchant;

		holder.myDealsMerchantIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		holder.myDealsMerchantIcon.setText(ApiUtil.getIcon(merchant.category));
		holder.myDealsMerchantIcon.setTextColor(row.getResources().getColor(ApiUtil.getIconColor(merchant.category)));

		holder.myDealsMerchantTitle.setText(merchant.getName());

        MerchantLocation_t closestLocation = ApiUtil.getClosestLocation(merchant);

		if (merchant.getLocations() != null && merchant.getLocations().size() > 0)
		{
			if(merchant.locations.size() > 1)
			{
				holder.myDealsMerchantLocation.setText("Multiple Locations");
			}
			else
			{
				holder.myDealsMerchantLocation.setText(closestLocation.address.address1 + ", " + closestLocation.address.city);
			}

			if (TaloolUser.get().getLocation() != null && TaloolUser.get().isRealLocation())
			{
				Location merchantLocation = new Location("Talool");
                if (merchantLocation != null && closestLocation != null && closestLocation.location != null){
                    merchantLocation.setLatitude(closestLocation.location.latitude);
                    merchantLocation.setLongitude(closestLocation.location.longitude);
                    float distance = TaloolUser.get().getLocation().distanceTo(merchantLocation);
                    distance = (float) (distance * 0.00062137);

                    holder.myDealsMerchantDistance.setText(String.valueOf(TaloolUtil.round(distance, 2)) + " miles");
                }
                else{
                    holder.myDealsMerchantDistance.setText("Unknown Location");
                }


			}
		}
		holder.myDealsMerchantArrow.setImageResource(R.drawable.navigation_next_item);

		return row;
	}
}
