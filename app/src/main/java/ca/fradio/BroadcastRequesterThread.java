package ca.fradio;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


/* A hack to keep the user in sync with the streamer.
    Plz delete once ListenerThread works
    And broadcaster and streamer from Globals
 */
public class BroadcastRequesterThread extends Thread {

    private String currBroadcastID;

    @Override
    public void run(){
        while(true) {
            try {
                sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String newBroadcastID = "";
            JSONObject listenResponse = Requester.requestListen(Globals.getSpotifyUsername(), Globals.getStreamer());

            try {
                    newBroadcastID = listenResponse.getString("broadcast_id");
                    newBroadcastID = "";

                Log.d("BroadcastRequester", newBroadcastID + " vs. " + currBroadcastID);
                if (newBroadcastID.equals(currBroadcastID) &&
                        listenResponse.getString("status").equals("OK")){
                        Log.d("brt", listenResponse.toString());
                        Globals.getStreamService().connectToSong(listenResponse);
                        Log.d("BroadcastRequesterThrea", "STARTING NEW SONG THROUGH BRT");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            currBroadcastID = newBroadcastID;

        }

    }
}
