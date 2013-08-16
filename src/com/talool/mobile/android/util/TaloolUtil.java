package com.talool.mobile.android.util;

import java.math.BigDecimal;

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
}
