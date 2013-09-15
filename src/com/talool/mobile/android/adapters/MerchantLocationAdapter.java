package com.talool.mobile.android.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.talool.api.thrift.MerchantLocation_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TypefaceFactory;

public class MerchantLocationAdapter extends ArrayAdapter<MerchantLocation_t>{
	Context context;
	int layoutResourceId;
	List<MerchantLocation_t> data = null;

	public MerchantLocationAdapter(Context context, int layoutResourceId, List<MerchantLocation_t> data)
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
		LocationRow holder = null;

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);

		holder = new LocationRow();
		holder.locationIcon = (TextView) row.findViewById(R.id.locationIcon);
		holder.locationTitle = (TextView) row.findViewById(R.id.locationTitle);
		holder.locationAddress = (TextView) row.findViewById(R.id.locationAddress);

		row.setTag(holder);

		MerchantLocation_t location = data.get(position);

		holder.locationIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		holder.locationIcon.setTextColor(this.context.getResources().getColor(R.color.teal));

		holder.locationTitle.setText(location.address.address1);
		holder.locationAddress.setText(location.address.city + ", " + location.address.stateProvinceCounty + ", " + location.address.zip);

		return row;
	}
}
