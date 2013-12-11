package com.talool.android.util;

public class AlertMessage
{
	public final String message;
	public final String title;
	public Throwable exception;

	public AlertMessage(String message, String title, Throwable exception)
	{
		this.title = title;
		this.message = message;
		this.exception = exception;
	}

	public AlertMessage(String message, String title)
	{
		this.title = title;
		this.message = message;
	}

}
