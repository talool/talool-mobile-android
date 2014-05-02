package com.talool.android.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.R;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;

import org.apache.thrift.transport.TTransportException;

public class TaloolActivity extends Activity {
	protected static ThriftHelper client;
	protected DealOffer_t dealOffer;
	protected Exception exception;
	protected DialogFragment df;
	protected String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		createThriftClient();

	}
	private void createThriftClient()
	{
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			sendExceptionToAnalytics(e);
		}
	}
	
	protected void sendExceptionToAnalytics(Exception exception)
	{
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
				.build()
				);
	}
	
	protected void popupErrorMessage(Exception exception, String errorMessage)
	{
		sendExceptionToAnalytics(exception);

		popupErrorMessage(errorMessage);
	}
	
	protected void popupErrorMessage(String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		if (message == null)
		{
			message = getResources().getString(R.string.error_access_code_message);
		}
		String title = getResources().getString(R.string.error_loading_deals);
		String label = getResources().getString(R.string.ok);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
		/*
		 * dialog.dismiss(); createThriftClient(); reloadData();
		 */
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
	}

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;

        if (listAdapter == null) {
            return;
        }
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem != null) {
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        if (params != null) {
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.setBackgroundColor(this.getResources().getColor(R.color.dark_tan));
        }

    }
}
