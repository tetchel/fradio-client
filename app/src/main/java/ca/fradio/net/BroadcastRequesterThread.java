package ca.fradio.net;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ca.fradio.Globals;

public class BroadcastRequesterThread extends Thread {

    private static final String TAG = "BroadcastReqThread";

    private static BroadcastRequesterThread instance;

    private String currBroadcastID;

    private boolean isEnabled = true;

    private String streamer;

    protected BroadcastRequesterThread() { }

    public static BroadcastRequesterThread instance() {
        if(instance == null) {
            instance = new BroadcastRequesterThread();
        }
        return instance;
    }

    @Override
    public void run(){
        while(!isInterrupted()) {
            try {
                sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!isEnabled || streamer == null) {
                // Not listening to anything right now
                continue;
            }

            String newBroadcastID = "";
            JSONObject listenResponse = Requester.requestListen(Globals.getSpotifyUsername(),
                    streamer);

            try {
                if(listenResponse == null) {
                    Log.e(TAG, "NULL listenResponse when checking broadcast status");
                    continue;
                }
                else if(!listenResponse.getString("status").equals("OK")) {
                    Log.e(TAG, "Error from server check broadcast status: " +
                            listenResponse.getString("status"));
                    continue;
                }

                newBroadcastID = listenResponse.getString("broadcast_id");

                //Log.d(TAG, newBroadcastID + " vs. " + currBroadcastID);
                if (!newBroadcastID.equals(currBroadcastID)) {
                    Log.d(TAG, listenResponse.toString());
                    Globals.getStreamService().connectToSong(listenResponse);
                    Log.d(TAG, "STARTING NEW SONG THROUGH BRT");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            currBroadcastID = newBroadcastID;
        }
    }

    public void setIsEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * Can be used to determine if the user is currently connected to a stream, since
     * the BRT state will match that.
     */
    public boolean isEnabled() {
        return isEnabled && streamer != null;
    }

    public void setStreamer(String streamer) { this.streamer = streamer; }

    public String getStreamer() { return streamer; }
}
