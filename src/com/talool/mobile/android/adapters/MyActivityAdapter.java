package com.talool.mobile.android.adapters;

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
import com.talool.mobile.android.util.ApiUtil;
import com.talool.mobile.android.util.Constants;
import com.talool.mobile.android.util.SafeSimpleDateFormat;
import com.talool.mobile.android.util.TypefaceFactory;

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

		final Activity_t activity = data.get(position);

		if (row == null)
		{
			final LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ActivityHolder();
		}

		else
		{
			holder = (ActivityHolder) row.getTag();
		}

		holder.activityTitle = (TextView) row.findViewById(R.id.activityTitle);
		holder.activitySubtitle = (TextView) row.findViewById(R.id.activitySubtitle);
		holder.activityDate = (TextView) row.findViewById(R.id.activityDate);

		final ImageView imageArrow = (ImageView) row.findViewById(R.id.activityLinkArrow);

		final TextView txt = (TextView) row.findViewById(R.id.iconView);
		txt.setTypeface(TypefaceFactory.get().getFontAwesome());

		if (activity.getActivityLink() != null && ApiUtil.isClickableActivityLink(activity))
		{

			imageArrow.setVisibility(View.VISIBLE);
			if (activity.actionTaken)
			{
				txt.setTextColor(row.getResources().getColor(R.color.color_grey));
			}
			else
			{
				txt.setTextColor(row.getResources().getColor(R.color.color_teal));
			}

		}
		else
		{
			imageArrow.setVisibility(View.GONE);
			txt.setTextColor(row.getResources().getColor(R.color.color_grey));
		}

		txt.setText(ApiUtil.getIcon(activity));

		row.setTag(holder);

		holder.activityTitle.setText(activity.getTitle());
		holder.activitySubtitle.setText(activity.getSubtitle());
		holder.activityDate.setText(new SafeSimpleDateFormat(Constants.GENERAL_DATE_TIME_FORMAT).format(activity.getActivityDate()));

		return row;
	}
}
