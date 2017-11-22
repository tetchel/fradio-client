package ca.fradio.spotify;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import ca.fradio.Globals;
import ca.fradio.R;
import ca.fradio.net.Requester;

public class SpotifyStreamingService extends Service implements ConnectionStateCallback,
        Player.NotificationCallback {

    private static final String TAG = "SpotifyStreamingService";

    private StreamServiceBinder serviceBinder = new StreamServiceBinder();

    private Player player;

    private BroadcastReceiver networkStateReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding service");

        String token = intent.getStringExtra("token");
        onAuthenticationComplete(token);

        setUpNetworkStateReceiver();

        return serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Starting Service");

        return START_NOT_STICKY;
    }

    private void setUpNetworkStateReceiver() {
        // Set up the ic_broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (player != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    Log.d(TAG, "Network state changed: " + connectivity.toString());
                    player.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);

        if (player != null) {
            player.addNotificationCallback(SpotifyStreamingService.this);
            player.addConnectionStateCallback(SpotifyStreamingService.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");

        StatusNotificationManager.cancel(this);

        unregisterReceiver(networkStateReceiver);
        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (player != null) {
            player.removeNotificationCallback(SpotifyStreamingService.this);
            player.removeConnectionStateCallback(SpotifyStreamingService.this);
        }

        Spotify.destroyPlayer(this);
        Requester.requestDisconnect(Globals.getSpotifyUsername());
    }

    public void onAuthenticationComplete(String token) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        Log.d(TAG, "Got authentication token");
        if (player == null) {
            Config playerConfig = new Config(getApplicationContext(), token,
                    Globals.getClientId());
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            player = Spotify.getPlayer(playerConfig, this,
                    new SpotifyPlayer.InitializationObserver() {

                @Override
                public void onInitialized(SpotifyPlayer player) {
                    Log.d(TAG, "-- Player initialized --");
                    player.setConnectivityStatus(mOperationCallback,
                            getNetworkConnectivity(SpotifyStreamingService.this));
                    player.addNotificationCallback(SpotifyStreamingService.this);
                    player.addConnectionStateCallback(SpotifyStreamingService.this);
                    Log.d(TAG, "Oh my god it ACTUALLY WORKED");
                }

                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, "Error in initialization: " + t.getMessage(), t);
                }
            });
        } else {
            Log.d(TAG, "Already logged in");
            player.login(token);
        }
    }

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d("PlayerOpCallback", "OK!");
        }

        @Override
        public void onError(Error error) {
            Log.d("PlayerOpCallback", "ERROR:" + error);
        }
    };

    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    public class StreamServiceBinder extends Binder {
        public SpotifyStreamingService service() {
            return SpotifyStreamingService.this;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.i(TAG, "Logged in");
        //Toast.makeText(this, "Welcome, " + Globals.getSpotifyUsername(),
        //        Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoggedOut() {
        Log.i(TAG, "Logged out");
        Requester.requestDisconnect(Globals.getSpotifyUsername());
    }

    @Override
    public void onLoginFailed(Error e) {
        Log.e(TAG, "Login FAILED: " + e.toString());
        Toast.makeText(this, "Login FAILED: " + e.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTemporaryError() {
        Log.w(TAG, "OnTempError");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "OnConnectionMsg: " + s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "Playback ERROR received: " + error);
    }

    public void playTrack(String hostusername, String trackId, int position) {
        player.playUri(mOperationCallback, trackId, 0, position);
        Log.d(TAG, "playing " + trackId + " from " + position);

        //player.

        /*
        if(player.getMetadata().currentTrack.durationMs < position) {
            Toast.makeText(this, player.getMetadata().currentTrack.name + " is over.",
                    Toast.LENGTH_SHORT).show();

            // TODO remove
            player.playUri(mOperationCallback, trackId, 0, 0);
        }
        */

        updateNotification("", hostusername);
    }

    public void updateNotification(String afterSongMsg, String hostUsername) {
        new UpdateNotificationTask().execute(afterSongMsg, hostUsername);
    }

    public void pause(String hostusername) {
        Log.d(TAG, "pause");
        player.pause(mOperationCallback);
        updateNotification(" (Paused)", hostusername);
    }

    public void seek(int position) {
        Log.d(TAG, "seek");
        player.seekToPosition(mOperationCallback, position);
    }

    public void stopMusic() {
        Log.d(TAG, "stopMusic");
        player.pause(mOperationCallback);

    }

    private class UpdateNotificationTask extends AsyncTask<String, Void, Void> {

        // 1 string parameter is a message to display after the song
        @Override
        protected Void doInBackground(String... strings) {
            String msg = strings[0];
            String host = strings[1];

            Log.d(TAG, "Got msg: " + msg);

            // in ms
            int elapsed = 0;
            while(elapsed < 5000) {
                try {
                    Thread.sleep(500);
                    elapsed += 500;
                    Metadata.Track track = player.getMetadata().currentTrack;
                    if (track != null) {
                        StatusNotificationManager.setStreamingTrack(
                               SpotifyStreamingService.this,
                                host, track.name, track.artistName, track.albumName);
                        break;
                    }
                }
                catch(InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                    // pass
                }
            }

            return null;
        }
    }

    public void connectToSong(JSONObject listenInfo) throws JSONException {
        if(listenInfo == null) {
            Log.e(TAG, "There was an error getting listeninfo - it was null");
            Toast.makeText(this, "Error getting listening info", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Log.d(TAG, listenInfo.toString());

        if(Globals.getStreamService() == null) {
            Toast.makeText(this, "You are not logged in!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String status = listenInfo.getString("status");
        if(status  != null && !status.equals("OK")) {
            Toast.makeText(this, status, Toast.LENGTH_LONG).show();
        }

        long serverTime = listenInfo.getLong("server_time");
        int trackTime = listenInfo.getInt("track_time");
        int trackLen = listenInfo.getInt("track_length");
        String trackid = listenInfo.getString("spotify_track_id");
        String hostusername = listenInfo.getString("host");
        int isPlaying = listenInfo.getInt("is_playing");

        // Account for the listening time that elapsed in transmission
        // Should handle the case of this being too long - if longer than track len, set to 0

        long now = System.currentTimeMillis();
        long elapsed = now - serverTime;
        trackTime += elapsed;

        Handler handler = new Handler(Looper.getMainLooper());

        if(trackTime > trackLen) {
            toast(handler, "Current track has ended", Toast.LENGTH_SHORT);
        }

        if (isPlaying == 1) {
            playTrack(hostusername, trackid, trackTime);
        } else {
            pause(hostusername);
            toast(handler, getString(R.string.app_name) + getString(R.string.paused),
                    Toast.LENGTH_SHORT);
        }
    }

    private void toast(final Handler handler, final String msg, final int len) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SpotifyStreamingService.this, msg, len).show();
            }
        });
    }

    public Metadata.Track getCurrentTrack() {
        return player.getMetadata().currentTrack;
    }
}
