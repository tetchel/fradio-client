package ca.fradio;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BroadcastRequesterThread extends Thread {

    private static final String TAG = "BroadcastReqThread";

    private String currBroadcastID;

    private boolean isEnabled = true;

    @Override
    public void run(){
        while(true) {
            try {
                sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!isEnabled) {
                continue;
            }

            String streamer = Globals.getStreamer();
            if(streamer == null) {
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
                newBroadcastID = "";

                Log.d(TAG, newBroadcastID + " vs. " + currBroadcastID);
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
}
