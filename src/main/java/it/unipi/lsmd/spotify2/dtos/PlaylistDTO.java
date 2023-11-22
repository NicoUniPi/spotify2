package it.unipi.lsmd.spotify2.dtos;

import org.bson.types.ObjectId;

public class PlaylistDTO {
    ObjectId idPlaylist;
    String name;
    public PlaylistDTO(){}

    public PlaylistDTO(ObjectId idPlaylist, String name) {
        this.idPlaylist = idPlaylist;
        this.name = name;
    }

    public ObjectId getIdPlaylist() {
        return idPlaylist;
    }

    public void setIdPlaylist(ObjectId idPlaylist) {
        this.idPlaylist = idPlaylist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
