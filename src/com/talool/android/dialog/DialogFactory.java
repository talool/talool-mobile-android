package com.talool.android.dialog;

import android.app.DialogFragment;

/**
 * A factory for Android Dialogs
 * 
 * @author dmccuen, clintz
 * 
 */
public class DialogFactory
{
	public interface DialogClickListener
	{
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	public interface DialogPositiveClickListener
	{
		public void onDialogPositiveClick(DialogFragment dialog);
	}

	static public DialogFragment getProgressDialog()
	{
		TaloolProgressDialogFragment frag = new TaloolProgressDialogFragment();
		return frag;
	}

	static public DialogFragment getAlertDialog(String title, String message, String confirmLabel, DialogPositiveClickListener listener)
	{
		TaloolAlertDialogFragment frag = new TaloolAlertDialogFragment(listener);
		frag.setTitle(title);
		frag.setMessage(message);
		frag.setPositiveLabel(confirmLabel);
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

	static public DialogFragment getConfirmDialog(String title, String message, DialogClickListener listener)
	{
		TaloolConfirmDialogFragment frag = new TaloolConfirmDialogFragment();
		frag.setTitle(title);
		frag.setMessage(message);
		frag.setListener(listener);
		return frag;
	}

	static public DialogFragment getConfirmDialog(String title, String message, String confirmLabel, DialogClickListener listener)
	{
		TaloolConfirmDialogFragment frag = new TaloolConfirmDialogFragment();
		frag.setTitle(title);
		frag.setPositiveLabel(confirmLabel);
		frag.setMessage(message);
		frag.setListener(listener);
		return frag;
	}
}
