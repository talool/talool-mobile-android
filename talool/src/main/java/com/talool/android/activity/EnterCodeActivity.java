package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.TaloolApplication;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.persistence.MerchantDao;
import com.talool.android.tasks.MyDealsTask;
import com.talool.android.util.Constants;
import com.talool.android.util.ErrorMessageCache;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.ValidateCodeResponse_t;
import com.talool.thrift.util.ThriftUtil;

import org.apache.thrift.TException;

import java.util.List;

/**
 * Created by zachmanc on 4/16/14.
 */
public class EnterCodeActivity extends TaloolActivity  implements DialogFactory.DialogClickListener {

    private DealOffer_t closestBook;
    private Button loadDealsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enter_code_activity);
        try {
            byte[] dealOfferBytes = (byte[]) getIntent().getSerializableExtra("dealOffer");
            closestBook = new DealOffer_t();
            ThriftUtil.deserialize(dealOfferBytes, closestBook);
            loadDealsButton = (Button) findViewById(R.id.loadDealsButton);
            loadDealsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//                    final Intent myIntent = new Intent(v.getContext(), PaymentActivity.class);
//
//                    myIntent.putExtra("dealOffer", ThriftUtil.serialize(closestBook));
//                    myIntent.putExtra("accessCode",getAccessCode().getBytes());
//                    startActivity(myIntent);


                    ValidateAccessCodeTask task = new ValidateAccessCodeTask(){
                        @Override
                        protected void onPostExecute(ValidateCodeResponse_t result) {
                            if (result != null)
                            {
                                if (result.isValid())
                                {
                                    if(result.codeType.equals("merchant_code"))
                                    {
                                        launchPaymentScreen();
                                    }else if (result.codeType.equals("activation_code")){
                                        redeemBook();
                                    }
                                }else{
                                    popupErrorMessage("Please check your code and try again. If you do not have a code, click the text at the bottom of the page");
                                }
                            }else{
                                popupErrorMessage("Please check your code and try again. If you do not have a code, click the text at the bottom of the page");
                            }

                        }
                    };

                    task.execute();
                }
            });

            TextView noAccessCodeTextView = (TextView) findViewById(R.id.noAccessCodeText);
            noAccessCodeTextView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v)
                {
                    launchPaymentScreen();
                }
            });
        } catch (TException e) {
            e.printStackTrace();
        }


    }

    private void redeemBook()
    {
        RedeemBook redeemBookTask = new RedeemBook() {
            @Override
            protected void onPostExecute(Void results) {
                super.onPostExecute(results);
                handleRedeemBookResponse(results);
            }
        };
        redeemBookTask.execute(new Void[]{});
        EditText editText;
        editText = (EditText) findViewById(R.id.accessCode);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    protected void handleRedeemBookResponse (Void results){
        if (df != null && !df.isHidden()) {
            df.dismiss();
        }
        if (exception != null) {
            if (exception instanceof ServiceException_t) {
                EasyTracker easyTracker = EasyTracker.getInstance(this);
                easyTracker.send(MapBuilder
                        .createEvent("redeemBook", "failure", ((ServiceException_t) exception).getErrorDesc(), null)
                        .build());

                popupErrorMessage(ErrorMessageCache.getMessage(((ServiceException_t) exception).getErrorCode()));

            } else {
                EasyTracker easyTracker = EasyTracker.getInstance(this);

                easyTracker
                        .send(MapBuilder
                                        .createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception),
                                                true)
                                        .build()
                        );
                popupErrorMessage(exception.getMessage());
            }
        }else {
            EasyTracker easyTracker = EasyTracker.getInstance(this);
            easyTracker.send(MapBuilder
                            .createEvent("redeem_book", "selected", closestBook.dealOfferId, null)
                            .build()
            );

            String title = getResources().getString(R.string.alert_new_deals_title);
            String message = getResources().getString(R.string.alert_new_deals_message);
            df = DialogFactory.getConfirmDialog(title, message, EnterCodeActivity.this);
            df.show(getFragmentManager(), "dialog");

        }

    }

    private void launchPaymentScreen(){
        final Intent myIntent = new Intent(this, PaymentActivity.class);
        myIntent.putExtra("dealOffer", ThriftUtil.serialize(closestBook));
        myIntent.putExtra("accessCode",getAccessCode());
        startActivity(myIntent);
    }

    private void redirectToMyDeals() {
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
        task.execute();


    }

    private class RedeemBook extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            df = DialogFactory.getProgressDialog();
            df.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                exception = null;
                client.getClient().activateCode(closestBook.dealOfferId, getAccessCode());
            } catch (ServiceException_t e) {
                exception = e;
            } catch (TException e) {
                exception = e;
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }
    }

    private class ValidateAccessCodeTask extends AsyncTask<Void, Void, ValidateCodeResponse_t> {

        @Override
        protected void onPreExecute() {
            df = DialogFactory.getProgressDialog();
            df.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(ValidateCodeResponse_t result) {
            super.onPostExecute(result);
        }

        @Override
        protected ValidateCodeResponse_t doInBackground(Void... arg0) {
            ValidateCodeResponse_t response = null;
            try {
                exception = null;
                response = client.getClient().validateCode(getAccessCode(), closestBook.dealOfferId);
                return response;
            } catch (ServiceException_t e) {
                exception = e;
            } catch (TException e) {
                exception = e;
            } catch (Exception e) {
                exception = e;
            } finally {
                return response;

            }
        }
    }

    private String getAccessCode() {
        EditText editText;
        editText = (EditText) findViewById(R.id.accessCode);
        if ((editText != null) && (editText.getText() != null)) {
            return editText.getText().toString();
        } else {
            return null;
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        redirectToMyDeals();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
