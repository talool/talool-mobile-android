package com.talool.mobile.android.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.talool.api.thrift.DealAcquire_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUtil;
import com.talool.mobile.android.util.TypefaceFactory;

public class DealsAcquiredAdapter extends ArrayAdapter<DealAcquire_t>
{
	Context context;
	int layoutResourceId;
	List<DealAcquire_t> data = null;

	public DealsAcquiredAdapter(Context context, int layoutResourceId, List<DealAcquire_t> data)
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
		DealAcquiredRow holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DealAcquiredRow();
			holder.dealsAcquiredIcon = (TextView) row.findViewById(R.id.dealsAcquiredIcon);
			holder.dealsAcquiredTitle = (TextView) row.findViewById(R.id.dealsAcquiredTitle);
			holder.dealsAcquiredExpires = (TextView) row.findViewById(R.id.dealsAcquiredExpires);
			holder.dealsAcquiredArrow = (ImageView) row.findViewById(R.id.dealsAcquiredArrow);

			row.setTag(holder);
		}
		else
		{
			holder = (DealAcquiredRow) row.getTag();
		}

		DealAcquire_t dealAcquire = data.get(position);

		holder.dealsAcquiredIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.teal));

		holder.dealsAcquiredTitle.setText(dealAcquire.deal.getTitle());
		holder.dealsAcquiredExpires.setText(TaloolUtil.getExpirationText(dealAcquire.deal.expires));
		holder.dealsAcquiredArrow.setImageResource(R.drawable.navigation_next_item);

		if (dealAcquire.redeemed != 0)
		{
			holder.dealsAcquiredTitle.setPaintFlags(holder.dealsAcquiredTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.gray_icon));
		}

		return row;
	}
}
