package ardel.musonloader;

public class ListItem {

    String title;
    private String trackLink;
    private String artist;
    private String track;
    private String duration;
    private boolean checked;

    private void log(String s) {
        Logger.getInstance().log("ListItem", s);
    }

    public ListItem(String title, String trackLink) {
        this.title = title;
        this.trackLink = trackLink;

        artist = title.substring(0, title.indexOf("-"));
        track = title.substring(title.indexOf("-") + 2, title.indexOf(":"));
        duration = title.substring(title.indexOf(":") + 1);
    }

    public String getArtist() {
        return artist;
    }

    public String getTrack() {
        return track;
    }

    public String getDuration() {
        return duration;
    }

    public String getTrackLink() {
        return trackLink;
    }

    public void setChecked(boolean b) {
        checked = b;
    }

    public boolean getChecked() {
        return checked;
    }
}