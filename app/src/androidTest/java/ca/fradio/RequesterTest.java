package ca.fradio;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import ca.fradio.net.Requester;
import ca.fradio.ui.SetupActivity;

@RunWith(AndroidJUnit4.class)
public class RequesterTest extends InstrumentationTestCase {

    @Rule
    public ActivityTestRule<SetupActivity> activityRule
            = new ActivityTestRule<>(
            SetupActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent

    private static final String
            SPOTIFY_NAME = "tetchel",
            SPOTFIY_HOST_NAME = "noahmurad",
            TRACKID = "spotify:track:2pwTzYUTIiwF7Pn8ygXD91";

    @Test
    public void testBroadcast() throws ExecutionException, InterruptedException, JSONException {
         JSONObject result = Requester.requestBroadcastResult(SPOTIFY_NAME, TRACKID,
                 0, 385880, true);

         assertNotNull("Result was null from broadcast request!", result);

         String status = result.getString("status");
         assertEquals(status, "OK");
    }

    @Test
    public void testListen() throws Throwable {
        JSONObject result = Requester.requestListen(SPOTIFY_NAME, SPOTFIY_HOST_NAME);

        assertNotNull("Result was null from listen request!", result);

        // make sure the result has all the expect keys
        assertEquals(result.getString("status"), "OK");
        result.getLong("server_time");
        result.getInt("track_time");
        result.getInt("track_length");
        result.getString("spotify_track_id");
        result.getString("host");
        result.getInt("is_playing");

        // Log in
        /*
        activityRule.launchActivity(new Intent());
        final Button loginBtn = activityRule.getActivity().findViewById(R.id.btn_login);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginBtn.performClick();
            }
        });
        
        int i = 0;
        while(!activityRule.getActivity().isFinishing() && i < 10) {
            i++;
            Thread.sleep(1000);
        }
        activityRule.finishActivity();*/
        /*
        SpotifyStreamingService streamService = Globals.getStreamService();
        assertNotNull("StreamService was null", streamService);
        streamService.connectToSong(result);

        Player player = streamService.getPlayer();
        assertTrue(player.getPlaybackState().isPlaying);
        assertEquals(player.getMetadata().currentTrack.uri, result.getString("trackid"));
        */
    }
}
