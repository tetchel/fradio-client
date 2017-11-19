package ca.fradio.ui;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;

public class StreamerListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "StreamerListAdapter";

    private final Activity context;
    private final String username;
    private final ArrayList<String> streamers;
    Requester requester;

    public StreamerListAdapter(Activity context, String username, ArrayList<String> streamers) {
        super(context, R.layout.streamer_list_item, streamers);
        this.context = context;
        this.username = username;
        this.streamers = streamers;
        requester = new Requester();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.streamer_list_item, null, true);

        TextView usernameTxt = rowView.findViewById(R.id.txt_username);
        ImageButton submitButton = rowView.findViewById(R.id.btn_submit);

        usernameTxt.setText(streamers.get(position));
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    JSONObject listenResponse =  Requester.requestListen(username, streamers.get(position));
                    connectToSong(listenResponse);
                } catch (JSONException e){
                    Log.e(TAG, "Could not properly parse listenResponse JSON", e);
                }
            }
        });

        return rowView;
    }

    private void connectToSong(JSONObject listenInfo) throws JSONException {
        Log.d(TAG, listenInfo.toString());


        if(Globals.getStreamService() == null) {
            Toast.makeText(context, "You are not logged in!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String status = listenInfo.getString("status");
        if(!status.equals("OK")) {
            Toast.makeText(context, status, Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, "Current track has ended", Toast.LENGTH_LONG).show();
        }

        Globals.getStreamService().playTrack(hostusername, trackid, trackTime);
        Toast.makeText(context, context.getString(R.string.now_listening_to) + ' ' + hostusername +
                context.getString(R.string.apostrophes_radio), Toast.LENGTH_LONG).show();
    }
}
