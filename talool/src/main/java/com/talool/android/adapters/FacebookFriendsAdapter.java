package com.talool.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.talool.android.R;

import java.util.List;

public class FacebookFriendsAdapter extends ArrayAdapter<FacebookFriend> {
    private List<FacebookFriend> data;
	int layoutResourceId;
	Context context;
	
	public FacebookFriendsAdapter(Context context, int layoutResourceId, List<FacebookFriend> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		FacebookFriendsRow holder = null;

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);

		holder = new FacebookFriendsRow();
		holder.text1 = (TextView) row.findViewById(R.id.text1);
		holder.text2 = (TextView) row.findViewById(R.id.text2);
		holder.icon = (ImageView) row.findViewById(R.id.icon);

		row.setTag(holder);

		FacebookFriend facebookFriend = data.get(position);

		holder.text1.setText(facebookFriend.text1);
		holder.text2.setText(facebookFriend.text2);

		return row;
    }

}
