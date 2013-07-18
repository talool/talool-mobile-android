package com.talool.mobile.android;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.adapters.MyActivityAdapter;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;

/**
 * 
 * @author clintz
 * @TODO Wire up proper exception handling/logging
 */
public class MyActivityFragment extends Fragment
{
	private ListView myActivityListView;
	private MyActivityAdapter activityAdapter;
	private ThriftHelper client;
	private View view;
	private Exception exception;

	private class MyActivityTask extends AsyncTask<String, Void, List<Activity_t>>
	{
		@Override
		protected void onPostExecute(final List<Activity_t> results)
		{
			final MyActivityAdapter adapter = new MyActivityAdapter(view.getContext(),
					R.layout.my_activity_item_row, results);
			activityAdapter = adapter;
			myActivityListView.setAdapter(activityAdapter);
		}

		@Override
		protected List<Activity_t> doInBackground(final String... arg0)
		{
			List<Activity_t> results = null;

			try
			{
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("activityDate").setAscending(false);
				results = client.getClient().getActivities(searchOptions);
			}
			catch (ServiceException_t e)
			{
				e.printStackTrace();
				exception = e;
			}
			catch (TException e)
			{
				e.printStackTrace();
				exception = e;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				exception = e;
			}

			return results;
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState)
	{

		this.view = inflater.inflate(R.layout.my_activity_fragment, container, false);
		myActivityListView = (ListView) view.findViewById(R.id.myActivityListView);

		final MyActivityTask dealsTask = new MyActivityTask();
		dealsTask.execute(new String[] {});

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		return view;
	}

}
