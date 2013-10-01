package com.talool.mobile.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.talool.mobile.android.R;
import com.talool.mobile.android.dialog.DialogFactory.DialogClickListener;

public class TaloolConfirmDialogFragment extends DialogFragment {

	String message;
	String title;
	String positiveLabel;
	String negativeLabel;
	DialogClickListener mListener;

	public void setPositiveLabel(String positiveLabel) {
		this.positiveLabel = positiveLabel;
	}

	public void setNegativeLabel(String negativeLabel) {
		this.negativeLabel = negativeLabel;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setListener(DialogClickListener listener) {
		this.mListener = listener;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_layout, null);
        
        TextView tView = (TextView) view.findViewById(R.id.title);
        tView.setText(title);
        TextView mView = (TextView) view.findViewById(R.id.message);
        mView.setText(message);
        
        if (positiveLabel==null)
        {
        	positiveLabel = getResources().getString(R.string.yes);
        }
        if (negativeLabel==null)
        {
        	negativeLabel = getResources().getString(R.string.no);
        }
        
        final TaloolConfirmDialogFragment self = this;
        builder.setView(view);
        builder.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogPositiveClick(self);
                       dialog.dismiss();
                   }
               });
        builder.setNegativeButton(negativeLabel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogNegativeClick(self);
                       dialog.dismiss();
                   }
               });
        
        
        return builder.create();
    }
	
	
	
	
}
