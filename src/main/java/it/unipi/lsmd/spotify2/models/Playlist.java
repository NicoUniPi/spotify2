package it.unipi.lsmd.spotify2.models;

import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;

public class Playlist {
    private ObjectId id;
    private String name;
    private int numberOfSongs;
    private LocalDate createdAt;
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numberOfSongs='" + numberOfSongs + '\'' +
                ", createdAt='" + createdAt +
                '}';
    }

}
