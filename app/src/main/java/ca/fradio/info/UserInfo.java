package ca.fradio.info;

// Data type class to hold all information we care about for a given user
public class UserInfo {

    private String username;

    public UserInfo(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
