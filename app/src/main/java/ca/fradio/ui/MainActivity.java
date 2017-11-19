package ca.fradio.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;
import ca.fradio.spotify.MediaStateReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private final MediaStateReceiver msr = new MediaStateReceiver();
    //private Adapter adapter;

    private boolean isBroadcasting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Requester requester = new Requester();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Populate list view with streamers */
        final ListView listView = (ListView) findViewById(R.id.list_streamers);
        ArrayList<String> streamers = requester.requestStreamers();
        String username = Globals.getSpotifyUsername();

        StreamerListAdapter streamerListAdapter =
                new StreamerListAdapter(this, username, streamers);

        listView.setAdapter(streamerListAdapter);

        final Button broadcastBtn = findViewById(R.id.btn_broadcast);
        broadcastBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(isBroadcasting) {
                    unregisterReceiver(msr);
                    isBroadcasting = false;
                    Toast.makeText(MainActivity.this, "Stopped broadcasting",
                            Toast.LENGTH_SHORT).show();
                    broadcastBtn.setText("Start Streaming");
                }
                else {
                    registerReceiver(msr, msr.getFilter());
                    isBroadcasting = true;
                    Toast.makeText(MainActivity.this, "Started broadcasting",
                            Toast.LENGTH_SHORT).show();
                    broadcastBtn.setText("Stop Streaming");
                }
            }
        });

        //adapter = new (this, R.layout.streamer_list_item, streamers);
        //listView.setAdapter(adapter);

        // ask db for streaming users
        /*
        final Button button = (Button) findViewById(R.id.btn_connect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requester.requestListen(Globals.getSpotifyUsername(), "TheRealGoon");

                Log.d("Poo", "Maybe starting listener");
                if (!Listener.isRunning()) {
                    Log.d("Poo", "Starting listener");
                    Intent listenerIntent = new Intent(MainActivity.this, Listener.class);
                    startService(listenerIntent);
                }
            }
        });
        */

    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       if(isBroadcasting) {
           unregisterReceiver(msr);
           isBroadcasting = false;
       }
    }
}
