package com.talool.mobile.android;

import org.apache.thrift.transport.TTransportException;

import com.talool.mobile.android.util.ThriftHelper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class DealsActivity extends Activity {
	private static ThriftHelper client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deals_activity_layout);

		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
		}

	}
}
