package ca.fradio;

import android.app.Application;

public class Globals {

    private static String spotifyUsername;

    public static void setSpotifyUsername(String newSpotifyUsername) {
       spotifyUsername = newSpotifyUsername;
    }

    public static String getSpotifyUsername() {
        return spotifyUsername;
    }
}
