package com.talool.mobile.android.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.talool.mobile.android.util.ApiUtil;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.thrift.util.ThriftUtil;

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
	private Menu menu;

	int selectedEventFilter = R.id.activity_filter_all;

	private static final Map<Integer, Set<ActivityEvent_t>> eventMap = new HashMap<Integer, Set<ActivityEvent_t>>();

	static
	{
		Set<ActivityEvent_t> ss = new HashSet<ActivityEvent_t>();
		ss.add(ActivityEvent_t.EMAIL_RECV_GIFT);
		ss.add(ActivityEvent_t.EMAIL_SEND_GIFT);
		ss.add(ActivityEvent_t.FACEBOOK_RECV_GIFT);
		ss.add(ActivityEvent_t.FACEBOOK_SEND_GIFT);
		ss.add(ActivityEvent_t.FRIEND_GIFT_ACCEPT);
		ss.add(ActivityEvent_t.FRIEND_GIFT_REDEEM);
		ss.add(ActivityEvent_t.FRIEND_GIFT_REJECT);

		eventMap.put(R.id.activity_filter_all, null);
		eventMap.put(R.id.activity_filter_gift, ss);

	}

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
				intent.putExtra(GiftActivity.ACTIVITY_OBJ_PARAM, ThriftUtil.serialize(activity));
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		this.menu = menu;

		inflater.inflate(R.menu.activities_action_bar, menu);

		final MenuItem menuItem = menu.findItem(selectedEventFilter);
		menuItem.setChecked(true);

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.activity_filter_root ||
				item.getItemId() == R.id.activity_filter_all)
		{
			return false;
		}

		selectedEventFilter = item.getItemId();

		item.setChecked(item.isChecked() ? false : true);

		reloadData();

		return true;

		// // Handle item selection
		// switch (item.getItemId())
		// {
		// case R.id.activity_all:
		// item.setChecked(item.isChecked() ? false : true);
		// return true;
		//
		// case R.id.activity_messages:
		// item.setChecked(item.isChecked() ? false : true);
		// return true;
		//
		// default:
		// return super.onOptionsItemSelected(item);
		// }
	}
	private class MyActivityTask extends AsyncTask<String, Void, List<Activity_t>>
	{
		@Override
		protected void onPostExecute(final List<Activity_t> results)
		{
			final MyActivityAdapter adapter = new MyActivityAdapter(view.getContext(),
					R.layout.my_activity_item_row, results)
			{

				@Override
				public boolean isEnabled(int position)
				{
					return ApiUtil.isClickableActivityLink(results.get(position));
				}

			};

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

	private void reloadData()
	{
		final MyActivityTask dealsTask = new MyActivityTask();
		dealsTask.execute(new String[] {});
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.my_activity_fragment, container, false);
		myActivityListView = (ListView) view.findViewById(R.id.myActivityListView);

		setRetainInstance(false);

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		setHasOptionsMenu(true);

		reloadData();

		return view;
	}

}
