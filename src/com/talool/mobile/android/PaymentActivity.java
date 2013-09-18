package com.talool.mobile.android;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.venmo.touch.activity.PaymentFormActivity;
import com.venmo.touch.model.CardDetails;
import com.venmo.touch.model.CardStub;

/**
 * 
 * @author clintz
 * 
 */
public class PaymentActivity extends Activity
{
	private static final String MERCHANT_ID = "mkf3rwysqz6w9x44";
	private static final String PUBLIC_KEY = "ck6f7kcdq8jwq5b8";
	private static final String PRIVATE_KEY = "ac3b232be33cce4cf3ce108106d0a93e";
	private static final boolean USE_SANDBOX = true;
	private static final int REQUEST_PAYMENT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final Intent intent;
		if (USE_SANDBOX)
		{
			intent = PaymentFormActivity.getSandboxStartIntent(PaymentActivity.this, MERCHANT_ID, PRIVATE_KEY);
		}
		else
		{
			intent = PaymentFormActivity.getStartIntent(PaymentActivity.this, MERCHANT_ID, PRIVATE_KEY);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivityForResult(intent, REQUEST_PAYMENT);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode != REQUEST_PAYMENT)
		{
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		PaymentFormActivity.processResultIntent(data, new PaymentFormActivity.ActivityResultHandler()
		{
			@Override
			public void onCardSelected(CardStub stub)
			{
				System.out.println("onCardAdded");
				// Venmo Touch card selected.
				// processVenmoTouchCard(stub);
			}

			@Override
			public void onCardAdded(CardDetails details, boolean saveToTouch,
					Map<String, String> encryptedCardDetails)
			{
				// New card added.
				System.out.println("onCardAdded");
				// processNewCard(details, saveToTouch, encryptedCardDetails);
			}

			@Override
			public void onCancelled()
			{
				// Aborted by user.
			}
		});
	}

}
