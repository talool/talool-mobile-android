package com.talool.mobile.android;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.MyDealsAdapter;
import com.talool.mobile.android.adapters.MyDealsRow;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class MyDealsFragment extends Fragment{
	private ListView myDealsListView;
	private MyDealsAdapter myDealsAdapter;
	private ThriftHelper client;
	private View view;
	private Context context;
	private Exception exception;
	private List<Merchant_t> merchants;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		this.view = inflater.inflate(R.layout.my_deals_fragment, container,false);
		myDealsListView = (ListView) view.findViewById(R.id.myDealsListView);
		this.context = view.getContext();
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
	
	private void loadListView()
	{
		MyDealsAdapter adapter = new MyDealsAdapter(view.getContext(), 
                R.layout.mydeals_item_row, merchants);
		myDealsAdapter = adapter;
		myDealsListView.setAdapter(myDealsAdapter);
		myDealsListView.setOnItemClickListener(onClickListener);
	}
	
	private class MyDealsTask extends AsyncTask<String,Void,List<Merchant_t>>{
		private ProgressDialog progressDialog;
		public MyDealsTask()
		{
			this.progressDialog = new ProgressDialog(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			this.progressDialog.setMessage("Loading Deals");
	        this.progressDialog.show();
		}

		@Override
		protected void onPostExecute(List<Merchant_t> results) {
			merchants = results;
			if(progressDialog.isShowing())
			{
				progressDialog.dismiss();
			}
			loadListView();
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
	
	protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			MyDealsAdapter myDealsAdapter = (MyDealsAdapter)arg0.getAdapter();
			Merchant_t merchant = (Merchant_t)myDealsAdapter.getItem(position);	
		
	        Intent myIntent = new Intent(arg1.getContext(), DealsActivity.class);
	        myIntent.putExtra("Lat", String.valueOf(merchant.getLocations().get(0).location.latitude));
	        myIntent.putExtra("Lon", String.valueOf(merchant.getLocations().get(0).location.longitude));

	        startActivity(myIntent);	

		}
	};


	
	
}
