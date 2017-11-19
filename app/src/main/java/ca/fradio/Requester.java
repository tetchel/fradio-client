package ca.fradio;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Requester {

    private static final String TAG = "Fradio-Requester";

    private static final String
            PROTOCOL = "http",
            DOMAIN = "ec2-35-182-236-140.ca-central-1.compute.amazonaws.com",
            ENCODING = "UTF-8",

            PARAM_SPOTIFY_USERNAME = "spotifyusername",
            PARAM_SPOTIFY_TRACK = "spotifytrack",
            PARAM_SCROLLTIME = "scrolltime";

    /**
     * Send a request to start BROADCASTING to the server.
     * @return The server's response, or NULL IF AN EXCEPTION OCCURS.
     */
    public JSONObject requestBroadcast(String spotifyUsername, String spotifyTrackid, long scrolltime) {

        try {
            return new BroadcastRequester().execute(spotifyUsername, spotifyTrackid,
                    "" + scrolltime).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject requestListen(String spotifyUsername, String hostToListenTo) {
        try {
            return new ListenRequester().execute(spotifyUsername, hostToListenTo).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class BroadcastRequester extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            String spotifyUsername = strings[0];
            String spotifyTrackid = strings[1];
            long scrolltime = Long.parseLong(strings[2]);

            if(spotifyUsername == null || spotifyTrackid == null) {
                Log.e(TAG, "NPE error!");
                Log.e(TAG, "spotifyUsername==" + spotifyUsername + " spotifyTrackid=="
                        + spotifyTrackid);
                return null;
            }

            try {
                spotifyUsername = URLEncoder.encode(spotifyUsername, ENCODING);
                spotifyTrackid = URLEncoder.encode(spotifyTrackid, ENCODING);

                String query = String.format(Locale.getDefault(),
                        "%s=%s&%s=%s&%s=%d",
                        PARAM_SPOTIFY_USERNAME, spotifyUsername,
                        PARAM_SPOTIFY_TRACK, spotifyTrackid,
                        PARAM_SCROLLTIME, scrolltime);

                return doRequest("broadcast", query);
            }
            catch(IOException e) {
                Log.e(TAG, "Catastrophe!", e);
                return null;
            }
        }
    }

    private static class ListenRequester extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {

            String spotifyUsername = strings[0];
            String hostToListenTo = strings[1];
            try {
                spotifyUsername = URLEncoder.encode(spotifyUsername, ENCODING);

                // First add the current's user's username
                String query = "host" + PARAM_SPOTIFY_USERNAME + '=' + hostToListenTo +
                            "&listener" + PARAM_SPOTIFY_USERNAME + '=' + spotifyUsername;

                JSONObject res = doRequest("listen", query);
                Log.d("Poo", res.toString());
                return res;
            } catch (IOException e) {
                Log.e(TAG, "Catastrophe!", e);
                return null;
            }
        }
    }

    private static JSONObject doRequest(String path, String query) throws IOException {
        String url = String.format("%s://%s/%s?%s", PROTOCOL, DOMAIN, path, query);
        Log.d(TAG, "The expanded broadcast request url is " + url);

        String responseStr = Utility.getFromUrl(url);
        try {
            JSONObject responseStrJSON = new JSONObject(responseStr);
            return responseStrJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

