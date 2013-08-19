package com.talool.mobile.android.util;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.talool.mobile.android.R;
import com.talool.mobile.android.tasks.ActivitySupervisor;


/**
 * Created with IntelliJ IDEA.
 * User: bryan
 * Date: 8/18/13
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationHelper {

    //todo add dynamic messages
    public static final String NOTIFICATION_MESSAGE = "Received gift \"2 for 1 lunch\" at The Kitchen";
    private final static String NOTIFICATION_TITLE = "Update Test Message";
    private int currentNotificationId = 0;
    private int previousCount = 0;
    private ActionBar.Tab notificationTab;
    private Context applicationContext;
    private Handler uiThreadHandler;
    private NotificationManager notificationManager;

    public NotificationHelper(ActionBar.Tab notificationTab, Context applicationContext) {
        this.notificationTab = notificationTab;
        this.applicationContext = applicationContext;

        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);


        uiThreadHandler = new Handler(Looper.getMainLooper());

        initializeActivitySupervisor();
    }

    private void initializeActivitySupervisor() {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                ActivitySupervisor.createInstance( applicationContext, notificationCallback);
            }
        });
    }

    private void newNotification(int notificationId, String notificationTitle, String notificationMessage, int totalNotifications, Object o) {
        updateNotificationTab(totalNotifications);

        //todo need a better way to distinguish updated notifications from new.

        if (previousCount != totalNotifications){
            previousCount = totalNotifications;
            /*if (currentNotificationId >= Integer.MAX_VALUE){
                currentNotificationId = 0;
            }
            else {
                currentNotificationId ++;
            } */

            //todo build intent. need to clear previousCount on resume
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.drawable.icon_teal)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(NOTIFICATION_MESSAGE)
                    .setNumber(totalNotifications)
                    .setContentIntent(null);

            // Builds the notification and issues it.
            notificationManager.notify(currentNotificationId, builder.build());
        }
    }


    public void updateNotificationTab(final int count){
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                //todo clean clean clean
                if (notificationTab != null){
                    if (count > 0){
                        ((TextView)notificationTab.getCustomView().findViewById(R.id.actionbar_notifcation_textview)).setText(Integer.toString(count));
                        notificationTab.getCustomView().findViewById(R.id.activity_text_count).setVisibility(RelativeLayout.VISIBLE);
                    }
                    else {
                        notificationTab.getCustomView().findViewById(R.id.activity_text_count).setVisibility(RelativeLayout.GONE);
                    }
                }
            }
        });
    }

    ActivitySupervisor.NotificationCallback notificationCallback = new ActivitySupervisor.NotificationCallback() {
        @Override
        public void handleNotificationCount(int totalNotifications) {
            if (totalNotifications > 0 && notificationManager != null){
                    newNotification(currentNotificationId, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, totalNotifications, null);
            }
        }
    };


}
