package com.talool.android;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.transport.TTransportException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.R;
import com.talool.android.cache.FavoriteMerchantCache;
import com.talool.android.util.ThriftHelper;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;

/*
 * Optimistically sets the heart icon on the Action bar with user input onclicks.  If the service side persistence fails the UI will be undone. 
 * 
 * @author clintz
 *
 */
@SuppressLint("NewApi")
public class FavoriteMerchantProvider extends ActionProvider
{
	private ThriftHelper client;
	private final Merchant_t merchant;

	TextView heartTextView;

	private final Context mContext;
	private View view;
	private final FavoriteOnClickListener favoriteOnClickListener = new FavoriteOnClickListener();

	protected void setIsFavorited(final boolean isFavorited)
	{
		if (isFavorited)
		{
			heartTextView.setText(R.string.icon_heart);
		}
		else
		{
			heartTextView.setText(R.string.icon_heart_empty);
		}
	}

	private class FavoriteOnClickListener implements OnClickListener
	{

		@Override
		public void onClick(final View view)
		{
			final boolean isCurrentFavorite = FavoriteMerchantCache.get().isFavorite(merchant.getMerchantId());

			// make UI speedy! Set now optimistically assume service side succeeds!
			setIsFavorited(!isCurrentFavorite);

			final AsyncTask<String, Void, Boolean> favTask = new AsyncTask<String, Void, Boolean>()
			{

				protected void onPostExecute(final Boolean success)
				{
					// we can update UI here because it runs on the main UI thread (unlike
					// doBackground)
					if (!success)
					{
						// undo what we optimistically set above
						setIsFavorited(isCurrentFavorite);
						EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());
						easyTracker.send(MapBuilder
								.createEvent("favorited","selected",merchant.merchantId,null)           
								.build()
								);
					}

				}

				@Override
				protected Boolean doInBackground(final String... arg0)
				{

					try
					{
						if (isCurrentFavorite)
						{
							client.getClient().removeFavoriteMerchant(merchant.getMerchantId());
							FavoriteMerchantCache.get().removeMerchant(merchant);
						}
						else
						{
							client.client.addFavoriteMerchant(merchant.getMerchantId());
							FavoriteMerchantCache.get().addMerchant(merchant);
						}

						return true;
					}
					catch (ServiceException_t e)
					{
						EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

						easyTracker.send(MapBuilder
								.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(),e),true)                                              
								.build()
								);
					}
					catch (Exception e)
					{
						EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

						easyTracker.send(MapBuilder
								.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(),e),true)                                              
								.build()
								);
					}

					return false;

				}

			};

			favTask.execute(new String[] {});

		}
	}

	public FavoriteMerchantProvider(final Merchant_t merchant, final Context context)
	{
		super(context);
		mContext = context;
		this.merchant = merchant;
	}

	@Override
	public View onCreateActionView()
	{

		final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		final View view = layoutInflater.inflate(R.layout.heart_layout, null);

		heartTextView = (TextView) view.findViewById(R.id.heart_text);

		heartTextView.setOnClickListener(favoriteOnClickListener);

		heartTextView.setText(R.string.icon_heart);
		heartTextView.setTypeface(TypefaceFactory.get().getFontAwesome());

		setIsFavorited(FavoriteMerchantCache.get().isFavorite(merchant.getMerchantId()));
		this.view = view;
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());

			easyTracker.send(MapBuilder
					.createException(new StandardExceptionParser(view.getContext(), null).getDescription(Thread.currentThread().getName(),e),true)                                              
					.build()
					);
		}

		return view;
	}

}