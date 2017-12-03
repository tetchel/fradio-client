package ca.fradio.info;

/**
 * Created by nwam on 21/11/17.
 */

public class StreamerInfo extends UserInfo {

    private boolean isPlaying;
    private SongInfo songInfo;

    public StreamerInfo(String username, boolean isPlaying){
        super(username);
        this.isPlaying = isPlaying;
    }

    public StreamerInfo(String username, boolean isPlaying, SongInfo songInfo){
        super(username);
        this.isPlaying = isPlaying;
       this.songInfo = songInfo;
    }

    public boolean getIsPlaying(){return isPlaying;}

    public SongInfo getSongInfo() {
        return songInfo;
    }

}
