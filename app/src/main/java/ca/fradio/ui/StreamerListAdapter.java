package ca.fradio.ui;

import android.app.Activity;
import android.support.annotation.NonNull;
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
import ca.fradio.UserInfo;
import ca.fradio.net.BroadcastRequesterThread;
import ca.fradio.net.Requester;

public class StreamerListAdapter extends ArrayAdapter<UserInfo> {
    private static final String TAG = "StreamerListAdapter";

    private final Activity activity;
    private final ArrayList<UserInfo> streamers;

    public StreamerListAdapter(Activity activityIn, ArrayList<UserInfo> streamersIn) {
        super(activityIn, R.layout.streamer_list_item, streamersIn);

        activity = activityIn;
        streamers = streamersIn;

        // You cannot stream from yourself
        for(int i = 0; i < streamers.size(); i++) {
            if(streamers.get(i).getUsername().equalsIgnoreCase(Globals.getSpotifyUsername())) {
                streamers.remove(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Log.d(TAG, "getview");

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.streamer_list_item, null, true);

        TextView usernameTxt = rowView.findViewById(R.id.txt_username);
        ImageButton joinStreamButton = rowView.findViewById(R.id.btn_submit);

        // Disallow connecting to stream if you are streaming, or if current user in list is not
        if(!streamers.get(position).isStreaming() ||
                !BroadcastRequesterThread.instance().isEnabled()) {

            joinStreamButton.setVisibility(View.INVISIBLE);
        }

        usernameTxt.setText(streamers.get(position).getUsername());
        joinStreamButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                connectToStream(Globals.getSpotifyUsername(),
                        streamers.get(position).getUsername());
            }
        });

        return rowView;
    }

    private void connectToStream(String listenerUsername, String streamerUsername) {
        Log.d(TAG, "Starting to connect to stream from " + streamerUsername);
        try {
            JSONObject listenResponse =  Requester.requestListen(listenerUsername,
                    streamerUsername);

            BroadcastRequesterThread.instance().setStreamer(streamerUsername);

            Globals.getStreamService().connectToSong(listenResponse);
            Toast.makeText(activity, activity.getString(R.string.now_listening_to) + " "
                    + streamerUsername, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Started listening to " + streamerUsername);
        } catch (JSONException e){
            Log.e(TAG, "Could not properly parse listenResponse JSON", e);
            Toast.makeText(activity, "Error connecting to stream " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
