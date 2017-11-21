package ca.fradio.ui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.fradio.BroadcastRequesterThread;
import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;

public class StreamerListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "StreamerListAdapter";

    private final Activity context;
    private final ArrayList<String> streamers;

    public StreamerListAdapter(Activity context, ArrayList<String> streamers) {
        super(context, R.layout.streamer_list_item, streamers);
        this.context = context;
        this.streamers = streamers;

        // You cannot stream from yourself
        for(int i = 0; i < streamers.size(); i++) {
            if(streamers.get(i).equalsIgnoreCase(Globals.getSpotifyUsername())) {
                streamers.remove(i);
            }
        }
    }

    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Log.d(TAG, "getview");

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.streamer_list_item, null, true);

        TextView usernameTxt = rowView.findViewById(R.id.txt_username);
        ImageButton submitButton = rowView.findViewById(R.id.btn_submit);

        usernameTxt.setText(streamers.get(position));
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                connectToStream(Globals.getSpotifyUsername(), streamers.get(position));
            }
        });

        return rowView;
    }

    private void connectToStream(String listenerUsername, String streamerUsername) {
        try {
            JSONObject listenResponse =  Requester.requestListen(listenerUsername,
                    streamerUsername);

            BroadcastRequesterThread.instance().setStreamer(streamerUsername);

            Globals.getStreamService().connectToSong(listenResponse);
            Toast.makeText(context, context.getString(R.string.now_listening_to) + " "
                    + streamerUsername, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Started listening to " + streamerUsername);
        } catch (JSONException e){
            Log.e(TAG, "Could not properly parse listenResponse JSON", e);
            Toast.makeText(context, "Error connecting to stream " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
