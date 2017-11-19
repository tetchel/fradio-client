package ca.fradio.spotify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import ca.fradio.Globals;
import ca.fradio.Requester;

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

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    final String token = response.getAccessToken();

                    try {
                        JSONObject userInfo = new UsernameGetter().execute(token).get();

                        String username = userInfo.getString("id");

                        if(username != null && !username.isEmpty()) {
                            Globals.setSpotifyUsername(username);
                        }
                        else {
                            Log.e(TAG, "THE USERNAME WAS NULL OR EMPTY OH NO");
                        }
                        String product = userInfo.getString("product");

                        if(!product.equals("premium")) {
                            Toast.makeText(this, username + " IS NOT A PREMIUM USER!",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(this, "Successfully logged in as "
                                            + username,  Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

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

    private static class UsernameGetter extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            String token = strings[0];
            try {
                URL meEndpoint = new URL("https://api.spotify.com/v1/me");
                HttpsURLConnection conn = (HttpsURLConnection)
                        meEndpoint.openConnection();
                conn.setRequestProperty("Authorization:", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response from ME : " + responseCode);
                String response = Requester
                        .readAllFromInputStream(conn.getInputStream());
                JSONObject responseObj = new JSONObject(response);
                Log.d(TAG, "Received ME response: " + response);

                return responseObj;

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
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
