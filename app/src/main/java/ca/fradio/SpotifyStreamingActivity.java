package ca.fradio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class SpotifyStreamingActivity extends Activity implements
        Player.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = "SpotifyStreamingAct";

    private static final String CLIENT_ID = "43e13eb4a573489e8413bc9d83c95719";
    private static final String REDIRECT_URI = "fradiospotifylogin://callback";

    private static final int REQUEST_CODE = 1337;

    private Player mPlayer;
    private BroadcastReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d(TAG, "Launching Spotify Auth");
        openLoginWindow();
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d(TAG, "Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d(TAG, "Auth result: " + response.getType());
            }
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

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        Log.d(TAG, "Got authentication token");
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(),
                    CLIENT_ID);
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            mPlayer = Spotify.getPlayer(playerConfig, this,
                    new SpotifyPlayer.InitializationObserver() {

                @Override
                public void onInitialized(SpotifyPlayer player) {
                    Log.d(TAG, "-- Player initialized --");
                    mPlayer.setConnectivityStatus(mOperationCallback,
                            getNetworkConnectivity(SpotifyStreamingActivity.this));
                    mPlayer.addNotificationCallback(SpotifyStreamingActivity.this);
                    mPlayer.addConnectionStateCallback(SpotifyStreamingActivity.this);
                    Log.d(TAG, "Oh my god it ACTUALLY WORKED");
                }

                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, "Error in initialization: " + t.getMessage(), t);
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
        moveTaskToBack(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "New Intent");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    Log.d(TAG, "Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(SpotifyStreamingActivity.this);
            mPlayer.addConnectionStateCallback(SpotifyStreamingActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkStateReceiver);

        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(SpotifyStreamingActivity.this);
            mPlayer.removeConnectionStateCallback(SpotifyStreamingActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Spotify.destroyPlayer(this);
    }

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

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
        Toast.makeText(this, "Logged in successfully", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "Playback ERROR received: " + error);
    }
}
