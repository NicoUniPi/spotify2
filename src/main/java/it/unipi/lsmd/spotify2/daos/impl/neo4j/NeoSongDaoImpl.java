package it.unipi.lsmd.spotify2.daos.impl.neo4j;

import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Song;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NeoSongDaoImpl implements NeoSongDao {
    private final Driver driver;
    private static final Logger logger = LoggerFactory.getLogger(NeoListenerDaoImpl.class);

    public NeoSongDaoImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void createSong(Song song, Transaction tx) {
        String createSongQuery = "CREATE (s:Song {title: $title, songId: $songId, genre: $genre})";
        Value parameters = Values.parameters("title", song.getTitle(),
                "songId", song.getId().toString(), "genre", song.getGenre());
        try {
            tx.run(createSongQuery, parameters);
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to create the song " + song.getTitle(), e);
        }
    }

    @Override
    public void updateSong(Song song, Transaction tx) {
        String query = "MATCH (s:Song {songId: $songId}) SET s.genre = $genre, " +
                "s.title = $title";
        Value parameters = Values.parameters("songId", song.getId().toString(),
                "genre", song.getGenre(), "title", song.getTitle());
        try {
            tx.run(query, parameters);
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to update the song: " + song.getTitle(), e);
        }
    }

    @Override
    public void deleteSong(String songId, Transaction tx) {
        // Write and execute a Cypher query to delete the song node
        String cypherQuery = "MATCH (song:Song {songId: $id}) DELETE song";
        Value parameter = Values.parameters("id", songId);
        try {
            tx.run(cypherQuery, parameter);
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to delete the song: " + songId, e);
        }
    }

    @Override
    public List<SongDTO> suggestSongsToListener(String loggedListener) {
        try (Session session = driver.session()) {
            List<SongDTO> suggestedSongs = new ArrayList<>();
            String cypherQuery = "MATCH (listener:Listener {username: $username})-[:FOLLOWS]->(follower:Listener) " +
                    "MATCH (follower)-[:CREATED]->(playlist:Playlist) " +
                    "MATCH (playlist)-[contains:CONTAINS]->(song:Song) " +
                    "WHERE contains.addedById <> $username " +
                    "AND NOT EXISTS {" +
                    "MATCH (listener)-[:CREATED]->(Playlist)-[:CONTAINS]->(song)} " +
                    "RETURN song " +
                    "LIMIT 10";

            Value parameters = Values.parameters("username", loggedListener);

            Result result = session.run(cypherQuery, parameters);

            while (result.hasNext()) {
                Record record = result.next();
                Node songNode = record.get("song").asNode();
                String songId = songNode.get("songId").asString();
                String title = songNode.get("title").toString();
                String genre = songNode.get("genre").toString();

                // Remove double quotes from the title
                title = title.replace("\"", "");
                SongDTO song = new SongDTO(new ObjectId(songId), title, genre, 0);
                suggestedSongs.add(song);
            }

            return suggestedSongs;
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to give suggested songs to listener " + loggedListener, e);
        }
    }
}
