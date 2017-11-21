package ca.fradio.spotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ca.fradio.R;

public class StatusNotificationManager {

    private static final String TAG = StatusNotificationManager.class.getSimpleName();

    private static final int NOTIF_ID = 0;

    private static boolean notifIsBeingShown = false;

    public static void notify(Context context, String title, String msg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_foreground);  //  should be album art

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager == null) {
            Log.e(TAG, "Unable to get notification manager to notify!!");
            return;
        }

        builder.setContentText(msg).setContentTitle(title);

        if(!notifIsBeingShown) {
            notifIsBeingShown = true;
            // Make the notification pop up on top
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }
            else {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }
        }

        builder.setOngoing(true);

        notificationManager.notify(NOTIF_ID, builder.build());
    }

    public static void cancel(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager == null) {
            Log.e(TAG, "Unable to get notification manager to cancel notification!!");
            return;
        }

        notifIsBeingShown = false;
        notificationManager.cancel(NOTIF_ID);
    }
}
