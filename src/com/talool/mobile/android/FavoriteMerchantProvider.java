package com.talool.mobile.android;

import org.apache.thrift.transport.TTransportException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

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

	private boolean isFavorited = false;

	private final Context mContext;

	private final FavoriteOnClickListener favoriteOnClickListener = new FavoriteOnClickListener();

	private class FavoriteOnClickListener implements OnClickListener
	{

		@Override
		public void onClick(final View view)
		{
			final TextView heartTextView = (TextView) view.findViewById(R.id.heart_text);

			if (isFavorited)
			{
				heartTextView.setText(R.string.icon_heart_empty);
				isFavorited = false;
			}
			else
			{
				heartTextView.setText(R.string.icon_heart);
				isFavorited = true;
			}

		}
	}

	public FavoriteMerchantProvider(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	public View onCreateActionView()
	{
		final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		final View view = layoutInflater.inflate(R.layout.heart_layout, null);

		final TextView heartTextView = (TextView) view.findViewById(R.id.heart_text);

		heartTextView.setOnClickListener(favoriteOnClickListener);

		heartTextView.setText(R.string.icon_heart);
		heartTextView.setTypeface(TypefaceFactory.get().getFontAwesome());

		return view;
	}

}