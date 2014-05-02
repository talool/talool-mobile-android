package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.loopj.android.image.SmartImageView;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.cache.DealOfferCache;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.dialog.DialogFactory.DialogPositiveClickListener;
import com.talool.android.tasks.DealOfferFetchTask;
import com.talool.android.tasks.GiftAcceptanceTask;
import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.TaloolSmartImageView;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.ThriftHelper;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.Activity_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.GiftStatus_t;
import com.talool.api.thrift.Gift_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.thrift.util.ThriftUtil;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

/**
 * @author clintz
 * @TODO Wire up proper exception handling/logging
 */
public class GiftActivity extends TaloolActivity {
    public static String GIFT_ID_PARAM = "giftId";
    public static String ACTIVITY_OBJ_PARAM = "activityObj";

    private String giftId;
    private Activity_t activity;
    private TaloolSmartImageView dealImageView;
    private SmartImageView logoImageView;
    private SmartImageView dealCreatorImageView;
    private TextView fromFriend;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gift_activity);
        if (TaloolUser.get().getAccessToken() == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        try {
            client = new ThriftHelper();
        } catch (TTransportException e) {
            sendExceptionToAnalytics(e);
        }

        byte[] activityObjBytes = (byte[]) getIntent().getSerializableExtra(ACTIVITY_OBJ_PARAM);
        if (activityObjBytes != null && activityObjBytes.length > 0) {
            activity = new Activity_t();
            try {
                ThriftUtil.deserialize(activityObjBytes, activity);
            } catch (TException e) {
                sendExceptionToAnalytics(e);
            }

            giftId = activity.getActivityLink().getLinkElement();
        } else {
            // Deep link. Parse the URI
            final Uri uri;
            try {
                uri = getIntent().getData();
                giftId = uri.getPathSegments().get(0);
            } catch (Exception e) {
                Log.e("ParseDeepLinkForGift", e.getLocalizedMessage());
            }
        }

        linearLayout = (LinearLayout) findViewById(R.id.giftLinearLayout);
        final GiftActivityTask dealsTask = new GiftActivityTask();
        dealsTask.execute();

        final TextView thumbsDownIcon = (TextView) findViewById(R.id.thumbsUpIcon);
        thumbsDownIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

        final TextView thumbsUpIcon = (TextView) findViewById(R.id.thumbsDownIcon);
        thumbsUpIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

        fromFriend = (TextView) findViewById(R.id.fromFriend);

    }

    private void setDealCreatorImageView(final DealOffer_t dealOffer) {
        dealCreatorImageView = (SmartImageView) findViewById(R.id.dealCreatorLogo);
        dealCreatorImageView.setImageUrl(dealOffer.getImageUrl());
    }

    public void acceptGiftClick(final View view) {
        if (activity != null) {
            // accept the gift and finish to return to My Activity
            final GiftAcceptanceTask task = new GiftAcceptanceTask(client, activity, true, view.getContext()) {
                @Override
                protected void onPostExecute(DealAcquire_t result) {
                    finish();
                }

            };

            task.execute(new String[]{});
        } else {
            // Deep Link Acceptance
            // accept the gift and redirect to "My Deals"
            final GiftAcceptanceTask task = new GiftAcceptanceTask(client, giftId, true, view.getContext()) {
                @Override
                protected void onPostExecute(DealAcquire_t result) {
                    final Intent intent = new Intent(GiftActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

            };

            task.execute();
        }
    }

    public void rejectGiftClick(final View view) {
        if (activity != null) {
            // reject the gift and finish to return to My Activity
            final GiftAcceptanceTask task = new GiftAcceptanceTask(client, activity, false, view.getContext()) {
                @Override
                protected void onPostExecute(DealAcquire_t result) {
                    finish();
                }

            };

            task.execute();
        } else {
            // Deep Link Rejection
            // reject the gift and redirect to "My Deals"
            final GiftAcceptanceTask task = new GiftAcceptanceTask(client, giftId, false, view.getContext()) {
                @Override
                protected void onPostExecute(DealAcquire_t result) {
                    redirectToMain();
                }

            };

            task.execute();
        }
    }

    private void redirectToMain() {
        final Intent intent = new Intent(GiftActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private class GiftActivityTask extends AsyncTask<String, Void, Gift_t> {
        @Override
        protected void onPreExecute() {
            df = DialogFactory.getProgressDialog();
            df.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(final Gift_t gift) {
            if (gift == null) {
                popupErrorMessage(exception, errorMessage);
                return;
            }
            linearLayout.setVisibility(View.VISIBLE);

            final DealOffer_t dealOffer = DealOfferCache.get().getDealOffer(gift.getDeal().getDealOfferId());
            if (dealOffer == null) {
                final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, gift.getDeal().getDealOfferId(), getApplicationContext()) {

                    @Override
                    protected void onPostExecute(final DealOffer_t dealOffer) {
                        if (dealOffer != null) {
                            setDealCreatorImageView(dealOffer);
                            // make sure we cache the dealOffer
                            DealOfferCache.get().setDealOffer(dealOffer);
                        }
                    }

                };

                dealOfferFetchTask.execute();
            } else {
                setDealCreatorImageView(dealOffer);
            }

            final TextView summary = (TextView) findViewById(R.id.summary);
            final TextView details = (TextView) findViewById(R.id.details);

            setTitle("A Gift to " + gift.getDeal().getMerchant().getName());

            String fn = gift.getFromCustomer().firstName;
            if (fn.length() > 10) {
                fn = (new StringBuilder(fn.substring(0, 7)).append("...")).toString();
            }
            StringBuilder fromFriendLabel = new StringBuilder("From\n");
            fromFriend.setText(fromFriendLabel.append(fn).toString());

            summary.setText(gift.getDeal().getSummary());
            summary.setTypeface(TypefaceFactory.get().getFontAwesome());
            details.setText(gift.getDeal().getDetails());
            details.setTypeface(TypefaceFactory.get().getFontAwesome());

            dealImageView = (TaloolSmartImageView) findViewById(R.id.dealImage);
            dealImageView.setImageUrl(gift.getDeal().getImageUrl());

            logoImageView = (SmartImageView) findViewById(R.id.merchantLogo);
            logoImageView.setImageUrl(gift.getDeal().getMerchant().getLocations().get(0).getLogoUrl());

            final TextView address1 = (TextView) findViewById(R.id.address1);

            final MerchantLocation_t location = gift.getDeal().getMerchant().getLocations().get(0);

            final TextView expires = (TextView) findViewById(R.id.expires);
            expires.setText(TaloolUtil.getExpirationText(gift.getDeal().getExpires()));

            StringBuilder sb = new StringBuilder(location.address.address1);
            if (location.address.address2 != null) {
                sb.append("\n").append(location.address.address2);
            }
            sb.append("\n")
                    .append(location.address.city)
                    .append(", ")
                    .append(location.address.stateProvinceCounty)
                    .append(" ")
                    .append(location.address.zip);

            address1.setText(sb.toString());

            if (df != null && !df.isHidden()) {
                df.dismiss();
            }

        }

        @Override
        protected Gift_t doInBackground(final String... arg0) {
            Gift_t gift = null;

            try {
                client.setAccessToken(TaloolUser.get().getAccessToken());
                gift = client.getClient().getGift(giftId);

                if (gift != null) {
                    if (gift.getGiftStatus().equals(GiftStatus_t.ACCEPTED)) {
                        // TODO replace with proper routing to the deal in future release.
                        // The future release will require persisting dealAcquires/gifts
                        gift = null;
                        exception = new Exception(ErrorMessageCache.Message.AlreadyAcceptedGift.getText());

                    }
                }

            } catch (ServiceException_t e) {
                errorMessage = e.getErrorDesc();
                exception = e;
            } catch (TException e) {
                if (e instanceof TTransportException) {
                    errorMessage = "Make sure you have a network connection";
                }
                exception = e;
            } catch (Exception e) {
                exception = e;
            }
            return gift;
        }
    }

    @Override
    protected void popupErrorMessage(Exception exception, String errorMessage) {

        EasyTracker easyTracker = EasyTracker.getInstance(this);

        easyTracker.send(MapBuilder
                .createException(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), exception), true)
                .build()
        );

        if (df != null && !df.isHidden()) {
            df.dismiss();
        }
        String message = errorMessage == null ? exception.getMessage() : errorMessage;
        String title = getResources().getString(R.string.error_loading_gifts);
        String label = getResources().getString(R.string.ok);
        df = DialogFactory.getAlertDialog(title, message, label, new DialogPositiveClickListener() {
            @Override
            public void onDialogPositiveClick(DialogFragment dialog) {
                redirectToMain();
                finish();
            }
        });
        df.show(getFragmentManager(), "dialog");

    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (df != null && !df.isHidden()) {
            df.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            ret = true;
        } else {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }
}
