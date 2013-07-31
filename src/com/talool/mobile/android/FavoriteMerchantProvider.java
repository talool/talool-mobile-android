package com.talool.mobile.android;

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

import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.mobile.android.cache.FavoriteMerchantCache;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;

@SuppressLint("NewApi")
/**
 * 
 * @author clintz
 *
 */
public class FavoriteMerchantProvider extends ActionProvider
{
	private ThriftHelper client;
	private final Merchant_t merchant;

	TextView heartTextView;

	private final Context mContext;

	private final FavoriteOnClickListener favoriteOnClickListener = new FavoriteOnClickListener();

	@Override
	public View onCreateActionView(final MenuItem forItem)
	{
		try
		{
			client = new ThriftHelper();
		}
		catch (TTransportException e)
		{
			e.printStackTrace();
		}

		return super.onCreateActionView(forItem);
	}

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

			final AsyncTask<String, Void, Boolean> favTask = new AsyncTask<String, Void, Boolean>()
			{

				protected void onPostExecute(final Boolean success)
				{
					// we can update UI here because it runs on the main UI thread (unlike
					// doBackground)
					if (success)
					{
						setIsFavorited(!isCurrentFavorite);
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
					catch (TApplicationException e)
					{
						Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
					}
					catch (ServiceException_t e)
					{
						Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
					}
					catch (Exception e)
					{
						Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
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

		return view;
	}

}