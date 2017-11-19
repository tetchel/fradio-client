package ca.fradio.ui;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;


import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

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

        long serverTime = listenInfo.getLong("server_time");
        int trackTime = listenInfo.getInt("track_time");
        String trackid = listenInfo.getString("spotify_track_id");

        // Account for the listening time that elapsed in transmission
        // Should handle the case of this being too long - if longer than track len, set to 0

        long now = System.currentTimeMillis();
        long elapsed = now - serverTime;
        trackTime += elapsed;

        //Globals.getStreamService().playTrack(trackid, trackTime, 0);
    }

}
