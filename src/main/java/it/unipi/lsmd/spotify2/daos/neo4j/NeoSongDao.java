package it.unipi.lsmd.spotify2.daos.neo4j;

import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Song;
import org.neo4j.driver.Transaction;

import java.util.List;

public interface NeoSongDao {
    void createSong(Song song, Transaction tx);
    void updateSong(Song song, Transaction tx);
    void deleteSong(String songId, Transaction tx);
    List<SongDTO>suggestSongsToListener(String listenerLogged);
}
