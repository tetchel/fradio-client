package ca.fradio.info;

// There's an Android calss called SongInfo
public class SongInfo {
    public String title;
    public String artist;
    public String album;
    public String artUrl;
    public String artThumbUrl;

    public SongInfo(String title, String artist, String album, String artUrl, String artThumbUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.artUrl = artUrl;
        this.artThumbUrl = artThumbUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtUrl() {
        return artUrl;
    }

    public String getArtThumbUrl() {
        return artThumbUrl;
    }

}
