package ca.fradio.spotify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import ca.fradio.Globals;

public class SpotifyLoginActivity extends Activity {

    private static final String TAG = "SpotifyLoginActiv";

    private static final String REDIRECT_URI = "fradiospotifylogin://callback";

    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d(TAG, "Launching Spotify Auth");

        openLoginWindow();
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(
                Globals.getClientId(),
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
                    String token = response.getAccessToken();

                    Intent result = new Intent();
                    result.putExtra("token", token);

                    setResult(RESULT_OK, result);
                    finish();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d(TAG, "Auth error: " + response.getError());
                    Toast.makeText(getApplicationContext(),
                            "Login error: " + response.getError(),
                            Toast.LENGTH_LONG).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d(TAG, "Auth result: " + response.getType());
                    Toast.makeText(getApplicationContext(),
                            "Login failure - You'll have to try again.",
                            Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
