package com.talool.android.adapters;

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

import com.talool.api.thrift.AcquireStatus_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.android.R;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.TypefaceFactory;

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

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);

		holder = new DealAcquiredRow();
		holder.dealsAcquiredIcon = (TextView) row.findViewById(R.id.dealsAcquiredIcon);
		holder.dealsAcquiredTitle = (TextView) row.findViewById(R.id.dealsAcquiredTitle);
		holder.dealsAcquiredExpires = (TextView) row.findViewById(R.id.dealsAcquiredExpires);
		holder.dealsAcquiredArrow = (ImageView) row.findViewById(R.id.dealsAcquiredArrow);
		holder.dealsAcquiredGifted = (TextView) row.findViewById(R.id.dealsAcquiredGifted);

		row.setTag(holder);

		final DealAcquire_t dealAcquire = data.get(position);

		holder.dealsAcquiredIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.default_icon_color));

		holder.dealsAcquiredTitle.setText(dealAcquire.deal.getTitle());
		holder.dealsAcquiredExpires.setText(TaloolUtil.getExpirationText(dealAcquire.deal.expires));
		holder.dealsAcquiredArrow.setImageResource(R.drawable.navigation_next_item);

		if (dealAcquire.redeemed != 0)
		{
			holder.dealsAcquiredTitle.setPaintFlags(holder.dealsAcquiredTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.gray_icon));
			holder.dealsAcquiredExpires.setText(TaloolUtil.getRedeemedTextNoCode(dealAcquire.redeemed));

		}else if(TaloolUtil.isExpired(dealAcquire.deal.expires)){
            holder.dealsAcquiredTitle.setPaintFlags(holder.dealsAcquiredTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.gray_icon));
            holder.dealsAcquiredExpires.setText(TaloolUtil.getExpiredText(dealAcquire.deal.expires));

        }

		if (dealAcquire.getStatus() == AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE)
		{
			holder.dealsAcquiredTitle.setPaintFlags(holder.dealsAcquiredTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			holder.dealsAcquiredIcon.setTextColor(this.context.getResources().getColor(R.color.gray_icon));
			holder.dealsAcquiredExpires.setText(TaloolUtil.getGiftedText(dealAcquire.updated));
		}

		if (dealAcquire.getGiftDetail() != null)
		{
			holder.dealsAcquiredGifted.setVisibility(View.VISIBLE);
			final StringBuilder sb = new StringBuilder();

			if (dealAcquire.getStatus() == AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE)
			{
				sb.append("Gifted to ").append(dealAcquire.getGiftDetail().getToName());
			}

			else if (dealAcquire.getStatus() == AcquireStatus_t.ACCEPTED_CUSTOMER_SHARE ||
					dealAcquire.getStatus() == AcquireStatus_t.REDEEMED)
			{
				sb.append("Gifted from ").append(dealAcquire.getGiftDetail().getFromName());
			}
			else if (dealAcquire.getStatus() == AcquireStatus_t.REJECTED_CUSTOMER_SHARE)
			{
				sb.append("Your gift to ").append(dealAcquire.getGiftDetail().getToName()).append(" was rejected");
			}

			holder.dealsAcquiredGifted.setText(sb.toString());

		}

		return row;
	}
}
