package it.unipi.lsmd.spotify2.daos.mongo.listener;

import it.unipi.lsmd.spotify2.dtos.SongDTO;
import org.bson.types.ObjectId;

import java.util.List;

public interface LibraryDao {

    List<SongDTO> getLibrary(String username);

    boolean checkSongFromLibrary(String username, ObjectId songId);

    void removeSongFromLibrary(String username, ObjectId songId);

    void addSongToLibrary(String username, SongDTO song);

    List<SongDTO> topLikedSongPerCountry(String country);

    List<String> getDistinctCountries();
}
