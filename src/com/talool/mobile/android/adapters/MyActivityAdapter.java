package com.talool.mobile.android.adapters;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.talool.api.thrift.Activity_t;
import com.talool.mobile.android.R;

/**
 * 
 * @author clintz
 * 
 */
public class MyActivityAdapter extends ArrayAdapter<Activity_t>
{
	Context context;
	int layoutResourceId;
	List<Activity_t> data = null;

	private static class ActivityHolder
	{
		TextView activityTitle;
		TextView activitySubtitle;
		TextView activityDate;
		ImageView myDealsMerchantArrow;
	}

	public MyActivityAdapter(final Context context, final int layoutResourceId, final List<Activity_t> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent)
	{
		View row = convertView;
		ActivityHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ActivityHolder();
			holder.activityTitle = (TextView) row.findViewById(R.id.activityTitle);
			holder.activitySubtitle = (TextView) row.findViewById(R.id.activitySubtitle);
			holder.activityDate = (TextView) row.findViewById(R.id.activityDate);
			holder.myDealsMerchantArrow = (ImageView) row.findViewById(R.id.myDealsMerchantArrow);

			row.setTag(holder);
		}
		else
		{
			holder = (ActivityHolder) row.getTag();
		}

		final Activity_t activity = data.get(position);
		holder.activityTitle.setText(activity.getTitle());
		holder.activitySubtitle.setText(activity.getSubtitle());
		holder.activityDate.setText(new Date(activity.getActivityDate()).toString());
		holder.myDealsMerchantArrow.setImageResource(R.drawable.navigation_next_item);

		return row;
	}

}
