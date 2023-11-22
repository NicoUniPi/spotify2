package it.unipi.lsmd.spotify2.utils;

import com.mongodb.client.MongoCursor;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Artist;
import it.unipi.lsmd.spotify2.models.Listener;
import it.unipi.lsmd.spotify2.models.Song;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Mappers {
    public static Song mapDocumentToSong(Document song) {
        Song newSong = new Song();
        newSong.setId(song.getObjectId("_id"));
        newSong.setTitle(song.getString("title"));
        newSong.setReleasedYear(song.getInteger("year"));
        newSong.setDuration(song.getInteger("duration"));
        List<Artist> artistsNames = new ArrayList<>();
        List<Document> artists = song.getList("artists", Document.class);
        for (Document a : artists) {
            Artist artist = new Artist(a.getString("name"), a.getInteger("id"));
            artistsNames.add(artist);
        }
        newSong.setArtists(artistsNames);
        newSong.setGenre(song.getString("genre"));
        newSong.setSongImage(song.getString("image"));
        return newSong;
    }

    public static Listener mapDocumentToListener(Document listener) {
        Listener newListener = new Listener();
        newListener.setUsername(listener.getString("username"));
        newListener.setEmail(listener.getString("email"));
        newListener.setCountry(listener.getString("country"));
        newListener.setListenerImage(listener.getString("picture"));
        return newListener;
    }

    public static List<SongDTO> mapDocumentToSongDTO(List<SongDTO> songs, MongoCursor<Document> cursor) {
        while (cursor.hasNext()) {
            Document songDocument = cursor.next();
            ObjectId idSong = songDocument.getObjectId("_id");
            String title = songDocument.getString("title");
            String genre = songDocument.getString("genre");
            int duration = songDocument.getInteger("duration");
            songs.add(new SongDTO(idSong, title, genre, duration));
        }
        return songs;
    }
}
