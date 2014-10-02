package com.talool.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.talool.android.util.TypefaceFactory;

/*
 *
 * 
 * @author dmccuen
 *
 */
@SuppressLint("NewApi")
public abstract class SortProvider extends ActionProvider
{

	TextView sortTextView;

	private final Context mContext;
	private View view;
	private final SortOnClickListener onClickListener = new SortOnClickListener();
    private Sort sort = Sort.ALPHA;

    public enum Sort {ALPHA, DISTANCE};

	abstract public void setSort(final Sort sort);

	private class SortOnClickListener implements OnClickListener
	{

		@Override
		public void onClick(final View view)
		{
            if (sort == Sort.DISTANCE)
            {
                sort = Sort.ALPHA;
                sortTextView.setText(R.string.icon_sort_alpha);
            }
            else
            {
                sort = Sort.DISTANCE;
                sortTextView.setText(R.string.icon_sort_number);
            }
			setSort(sort);

		}
	}

	public SortProvider(final Context context, final Sort sort)
	{
		super(context);
		mContext = context;
        this.sort = sort;
	}

	@Override
	public View onCreateActionView()
	{

		final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		final View view = layoutInflater.inflate(R.layout.sort_layout, null);

		sortTextView = (TextView) view.findViewById(R.id.sort_text);

		sortTextView.setOnClickListener(onClickListener);

        if (sort == Sort.ALPHA)
        {
            sortTextView.setText(R.string.icon_sort_alpha);
        }
        else
        {
            sortTextView.setText(R.string.icon_sort_number);
        }


		sortTextView.setTypeface(TypefaceFactory.get().getFontAwesome());

		this.view = view;

		return view;
	}

}