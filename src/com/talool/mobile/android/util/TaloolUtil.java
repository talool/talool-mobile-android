package com.talool.mobile.android.util;

import java.math.BigDecimal;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class TaloolUtil
{
	public static BigDecimal round(float d, int decimalPlace)
	{
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

	public static String getExpirationText(long expirationDate)
	{
		if (expirationDate == 0)
		{
			return "Never expires";
		}
		else
		{
			return "Expires on " + new SafeSimpleDateFormat(Constants.MONTH_DAY_YEAR_FORMAT).format(expirationDate);
		}
	}
	
	public static String getSharedText(long shareDate)
	{
		if (shareDate == 0)
		{
			return "";
		}
		else
		{
			return "Shared on " + new SafeSimpleDateFormat(Constants.MONTH_DAY_YEAR_FORMAT).format(shareDate);
		}
	}
	
	public static String getRedeemedText(long redeemedDate)
	{
		if (redeemedDate == 0)
		{
			return "";
		}
		else
		{
			return "Redeemed on " + new SafeSimpleDateFormat(Constants.MONTH_DAY_YEAR_FORMAT).format(redeemedDate);
		}
	}
	
	public static void sendException(Exception e, Context context)
	{
		e.printStackTrace();
		EasyTracker.getInstance(context).send(MapBuilder
				.createException(new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), e), true)
				.build()
				);
	}
}
