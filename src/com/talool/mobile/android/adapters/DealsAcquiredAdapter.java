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

import com.talool.api.thrift.DealAcquire_t;
import com.talool.mobile.android.R;

public class DealsAcquiredAdapter extends ArrayAdapter<DealAcquire_t> {
	Context context; 
    int layoutResourceId;    
    List<DealAcquire_t> data = null;
    
    public DealsAcquiredAdapter(Context context, int layoutResourceId, List<DealAcquire_t> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DealAcquiredRow holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new DealAcquiredRow();
            holder.dealsAcquiredIcon = (ImageView)row.findViewById(R.id.dealsAcquiredIcon);
            holder.dealsAcquiredTitle = (TextView)row.findViewById(R.id.dealsAcquiredTitle);
            holder.dealsAcquiredExpires = (TextView)row.findViewById(R.id.dealsAcquiredExpires);
            holder.dealsAcquiredArrow = (ImageView)row.findViewById(R.id.dealsAcquiredArrow);

            row.setTag(holder);
        }
        else
        {
            holder = (DealAcquiredRow)row.getTag();
        }
        
        DealAcquire_t dealAcquire = data.get(position);
        holder.dealsAcquiredIcon.setImageResource(R.drawable.icon_teal);
        holder.dealsAcquiredTitle.setText(dealAcquire.deal.summary);
        holder.dealsAcquiredExpires.setText("Expires on " + new Date(dealAcquire.deal.expires).toString());

        holder.dealsAcquiredArrow.setImageResource(R.drawable.navigation_next_item);

        
        return row;
    }    
}
