package com.talool.android.activity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.loopj.android.image.SmartImageView;
import com.talool.android.R;
import com.talool.android.cache.DealOfferCache;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.dialog.DialogFactory.DialogClickListener;
import com.talool.android.tasks.DealOfferFetchTask;
import com.talool.android.tasks.DealRedemptionTask;
import com.talool.android.tasks.FacebookGiftIdTask;
import com.talool.android.tasks.FacebookShareTask;
import com.talool.android.util.AlertMessage;
import com.talool.android.util.AndroidUtils;
import com.talool.android.util.ErrorMessageCache;
import com.talool.android.util.FacebookHelper;
import com.talool.android.util.TaloolSmartImageView;
import com.talool.android.util.TaloolUser;
import com.talool.android.util.TaloolUtil;
import com.talool.android.util.TypefaceFactory;
import com.talool.api.thrift.AcquireStatus_t;
import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.DealOffer_t;
import com.talool.api.thrift.MerchantLocation_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.thrift.util.ThriftUtil;

public class DealActivity extends TaloolActivity {
    private static final int REAUTH_ACTIVITY_CODE = 300;
    private static final int FACEBOOK_REQUEST_CODE = 666;
    private boolean pendingAnnounce;
    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private DealAcquire_t deal;
    private Merchant_t merchant;
    private TaloolSmartImageView dealMerchantImage;
    private SmartImageView logoImageView;
    private SmartImageView dealOfferCreatorImage;
    private TextView dealAddressText;
    private TextView dealSummaryText;
    private TextView dealValidText;
    private TextView dealExpirationText;
    private TextView offerValidAtText;
    private LinearLayout dealActivityButtonLayout;
    private String redemptionCode;
    private String email;
    private String name;
    private Session facebookSession;
    private String giftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deal_activity_layout);

        logoImageView = (SmartImageView) findViewById(R.id.dealLogoImage);
        dealMerchantImage = (TaloolSmartImageView) findViewById(R.id.dealMerchantImage);
        dealAddressText = (TextView) findViewById(R.id.dealAddressText);
        dealValidText = (TextView) findViewById(R.id.dealValidText);
        offerValidAtText = (TextView) findViewById(R.id.offValidAtText);
        dealSummaryText = (TextView) findViewById(R.id.dealSummaryText);
        dealOfferCreatorImage = (SmartImageView) findViewById(R.id.dealActivityCreatorImage);
        dealExpirationText = (TextView) findViewById(R.id.dealActivityExpires);
        dealActivityButtonLayout = (LinearLayout) findViewById(R.id.dealActivityButtonLayout);
        redemptionCode = null;

        dealAddressText.setClickable(true);

        OnClickListener mapClickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), MapActivity.class);
                myIntent.putExtra("merchant", ThriftUtil.serialize(merchant));
                startActivity(myIntent);

            }
        };

        dealAddressText.setOnClickListener(mapClickListener);
        offerValidAtText.setOnClickListener(mapClickListener);

        facebookSession = Session.getActiveSession();
        if (TaloolUser.get().getAccessToken() == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        try {
            byte[] dealBytes = (byte[]) getIntent().getSerializableExtra("deal");
            byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");

            deal = new DealAcquire_t();
            merchant = new Merchant_t();
            ThriftUtil.deserialize(dealBytes, deal);
            ThriftUtil.deserialize(merchantBytes, merchant);

            setText();
            loadImages();
            checkDealStatus();
            setDealCreatorImage();

        } catch (TException e) {
            sendExceptionToAnalytics(e);
        }

    }

    private void updateBackgroundForFacebookShare(String name) {
        dealActivityButtonLayout.removeAllViewsInLayout();
        dealActivityButtonLayout.setBackgroundDrawable(null);
        TextView redemptionCodeTextView = new TextView(DealActivity.this);
        redemptionCodeTextView.setText("Gifted to " + name);
        redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
        redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
        redemptionCodeTextView.setPadding(30, 0, 30, 0);
        dealActivityButtonLayout.addView(redemptionCodeTextView);
        dealActivityButtonLayout.setGravity(Gravity.CENTER);
        dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
        dealActivityButtonLayout.setPadding(0, 0, 0, 0);
    }

    private void checkDealStatus() {

        if (deal.status == AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE) {
            dealActivityButtonLayout.removeAllViewsInLayout();
            dealActivityButtonLayout.setBackgroundDrawable(null);
            TextView redemptionCodeTextView = new TextView(DealActivity.this);
            redemptionCodeTextView.setText(TaloolUtil.getGiftedText(deal.updated));
            redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
            redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
            redemptionCodeTextView.setPadding(30, 0, 30, 0);
            dealActivityButtonLayout.addView(redemptionCodeTextView);
            dealActivityButtonLayout.setGravity(Gravity.CENTER);
            dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
            dealActivityButtonLayout.setPadding(0, 0, 0, 0);
            return;
        } else if (deal.redeemed != 0) {
            dealActivityButtonLayout.removeAllViewsInLayout();
            dealActivityButtonLayout.setBackgroundDrawable(null);
            TextView redemptionCodeTextView = new TextView(DealActivity.this);
            redemptionCodeTextView.setText(TaloolUtil.getRedeemedText(deal.redemptionCode,deal.redeemed));
            redemptionCodeTextView.setGravity(Gravity.CENTER);
            redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
            redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
            redemptionCodeTextView.setPadding(30, 0, 30, 0);
            dealActivityButtonLayout.addView(redemptionCodeTextView);
            dealActivityButtonLayout.setGravity(Gravity.CENTER);
            dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
            dealActivityButtonLayout.setPadding(0, 0, 0, 0);
            return;
        }else if (TaloolUtil.isExpired(deal.deal.expires)){
            dealActivityButtonLayout.removeAllViewsInLayout();
            dealActivityButtonLayout.setBackgroundDrawable(null);
            TextView redemptionCodeTextView = new TextView(DealActivity.this);
            redemptionCodeTextView.setText(TaloolUtil.getExpiredText(deal.deal.expires));
            redemptionCodeTextView.setGravity(Gravity.CENTER);
            redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
            redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
            redemptionCodeTextView.setPadding(30, 0, 30, 0);
            dealActivityButtonLayout.addView(redemptionCodeTextView);
            dealActivityButtonLayout.setGravity(Gravity.CENTER);
            dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
            dealActivityButtonLayout.setPadding(0, 0, 0, 0);
        }

    }

    private void setDealCreatorImage() {
        final DealOfferFetchTask dealOfferFetchTask = new DealOfferFetchTask(client, deal.getDeal().getDealOfferId(), DealActivity.this) {

            @Override
            protected void onPostExecute(final DealOffer_t dealOffer) {
                if (dealOffer != null) {
                    setDealCreatorImageView(dealOffer);
                    // make sure we cache the dealOffer
                    DealOfferCache.get().setDealOffer(dealOffer);
                }
            }

        };

        dealOfferFetchTask.execute(new String[]{});
    }

    private void setDealCreatorImageView(DealOffer_t dealOffer) {
        dealOfferCreatorImage.setImageUrl(dealOffer.getImageUrl());
    }

    private void loadImages() {
        if (deal.deal.imageUrl != null) {
            dealMerchantImage.setImageUrl(deal.deal.imageUrl);
        }

        if (merchant.locations.get(0).logoUrl != null) {
            logoImageView.setImageUrl(merchant.locations.get(0).logoUrl);
        }

    }

    private void setText() {
        setAddressText();
        dealSummaryText.setText(deal.deal.summary);
        dealValidText.setText(deal.deal.details);
        dealValidText.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.NORMAL);
        if(TaloolUtil.isExpired(deal.deal.expires)){
            dealExpirationText.setText(TaloolUtil.getExpiredText(deal.deal.expires));
        }else{
            dealExpirationText.setText(TaloolUtil.getExpirationText(deal.deal.expires));
        }
        setTitle(merchant.name);

        final TextView useDealIcon = (TextView) findViewById(R.id.useDealIcon);
        if (useDealIcon != null) {
            useDealIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
        }

        final TextView facebookIcon = (TextView) findViewById(R.id.facebookIcon);
        if (facebookIcon != null) {
            facebookIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
        }

        final TextView giftIcon = (TextView) findViewById(R.id.giftIcon);
        if (giftIcon != null) {
            giftIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
        }

    }

    public void onUseDealNowClick(final View view) {
        df = DialogFactory.getConfirmDialog(getResources().getString(R.string.please_confirm),
                getResources().getString(R.string.deal_activity_confirm_redeem_message), new DialogClickListener() {

            @Override
            public void onDialogPositiveClick(DialogFragment dialog) {
                DealRedemptionTask dealAcceptanceTask = new DealRedemptionTask(client, deal.dealAcquireId, view.getContext()) {
                    @Override
                    protected void onPreExecute() {
                        df = DialogFactory.getProgressDialog();
                        df.show(getFragmentManager(), "dialog");
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        if (df != null && !df.isHidden()) {
                            df.dismiss();
                        }

                        redemptionCode = result;
                        dealActivityButtonLayout.removeAllViewsInLayout();
                        dealActivityButtonLayout.setBackgroundDrawable(null);
                        TextView redemptionCodeTextView = new TextView(DealActivity.this);
                        redemptionCodeTextView.setText(TaloolUtil.getRedeemedText(redemptionCode,new Date().getTime()));
                        redemptionCodeTextView.setGravity(Gravity.CENTER);
                        redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
                        redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
                        redemptionCodeTextView.setPadding(30, 0, 30, 0);
                        dealActivityButtonLayout.addView(redemptionCodeTextView);
                        dealActivityButtonLayout.setGravity(Gravity.CENTER);
                        dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));
                        dealActivityButtonLayout.setPadding(0, 0, 0, 0);

                        EasyTracker easyTracker = EasyTracker.getInstance(view.getContext());
                        easyTracker.send(MapBuilder
                                .createEvent("redeem", "selected", deal.dealAcquireId, null)
                                .build());
                    }

                };
                dealAcceptanceTask.execute(new String[]{});

            }

            @Override
            public void onDialogNegativeClick(DialogFragment dialog) {
                // do nothing
            }
        });

        df.show(getFragmentManager(), "dialog");

    }

    private void setAddressText() {
        if (merchant.locations.size() > 1) {
            dealAddressText.setText("Multiple Locations \ncheck the map >");
        } else {
            MerchantLocation_t location = merchant.locations.get(0);
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

            dealAddressText.setText(sb.toString());
        }
    }

    public void onGiftViaEmail(View view) {
        try {
            if (Build.VERSION.SDK_INT < 16) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 100);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        } catch (Exception e) {
            AlertMessage alertMessage = new AlertMessage("Gift Via Email Picker", "Error on Picker ", e);
            AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
        }
    }

    public void onGiftViaFacebook(View view) {
        final Context context = view.getContext();
        if (facebookSession != null && facebookSession.isOpened()) {
            Intent intent = new Intent();
            intent.setData(FacebookFriendActivity.FRIEND_PICKER);
            intent.setClass(view.getContext(), FacebookFriendActivity.class);
            startActivityForResult(intent, 200);

        } else {
            // start Facebook Login

            facebookSession = new Session.Builder(context).setApplicationId(getString(R.string.facebook_app_id)).build();
            Session.setActiveSession(facebookSession);
            OpenRequest openRequest = new Session.OpenRequest(this);
            openRequest.setCallback(new Session.StatusCallback() {

                // callback when session changes state
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                }
            });
            openRequest.setRequestCode(FACEBOOK_REQUEST_CODE);
            openRequest.setPermissions(PERMISSIONS);
            facebookSession.openForPublish(openRequest);
        }
    }

    protected void processFacebookResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            executeFacebookTask();
        }
    }

    protected void executeFacebookTask() {
        if (FacebookHelper.get().getSelectedFriends() != null && !FacebookHelper.get().getSelectedFriends().isEmpty()) {
            df = DialogFactory.getProgressDialog();

            df.show(getFragmentManager(), "dialog");
            String facebookId = FacebookHelper.get().getSelectedFriends().get(0).getId();
            String name = FacebookHelper.get().getSelectedFriends().get(0).getName();
            if (giftId != null && !giftId.equalsIgnoreCase("")) {
                executeFacebookShareTask(giftId);
            } else {
                FacebookGiftIdTask task = new FacebookGiftIdTask(client, deal.dealAcquireId, facebookId, name, this) {
                    @Override
                    protected void onPostExecute(String result) {
                        if (result != null && result != "") {
                            giftId = result;
                            executeFacebookShareTask(result);
                        } else {
                            if (df != null && !df.isHidden()) {
                                df.dismiss();
                            }

                            AlertMessage alertMessage = new AlertMessage("Check your network connection and retry", "Error sharing gift", null);
                            AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
                        }
                    }

                    ;
                };
                task.execute();
            }
        }
    }

    protected void executeFacebookShareTask(String giftId) {
        FacebookShareTask task = new FacebookShareTask(giftId) {
            @Override
            protected void onPostExecute(com.facebook.Response result) {
                if (df != null && !df.isHidden()) {
                    df.dismiss();
                }
                if (result != null && result.getError() != null) {
                    handleError(result.getError());
                } else {
                    deal.status = AcquireStatus_t.PENDING_ACCEPT_CUSTOMER_SHARE;
                    updateBackgroundForFacebookShare(FacebookHelper.get().getSelectedFriends().get(0).getName());
                }
            }

            ;
        };
        task.execute();
    }

    private void requestPublishPermissions(Session session) {
        if (session != null) {
            Session.NewPermissionsRequest newPermissionsRequest =
                    new Session.NewPermissionsRequest(this, PERMISSIONS).
                            setRequestCode(REAUTH_ACTIVITY_CODE);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
    }

    protected void handleError(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            // There was no response from the server.
            dialogBody = "No Response From Facebook Server";
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // Tell the user what happened by getting the
                    // message id, and retry the operation later.
                    String userAction = (error.shouldNotifyUser()) ? "" :
                            getString(error.getUserActionMessageId());
                    dialogBody = ErrorMessageCache.Message.FacebookAuthenticationRetry.getText();
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            // Take the user to the mobile site.
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    M_FACEBOOK_URL);
                            startActivity(intent);
                        }
                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                    // Close the session and reopen it.
                    dialogBody =
                            ErrorMessageCache.Message.FacebookAuthenticationRetry.getText();
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };
                    break;

                case PERMISSION:
                    // A permissions-related error
                    dialogBody = ErrorMessageCache.Message.FacebookPermissionRetry.getText();
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            pendingAnnounce = true;
                            // Request publish permission
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                    dialogBody = ErrorMessageCache.Message.FacebookRetry.getText();
                    break;
                case THROTTLING:
                    // This is usually temporary, don't clear the fields, and
                    // ask the user to try again.
                    dialogBody = ErrorMessageCache.Message.FacebookRetry.getText();
                    break;

                case BAD_REQUEST:
                    // This is likely a coding error, ask the user to file a bug.
                    dialogBody = ErrorMessageCache.Message.FacebookRetry.getText();
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // An unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug.
                    dialogBody = ErrorMessageCache.Message.FacebookRetry.getText();
                    break;
            }
        }

        // Show the error and pass in the listener so action
        // can be taken, if necessary.
        new AlertDialog.Builder(this)
                .setPositiveButton("Okay", listener)
                .setTitle("Facebook Error")
                .setMessage("Could not share via Facebook" + dialogBody)
                .show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Cursor cursor = null;

        try {
            if (requestCode == FACEBOOK_REQUEST_CODE) {
                facebookSession = Session.getActiveSession();
                facebookSession.onActivityResult(this, requestCode, resultCode, data);
                if (facebookSession != null && facebookSession.isOpened()) {
                    Intent intent = new Intent();
                    intent.setData(FacebookFriendActivity.FRIEND_PICKER);
                    intent.setClass(getBaseContext(), FacebookFriendActivity.class);
                    startActivityForResult(intent, 200);
                } else {
                    AlertMessage alertMessage = new AlertMessage("Error Sharing Gift", "Error sharing gift. Please retry", null);
                    AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
                }
            } else if (requestCode == 200) {
                processFacebookResult(requestCode, resultCode, data);
            } else if (requestCode == REAUTH_ACTIVITY_CODE) {
                if (giftId != null && giftId != "") {
                    executeFacebookShareTask(giftId);
                }
            } else if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case 100:

                        if (Build.VERSION.SDK_INT < 16) {
                            Uri result = data.getData();

                            // get the contact id from the Uri
                            String id = result.getLastPathSegment();

                            // query for everything email
                            cursor = getContentResolver().query(Email.CONTENT_URI, null, Email.CONTACT_ID + "=?", new String[]{id}, null);

                            int emailIdx = cursor.getColumnIndex(Email.DATA);
                            int nameId = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
                            // let's just get the first email
                            if (cursor.moveToFirst()) {
                                email = cursor.getString(emailIdx);
                                name = cursor.getString(nameId);
                            }

                            if (cursor != null) {
                                cursor.close();
                            }
                        } else {
                            Uri result = data.getData();

                            // get the contact id from the Uri
                            String id = result.getLastPathSegment();

                            cursor = getContentResolver().query(Email.CONTENT_URI, null, Email._ID + "=?", new String[]{id}, null);

                            int emailIdx = cursor.getColumnIndex(Email.DATA);
                            int nameId = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
                            // let's just get the first email
                            if (cursor.moveToFirst()) {
                                email = cursor.getString(emailIdx);
                                name = cursor.getString(nameId);
                            }

                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                        sendGift();
                        break;
                }
            }

        } catch (Exception e) {
            if (df != null && !df.isHidden()) {
                df.dismiss();
            }

            if (cursor != null) {
                cursor.close();
            }
            AlertMessage alertMessage = new AlertMessage("Error Sharing Gift", "Error sharing gift. Please retry", e);
            AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
        }
    }

    private void sendGift() {

        if (this.email == null || this.email.isEmpty()) {
            Toast.makeText(DealActivity.this, "Please select a contact with an email address", Toast.LENGTH_LONG).show();
        } else {
            GiftTask giftTask = new GiftTask();
            giftTask.execute(new String[]{});
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);

        easyTracker.activityStart(this); // Add this method.

        // MapBuilder.createEvent().build() returns a Map of event fields and values
        // that are set and sent with the hit.
        easyTracker.send(MapBuilder
                .createEvent("deal_activity", "selected", deal.deal.dealId, null)
                .build()
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(DealActivity.this, SettingsActivity.class);
            startActivity(intent);
            ret = true;
        } else {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

    private class GiftTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            df = DialogFactory.getProgressDialog();
            df.show(getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(final String results) {
            if (df != null && !df.isHidden()) {
                df.dismiss();
            }
            try {
                if (exception == null) {
                    dealActivityButtonLayout.removeAllViewsInLayout();
                    dealActivityButtonLayout.setBackgroundDrawable(null);
                    TextView redemptionCodeTextView = new TextView(DealActivity.this);
                    redemptionCodeTextView.setText(TaloolUtil.getGiftedText(new Date().getTime()));
                    redemptionCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    redemptionCodeTextView.setTextColor(getResources().getColor(R.color.white));
                    redemptionCodeTextView.setTypeface(TypefaceFactory.get().getFontAwesome(), Typeface.BOLD);
                    redemptionCodeTextView.setPadding(30, 0, 30, 0);
                    dealActivityButtonLayout.addView(redemptionCodeTextView);
                    dealActivityButtonLayout.setGravity(Gravity.CENTER);
                    dealActivityButtonLayout.setPadding(0, 0, 0, 0);
                    dealActivityButtonLayout.setBackgroundColor(getResources().getColor(R.color.orange));

                    EasyTracker easyTracker = EasyTracker.getInstance(DealActivity.this);
                    easyTracker.send(MapBuilder
                            .createEvent("gift", "selected", deal.dealAcquireId, null)
                            .build());
                } else {
                    AlertMessage alertMessage = new AlertMessage("An exception has occured", "Please try again", exception);
                    AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
                }
            } catch (Exception e) {
                AlertMessage alertMessage = new AlertMessage("An exception has occured", "Please try again", e);
                AndroidUtils.popupMessageWithOk(alertMessage, DealActivity.this);
            }
        }

        @Override
        protected String doInBackground(String... arg0) {
            String results = null;

            try {
                exception = null;
                results = client.getClient().giftToEmail(deal.dealAcquireId, email, name);
            } catch (ServiceException_t e) {
                exception = e;
            } catch (TException e) {
                exception = e;

            } catch (Exception e) {
                exception = e;
            }

            return results;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
