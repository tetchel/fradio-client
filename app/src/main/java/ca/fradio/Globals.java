package ca.fradio;

import ca.fradio.spotify.SpotifyStreamingService;

public class Globals {

    private static String spotifyUsername;

    public static void setSpotifyUsername(String newSpotifyUsername) {
       spotifyUsername = newSpotifyUsername;
    }

    public static String getSpotifyUsername() {
        return spotifyUsername;
    }

    private static SpotifyStreamingService streamService;

    public static void setStreamService(SpotifyStreamingService newStreamService) {
        streamService = newStreamService;
    }

    public static SpotifyStreamingService getStreamService() {
        return streamService;
    }

    private static final String CLIENT_ID = "43e13eb4a573489e8413bc9d83c95719";

    public static String getClientId() {
        return CLIENT_ID;
    }

    private static String streamer;
    public static String getStreamer() {
        return streamer;
    }
    public static void setStreamer(String streamer) {
        Globals.streamer = streamer;
    }

    private static String broadcastID;
    public static void setBroadcastID(String tid){broadcastID = tid;}
    public static String getBroadcastID(){return broadcastID;}

}
