package it.unipi.lsmd.spotify2.models;

import org.bson.types.ObjectId;

import java.util.List;

public class Song {
    private ObjectId id;
    private String title;
    private int releasedYear;
    private int duration;
    private List<Artist> artists;
    private String genre;
    private String songImage;

    public Song(){

    }

    public Song(String title){
        this.title = title;
    }
    public Song(ObjectId id, String title, String genre) {
        this.id = id;
        this.title = title;
        this.genre = genre;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleasedYear() {
        return releasedYear;
    }

    public void setReleasedYear(int releasedYear) {
        this.releasedYear = releasedYear;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSongImage() {
        return songImage;
    }

    public void setSongImage(String songImage) {
        this.songImage = songImage;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", releasedYear='" + releasedYear + '\'' +
                ", artists='" + artists + '\'' +
                ", genres='" + genre + '\'' +
                ", songImage='" + songImage + '\'' +
                '}';
    }
}
