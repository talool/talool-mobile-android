package com.talool.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.talool.android.util.AlertMessage;

/**
 * 
 * @author clintz
 * 
 */
public abstract class AbstractTaloolTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
	protected Context context;
	protected AlertMessage alertMessage;

	public AbstractTaloolTask(final Context context)
	{
		this.context = context;
	}

}
