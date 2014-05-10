package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.TaloolApplication;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.dialog.DialogFactory.DialogPositiveClickListener;
import com.talool.android.persistence.MerchantDao;
import com.talool.android.tasks.MyDealsTask;
import com.talool.android.util.Constants;
import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.SafeSimpleDecimalFormat;
import com.talool.android.util.ThriftHelper;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.Card_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.PaymentDetail_t;
import com.talool.api.thrift.TNotFoundException_t;
import com.talool.api.thrift.TServiceException_t;
import com.talool.api.thrift.TUserException_t;
import com.talool.api.thrift.TransactionResult_t;
import com.talool.thrift.util.ThriftUtil;
import com.venmo.touch.client.VenmoTouchClient;
import com.venmo.touch.controller.VTComboCardViewController;
import com.venmo.touch.model.CardDetails;
import com.venmo.touch.model.CardStub;
import com.venmo.touch.view.VTComboCardView;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author clintz
 * 
 */
public class PaymentActivity extends TaloolActivity implements DialogPositiveClickListener
{
	private static final String LOG_TAG = PaymentActivity.class.getSimpleName();

	private VenmoTouchClient touchClient;
	private VTComboCardView comboView;
	private VTComboCardViewController mComboController;

	private DealOffer_t dealOffer;
	private String errorMessage;
    private String accessCode;

	private class PaymentTask extends AsyncTask<String, Void, TransactionResult_t>
	{
		private PaymentDetail_t paymentDetail;
		private String paymentCode;

		public PaymentTask(final PaymentDetail_t paymentDetail)
		{
			this.paymentDetail = paymentDetail;
		}

		public PaymentTask(final String paymentCode)
		{
			this.paymentCode = paymentCode;
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
            Map<String,String> paymentProperties = null;
            paymentProperties=new HashMap<String,String>(1);

            if(StringUtils.isNotEmpty( accessCode ) )
            {
                paymentProperties.put("merchant_code",accessCode);

            }


			try
			{
				thriftHelper = new ThriftHelper();
			}
			catch (TTransportException e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
				return null;
			}

			try
			{
				if (paymentDetail != null)
				{

					transactionResult = thriftHelper.getClient().purchaseWithCard(dealOffer.getDealOfferId(), paymentDetail, paymentProperties);
				}
				else
				{

					transactionResult = thriftHelper.getClient().purchaseWithCode(dealOffer.getDealOfferId(), paymentCode,paymentProperties);
				}

			}
			catch (TServiceException_t e)
			{
				errorMessage = ErrorMessageCache.getServiceErrorMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			catch (TUserException_t e)
			{
				errorMessage = ErrorMessageCache.getMessage(e.getErrorCode());
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			catch (TNotFoundException_t e)
			{
				errorMessage = ErrorMessageCache.getNotFoundMessage(e.getIdentifier(), e.getKey());
				Log.e(LOG_TAG, errorMessage, e);
			}
			catch (TException e)
			{
				errorMessage = ErrorMessageCache.getNetworkIssueMessage();
				Log.e(LOG_TAG, e.getMessage(), e);
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
		if (Constants.IS_DEVELOPMENT_MODE)
		{
			touchClient = VenmoTouchClient.forSandboxMerchant(this, Constants.getBTMerchantId(),null, Constants.getBTMerchantKey());
		}
		else
		{
			touchClient = VenmoTouchClient.forMerchant(this, Constants.getBTMerchantId(),null, Constants.getBTMerchantKey());
		}

		comboView = (VTComboCardView) findViewById(R.id.combo_view);
		comboView.getNewCardTitle().setText(getResources().getString(R.string.payment_new_card));

		comboView.getSavedCardTitle().setText(getResources().getString(R.string.payment_saved_card));

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
            accessCode = getIntent().getExtras().getString("accessCode");

            ThriftUtil.deserialize(dealOfferBytes, dealOffer);

			TextView titleView = (TextView) findViewById(R.id.payment_deal_offer_detail);
			titleView.setText(dealOffer.getTitle());

			TextView priceView = (TextView) findViewById(R.id.payment_deal_offer_price);
			priceView.setText(formatDealPrice());

			// holder.myDealsMerchantIcon.setText(ApiUtil.getIcon(merchant.category));
		}
		catch (TException e)
		{
			e.printStackTrace();
		}

	}

	private String formatDealPrice()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.payment_price))
				.append(" ")
				.append(new SafeSimpleDecimalFormat(Constants.FORMAT_DECIMAL_MONEY).format(dealOffer.getPrice()));
		return sb.toString();
	}

	public void processVenmoTouchCard(final CardStub stub)
	{
		final PaymentTask task = new PaymentTask(stub.getPaymentMethodCode());
		task.execute(new String[] {});
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

		df = DialogFactory.getAlertDialog("An error has occured", message, getResources().getString(R.string.retry));
		df.show(getFragmentManager(), "dialog");
		errorMessage = null;
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog)
	{
        final MerchantDao merchantDao = new MerchantDao(TaloolApplication.getAppContext());
        final Context context = this;
        MyDealsTask task = new MyDealsTask(client, this, merchantDao) {

            @Override
            protected void onPreExecute() {
                df = DialogFactory.getProgressDialog();
                df.show(getFragmentManager(), "dialog");
            }

            @Override
            protected void onPostExecute(final List<Merchant_t> results) {
                if (df != null && !df.isHidden()) {
                    df.dismiss();
                }
                if (results != null) {
                    merchantDao.saveMerchants(results);
                    final Intent myIntent = new Intent(context, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putInt(Constants.TAB_SELECTED_KEY, 0);
                    myIntent.putExtras(b);
                    startActivity(myIntent);
                }
            }
        };
        task.execute(new String[]{});
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
