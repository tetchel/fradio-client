package ca.fradio.ui;

import android.app.Activity;
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

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;

public class StreamerListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "StreamerListAdapter";

    private final Activity context;
    private final String username;
    private final ArrayList<String> streamers;
    private final Requester requester;

    public StreamerListAdapter(Activity context, String username, ArrayList<String> streamers) {
        super(context, R.layout.streamer_list_item, streamers);
        this.context = context;
        this.username = username;
        this.streamers = streamers;

        // You cannot stream from yourself
        for(int i = 0; i < streamers.size(); i++) {
            if(streamers.get(i).equalsIgnoreCase(username)) {
                streamers.remove(i);
            }
        }

        requester = new Requester();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Log.d(TAG, "getview");

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.streamer_list_item, null, true);

        TextView usernameTxt = rowView.findViewById(R.id.txt_username);
        ImageButton submitButton = rowView.findViewById(R.id.btn_submit);

        usernameTxt.setText(streamers.get(position));
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    JSONObject listenResponse =  Requester.requestListen(username,
                            streamers.get(position));
                    Globals.setStreamer(streamers.get(position));
                    Globals.getStreamService().connectToSong(listenResponse);
                    Toast.makeText(context, context.getString(R.string.now_listening_to) + " "
                            + streamers.get(position), Toast.LENGTH_SHORT).show();
                } catch (JSONException e){
                    Log.e(TAG, "Could not properly parse listenResponse JSON", e);
                }
            }
        });

        return rowView;
    }
}
