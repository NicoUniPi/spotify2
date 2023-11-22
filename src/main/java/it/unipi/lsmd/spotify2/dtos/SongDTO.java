package it.unipi.lsmd.spotify2.dtos;

import org.bson.types.ObjectId;

public class SongDTO {
    private ObjectId id;
    private String title;
    private final String genre;
    private final int duration;

    public SongDTO(ObjectId id, String title, String genre, int duration){
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
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
    public String getGenre() {
        return genre;
    }
    public int getDuration() {
        return duration;
    }
}

