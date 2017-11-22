package ca.fradio.ui;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ca.fradio.Globals;
import ca.fradio.InactiveInfo;
import ca.fradio.ListenerInfo;
import ca.fradio.R;
import ca.fradio.StreamerInfo;
import ca.fradio.UserInfo;
import ca.fradio.net.BroadcastRequesterThread;

public class StreamerListAdapter extends ArrayAdapter<UserInfo> {
    private static final String TAG = "UserListAdapter";

    private final MainActivity activity;
    private final ArrayList<UserInfo> users;

    public StreamerListAdapter(MainActivity mainActivity, ArrayList<UserInfo> usersIn) {
        super(mainActivity, R.layout.user_list_item, usersIn);

        activity = mainActivity;
        users = usersIn;

        // You cannot stream from yourself
        for(int i = 0; i < users.size(); i++) {
            if(users.get(i).getUsername().equalsIgnoreCase(Globals.getSpotifyUsername())) {
                users.remove(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Log.d(TAG, "getview");

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.user_list_item, null, true);

        TextView usernameTxt = rowView.findViewById(R.id.txt_username);
        TextView detailTxt = rowView.findViewById(R.id.txt_detail);
        final ImageButton joinStreamButton = rowView.findViewById(R.id.btn_listen);

        final UserInfo user = users.get(position);
        usernameTxt.setText(user.getUsername());

        if (user instanceof ListenerInfo){
            ListenerInfo info = (ListenerInfo) user;
            detailTxt.setText("Listening to " + info.getListening() + "\'s Radio");
            joinStreamButton.setVisibility(View.INVISIBLE);

        } else if (user instanceof StreamerInfo){
            StreamerInfo info = (StreamerInfo) user;
            detailTxt.setText("Streaming great music");

        } else if (user instanceof InactiveInfo){
            InactiveInfo info = (InactiveInfo) user;
            detailTxt.setText("Offline");
            joinStreamButton.setVisibility(View.INVISIBLE);

        }


        // Disallow connecting to stream if you are streaming
        if(!BroadcastRequesterThread.instance().isEnabled()) {
            //joinStreamButton.setVisibility(View.INVISIBLE);
        }

        joinStreamButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(users.get(position).getUsername().equals(
                        BroadcastRequesterThread.instance().getStreamer())) {
                    // Currently listening to this guy - Stop listening
                    activity.disconnectFromStream();

                    joinStreamButton.setImageResource(R.mipmap.ic_broadcast);
                }
                else {
                    activity.connectToStream(Globals.getSpotifyUsername(),
                            users.get(position).getUsername());

                    // Update this streamers's icon to be a Stop icon
                    joinStreamButton.setImageResource(R.mipmap.ic_stop);
                }
            }
        });

        return rowView;
    }


}
