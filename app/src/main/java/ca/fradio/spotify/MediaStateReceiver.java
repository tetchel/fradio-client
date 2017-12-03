package ca.fradio.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import ca.fradio.info.Globals;
import ca.fradio.net.Requester;
import ca.fradio.ui.MainActivity;

// Receiver for media events. Reacts to song change, play/pause, seek, etc.
public class MediaStateReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaStateReceiver";

    private MainActivity boundActivity;

    private IntentFilter filter;

    private String currentTrackName, currentArtist, currentAlbum;

    private static final class SpotifyBroadcasts {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";

        public static String[] values() {
            return new String[] { PLAYBACK_STATE_CHANGED, QUEUE_CHANGED, METADATA_CHANGED };
        }
    }

    public MediaStateReceiver(MainActivity activity) {
        super();
        boundActivity = activity;
        filter = new IntentFilter();
        for(String s : SpotifyBroadcasts.values()) {
            filter.addAction(s);
        }
    }

    public IntentFilter getFilter() {
        return filter;
    }

    @Override
    // This is sent with all broadcasts, regardless of type. The value is taken from
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received media broadcast: " + intent.getAction());
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);
        long age = System.currentTimeMillis() - timeSentInMs;

        String action = intent.getAction();

        // The currentTrackId must always be set, or there will be an error.
        String currentTrackId = intent.getStringExtra("id");

        int trackLengthInSec = intent.getIntExtra("length", 0);
        boolean playing = intent.getBooleanExtra("playing", false);

        // Metadata.Track track =
        //mostRecentTrack = (Metadata.Track) intent.getExtras().get("track");

        /*
        Log.d(TAG, "KEYS FOLLOW:");

        for(String key : intent.getExtras().keySet()) {
            Log.d(TAG, key);
        }*/
        currentTrackName = intent.getStringExtra("track");
        currentArtist = intent.getStringExtra("artist");
        currentAlbum = intent.getStringExtra("album");
        Log.d(TAG, String.format("Updated current track info. name=%s artist=%s album=%s",
                currentTrackName, currentArtist, currentAlbum));

        if (action.equals(SpotifyBroadcasts.METADATA_CHANGED)) {
            Log.d(TAG, "Metadata Changed");

            //Log.d(TAG, String.format("trackid %s artist %s album %s track %s len %d",
            //        trackId, artistName, albumName, trackName, trackLengthInSec));

            broadcast(currentTrackId, age, trackLengthInSec, playing);
        } else if (action.equals(SpotifyBroadcasts.PLAYBACK_STATE_CHANGED)) {
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            Log.d(TAG, "Playback State Changed");

            broadcast(currentTrackId, positionInMs + age, trackLengthInSec, playing);

        } else if (action.equals(SpotifyBroadcasts.QUEUE_CHANGED)) {
            // Sent only as a notification, your app may want to respond accordingly.
            // we don't care about this one
        }
        Log.d(TAG, "Finished handling broadcast");

        if(boundActivity.isBroadcasting()) {
            updateNotification();
        }
    }

    private void broadcast(String trackid, long positionMs, long trackLength, boolean playing) {
        Log.d(TAG, "Broadcasting trackid " + trackid + " timestamp " + positionMs
                + " length " + trackLength + " playing " + playing);

        Requester.requestBroadcast(Globals.getSpotifyUsername(), trackid, positionMs, trackLength,
                playing);
    }

    public String getCurrentTrackName() {
        return currentTrackName;
    }

    public void updateNotification() {
        StatusNotificationManager.cancel(boundActivity);
        StatusNotificationManager.setSharingTrack(boundActivity, currentTrackName, currentArtist, currentAlbum);
    }
}
