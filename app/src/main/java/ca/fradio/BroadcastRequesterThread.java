package ca.fradio;

import android.app.DownloadManager;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ca.fradio.ui.StreamerListAdapter;

/* A hack to keep the user in sync with the streamer.
    Plz delete once ListenerThread works
    And broadcaster and streamer from Globals
 */
public class BroadcastRequesterThread extends Thread {

    String currBroadcastID;

    public void run(){
        while(true) {
            try {
                sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currBroadcastID = Globals.getBroadcastID();

            String newBroadcastID;
            JSONObject listenResponse = Requester.requestListen(Globals.getSpotifyUsername(), Globals.getStreamer());

            try {
                newBroadcastID = listenResponse.getString("broadcast_id");
            } catch (JSONException e){
                e.printStackTrace();
                newBroadcastID = "";
            }

            Log.d("BroadcastRequester", newBroadcastID + " vs. " + currBroadcastID);
            if (newBroadcastID.equals(currBroadcastID)){
                try {
                    connectToSong(listenResponse);
                    Log.d("BroadcastRequesterThrea", "STARTING NEW SONG THROUGH BRT");
                }catch(JSONException e) {
                    e.printStackTrace();
                }

            }
            Globals.setBroadcastID(newBroadcastID);

        }

    }

    private void connectToSong(JSONObject listenInfo) throws JSONException {


        if(Globals.getStreamService() == null) {
            return;
        }

        String status = listenInfo.getString("status");
        if(!status.equals("OK")) {
        }

        long serverTime = listenInfo.getLong("server_time");
        int trackTime = listenInfo.getInt("track_time");
        int trackLen = listenInfo.getInt("track_length");
        String trackid = listenInfo.getString("spotify_track_id");
        String hostusername = listenInfo.getString("host");

        // Account for the listening time that elapsed in transmission
        // Should handle the case of this being too long - if longer than track len, set to 0

        long now = System.currentTimeMillis();
        long elapsed = now - serverTime;
        trackTime += elapsed;

        if(trackTime > trackLen) {
        }

        Globals.getStreamService().playTrack(hostusername, trackid, trackTime);
    }
}
