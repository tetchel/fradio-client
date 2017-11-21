package ca.fradio;

/**
 * Created by nwam on 21/11/17.
 */

public class StreamerInfo extends UserInfo {

    private boolean isPlaying;

    public StreamerInfo(String username, boolean isPlaying){
        super(username);
        this.isPlaying = isPlaying;
    }

    public boolean getIsPlaying(){return isPlaying;}
}
