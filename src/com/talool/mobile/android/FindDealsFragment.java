package com.talool.mobile.android;

import java.util.List;

import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FindDealsFragment extends Fragment {
	private ListView myDealsListView;
	private MyDealsAdapter myDealsAdapter;
	private ThriftHelper client;
	private View view;
	private Exception exception;
	private List<Merchant_t> merchants;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.find_deals_fragment, container,false);
	}

}
