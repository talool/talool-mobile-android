package com.talool.mobile.android.dialog;

import android.app.DialogFragment;

public class DialogFactory {
	
	public interface ConfirmDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	static public DialogFragment getProgressDialog()
	{
		TaloolProgressDialogFragment frag = new TaloolProgressDialogFragment();
		return frag;
	}
	
	static public DialogFragment getAlertDialog(String title, String message, String confirmLabel)
	{
		TaloolAlertDialogFragment frag = new TaloolAlertDialogFragment();
		frag.setTitle(title);
		frag.setMessage(message);
		frag.setPositiveLabel(confirmLabel);
		return frag;
	}
	
	static public DialogFragment getConfirmDialog(String title, String message, ConfirmDialogListener listener)
	{

		TaloolConfirmDialogFragment frag = new TaloolConfirmDialogFragment();
		frag.setTitle(title);
		frag.setMessage(message);
		frag.setListener(listener);
		return frag;
	}
}
