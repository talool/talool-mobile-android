package com.talool.mobile.android.activity;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.BasicWebViewActivity;
import com.talool.mobile.android.R;
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

	protected AdapterView.OnItemClickListener activityClickListener = new AdapterView.OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Intent intent = null;
			final MyActivityAdapter activityAdapter = (MyActivityAdapter) parent.getAdapter();
			final Activity_t activity = (Activity_t) activityAdapter.getItem(position);

			if (activity.getActivityEvent().equals(ActivityEvent_t.EMAIL_RECV_GIFT) ||
					activity.getActivityEvent().equals(ActivityEvent_t.FACEBOOK_RECV_GIFT))
			{
				intent = new Intent(parent.getContext(), GiftActivity.class);
				intent.putExtra(GiftActivity.GIFT_ID_PARAM, activity.getActivityLink().getLinkElement());
			}
			else if (activity.getActivityLink() != null)
			{
				intent = new Intent(parent.getContext(), BasicWebViewActivity.class);
				// we have an external link
				intent.putExtra(BasicWebViewActivity.TARGET_URL_PARAM, activity.getActivityLink().getLinkElement());
				intent.putExtra(BasicWebViewActivity.TITLE_PARAM, activity.getTitle());
			}

			startActivity(intent);
		}

	};

	private class MyActivityTask extends AsyncTask<String, Void, List<Activity_t>>
	{
		@Override
		protected void onPostExecute(final List<Activity_t> results)
		{
			final MyActivityAdapter adapter = new MyActivityAdapter(view.getContext(),
					R.layout.my_activity_item_row, results);
			activityAdapter = adapter;
			myActivityListView.setAdapter(activityAdapter);

			myActivityListView.setOnItemClickListener(activityClickListener);
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
			}
			catch (TException e)
			{
				e.printStackTrace();

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return results;
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState)
	{
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		this.view = inflater.inflate(R.layout.my_activity_fragment, container, false);
		myActivityListView = (ListView) view.findViewById(R.id.myActivityListView);

		final MyActivityTask dealsTask = new MyActivityTask();
		dealsTask.execute(new String[] {});

		return view;
	}

}
