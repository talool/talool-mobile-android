package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class MyDealsFragment extends Fragment{
	private ListView myDealsListView;
	private MyDealsAdapter myDealsAdapter;
	private ThriftHelper client;
	private View view;
	private Exception exception;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		this.view = inflater.inflate(R.layout.my_deals_fragment, container,false);
		myDealsListView = (ListView) view.findViewById(R.id.myDealsListView);
		
		MyDealsTask dealsTask = new MyDealsTask();
		dealsTask.execute(new String[]{});
		
		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return view;
	}
	
	private class MyDealsTask extends AsyncTask<String,Void,List<Merchant_t>>{
		@Override
		protected void onPostExecute(List<Merchant_t> results) {		
			MyDealsAdapter adapter = new MyDealsAdapter(view.getContext(), 
	                R.layout.mydeals_item_row, results);
			myDealsAdapter = adapter;
			myDealsListView.setAdapter(myDealsAdapter);
		}

		@Override
		protected List<Merchant_t> doInBackground(String... arg0) {
			List<Merchant_t> results = new ArrayList<Merchant_t>();

			try {
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("merchant.name").setAscending(true);
				results = client.getClient().getMerchantAcquires(searchOptions);
			} catch (ServiceException_t e) {
				// TODO Auto-generated catch block
				exception = e;
			} catch (TException e) {
				// TODO Auto-generated catch block
				exception = e;
			} catch (Exception e)
			{
				exception = e;
			}
			
			return results;
		}

	}


	
	
}
