package ca.fradio.spotify;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import ca.fradio.R;

public class StatusNotificationManager {

    private static StatusNotificationManager instance;

    private Context context;

    private NotificationCompat.Builder builder;

    private NotificationManager notificationManager;

    private static final int NOTIF_ID = 0;

    public static StatusNotificationManager instance() {
        if(instance == null) {
            instance = new StatusNotificationManager();
        }
        return instance;
    }

    // Can't do anything before set context - This is basically the constructor.
    // There is probably a better way to do this
    public void setContext(Context c) {
        context = c;

        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_background);   //  should be album art

        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void stop() {
        notificationManager.cancel(NOTIF_ID);
    }

    public void setMsg(String title, String msg) {
        builder.setContentText(msg).setContentTitle(title);
        notificationManager.notify(NOTIF_ID, builder.build());
    }
}
