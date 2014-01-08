package com.talool.android.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.TimeZone;

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
			return "Expires on " + new SafeSimpleDateFormat(Constants.FORMAT_MONTH_DAY_YEAR).format(expirationDate);
		}
	}

	public static String getGiftedText(long giftedDate)
	{
		if (giftedDate == 0)
		{
			return "";
		}
		else
		{
            return "Gifted on " + new SafeSimpleDateFormat(Constants.FORMAT_GENERAL_DATE_TIME).format(giftedDate);

		}
	}

	public static String getRedeemedText(String redemptionCode, long redeemedDate)
	{
		if (redeemedDate == 0)
		{
			return "";
		}
		else
		{
			return "Redemption Code: " +redemptionCode + " issued on " + new SafeSimpleDateFormat(Constants.FORMAT_GENERAL_DATE_TIME).format(redeemedDate);
		}
	}

    public static String getRedeemedTextNoCode(long redeemedDate)
    {
        if (redeemedDate == 0)
        {
            return "";
        }
        else
        {
            return "Redeemed on " + new SafeSimpleDateFormat(Constants.FORMAT_GENERAL_DATE_TIME).format(redeemedDate);
        }
    }

	public static void sendException(Exception e, Context context)
	{
		EasyTracker.getInstance(context).send(MapBuilder
				.createException(new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), e), true)
				.build()
				);
	}
	
	public static String moneyFormattedString(double price)
	{
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		String moneyString = formatter.format(price);
		moneyString = "Price: " + moneyString;
		return moneyString;
	}
	
}
