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
            if (notificationManager != null && totalNotifications != previousCount){
                updateNotificationTab(totalNotifications);
            }
        }
    };
}
