package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import com.talool.android.R;
import com.talool.android.dialog.DialogFactory.DialogClickListener;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.ThriftHelper;
import com.talool.api.thrift.DealOffer_t;

public class DealSampleActivity extends TaloolActivity implements DialogClickListener{
	private static ThriftHelper client;
	private DealOffer_t dealOffer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_activity_layout);

		if (TaloolUser.get().getAccessToken() == null)
		{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}

		byte[] dealBytes = (byte[]) getIntent().getSerializableExtra("deal");
		byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");

	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

}
