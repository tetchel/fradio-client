package ca.fradio;

// Data type class to hold all information we care about for a given user
public class UserInfo {

    private String username;
    private boolean isStreaming;

    public UserInfo(String username, boolean isStreaming) {
        this.username = username;
        this.isStreaming = isStreaming;
    }

    public String getUsername() {
        return username;
    }

    public boolean isStreaming() {
        return isStreaming;
    }
}
