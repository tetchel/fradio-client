package ca.fradio.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;
import ca.fradio.spotify.MediaStateReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private Requester requester;
    //private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requester = new Requester();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MediaStateReceiver msr = new MediaStateReceiver();
        registerReceiver(msr, msr.getFilter());

        Globals.setSpotifyUsername("tetchel");

        /* Populate list view with streamers */
        final ListView listView = (ListView) findViewById(R.id.list_streamers);
        ArrayList<String> streamers = requester.requestStreamers();
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
}
