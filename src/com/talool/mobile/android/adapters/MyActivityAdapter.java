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

import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.mobile.android.R;
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
		TextView icon;
		ImageView activityLinkArrow;
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
			holder.activityTitle = (TextView) row.findViewById(R.id.activityTitle);
			holder.activitySubtitle = (TextView) row.findViewById(R.id.activitySubtitle);
			holder.activityDate = (TextView) row.findViewById(R.id.activityDate);

			final ImageView imageArrow = (ImageView) row.findViewById(R.id.activityLinkArrow);

			final TextView txt = (TextView) row.findViewById(R.id.iconView);
			txt.setTypeface(TypefaceFactory.get().getFontAwesome());

			boolean hasActionLink = (activity.getActivityLink() != null);

			if ((hasActionLink && activity.getActivityEvent() != ActivityEvent_t.REDEEM) && !activity.isActionTaken())
			{
				txt.setTextColor(row.getResources().getColor(R.color.color_teal));
			}

			else
			{
				imageArrow.setVisibility(View.GONE);
				txt.setTextColor(row.getResources().getColor(R.color.color_grey));
			}

			switch (activity.activityEvent)
			{
				case EMAIL_RECV_GIFT:
				case EMAIL_SEND_GIFT:
				case FACEBOOK_RECV_GIFT:
				case FACEBOOK_SEND_GIFT:
				case REJECT_GIFT:
					txt.setText(R.string.icon_gift);
					break;

				case FRIEND_PURCHASE_DEAL_OFFER:
				case PURCHASE:
				case REDEEM:
					txt.setText(R.string.icon_money);
					break;

				case WELCOME:
				case MERCHANT_REACH:
				case TALOOL_REACH:
					txt.setText(R.string.icon_envelope_alt);
					break;

				case UNKNOWN:
					txt.setText(R.string.icon_envelope_alt);
					break;

			}

			row.setTag(holder);
		}
		else
		{
			holder = (ActivityHolder) row.getTag();
		}

		holder.activityTitle.setText(activity.getTitle());
		holder.activitySubtitle.setText(activity.getSubtitle());
		holder.activityDate.setText(new Date(activity.getActivityDate()).toString());

		return row;
	}
}
