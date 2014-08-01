package com.talool.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.talool.android.R;
import com.talool.android.util.ApiUtil;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.Deal_t;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindDealsAdapter extends ArrayAdapter<Deal_t> {

	Context context;
	int layoutResourceId;
	List<Deal_t> data = null;

	public FindDealsAdapter(Context context, int layoutResourceId, List<Deal_t> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;

        // sort the data by category, name
        Collections.sort(data, DealComparator);
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		FindDealRow holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new FindDealRow();
			holder.dealIcon = (TextView) row.findViewById(R.id.dealIcon);
			holder.dealTitle = (TextView) row.findViewById(R.id.dealTitle);
			holder.dealMerchant = (TextView) row.findViewById(R.id.dealMerchant);

			row.setTag(holder);
		}
		else
		{
			holder = (FindDealRow) row.getTag();
		}

		Deal_t deal = data.get(position);

		holder.dealIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		holder.dealIcon.setText(ApiUtil.getIcon(deal.merchant.category));
		holder.dealIcon.setTextColor(row.getResources().getColor(ApiUtil.getIconColor(deal.merchant.category)));
		
		holder.dealTitle.setText(deal.getTitle());
		holder.dealMerchant.setText(deal.merchant.name);


		return row;
	}

    public static Comparator<Deal_t> DealComparator
            = new Comparator<Deal_t>() {

        public int compare(Deal_t d1, Deal_t d2) {

            String categoryName1 = d1.getMerchant().category.getName().toUpperCase();
            String categoryName2 = d2.getMerchant().category.getName().toUpperCase();

            String merchantName1 = d1.getMerchant().getName().toUpperCase();
            String merchantName2 = d2.getMerchant().getName().toUpperCase();

            if (categoryName1.equals(categoryName2))
            {
                // compare merchant name
                return merchantName1.compareTo(merchantName2);
            }
            else
            {
                return categoryName1.compareTo(categoryName2);
            }
        }

    };

}
