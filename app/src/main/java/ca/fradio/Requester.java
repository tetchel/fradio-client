package ca.fradio;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Requester {

    private static final String TAG = "Fradio-Requester";

    private static final String
            PROTOCOL = "http",
            DOMAIN = "ec2-35-182-236-140.ca-central-1.compute.amazonaws.com",
            ENCODING = "UTF-8",

            PARAM_SPOTIFY_USERNAME = "spotifyusername",
            PARAM_SPOTIFY_TRACK = "trackid",
            PARAM_SCROLLTIME = "t",
            PARAM_LENGTH="len";

    /**
     * Send a request to start BROADCASTING to the server.
     * @return The server's response, or NULL IF AN EXCEPTION OCCURS.
     */
    public static void requestBroadcast(String spotifyUsername, String spotifyTrackid,
                                       long scrolltime, long trackLength) {

        new BroadcastRequester().execute(spotifyUsername, spotifyTrackid,
                "" + scrolltime, "" + trackLength);
    }

    public static JSONObject requestListen(String spotifyUsername, String hostToListenTo) {
        try {
            return new ListenRequester().execute(spotifyUsername, hostToListenTo).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> requestStreamers() {
        try {
            return parseJSONArray(new StreamersRequester().execute().get()
                    .getJSONArray("streamers"));
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class BroadcastRequester extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String spotifyUsername = strings[0];
            String spotifyTrackid = strings[1];
            long scrolltime = Long.parseLong(strings[2]);
            long length = Long.parseLong(strings[3]);

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
                        "%s=%s&%s=%s&%s=%d&%s=%d",
                        PARAM_SPOTIFY_USERNAME, spotifyUsername,
                        PARAM_SPOTIFY_TRACK, spotifyTrackid,
                        PARAM_SCROLLTIME, scrolltime,
                        PARAM_LENGTH, length);

                JSONObject result = doRequest("broadcast", query);
                String status = result.getString("status");
                if(!status.equals("OK")) {
                    Log.e(TAG, "Received error after broadcast report: " + status);
                }
            }
            catch(IOException | JSONException e) {
                Log.e(TAG, "Catastrophe!", e);
            }
            return null;
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
                // Also put the host into the result
                res.put("host", hostToListenTo);
                Log.d(TAG, res.toString());
                return res;
            } catch (JSONException | IOException e) {
                Log.e(TAG, "Catastrophe!", e);
                return null;
            }
        }
    }

    private static class StreamersRequester extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {

            try {
                // No args (for now)
                String query = "";

                JSONObject res = doRequest("streamers", query);
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

        String responseStr = getFromUrl(url);
        try {
            JSONObject responseStrJSON = new JSONObject(responseStr);
            return responseStrJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<String> parseJSONArray(JSONArray jsonArray){

        try {
            ArrayList<String> list = new ArrayList<String>();

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }

            }
            return list;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON of streamers", e);
        }
        return null;
    }

    public static String getFromUrl(String url) throws IOException {
        Log.d(TAG, "Getting from " + url);

        URLConnection conn = new URL(url).openConnection();

        //conn.setRequestProperty("Accept-Charset", ENCODING);
        InputStream is = conn.getInputStream();
        Log.d(TAG, "Finished getting response");

        return readAllFromInputStream(is);
    }

    public static String readAllFromInputStream(InputStream is) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        int i;
        while ((i = is.read()) != -1) {
            responseBuilder.append((char) i);
        }

        return responseBuilder.toString();
    }
}

