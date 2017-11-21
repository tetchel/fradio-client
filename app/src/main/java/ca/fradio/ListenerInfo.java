package ca.fradio;

/**
 * Created by nwam on 21/11/17.
 */

public class ListenerInfo extends UserInfo {

    private String listening;

    public ListenerInfo(String username, String listening){
        super(username);
        this.listening = listening;
    }

    public String getListening(){return listening;}
}
