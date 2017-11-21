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

    private final MainActivity activity;
    private final ArrayList<UserInfo> streamers;

    public StreamerListAdapter(MainActivity mainActivity, ArrayList<UserInfo> streamersIn) {
        super(mainActivity, R.layout.streamer_list_item, streamersIn);

        activity = mainActivity;
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
        final ImageButton joinStreamButton = rowView.findViewById(R.id.btn_submit);

        // Disallow connecting to stream if you are streaming, or if current user in list is not
        if(!streamers.get(position).isStreaming()) {
            joinStreamButton.setVisibility(View.INVISIBLE);
        }

        usernameTxt.setText(streamers.get(position).getUsername());
        joinStreamButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(streamers.get(position).getUsername().equals(
                        BroadcastRequesterThread.instance().getStreamer())) {
                    // Currently listening to this guy - Stop listening
                    activity.disconnectFromStream();

                    joinStreamButton.setImageResource(R.drawable.broadcast_64);
                }
                else {
                    activity.connectToStream(Globals.getSpotifyUsername(),
                            streamers.get(position).getUsername());

                    // Update this streamers's icon to be a Stop icon

                    joinStreamButton.setImageResource(R.mipmap.ic_stop);
                }
            }
        });

        return rowView;
    }


}
