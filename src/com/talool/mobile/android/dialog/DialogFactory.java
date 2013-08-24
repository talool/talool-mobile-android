package com.talool.mobile.android.dialog;

import android.app.DialogFragment;

public class DialogFactory {
	
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
}
