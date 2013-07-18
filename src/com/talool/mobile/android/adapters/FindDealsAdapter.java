package com.talool.mobile.android.adapters;

import java.util.List;

import com.talool.api.thrift.Merchant_t;

import android.content.Context;
import android.widget.ArrayAdapter;

public class FindDealsAdapter extends ArrayAdapter<Merchant_t> {

	public FindDealsAdapter(Context context, int textViewResourceId, List<Merchant_t> data) {
		super(context, textViewResourceId,data);
		// TODO Auto-generated constructor stub
	}

}
