package com.talool.mobile.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.talool.api.thrift.Card_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.PaymentDetail_t;
import com.talool.api.thrift.TNotFoundException_t;
import com.talool.api.thrift.TServiceException_t;
import com.talool.api.thrift.TUserException_t;
import com.talool.api.thrift.TransactionResult_t;
import com.talool.mobile.android.dialog.DialogFactory;
import com.talool.mobile.android.dialog.DialogFactory.DialogPositiveClickListener;
import com.talool.mobile.android.util.Constants;
import com.talool.mobile.android.util.ErrorMessageCache;
import com.talool.mobile.android.util.SafeSimpleDecimalFormat;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;
import com.venmo.touch.client.VenmoTouchClient;
import com.venmo.touch.controller.VTComboCardViewController;
import com.venmo.touch.model.CardDetails;
import com.venmo.touch.model.CardStub;
import com.venmo.touch.view.VTComboCardView;

/**
 * 
 * @author clintz
 * 
 */
public class PaymentActivity extends Activity implements DialogPositiveClickListener
{
	private static final String LOG_TAG = PaymentActivity.class.getSimpleName();

	private VenmoTouchClient touchClient;
	private VTComboCardView comboView;
	private VTComboCardViewController mComboController;

	private DealOffer_t dealOffer;
	private DialogFragment df;
	private String errorMessage;

	private class PaymentTask extends AsyncTask<String, Void, TransactionResult_t>
	{
		private final PaymentDetail_t paymentDetail;

		public PaymentTask(final PaymentDetail_t paymentDetail)
		{
			this.paymentDetail = paymentDetail;
		}

		@Override
		protected void onPreExecute()
		{
			df = DialogFactory.getProgressDialog();
			df.show(getFragmentManager(), "dialog");
		}

		protected void onPostExecute(final TransactionResult_t transactionResult)
		{
			if (df != null && !df.isHidden())
			{
				df.dismiss();
			}

			if (errorMessage != null)
			{
				popupErrorMessage(errorMessage);
				return;
			}

			if (!transactionResult.isSuccess())
			{
				popupErrorMessage(transactionResult.getMessage());
				return;
			}

			df = DialogFactory.getAlertDialog(getResources().getString(R.string.payment_trans_success_title),
					String.format(getResources().getString(R.string.payment_trans_success_message), dealOffer.getTitle()),
					getResources().getString(R.string.payment_trans_success_positive_label),
					PaymentActivity.this);

			df.show(getFragmentManager(), "dialog");
		}

		@Override
		protected TransactionResult_t doInBackground(final String... params)
		{
			ThriftHelper thriftHelper = null;
			TransactionResult_t transactionResult = null;

			try
			{
				thriftHelper = new ThriftHelper();
			}
			catch (TTransportException e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.i(LOG_TAG, e.getMessage(), e);
				return null;
			}

			try
			{
				transactionResult = thriftHelper.getClient().purchaseByCard(dealOffer.getDealOfferId(), paymentDetail);
			}
			catch (TServiceException_t e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.i(LOG_TAG, e.getMessage(), e);
			}
			catch (TUserException_t e)
			{
				errorMessage = ErrorMessageCache.getMessage(e.getErrorCode());
				Log.i(LOG_TAG, e.getErrorCode().name(), e);
			}
			catch (TNotFoundException_t e)
			{
				errorMessage = ErrorMessageCache.getNotFoundMessage(e.getIdentifier(), e.getKey());
				Log.i(LOG_TAG, errorMessage, e);
			}
			catch (TException e)
			{
				errorMessage = ErrorMessageCache.getNetworkIssueMessage();
				Log.i(LOG_TAG, e.getMessage(), e);
			}

			return transactionResult;

		}
	}

	public boolean onCreateOptionsMenu(final Menu menu)
	{
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.payment_secure_lock_bar, menu);

		final LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
		final View view = layoutInflater.inflate(R.layout.payment_secure_lock_layout, null);

		final TextView secureLockText = (TextView) view.findViewById(R.id.secure_lock_text);
		secureLockText.setTypeface(TypefaceFactory.get().getFontAwesome());

		menu.getItem(0).setActionView(view);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.venmo_payment_activity_layout);
		touchClient = VenmoTouchClient.forSandboxMerchant(this, Constants.BRAINTREE_MERCHANT_ID, Constants.BRAINTREE_MERCHANT_KEY);
		comboView = (VTComboCardView) findViewById(R.id.combo_view);
		comboView.getNewCardTitle().setText("Add Card");

		mComboController = new VTComboCardViewController(this, touchClient, comboView);

		mComboController.setListener(new VTComboCardViewController.Listener()
		{
			@Override
			public void onCardSelected(CardStub card)
			{
				processVenmoTouchCard(card);
			}

			@Override
			public void onCardDetailsSubmitted(CardDetails details, boolean addToTouch,
					Map<String, String> encryptedCardDetails)
			{
				processNewCard(details, addToTouch, encryptedCardDetails);
			}

			@Override
			public void onCardListUpdated(List<CardStub> cards)
			{}
		});

		final byte[] dealOfferBytes = (byte[]) getIntent().getSerializableExtra("dealOffer");
		dealOffer = new DealOffer_t();

		try
		{
			ThriftUtil.deserialize(dealOfferBytes, dealOffer);

			TextView textView = (TextView) findViewById(R.id.payment_deal_offer_detail);
			textView.setText(formatDealDetail());

			// holder.myDealsMerchantIcon.setText(ApiUtil.getIcon(merchant.category));
		}
		catch (TException e)
		{
			e.printStackTrace();
		}

	}

	private String formatDealDetail()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(dealOffer.getTitle()).append(" - ").append(
				new SafeSimpleDecimalFormat(Constants.FORMAT_DECIMAL_MONEY).format(dealOffer.getPrice()));
		return sb.toString();
	}

	public void processVenmoTouchCard(final CardStub stub)
	{

	}

	public void processNewCard(final CardDetails details, final boolean saveToTouch,
			final Map<String, String> encryptedCardDetails)
	{
		final PaymentDetail_t paymentDetail = new PaymentDetail_t();
		paymentDetail.setSaveCard(saveToTouch);
		paymentDetail.setEncryptedFields(true);

		final Card_t card = new Card_t().setAccountNumber(encryptedCardDetails.get(CardDetails.KEY_ACCOUNT_NUMBER));
		card.setExpirationMonth(encryptedCardDetails.get(CardDetails.KEY_EXPIRATION_MONTH));
		card.setExpirationYear(encryptedCardDetails.get(CardDetails.KEY_EXPIRATION_YEAR));
		card.setSecurityCode(encryptedCardDetails.get(CardDetails.KEY_CVV));
		card.setZipCode(encryptedCardDetails.get(CardDetails.KEY_ZIPCODE));
		paymentDetail.setCard(card);

		final Map<String, String> paymentMetadata = new HashMap<String, String>();
		paymentMetadata.put(Constants.VENMO_SDK_SESSION, encryptedCardDetails.get(Constants.VENMO_SDK_SESSION));
		paymentDetail.setPaymentMetadata(paymentMetadata);

		final PaymentTask task = new PaymentTask(paymentDetail);
		task.execute(new String[] {});
	}

	public void popupErrorMessage(String message)
	{
		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}

		df = DialogFactory.getAlertDialog("", message, getResources().getString(R.string.retry));
		df.show(getFragmentManager(), "dialog");
		errorMessage = null;
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
		final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		touchClient.start();
		mComboController.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		mComboController.onStop();
		touchClient.stop();
	}

}
