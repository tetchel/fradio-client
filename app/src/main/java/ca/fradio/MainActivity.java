package ca.fradio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.fradio.Listener;

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
