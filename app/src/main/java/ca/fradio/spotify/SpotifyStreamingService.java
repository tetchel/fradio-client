package ca.fradio.spotify;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import ca.fradio.Globals;

public class SpotifyStreamingService extends Service implements ConnectionStateCallback,
        Player.NotificationCallback {

    private static final String TAG = "SpotifyStreamingService";

    private StreamServiceBinder streamBinder = new StreamServiceBinder();

    private Player player;

    private BroadcastReceiver networkStateReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding service");

        String token = intent.getStringExtra("token");
        onAuthenticationComplete(token);

        setUpNetworkStateReceiver();

        StatusNotificationManager.instance().setContext(this);

        Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_LONG)
                .show();

        return streamBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Starting Service");

        return START_NOT_STICKY;
    }

    private void setUpNetworkStateReceiver() {
        // Set up the broadcast receiver for network events. Note that we also unregister
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
    }

    public void onAuthenticationComplete(String token)
        {

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
    }

    @Override
    public void onLoggedOut() {
        Log.i(TAG, "Logged out");
    }

    @Override
    public void onLoginFailed(Error e) {
        Log.e(TAG, "Login FAILED: " + e.toString());
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

    public void playTrack(String trackId, int position) {
        player.playUri(mOperationCallback, trackId, 0, position);
        Log.d(TAG, "playing " + trackId + " from " + position);

        /*
        if(player.getMetadata().currentTrack.durationMs < position) {
            Toast.makeText(this, player.getMetadata().currentTrack.name + " is over.",
                    Toast.LENGTH_SHORT).show();

            // TODO remove
            player.playUri(mOperationCallback, trackId, 0, 0);
        }
        */

        StatusNotificationManager.instance().playing(player.getMetadata().currentTrack);
    }

    public void resume() {
        Log.d(TAG, "Resume");
        player.playUri(mOperationCallback, player.getMetadata().contextUri, 0, (int)
                player.getPlaybackState().positionMs);

        StatusNotificationManager.instance().playing(player.getMetadata().currentTrack);
    }

    public void pause() {
        Log.d(TAG, "pause");
        player.pause(mOperationCallback);
        StatusNotificationManager.instance().paused(player.getMetadata().currentTrack);
    }

    public void seek(int position) {
        Log.d(TAG, "seek");
        player.seekToPosition(mOperationCallback, position);
    }

    public void stop() {
        Log.d(TAG, "stop");
    }
}
