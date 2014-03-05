package com.talool.android.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.talool.android.TaloolApplication;

import java.io.PrintWriter;
import java.io.StringWriter;

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

	public static String getTaloolVersion()
	{
		try {
			return TaloolApplication.getAppContext().getPackageManager().getPackageInfo(
					TaloolApplication.getAppContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}	
	}

	public static String getUserAgent()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Talool/");
		sb.append(getTaloolVersion());
		sb.append(" (Linux; Android ");
		sb.append(android.os.Build.VERSION.RELEASE);
		sb.append("; ");
		sb.append(getDeviceName());
		sb.append(")");
		return sb.toString();
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 

	public static String getReleaseInfo()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("talool-");
		try
		{
			sb.append(TaloolApplication.getAppContext().getPackageManager().getPackageInfo(
					TaloolApplication.getAppContext().getPackageName(), 0).versionName);
			sb.append(", ");
			sb.append("android-").append(android.os.Build.VERSION.RELEASE);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}

		sb.append(", sdk-").append(android.os.Build.VERSION.SDK_INT);
		return sb.toString();

	}
}
