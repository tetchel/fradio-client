package ca.fradio.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import ca.fradio.Globals;
import ca.fradio.Requester;

// Receiver for media events. Reacts to song change, play/pause, seek, etc.
public class MediaStateReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaStateReceiver";
    private IntentFilter filter;


    private static final class SpotifyBroadcasts {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";

        public static String[] values() {
            return new String[] { PLAYBACK_STATE_CHANGED, QUEUE_CHANGED, METADATA_CHANGED };
        }
    }

    public MediaStateReceiver() {
        super();
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
        boolean playing = intent.getBooleanExtra("playing", true);

        if (action.equals(SpotifyBroadcasts.METADATA_CHANGED)) {
            //String artistName = intent.getStringExtra("artist");
            //String albumName = intent.getStringExtra("album");
            //String trackName = intent.getStringExtra("track");
            // Start a timer to find out when this song is going to end

            //Log.d(TAG, String.format("trackid %s artist %s album %s track %s len %d",
            //        trackId, artistName, albumName, trackName, trackLengthInSec));


            broadcast(currentTrackId, age, trackLengthInSec, playing);
        } else if (action.equals(SpotifyBroadcasts.PLAYBACK_STATE_CHANGED)) {
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            Log.d(TAG, "playing? " + playing + " position changed to " + positionInMs);

            broadcast(currentTrackId, positionInMs + age, trackLengthInSec, playing);

        } else if (action.equals(SpotifyBroadcasts.QUEUE_CHANGED)) {
            // Sent only as a notification, your app may want to respond accordingly.
            // we don't care about this one
        }
        Log.d(TAG, "Finished handling broadcast");
    }

    private void broadcast(String trackid, long positionMs, long trackLength, boolean playing) {
        Log.d(TAG, "Broadcasting trackid " + trackid + " timestamp " + positionMs);

        Requester.requestBroadcast(Globals.getSpotifyUsername(), trackid, positionMs, trackLength, playing);
    }
}
