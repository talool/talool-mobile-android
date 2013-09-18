package com.talool.mobile.android.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.talool.mobile.android.TaloolApplication;

/**
 * 
 * @author clintz
 * 
 */
public final class AndroidUtils
{
	public static void popupMessageWithOk(final AlertMessage alertMessage, final Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(alertMessage.title);

		if (alertMessage.exception != null)
		{
			alertDialogBuilder.setMessage(alertMessage.message +
					AndroidUtils.getStackTrace(alertMessage.exception));
		}

		alertDialogBuilder.setMessage(alertMessage.message);

		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();

			}
		});

		// create alert dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static void popupGeneralMessageWithOk(final String title, final String message, final Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();

			}
		});

		// create alert dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static void popupExceptionWithOk(final Exception exception, final Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle("An Errror Has Occured");
		alertDialogBuilder.setMessage(AndroidUtils.getStackTrace(exception));
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();

			}
		});

		// create alert dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	/**
	 * 
	 * Be careful - returns true if you have a SIM card but no data plan
	 * 
	 * @return true of WIFI or MOBILE connection, false otherwise
	 */
	public static boolean hasNetworkConnection()
	{
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) TaloolApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo)
		{
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
			{
				if (ni.isConnected())
				{
					haveConnectedWifi = true;
				}
			}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
			{
				if (ni.isConnected())
				{
					haveConnectedMobile = true;
				}
			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}
