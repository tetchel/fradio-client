package ca.fradio.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.fradio.UserInfo;
import ca.fradio.net.BroadcastRequesterThread;
import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.net.Requester;
import ca.fradio.spotify.MediaStateReceiver;
import ca.fradio.spotify.StatusNotificationManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private MediaStateReceiver _msr;
    //private Adapter adapter;

    private SwipeRefreshLayout _swipeToRefresh;

    private boolean _isBroadcasting = false;

    private final BroadcastRequesterThread _broadcastRequesterThread =
            BroadcastRequesterThread.instance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting MainActivity");
        setContentView(R.layout.activity_main);

        _msr = new MediaStateReceiver(this);

        // Configure the refreshable list
        _swipeToRefresh = findViewById(R.id.refreshableList);
        _swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUsersList();
            }
        });

        Log.d(TAG, "Finished setting up swipe refresh user_list_item");

        // Populate the list for the first time
        refreshUsersList();

        Log.d(TAG, "Starting broadcast requester");
        if(!_broadcastRequesterThread.isAlive()) {
            _broadcastRequesterThread.start();
        }
        // Disabled until connect to a stream
        _broadcastRequesterThread.setIsEnabled(false);

        registerReceiver(_msr, _msr.getFilter());

        final Button broadcastBtn = findViewById(R.id.btn_broadcast);
        broadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleIsBroadcasting();
            }
        });
    }

    // Callback for refresh button on top
    public void refreshUsersList(View view) {
        refreshUsersList();
    }

    /** Populate list view with streamers */
    private void refreshUsersList() {
        _swipeToRefresh.setRefreshing(true);

        Log.d(TAG, "Refreshing list of users");
        final ListView listView = findViewById(R.id.list_streamers);
        ArrayList<UserInfo> streamers = Requester.requestUsers();

        StreamerListAdapter _listAdapter = new StreamerListAdapter(this, streamers);
        listView.setAdapter(_listAdapter);

        _swipeToRefresh.setRefreshing(false);
    }

    private void toggleIsBroadcasting() {


        final Button broadcastBtn = findViewById(R.id.btn_broadcast);
        if(_isBroadcasting) {
            Log.d(TAG, "Stopped broadcasting");

            // You MUST unregister the MSR here, and in onDestroy or it will leak
            // Use _isbroadcasting to track the status of the receiver
            _isBroadcasting = false;
            unregisterReceiver(_msr);

            Requester.requestDisconnect(Globals.getSpotifyUsername());

            broadcastBtn.setText(getString(R.string.share_your_music));

            StatusNotificationManager.cancel(this);
            //Globals.getStreamService().updateNotification(_broadcastRequesterThread.getStreamer());

            _broadcastRequesterThread.setIsEnabled(true);
        }
        else {
            Log.d(TAG, "Started broadcasting");
            String trackName, artist, album;

            if(_broadcastRequesterThread.isEnabled()) {
                Log.d(TAG, "Propagating " + _broadcastRequesterThread.getStreamer() +
                        "'s stream");
                // The user is currently connected to a stream
                // Propagate that stream
                Toast.makeText(this, "You can't stream when already streaming " +
                                "from someone else... yet",
                        Toast.LENGTH_SHORT).show();
                return;

                /*
                String nowSharingMsg = getString(R.string.sharing) + ' ' +

                                _broadcastRequesterThread.getStreamer() +
                                getString(R.string.apostrophes_radio);
                Toast.makeText(this, nowSharingMsg, Toast.LENGTH_SHORT).show();

                Metadata.Track track = Globals.getStreamService().getCurrentTrack();
                trackName = track.name;
                artist = track.artistName;
                album = track.albumName;

                StatusNotificationManager.notify(this, nowSharingMsg,
                        trackName + " - " + artist);
                */
            }
            else {
                // Streaming own music
                trackName = _msr.getCurrentTrackName();

                if (trackName == null) {
                    Log.d(TAG, "No music playing");
                    Toast.makeText(this, "You are not playing any music!",
                            Toast.LENGTH_SHORT).show();
                    // Don't bother with the rest of the broadcasting
                    return;
                }
                _msr.updateNotification();
            }

            Log.d(TAG, "Sharing own music");
            // Use _isbroadcasting to track the status of the receiver
            _isBroadcasting = true;
            registerReceiver(_msr, _msr.getFilter());

            //Requester.requestStopListen(Globals.getSpotifyUsername());

            broadcastBtn.setText(getString(R.string.stop_sharing));

            _broadcastRequesterThread.setIsEnabled(false);
        }
    }

    public void connectToStream(String listenerUsername, String streamerUsername) {
        Log.d(TAG, "Starting to connect to stream from " + streamerUsername);
        try {
            JSONObject listenResponse =  Requester.requestListen(listenerUsername,
                    streamerUsername);

            _broadcastRequesterThread.setStreamer(streamerUsername);
            _broadcastRequesterThread.setIsEnabled(true);

            Globals.getStreamService().connectToSong(listenResponse);
            Log.d(TAG, "Started listening to " + streamerUsername);

            Toast.makeText(this,
                    getString(R.string.now_listening_to) + ' '
                    + streamerUsername + getString(R.string.apostrophes_radio),
                    Toast.LENGTH_SHORT).show();

        } catch (JSONException e){
            Log.e(TAG, "Could not properly parse listenResponse JSON", e);
            Toast.makeText(this, "Error connecting to stream " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void disconnectFromStream() {
        _broadcastRequesterThread.setStreamer(null);
        Globals.getStreamService().stopMusic();
        StatusNotificationManager.cancel(this);

        Requester.requestStopListen(Globals.getSpotifyUsername());
    }

    public boolean isBroadcasting() {
        return _isBroadcasting;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       unregisterReceiver(_msr);
    }
}
