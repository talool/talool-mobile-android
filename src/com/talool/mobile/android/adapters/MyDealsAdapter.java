package com.talool.mobile.android.adapters;

import java.util.ArrayList;
import java.util.List;

import com.talool.api.thrift.Category_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.TaloolUtil;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class MyDealsAdapter extends ArrayAdapter<Merchant_t> implements Filterable{
	private Context context; 
    private int layoutResourceId;    
    private List<Merchant_t> data = null;
    private List<Merchant_t> filteredData = null;
    
    public MyDealsAdapter(Context context, int layoutResourceId, List<Merchant_t> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.filteredData = data;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return filteredData.size();
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return new Filter()
       {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();

                //If there's nothing to filter on, return the original data for your list
                if(charSequence == null || charSequence.length() == 0)
                {
                    results.values = data;
                    results.count = data.size();
                }
                else
                {
                    List<Merchant_t> filterResultsData = new ArrayList<Merchant_t>();

                    for(Merchant_t merchant : data)
                    {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        //I'm not sure how you're going to do comparison, so you'll need to fill out this conditional
                    	if(merchant.category.categoryId == Integer.valueOf(charSequence.toString()))
                        {
                            filterResultsData.add(merchant);
                        }
                    }            

                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }

                return results;
            }

            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                filteredData = (List<Merchant_t>)filterResults.values;
                notifyDataSetChanged();
            }
        };
	}

	@Override
	public Merchant_t getItem(int position) {
		// TODO Auto-generated method stub
		return filteredData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyDealsRow holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new MyDealsRow();
            holder.myDealsMerchantIcon = (ImageView)row.findViewById(R.id.myDealsMerchantIcon);
            holder.myDealsMerchantTitle = (TextView)row.findViewById(R.id.myDealsMerchantTitle);
            holder.myDealsMerchantLocation = (TextView)row.findViewById(R.id.myDealsMerchantLocation);
            holder.myDealsMerchantDistance = (TextView)row.findViewById(R.id.myDealsMerchantDistance);
            holder.myDealsMerchantArrow = (ImageView)row.findViewById(R.id.myDealsMerchantArrow);

            row.setTag(holder);
        }
        else
        {
            holder = (MyDealsRow)row.getTag();
        }
        
        Merchant_t merchant = data.get(position);
        holder.merchant = merchant;
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
            	
            	holder.myDealsMerchantDistance.setText(String.valueOf(TaloolUtil.round(distance, 2)) + " miles");

        	}
        	holder.myDealsMerchantLocation.setText(merchant.getLocations().get(0).address.city);
        }
        holder.myDealsMerchantArrow.setImageResource(R.drawable.navigation_next_item);

        
        return row;
    }    
}
