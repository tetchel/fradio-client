package ca.fradio.net;

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

import ca.fradio.UserInfo;

public class Requester {

    private static final String TAG = "Fradio-Requester";

    private static final String
            PROTOCOL = "http",
            DOMAIN = "ec2-35-182-236-140.ca-central-1.compute.amazonaws.com",
            ENCODING = "UTF-8",

            PARAM_SPOTIFY_USERNAME = "spotifyusername",
            PARAM_SPOTIFY_TRACK = "trackid",
            PARAM_SCROLLTIME = "t",
            PARAM_LENGTH = "len",
            PARAM_PLAYING = "playing";

    // Static methods only
    protected Requester() { }

    /**
     * Send a request to start BROADCASTING to the server. Does not wait for server's response.
     */
    public static void requestBroadcast(String spotifyUsername, String spotifyTrackid,
                                       long scrolltime, long trackLength, boolean playing) {

        new BroadcastRequester().execute(spotifyUsername, spotifyTrackid,
                "" + scrolltime, "" + trackLength, "" + isPlayingToInt(playing));
    }

    /**
     * For use by tests only - Waits for response before returning it.
     * @return
     */
    public static JSONObject requestBroadcastResult(String spotifyUsername, String spotifyTrackid,
            long scrolltime, long trackLength, boolean playing)
            throws ExecutionException, InterruptedException {

        return new BroadcastRequester().execute(spotifyUsername, spotifyTrackid,
                "" + scrolltime, "" + trackLength, "" + isPlayingToInt(playing)).get();
    }

    private static int isPlayingToInt(boolean isPlaying) {
        return isPlaying ? 1 : 0;
    }

    public static JSONObject requestListen(String spotifyUsername, String hostToListenTo) {
        try {
            return new ListenRequester().execute(spotifyUsername, hostToListenTo).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<UserInfo> requestStreamers() {
        try {
            return getUserInfo(new StreamersRequester().execute().get()
                    .getJSONArray("streamers"));
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void requestDisconnect(String spotifyUsername){
        new DisconnectRequester().execute(spotifyUsername);
    }

    public static void requestStopListen(String spotifyUsername){
        new StopListenRequester().execute(spotifyUsername);
    }

    private static class BroadcastRequester extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String spotifyUsername = strings[0];
            String spotifyTrackid = strings[1];
            long scrolltime = Long.parseLong(strings[2]);
            long length = Long.parseLong(strings[3]);
            int isPlaying = Integer.parseInt(strings[4]);

            if(spotifyUsername == null || spotifyTrackid == null || length == 0) {
                Log.e(TAG, "Invalid broadcast error!");
                Log.e(TAG, "spotifyUsername=" + spotifyUsername + " spotifyTrackid="
                        + spotifyTrackid + " length=" + length);
                return null;
            }

            try {
                spotifyUsername = URLEncoder.encode(spotifyUsername, ENCODING);
                spotifyTrackid = URLEncoder.encode(spotifyTrackid, ENCODING);

                String query = String.format(Locale.getDefault(),
                        "%s=%s&%s=%s&%s=%d&%s=%d&%s=%d",
                        PARAM_SPOTIFY_USERNAME, spotifyUsername,
                        PARAM_SPOTIFY_TRACK, spotifyTrackid,
                        PARAM_SCROLLTIME, scrolltime,
                        PARAM_LENGTH, length,
                        PARAM_PLAYING, isPlaying);

                JSONObject result = doRequest("broadcast", query);
                String status = result.getString("status");
                if(!status.equals("OK")) {
                    Log.e(TAG, "Received error after broadcast report: " + status);
                }
                return result;
            }
            catch(IOException | JSONException e) {
                Log.e(TAG, "BroadcastReq Catastrophe!", e);
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
                Log.e(TAG, "ListenReq Catastrophe!", e);
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

                Log.d(TAG, "Streamers request");
                JSONObject res = doRequest("streamers", query);
                Log.d(TAG, res.toString());
                return res;
            } catch (JSONException | IOException e) {
                Log.e(TAG, "StreamerReq Catastrophe!", e);
                return null;
            }
        }
    }

    private static class DisconnectRequester extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {

            String spotifyUsername = strings[0];
            try {
                spotifyUsername = URLEncoder.encode(spotifyUsername, ENCODING);
                String query = PARAM_SPOTIFY_USERNAME + '=' + spotifyUsername;

                Log.d(TAG, "Disconnect request");
                JSONObject res = doRequest("disconnect", query);
                Log.d(TAG, res.toString());
                return res;
            } catch (JSONException | IOException e) {
                Log.e(TAG, "DisconnectReq Catastrophe!", e);
                return null;
            }
        }
    }

    private static class StopListenRequester extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {

            String spotifyUsername = strings[0];
            try {
                spotifyUsername = URLEncoder.encode(spotifyUsername, ENCODING);
                String query = PARAM_SPOTIFY_USERNAME + '=' + spotifyUsername;

                Log.d(TAG, "StopListen request");
                JSONObject res = doRequest("stop_listen", query);
                Log.d(TAG, res.toString());
                return res;
            } catch (JSONException | IOException e) {
                Log.e(TAG, "Catastrophe!", e);
                return null;
            }
        }
    }

    private static JSONObject doRequest(String path, String query)
            throws IOException, JSONException {

        String url = String.format("%s://%s/%s?%s", PROTOCOL, DOMAIN, path, query);

        String responseStr = getFromUrl(url);
        return new JSONObject(responseStr);
    }

    private static ArrayList<UserInfo> getUserInfo(JSONArray userInfoJsa){
        ArrayList<UserInfo> result = new ArrayList<>(userInfoJsa.length());
        try {
            for(int i = 0; i < userInfoJsa.length(); i++) {
                JSONObject user = userInfoJsa.getJSONObject(i);
                String username = user.getString("name");
                int streamStatus = user.getInt("status");
                boolean isOnline = streamStatus == 1;
                Log.d(TAG, "A streamer : " + username + " is online ? " + streamStatus);

                result.add(new UserInfo(username, isOnline));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON of streamers", e);
        }
        return result;
    }

    public static String getFromUrl(String url) throws IOException {
        Log.d(TAG, "Getting from " + url);

        URLConnection conn = new URL(url).openConnection();

        //conn.setRequestProperty("Accept-Charset", ENCODING);
        InputStream is = conn.getInputStream();
        Log.d(TAG, "Finished getting response");

        String resp = readAllFromInputStream(is);
        Log.d(TAG, resp);
        return resp;
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

