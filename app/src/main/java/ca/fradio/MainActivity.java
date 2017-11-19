package ca.fradio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

        final Button button = findViewById(R.id.btn_connect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "click connect");
                JSONObject listenInfo = requester.requestListen(Globals.getSpotifyUsername(),
                        "TheRealGoon");
                try {
                    connectToSong(listenInfo);
                } catch (JSONException e) {
                    Log.e(TAG, "Terrible horrible error", e);
                }
            }
        });

        final Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LoginButton");
                Intent intent = new Intent(MainActivity.this,
                        SpotifyStreamingActivity.class);
                startActivity(intent);
            }
        });

    }

    private void connectToSong(JSONObject listenInfo) throws JSONException {
        Log.d(TAG, listenInfo.toString());

        long serverTime = listenInfo.getLong("server_time");
        long trackTime = listenInfo.getLong("track_time");
        String trackid = listenInfo.getString("spotify_track_id");

        long now = System.currentTimeMillis();
        long elapsed = now - serverTime;
        // Account for the listening time that elapsed in transmission
        trackTime += elapsed;

        Intent intent = new Intent(this, SpotifyStreamingActivity.class);
        intent.putExtra("trackid", trackid);
        intent.putExtra("tracktime", trackTime);
        startActivity(intent);
    }
}
