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

import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.braintreepayments.api.dropin.Customization;
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
    private static final int REQUEST_CODE = 1;

	private DealOffer_t dealOffer;
	private String errorMessage;
    private String accessCode;

    private class ClientTokenTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            df = DialogFactory.getProgressDialog();
            df.show(getFragmentManager(), "dialog");
        }

        protected void onPostExecute(final String token)
        {
            showPaymentActivity(token);
        }

        @Override
        protected String doInBackground(final String... params)
        {
            String token = null;
            try
            {
                token = client.getClient().generateBraintreeClientToken();
            }
            catch (Exception e)
            {
                // TODO do something
                exception = e;
            }
            return token;
        }
    }

    private void showPaymentActivity(String token)
    {
        Intent intent = new Intent(this, BraintreePaymentActivity.class);

        // add the client token
        intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, token);

        // customize the view
        Customization customization = new Customization.CustomizationBuilder()
                .primaryDescription(dealOffer.getTitle())
                .secondaryDescription(formatDealPrice())
                .submitButtonText("Buy Now")
                .build();
        intent.putExtra(BraintreePaymentActivity.EXTRA_CUSTOMIZATION, customization);

        // add our extra bits...
        intent.putExtra("dealOffer", ThriftUtil.serialize(dealOffer));
        intent.putExtra("accessCode",accessCode);

        startActivityForResult(intent, REQUEST_CODE);
    }

	private class PaymentTask extends AsyncTask<String, Void, TransactionResult_t>
	{
		private String nonce;

		public PaymentTask(final String nonce)
		{
			this.nonce = nonce;
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
			TransactionResult_t transactionResult = null;
            Map<String,String> paymentProperties = new HashMap<String,String>(1);

            if(StringUtils.isNotEmpty( accessCode ) )
            {
                paymentProperties.put("merchant_code",accessCode);
            }

			try
			{
				transactionResult = client.getClient().purchaseWithNonce(dealOffer.getDealOfferId(), nonce, paymentProperties);
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

        final ClientTokenTask task = new ClientTokenTask();
        task.execute(new String[] {});

        final byte[] dealOfferBytes = (byte[]) getIntent().getSerializableExtra("dealOffer");

        dealOffer = new DealOffer_t();
        try
        {
            accessCode = getIntent().getExtras().getString("accessCode");
            ThriftUtil.deserialize(dealOfferBytes, dealOffer);
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
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == BraintreePaymentActivity.RESULT_OK) {
                final String paymentMethodNonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

                final PaymentTask task = new PaymentTask(paymentMethodNonce);
                task.execute(new String[] {});

            }
        }
    }

}
