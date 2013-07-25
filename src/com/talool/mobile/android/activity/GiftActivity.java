package com.talool.mobile.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.Gift_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.TaloolUser;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

/**
 * 
 * @author clintz
 * 
 * @TODO Wire up proper exception handling/logging
 */
public class GiftActivity extends Activity
{
	public static String GIFT_ID_PARAM = "giftId";
	public static String ACTICITY_OBJ_PARAM = "activityObj";

	private ThriftHelper client;
	private String giftId;
	private ImageView dealImageView;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gift_activity);

		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		giftId = (String) getIntent().getSerializableExtra(GIFT_ID_PARAM);

		final GiftActivityTask dealsTask = new GiftActivityTask();
		dealsTask.execute(new String[] {});

		byte[] activityObjBytes = (byte[]) getIntent().getSerializableExtra(ACTICITY_OBJ_PARAM);
		final Activity_t activity = new Activity_t();
		try
		{
			ThriftUtil.deserialize(activityObjBytes, activity);
		}
		catch (TException e)
		{
			e.printStackTrace();
		}

		setTitle("A Gift For You");

		final TextView thumbsDownIcon = (TextView) findViewById(R.id.thumbsUpIcon);
		thumbsDownIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		final TextView thumbsUpIcon = (TextView) findViewById(R.id.thumbsDownIcon);
		thumbsUpIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

		final TextView activityTitle = (TextView) findViewById(R.id.activityTitle);
		activityTitle.setText(activity.getTitle());

		if (activity.getSubtitle() != null)
		{
			final TextView activitySubTitle = (TextView) findViewById(R.id.activitySubtitle);
			activitySubTitle.setText(activity.getSubtitle());
		}

	}

	private class GiftActivityTask extends AsyncTask<String, Void, Gift_t>
	{
		@Override
		protected void onPostExecute(final Gift_t gift)
		{
			final TextView title = (TextView) findViewById(R.id.title);

			title.setText(gift.getDeal().getTitle());
			dealImageView = (ImageView) findViewById(R.id.dealImage);

			ImageDownloader imageTask = new ImageDownloader(dealImageView);
			imageTask.execute(new String[] { gift.getDeal().getImageUrl() });

		}

		@Override
		protected Gift_t doInBackground(final String... arg0)
		{
			Gift_t gift = null;

			try
			{
				client.setAccessToken(TaloolUser.getInstance().getAccessToken());
				gift = client.getClient().getGift(giftId);

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

			return gift;
		}
	}

}
