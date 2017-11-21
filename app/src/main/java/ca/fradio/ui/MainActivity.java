package ca.fradio.ui;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Metadata;

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

    private final MediaStateReceiver _msr = new MediaStateReceiver();
    //private Adapter adapter;

    private SwipeRefreshLayout swipeToRefresh;

    private boolean _isBroadcasting = false;

    private final BroadcastRequesterThread _broadcastRequesterThread =
            new BroadcastRequesterThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting MainActivity");
        setContentView(R.layout.activity_main);

        // Configure the refreshable list
        swipeToRefresh = findViewById(R.id.refreshableList);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUsersList();
            }
        });

        Log.d(TAG, "Finished setting up swipe refresh layout");

        // Populate the list for the first time
        refreshUsersList();

        Log.d(TAG, "Starting broadcast requester");
        _broadcastRequesterThread.start();

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
        swipeToRefresh.setRefreshing(true);

        Log.d(TAG, "Refreshing list of users");
        final ListView listView = findViewById(R.id.list_streamers);
        ArrayList<UserInfo> streamers = Requester.requestStreamers();

        StreamerListAdapter _listAdapter = new StreamerListAdapter(this, streamers);
        listView.setAdapter(_listAdapter);

        swipeToRefresh.setRefreshing(false);
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

            _broadcastRequesterThread.setIsEnabled(true);
        }
        else {
            Log.d(TAG, "Started broadcasting");

            String trackName = _msr.getMostRecentTrack();
            String artist = _msr.getCurrentArtist();
            if(trackName == null) {
                Toast.makeText(this, "You are not playing any music!",
                        Toast.LENGTH_SHORT).show();
                // Don't bother with the rest of the broadcasting
                return;
            }

            // Use _isbroadcasting to track the status of the receiver
            _isBroadcasting = true;
            registerReceiver(_msr, _msr.getFilter());

            Requester.requestStopListen(Globals.getSpotifyUsername());

            broadcastBtn.setText(getString(R.string.stop_sharing));
            StatusNotificationManager.notify(this, "Sharing your music",
                    trackName + " - " + artist);

            _broadcastRequesterThread.setIsEnabled(false);
        }
    }

    public void connectToStream(String listenerUsername, String streamerUsername) {
        Log.d(TAG, "Starting to connect to stream from " + streamerUsername);
        try {
            if(_isBroadcasting) {
                Toast.makeText(this,
                        "Can't connect to stream while sharing",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject listenResponse =  Requester.requestListen(listenerUsername,
                    streamerUsername);

            BroadcastRequesterThread.instance().setStreamer(streamerUsername);

            Globals.getStreamService().connectToSong(listenResponse);
            Log.d(TAG, "Started listening to " + streamerUsername);

        } catch (JSONException e){
            Log.e(TAG, "Could not properly parse listenResponse JSON", e);
            Toast.makeText(this, "Error connecting to stream " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       if(_isBroadcasting) {
           StatusNotificationManager.cancel(this);
           unregisterReceiver(_msr);
           _isBroadcasting = false;
       }
    }
}
