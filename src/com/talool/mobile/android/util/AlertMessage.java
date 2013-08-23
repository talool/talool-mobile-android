package com.talool.mobile.android.util;

public class AlertMessage
{
	public final String message;
	public final String title;
	public final Throwable exception;

	public AlertMessage(String message, String title, Throwable exception)
	{
		this.title = title;
		this.message = message;
		this.exception = exception;
	}

}
