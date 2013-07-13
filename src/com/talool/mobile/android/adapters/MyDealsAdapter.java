package com.talool.mobile.android.adapters;

import java.util.List;

import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUser;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyDealsAdapter extends ArrayAdapter<Merchant_t> {

	Context context; 
    int layoutResourceId;    
    List<Merchant_t> data = null;
    
    public MyDealsAdapter(Context context, int layoutResourceId, List<Merchant_t> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyDealsHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new MyDealsHolder();
            holder.myDealsMerchantIcon = (ImageView)row.findViewById(R.id.myDealsMerchantIcon);
            holder.myDealsMerchantTitle = (TextView)row.findViewById(R.id.myDealsMerchantTitle);
            holder.myDealsMerchantLocation = (TextView)row.findViewById(R.id.myDealsMerchantLocation);
            holder.myDealsMerchantDistance = (TextView)row.findViewById(R.id.myDealsMerchantDistance);
            holder.myDealsMerchantArrow = (ImageView)row.findViewById(R.id.myDealsMerchantArrow);

            row.setTag(holder);
        }
        else
        {
            holder = (MyDealsHolder)row.getTag();
        }
        
        Merchant_t merchant = data.get(position);
        holder.myDealsMerchantIcon.setImageResource(R.drawable.icon_teal);
        holder.myDealsMerchantTitle.setText(merchant.getName());
        if(merchant.getLocations() != null && merchant.getLocations().size() >0)
        {
        	holder.myDealsMerchantLocation.setText(merchant.getLocations().get(0).address.city);

        	if(TaloolUser.getInstance().getLocation() != null)
        	{
            	Location merchantLocation = new Location("Talool");
            	merchantLocation.setLatitude(merchant.getLocations().get(0).location.latitude);
            	merchantLocation.setLongitude(merchant.getLocations().get(0).location.longitude);
            	float distance = TaloolUser.getInstance().getLocation().distanceTo(merchantLocation);
            	distance = (float) (distance * 0.00062137);
            	
            	holder.myDealsMerchantDistance.setText(String.valueOf(distance));

        	}
        	holder.myDealsMerchantLocation.setText(merchant.getLocations().get(0).address.city);
        }
        holder.myDealsMerchantArrow.setImageResource(R.drawable.navigation_next_item);

        
        return row;
    }
    
    static class MyDealsHolder
    {
        ImageView myDealsMerchantIcon;
        TextView myDealsMerchantTitle;
        TextView myDealsMerchantLocation;
        TextView myDealsMerchantDistance;
        ImageView myDealsMerchantArrow;
    }
    
    
}
