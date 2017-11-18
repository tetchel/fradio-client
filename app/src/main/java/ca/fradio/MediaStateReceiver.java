package ca.fradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.json.JSONObject;

// Receiver for media events. Reacts to song change, play/pause, seek, etc.
public class MediaStateReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaStateReceiver";
    private IntentFilter filter;

    private Requester requester = new Requester();

    private String currentTrackId;

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
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        long timeSentInMs = intent.getLongExtra("timeSent", 0L);
        long age = System.currentTimeMillis() - timeSentInMs;

        String action = intent.getAction();

        if (action.equals(SpotifyBroadcasts.METADATA_CHANGED)) {
            String trackId = intent.getStringExtra("id");
            //String artistName = intent.getStringExtra("artist");
            //String albumName = intent.getStringExtra("album");
            //String trackName = intent.getStringExtra("track");
            //int trackLengthInSec = intent.getIntExtra("length", 0);

            //Log.d(TAG, String.format("trackid %s artist %s album %s track %s len %d",
            //        trackId, artistName, albumName, trackName, trackLengthInSec));

            currentTrackId = trackId;
            broadcast(currentTrackId, age);
        } else if (action.equals(SpotifyBroadcasts.PLAYBACK_STATE_CHANGED)) {
            boolean playing = intent.getBooleanExtra("playing", false);
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            Log.d(TAG, "playing? " + playing + " position changed to " + positionInMs);

            if(playing) {
                broadcast(currentTrackId, positionInMs + age);
            }
            else {
                // TODO Stop broadcasting?
            }

        } else if (action.equals(SpotifyBroadcasts.QUEUE_CHANGED)) {
            // Sent only as a notification, your app may want to respond accordingly.
            // we don't care about this one
        }
    }

    private void broadcast(String trackid, long positionMs) {
        Log.d(TAG, "Broadcasting trackid " + trackid + " timestamp " + positionMs);

        JSONObject resp = requester
                .requestBroadcast(Globals.getSpotifyUsername(), trackid, positionMs);

        if(resp == null) {
            Log.e(TAG, "THE RESPONSE WAS NULL TRYING TO BROADCAST!!!!!!");
        }
        else {
            Log.d(TAG, "WE GOT A RESPONSE!!!!!\n" + resp);
        }
    }
}
