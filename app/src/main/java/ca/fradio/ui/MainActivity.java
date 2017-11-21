package ca.fradio.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ca.fradio.UserInfo;
import ca.fradio.net.BroadcastRequesterThread;
import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.net.Requester;
import ca.fradio.spotify.MediaStateReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private final MediaStateReceiver _msr = new MediaStateReceiver();
    //private Adapter adapter;

    private boolean _isBroadcasting = false;

    private StreamerListAdapter _listAdapter;

    private final BroadcastRequesterThread broadcastRequesterThread =
            new BroadcastRequesterThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Populate list view with streamers */
        final ListView listView = findViewById(R.id.list_streamers);
        ArrayList<UserInfo> streamers = Requester.requestStreamers();

        _listAdapter = new StreamerListAdapter(this, streamers);

        listView.setAdapter(_listAdapter);

        broadcastRequesterThread.start();
        Log.d(TAG, "Started broadcast requester");

        final Button broadcastBtn = findViewById(R.id.btn_broadcast);
        broadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleIsBroadcasting();
            }
        });

    }

    private void toggleIsBroadcasting() {
        final Button broadcastBtn = findViewById(R.id.btn_broadcast);
        if(_isBroadcasting) {
            Log.d(TAG, "Stopped broadcasting");
            Requester.requestDisconnect(Globals.getSpotifyUsername());

            unregisterReceiver(_msr);

            _isBroadcasting = false;

            Toast.makeText(MainActivity.this, "Stopped broadcasting",
                    Toast.LENGTH_SHORT).show();

            broadcastBtn.setText("Start Streaming");

            broadcastRequesterThread.setIsEnabled(true);
        }
        else {
            Log.d(TAG, "Started broadcasting");
            Requester.requestStopListen(Globals.getSpotifyUsername());

            registerReceiver(_msr, _msr.getFilter());

            _isBroadcasting = true;

            Toast.makeText(MainActivity.this, "Started broadcasting",
                    Toast.LENGTH_SHORT).show();

            broadcastBtn.setText("Stop Streaming");

            broadcastRequesterThread.setIsEnabled(false);
        }

        // If you are broadcasting, do not allow connecting to streams
        // If you are not broadcasting, re-enable connecting to streams
        for(View view : ((View)findViewById(R.id.list_streamers)).getTouchables()) {
            view.setEnabled(!_isBroadcasting);
        }
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       if(_isBroadcasting) {
           unregisterReceiver(_msr);
           _isBroadcasting = false;
       }
    }
}
