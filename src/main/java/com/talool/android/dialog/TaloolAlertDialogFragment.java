package com.talool.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.talool.android.R;
import com.talool.android.dialog.DialogFactory.DialogPositiveClickListener;

/**
 * 
 * @author dmccuen, clintz
 * 
 */
public class TaloolAlertDialogFragment extends DialogFragment
{
	protected String message;
	protected String title;
	protected String positiveLabel;
	protected DialogPositiveClickListener listener;

	public TaloolAlertDialogFragment()
	{}

	public TaloolAlertDialogFragment(final DialogPositiveClickListener dialogClickListener)
	{
		this.listener = dialogClickListener;
	}

	public void setPositiveLabel(String positiveLabel)
	{
		this.positiveLabel = positiveLabel;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.alert_dialog_layout, null);

		TextView tView = (TextView) view.findViewById(R.id.title);
		tView.setText(title);
		TextView mView = (TextView) view.findViewById(R.id.message);
		mView.setText(message);

		builder.setView(view)
				.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.dismiss();
						if (listener != null)
						{
							listener.onDialogPositiveClick(TaloolAlertDialogFragment.this);
						}
					}
				});

		return builder.create();
	}

}
