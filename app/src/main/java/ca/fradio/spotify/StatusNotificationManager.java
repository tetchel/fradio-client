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

        /*
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
        */

        builder.setOngoing(true);

        notificationManager.notify(NOTIF_ID, builder.build());
    }

    private static final String TITLE_FORMAT = "%s: %s";       // Message (Streaming/Sharing): trackName
    private static final String BODY_FORMAT  = "%s - %s";      // ArtistName - AlbumName

    public static void setStreamingTrack(Context context, String hostUsername, String trackName,
                                         String artist, String album) {
        Log.d(TAG, "Updating streaming notification for song: " + trackName);

        String title = String.format(TITLE_FORMAT,
                hostUsername + context.getString(R.string.apostrophes_radio), trackName);
        String body = String.format(BODY_FORMAT, artist, album);

        notify(context, title, body);
    }

    public static void setSharingTrack(Context context,
                                       String trackName, String artist, String album) {
        Log.d(TAG, "Updating sharing notification for song: " + trackName);

        String title = String.format(TITLE_FORMAT, context.getString(R.string.sharing), trackName);
        String body = String.format(BODY_FORMAT, artist, album);

        notify(context, title, body);
    }

    public static void cancel(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager == null) {
            Log.e(TAG, "Unable to get notification manager to cancel notification!!");
            return;
        }

        notificationManager.cancel(NOTIF_ID);
    }
}
