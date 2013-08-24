package com.talool.mobile.android.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
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
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.api.thrift.ActivityEvent_t;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.BasicWebViewActivity;
import com.talool.mobile.android.MainActivity;
import com.talool.mobile.android.R;
import com.talool.mobile.android.TaloolApplication;
import com.talool.mobile.android.adapters.MyActivityAdapter;
import com.talool.mobile.android.persistence.ActivityDao;
import com.talool.mobile.android.tasks.ActivityActionTakenTask;
import com.talool.mobile.android.tasks.ActivitySupervisor;
import com.talool.mobile.android.tasks.ActivitySupervisor.ActivityObservable;
import com.talool.mobile.android.tasks.ActivitySupervisor.ActivityUpdateSummary;
import com.talool.mobile.android.util.ApiUtil;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz
 * @TODO Wire up proper exception handling/logging
 */
public class MyActivityFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener
{
	private static final SparseArray<List<ActivityEvent_t>> eventMap = new SparseArray<List<ActivityEvent_t>>();

	private ListView myActivityListView;
	private MyActivityAdapter activityAdapter;
	private ThriftHelper client;
	private View view;
	private Menu menu;
	private TextView noResultsMessage;
	private ActivityDao activityDao;
	private Activity_t mostCurrentActivity;
	private ActivityObserver activityObserver;
	private PullToRefreshAttacher mPullToRefreshAttacher;

	int selectedEventFilter = R.id.activity_filter_all;

	/**
	 * Observers activity changes
	 * 
	 * @author clintz
	 * 
	 */
	private class ActivityObserver implements Observer
	{
		@Override
		public void update(final Observable observable, final Object data)
		{
			if (observable instanceof ActivityObservable)
			{
				final ActivityUpdateSummary summary = ((ActivityObservable) observable).getActivityUpdateSummary();

				boolean firstPass = false;
				final Activity_t act = summary.getCurrentActivity();

				if (mostCurrentActivity == null)
				{
					firstPass = true;
					mostCurrentActivity = act;
				}

				if (firstPass || !mostCurrentActivity.equals(act))
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
	};

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
					final ActivityActionTakenTask task = new ActivityActionTakenTask(client, activity.getActivityId(), view.getContext());
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

		myActivityListView = (ListView) view.findViewById(R.id.myActivityListView);

		noResultsMessage.setVisibility(View.GONE);

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
			activityDao.saveActivities(results);
			updateActivityList(activityDao.getAllActivities(MyActivityFragment.eventMap.get(selectedEventFilter)));
		}

		@Override
		protected List<Activity_t> doInBackground(final String... arg0)
		{
			List<Activity_t> results = null;

			try
			{
				client.setAccessToken(TaloolUser.get().getAccessToken());
				SearchOptions_t searchOptions = new SearchOptions_t();
				searchOptions.setSortProperty("activityDate").setAscending(false);
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
		mPullToRefreshAttacher.setRefreshComplete();
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
			final StringBuilder sb = new StringBuilder();

			noResultsMessage.setVisibility(View.VISIBLE);
			sb.append(getResources().getString(R.string.activity_no_results_prefix)).append(" '").
					append(((MenuItem) menu.findItem(selectedEventFilter)).getTitle()).append("'");

			noResultsMessage.setText(sb.toString());
		}
	}

	private void refreshViaService()
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

		getActivity().setTitle("Activity");

		noResultsMessage = (TextView) view.findViewById(R.id.activity_no_results_msg);
		noResultsMessage.setVisibility(View.GONE);

		setRetainInstance(false);

		activityDao = new ActivityDao(TaloolApplication.getAppContext());
		activityDao.open();

		mPullToRefreshAttacher = ((MainActivity) getActivity())
				.getPullToRefreshAttacher();
		mPullToRefreshAttacher.addRefreshableView(myActivityListView, this);

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
		super.onStart();
		EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
		easyTracker.set(Fields.SCREEN_NAME, "My Activity");

		easyTracker.send(MapBuilder.createAppView().build());

		if (activityObserver == null)
		{
			activityObserver = new ActivityObserver();
			ActivitySupervisor.get().addActivityObserver(activityObserver);
		}

		reloadData();

	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (activityObserver != null)
		{
			ActivitySupervisor.get().removeActivityObserver(activityObserver);
			activityObserver = null;
		}
	}

	@Override
	public void onRefreshStarted(View view)
	{
		refreshViaService();
	}

}
