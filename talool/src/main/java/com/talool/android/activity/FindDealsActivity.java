package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.TaloolApplication;
import com.talool.android.adapters.FindDealsAdapter;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.persistence.MerchantDao;
import com.talool.android.tasks.MyDealsTask;
import com.talool.android.tasks.PaymentTask;
import com.talool.android.util.Constants;
import com.talool.android.util.SafeSimpleDecimalFormat;
import com.talool.android.util.TaloolSmartImageView;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.Deal_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.SearchOptions_t;
import com.talool.api.thrift.TransactionResult_t;
import com.talool.thrift.util.ThriftUtil;

import org.apache.thrift.TException;

import java.util.List;

public class FindDealsActivity extends TaloolActivity implements DialogFactory.DialogClickListener, DialogFactory.DialogPositiveClickListener {
    protected AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            FindDealsAdapter findDealsAdapter = (FindDealsAdapter) arg0.getAdapter();
            Deal_t deal = (Deal_t) findDealsAdapter.getItem(position);

            Intent myIntent = new Intent(arg1.getContext(), DealSampleActivity.class);
            myIntent.putExtra("deal", ThriftUtil.serialize(deal));
            startActivity(myIntent);
        }
    };
    private List<Deal_t> dealOffers;
    private ListView dealOffersListView;
    private LinearLayout purchaseClickLayout;
    private TaloolSmartImageView bookImage;
    private TextView bookDescription;
    private DealOffer_t closestBook;
    private LinearLayout listViewLinearLayout;
    private String dealOfferSummaryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.find_deals_activity_layout);

        bookImage = (TaloolSmartImageView) findViewById(R.id.bookImageView);
        dealOffersListView = (ListView) findViewById(R.id.dealOffersListView);
        bookDescription = (TextView) findViewById(R.id.dealOfferDescription);
        listViewLinearLayout = (LinearLayout) findViewById(R.id.listViewLinearLayout);
        purchaseClickLayout = (LinearLayout) findViewById(R.id.purchaseClickLayout);
        purchaseClickLayout.setVisibility(View.GONE);

        try {

            dealOfferSummaryText = (String) getIntent().getSerializableExtra("dealOfferSummaryText");

            byte[] dealOfferBytes = (byte[]) getIntent().getSerializableExtra("dealOffer");
            closestBook = new DealOffer_t();
            ThriftUtil.deserialize(dealOfferBytes, closestBook);
            setTitle(closestBook.getTitle());
            loadBookDetails();
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public void onPurchaseClick(View view) {
        if (closestBook.getPrice() > 0) {
            final Intent myIntent = new Intent(view.getContext(), EnterCodeActivity.class);
            myIntent.putExtra("dealOffer", ThriftUtil.serialize(closestBook));
            startActivity(myIntent);
        } else {
            final PaymentTask task = new PaymentTask(closestBook, "", "", this, client) {

                @Override
                protected void onPreExecute() {
                    df = DialogFactory.getProgressDialog();
                    df.show(getFragmentManager(), "dialog");
                    this.errorMessage = null;
                }

                @Override
                protected void onPostExecute(final TransactionResult_t transactionResult) {
                    if (df != null && !df.isHidden()) {
                        df.dismiss();
                    }

                    if (this.errorMessage == null) {
                        if (transactionResult.isSuccess()) {
                            String title = getResources().getString(R.string.payment_trans_success_title);
                            String message = String.format(getResources().getString(R.string.free_book_trans_success_message), closestBook.getTitle());
                            String label = getResources().getString(R.string.payment_trans_success_positive_label);
                            df = DialogFactory.getAlertDialog(title, message, label, FindDealsActivity.this);
                            df.show(getFragmentManager(), "dialog");
                        } else {
                            popupErrorMessage(transactionResult.getMessage());
                        }
                    } else {
                        popupErrorMessage(this.errorMessage);
                    }
                }
            };
            task.execute();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (df != null && !df.isHidden()) {
            df.dismiss();
        }
        reloadData();
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, "Find Deals");
        easyTracker.send(MapBuilder.createAppView().build());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void reloadData() {
        loadBookDetails();
        loadBookDeals();
    }

    private void loadBookDetails() {
        if (closestBook.imageUrl != null) {
            bookImage.setImageUrl(closestBook.imageUrl);
        }

        if (closestBook.summary != null) {
            bookDescription.setText(closestBook.summary);
        }

        if (dealOfferSummaryText != null) {
            final TextView textView = (TextView) findViewById(R.id.summaryText);
            textView.setText(dealOfferSummaryText);
        }

        purchaseClickLayout.setVisibility(View.VISIBLE);
        final TextView buyNowView = (TextView) findViewById(R.id.buy_now_text);
        buyNowView.setText(getBuyNowText());
    }

    private void loadBookDeals() {
        FindDealsTask dealsTask = new FindDealsTask();
        dealsTask.execute(new String[]{});
    }

    private void loadListView() {
        FindDealsAdapter adapter = new FindDealsAdapter(this,
                R.layout.find_deal_row, dealOffers);
        FindDealsAdapter dealOffersAdapter = adapter;
        dealOffersListView.setAdapter(dealOffersAdapter);
        setListViewHeightBasedOnChildren(dealOffersListView);
    }

    private String getBuyNowText() {
        if (closestBook.getPrice() > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(getResources().getString(R.string.find_deal_buy_now)).append(" ").
                    append(new SafeSimpleDecimalFormat(Constants.FORMAT_DECIMAL_MONEY).format(closestBook.getPrice()));
            return sb.toString();
        }
        return getResources().getString(R.string.free_deal_get_now);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        redirectToMyDeals();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
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

    private class FindDealsTask extends AsyncTask<String, Void, List<Deal_t>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(final List<Deal_t> results) {
            if (exception != null) {
                popupErrorMessage(exception.getMessage());
            } else {
                dealOffers = results;
                loadListView();
            }
        }

        @Override
        protected List<Deal_t> doInBackground(String... arg0) {
            List<Deal_t> results = null;

            try {
                exception = null;
                final SearchOptions_t searchOptions = new SearchOptions_t();
                searchOptions.setMaxResults(1000).setPage(0).setSortProperty("title").setAscending(true);
                results = client.getClient().getDealsByDealOfferId(closestBook.dealOfferId, searchOptions);
            } catch (Exception e) {
                exception = e;
            }

            return results;
        }
    }


}
