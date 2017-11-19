package ca.fradio.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.Requester;
import ca.fradio.spotify.MediaStateReceiver;
import ca.fradio.spotify.SpotifyLoginActivity;
import ca.fradio.spotify.SpotifyStreamingService;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "Fradio-Main";

    private final Requester requester = new Requester();

    private final MediaStateReceiver msr = new MediaStateReceiver();

    public static final int LOGIN_ACTIVITY_REQUEST_CODE = 14321;

    private boolean hasSpotifyServiceBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private static ServiceConnection streamServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d("StreamServiceConn", "SERVICE CONNECTED TO STREAM SERVICE CONNECTION");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpotifyStreamingService.StreamServiceBinder binder =
                    (SpotifyStreamingService.StreamServiceBinder) service;
            Globals.setStreamService(binder.service());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Globals.setStreamService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        registerReceiver(msr, msr.getFilter());

        Globals.setSpotifyUsername("tetchel");

        final Button button = findViewById(R.id.btn_connect);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "click connect");
                JSONObject listenInfo = requester.requestListen(Globals.getSpotifyUsername(),
                        "tetchel");
               /*
                try {
                    //connectToSong(listenInfo);
                } catch (JSONException e) {
                    Log.e(TAG, "Terrible horrible error", e);
                }
                */
               startActivity(new Intent(SetupActivity.this, MainActivity.class));
            }
        });

        final Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LoginButton");
                Intent intent = new Intent(SetupActivity.this,
                        SpotifyLoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if(requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                // The login activity calls back after populating this Service reference
                Log.d(TAG, "We got an activity result");

                String token = result.getStringExtra("token");

                Intent serviceIntent = new Intent(this,
                        SpotifyStreamingService.class);
                serviceIntent.putExtra("token", token);
                bindService(serviceIntent, streamServiceConn, Context.BIND_AUTO_CREATE);
                hasSpotifyServiceBound = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(hasSpotifyServiceBound) {
            unbindService(streamServiceConn);
        }
        unregisterReceiver(msr);
    }
}
