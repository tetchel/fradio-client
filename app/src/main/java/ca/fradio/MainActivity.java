package ca.fradio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private Requester requester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requester = new Requester();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MediaStateReceiver msr = new MediaStateReceiver();
        registerReceiver(msr, msr.getFilter());

        Globals.setSpotifyUsername("tetchel");

        final Button button = (Button) findViewById(R.id.btn_connect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject listenInfo = requester.requestListen(Globals.getSpotifyUsername(),
                        "TheRealGoon");
                try {
                    connectToSong(listenInfo);
                } catch (JSONException e) {
                    Log.e(TAG, "Terrible horrible error", e);
                }
            }
        });

    }

    private void connectToSong(JSONObject listenInfo) throws JSONException {
        Log.d(TAG, listenInfo.toString());

        long serverTime = listenInfo.getLong("server_time");
        long trackTime = listenInfo.getLong("track_time");
        String trackid = listenInfo.getString("spotify_track_id");


    }
}
