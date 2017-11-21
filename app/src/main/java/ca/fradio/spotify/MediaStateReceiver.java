package ca.fradio.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.spotify.sdk.android.player.Metadata;

import ca.fradio.Globals;
import ca.fradio.net.Requester;

// Receiver for media events. Reacts to song change, play/pause, seek, etc.
public class MediaStateReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaStateReceiver";
    private IntentFilter filter;

    private static String   currentTrackName,
                            currentArtist;

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
        boolean playing = intent.getBooleanExtra("playing", false);

        // Metadata.Track track =
        //mostRecentTrack = (Metadata.Track) intent.getExtras().get("track");

        /*
        Log.d(TAG, "KEYS FOLLOW:");

        for(String key : intent.getExtras().keySet()) {
            Log.d(TAG, key);
        }*/

        if (action.equals(SpotifyBroadcasts.METADATA_CHANGED)) {
            Log.d(TAG, "Metadata Changed");
            currentTrackName = intent.getStringExtra("track");
            currentArtist = intent.getStringExtra("artist");
            //String albumName = intent.getStringExtra("album");

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
    }

    private void broadcast(String trackid, long positionMs, long trackLength, boolean playing) {
        Log.d(TAG, "Broadcasting trackid " + trackid + " timestamp " + positionMs
                + " length " + trackLength + " playing " + playing);

        Requester.requestBroadcast(Globals.getSpotifyUsername(), trackid, positionMs, trackLength, playing);
    }

    public String getMostRecentTrack() {
        return currentTrackName;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }
}
