package com.talool.mobile.android.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.BasicWebViewActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.TaloolApplication;
import com.talool.mobile.android.adapters.MyActivityAdapter;
import com.talool.mobile.android.persistence.ActivityDao;
import com.talool.mobile.android.tasks.ActivityActionTakenTask;
import com.talool.mobile.android.tasks.ActivitySupervisor;
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
	private ActivityDao activityDao;
	private Activity_t mostCurrentActivity;

	int selectedEventFilter = R.id.activity_filter_all;

	private static final SparseArray<List<ActivityEvent_t>> eventMap = new SparseArray<List<ActivityEvent_t>>();

	static
	{
		final List<ActivityEvent_t> gifts = new ArrayList<ActivityEvent_t>();
		eventMap.put(R.id.activity_filter_all, null);

		gifts.add(ActivityEvent_t.EMAIL_RECV_GIFT);
		gifts.add(ActivityEvent_t.EMAIL_SEND_GIFT);
		gifts.add(ActivityEvent_t.FACEBOOK_RECV_GIFT);
		gifts.add(ActivityEvent_t.FACEBOOK_SEND_GIFT);
		eventMap.put(R.id.activity_filter_gift, gifts);

		final List<ActivityEvent_t> transactions = new ArrayList<ActivityEvent_t>();
		transactions.add(ActivityEvent_t.PURCHASE);
		transactions.add(ActivityEvent_t.REDEEM);
		eventMap.put(R.id.activity_filter_transactions, transactions);

		final List<ActivityEvent_t> friends = new ArrayList<ActivityEvent_t>();
		friends.add(ActivityEvent_t.FRIEND_PURCHASE_DEAL_OFFER);
		friends.add(ActivityEvent_t.FRIEND_GIFT_REDEEM);
		friends.add(ActivityEvent_t.FRIEND_GIFT_ACCEPT);
		friends.add(ActivityEvent_t.FRIEND_GIFT_REDEEM);
		friends.add(ActivityEvent_t.FRIEND_GIFT_REJECT);
		eventMap.put(R.id.activity_filter_friends, friends);

		final List<ActivityEvent_t> messages = new ArrayList<ActivityEvent_t>();
		messages.add(ActivityEvent_t.TALOOL_REACH);
		messages.add(ActivityEvent_t.AD);
		messages.add(ActivityEvent_t.WELCOME);
		messages.add(ActivityEvent_t.UNKNOWN);
		eventMap.put(R.id.activity_filter_messages, messages);

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

				if (activity.getActivityEvent().equals(ActivityEvent_t.TALOOL_REACH) ||
						activity.getActivityEvent().equals(ActivityEvent_t.WELCOME))
				{
					final ActivityActionTakenTask task = new ActivityActionTakenTask(client, activity.getActivityId());
					task.execute();
				}
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
		if (item.getItemId() == R.id.activity_filter_root)
		{
			return true;
		}

		selectedEventFilter = item.getItemId();
		item.setChecked(item.isChecked() ? false : true);

		reloadData();

		return true;

	}

	private class MyActivityTask extends AsyncTask<String, Void, List<Activity_t>>
	{
		@Override
		protected void onPostExecute(final List<Activity_t> results)
		{
			updateActivityList(results);
			activityDao.saveActivities(results);
		}

		@Override
		protected List<Activity_t> doInBackground(final String... arg0)
		{
			List<Activity_t> results = null;

			try
			{
				client.setAccessToken(TaloolUser.get().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setMaxResults(1000).setPage(0).setSortProperty("activityDate").setAscending(false);
				results = client.getClient().getActivities(searchOptions);

			}
			catch (ServiceException_t e)
			{
				e.printStackTrace();
				EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(), e), true)
						.build()
						);
			}
			catch (TException e)
			{
				e.printStackTrace();
				EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(), e), true)
						.build()
						);

			}
			catch (Exception e)
			{
				e.printStackTrace();
				EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

				easyTracker.send(MapBuilder
						.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(), e), true)
						.build()
						);
			}

			return results;
		}
	}

	private void updateActivityList(final List<Activity_t> results)
	{
		final MyActivityAdapter adapter = newMyActivityAdapter(results);

		activityAdapter = adapter;
		myActivityListView.setAdapter(activityAdapter);
		myActivityListView.setOnItemClickListener(activityClickListener);
	}

	private MyActivityAdapter newMyActivityAdapter(final List<Activity_t> results)
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

		return adapter;

	};

	private void reloadData()
	{
		final List<Activity_t> acts = activityDao.getAllActivities(MyActivityFragment.eventMap.get(selectedEventFilter));

		if (acts.size() > 0)
		{
			updateActivityList(acts);
		}
		else
		{
			final MyActivityTask dealsTask = new MyActivityTask();
			dealsTask.execute(new String[] {});
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.my_activity_fragment, container, false);
		myActivityListView = (ListView) view.findViewById(R.id.myActivityListView);

		setRetainInstance(false);

		activityDao = new ActivityDao(TaloolApplication.getAppContext());

		activityDao.open();

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
			EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(), e), true)
					.build()
					);
		}

		setHasOptionsMenu(true);

		reloadData();

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (activityDao != null)
		{
			activityDao.close();
		}
	}

	@Override
	public void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
		easyTracker.set(Fields.SCREEN_NAME, "My Activity");

		easyTracker.send(MapBuilder.createAppView().build());

	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (ActivitySupervisor.get().getActionsPending() > 0)
		{
			final Activity_t act = ActivitySupervisor.get().getMostCurrentActivity();

			if (mostCurrentActivity == null)
			{
				mostCurrentActivity = act;
			}
			else
			{
				if (!mostCurrentActivity.equals(mostCurrentActivity))
				{
					mostCurrentActivity = act;

					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							reloadData();
						}
					});
				}
			}

		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

}
