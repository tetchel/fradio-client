package ca.fradio;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Utility {

    private static final String TAG = "UTIL";

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
