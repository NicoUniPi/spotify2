package it.unipi.lsmd.spotify2.daos.impl.neo4j;

import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoPlaylistDaoImpl implements NeoPlaylistDao {

    private final Driver driver;
    private static final Logger logger = LoggerFactory.getLogger(NeoListenerDaoImpl.class);

    public NeoPlaylistDaoImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void createPlaylist(PlaylistDTO newPlaylist, String ownerPlaylist, List<String> songIds, Transaction tx) {
        // Define the Cypher query to create a playlist with a specific ID and name
        String cypherQuery = "MATCH (listener:Listener {username: $owner})" +
                "CREATE (playlist:Playlist {idPlaylist: $playlistId, name: $playlistName})" +
                "CREATE (listener)-[:CREATED]->(playlist)";
        try {
            tx.run(cypherQuery, Values.parameters("owner", ownerPlaylist,
                    "playlistId", newPlaylist.getIdPlaylist().toString(),
                    "playlistName", newPlaylist.getName()));

        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to create the playlist " + newPlaylist.getName(), e);
        }
        cypherQuery = "MATCH (playlist:Playlist {idPlaylist: $playlistId}), (song:Song {songId: $songId}) " +
                "CREATE (playlist)-[:CONTAINS {addedById: $addedById}]->(song)";
        for (String songId : songIds) {
            try {
                // Create a CONTAINS relationship between the playlist and song
                tx.run(cypherQuery, Values.parameters("playlistId",
                        newPlaylist.getIdPlaylist().toString(), "songId", songId, "addedById", ownerPlaylist));
            } catch (Neo4jException e) {
                logger.error("Neo4jDB exception:", e);
                throw new DaoException("Failed to add songs to the playlist with id: " + newPlaylist.getIdPlaylist().toString(), e);
            }
        }
    }

    @Override
    public void addSongToPlaylist(String songId, String playlistId, String loggedUser, Transaction tx) {
        String cypherQuery = "MATCH (playlist:Playlist {idPlaylist: $playlistId}), (song:Song {songId: $songId}) " +
                "CREATE (playlist)-[:CONTAINS {addedById: $addedById}]->(song)";

        try {
            // Create a CONTAINS relationship between the playlist and song
            tx.run(cypherQuery, Values.parameters("playlistId",
                    playlistId, "songId", songId, "addedById", loggedUser));
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to add songs " + songId + "to the playlist with id: " + playlistId, e);
        }
    }

    @Override
    public void removeSongFromPlaylist(String songId, String playlistId, Transaction tx) {
        String cypherQuery = "MATCH (playlist:Playlist {idPlaylist: $playlistId})-[c:CONTAINS]->(song:Song " +
                "{songId: $songId}) DELETE c";
        try {
            // delete a CONTAINS relationship between the playlist and song
            tx.run(cypherQuery, Values.parameters("playlistId",
                    playlistId, "songId", songId));
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to add songs " + songId + "to the playlist with id: " + playlistId, e);
        }
    }

    @Override
    public void removeSongFromFriendPlaylist(String songId, String playlistId, String friend, Transaction tx) {
        String cypherQuery =
                "MATCH (playlist:Playlist {idPlaylist: $playlistId})-[c:CONTAINS]->(song:Song {songId: $songId}) " +
                        "MATCH (friend:Listener {username: $friend})-[a:ADDED]->(playlist) " +
                        "WHERE a.songId = $addedSongId " +  // Add a condition to match a specific relationship
                        "DELETE c, a";

        try {
            // Delete both the CONTAINS relationship between the playlist and song
            // and the ADDED relationship between the friend and the playlist
            tx.run(cypherQuery, Values.parameters("playlistId", playlistId, "songId", songId, "friend", friend, "addedSongId", songId));
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to remove song " + songId + " from the playlist with id: " + playlistId, e);
        }
    }


    @Override
    public void deletePlaylist(String idPlaylist, Transaction tx) {
        // Define the Cypher query to delete a playlist node by its ID
        String cypherQuery = "MATCH (p:Playlist {idPlaylist: $playlistId}) DETACH DELETE p";
        Value parameters = Values.parameters("playlistId", idPlaylist);
        if (tx == null) {
            try {
                Session session = driver.session();
                session.run(cypherQuery, parameters);
            } catch (Neo4jException e) {
                logger.error("Neo4jDB exception:", e);
                throw new DaoException("Failed to delete the playlist with the id " + idPlaylist, e);
            }
        } else {
            try {
                tx.run(cypherQuery, parameters);
            } catch (Neo4jException e) {
                logger.error("Neo4jDB exception:", e);
                throw new DaoException("Failed to delete the playlist with the id " + idPlaylist, e);
            }
        }
    }

    @Override
    public boolean checkPlaylistPresence(String idPlaylist) {
        try (Session session = driver.session()) {
            // Define the Cypher query to check if a playlist with a specific ID exists
            String cypherQuery = "MATCH (p:Playlist {idPlaylist: $playlistId}) RETURN p";

            Value parameters = Values.parameters("playlistId", idPlaylist);

            // Execute the query with the provided parameters
            Result result = session.run(cypherQuery, parameters);

            // Check if the result contains any nodes
            return result.hasNext();
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to check if the playlist with id " + idPlaylist + " is present", e);
        }
    }

    @Override
    public List<PlaylistDTO> getFriendPlaylists(String friendUsername) {
        List<PlaylistDTO> playlists = new ArrayList<>();
        try (Session session = driver.session()) {
            String cypherQuery = "MATCH (listener:Listener {username: $username})-[:CREATED]->(playlist:Playlist) RETURN playlist";
            Result result = session.run(cypherQuery, Values.parameters("username", friendUsername));

            while (result.hasNext()) {
                Record record = result.next();
                Node playlistNode = record.get("playlist").asNode();
                PlaylistDTO playlist = new PlaylistDTO();
                playlist.setIdPlaylist(new ObjectId(playlistNode.get("idPlaylist").asString()));
                playlist.setName(playlistNode.get("name").asString());
                playlists.add(playlist);
            }
        } catch (Neo4jException | IllegalArgumentException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to show the playlists of " + friendUsername + ": ", e);
        }
        return playlists;
    }

    @Override
    public void addSongToFriendPlaylist(String songId, String songTitle, String playlistId, String loggedUser, Transaction tx) {
        // Create an ADDED relationship between a listener and a playlist, including songId
        String query =
                "MATCH (listener:Listener{username: $username}), (playlist:Playlist {idPlaylist: $playlistId}) " +
                        "CREATE (listener)-[:ADDED {songId: $songId}]->(playlist)";

        try {
            tx.run(query, Values.parameters("username", loggedUser, "playlistId", playlistId, "songId", songId));
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to create the relation between " + loggedUser + "and " + playlistId, e);
        }

        // Create a CONTAINS relationship between a playlist and a song with the listener's username
        query =
                "MATCH (playlist:Playlist {idPlaylist: $playlistId}), (song:Song {songId: $songId}) " +
                        "CREATE (playlist)-[:CONTAINS {addedById: $username}]->(song)";

        try {
            tx.run(query, Values.parameters("playlistId", playlistId, "songId", songId, "username", loggedUser));
        } catch (Neo4jException e) {
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to add the song " + songId + " to the friend playlist " + playlistId, e);
        }
    }



    @Override
    public int checkSongInPlaylistPresence(String songId, String playlistId, String friend) {
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String query =  "MATCH (playlist:Playlist {idPlaylist: $playlistId})-[contains: CONTAINS]->(song:Song {songId: $songId}) " +
                        "RETURN contains.addedById AS addedById";

                Value parameters = Values.parameters(
                        "playlistId", playlistId,
                        "songId", songId
                );

                Result result = tx.run(query, parameters);

                if (result.hasNext()) {
                        String addedById = result.single().get("addedById").asString();
                        if (!Objects.equals(addedById, friend))
                            return 1;
                        else
                            return 0;
                }
                return -1;
            } catch (Neo4jException e) {
                logger.error("Neo4jDB transaction exception:", e);
                throw new DaoException("Failed to delete the playlist with the id " + playlistId, e);
            }
        } catch (Exception e) {
            logger.error("Neo4jDB session exception:", e);
        }
        return 0;
    }

    @Override
    public String getUsernameWhoAdded(String songId, String playlistId) {
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String query =  "MATCH (playlist:Playlist {idPlaylist: $playlistId})-[contains: CONTAINS]->(song:Song {songId: $songId}) " +
                        "RETURN contains.addedById AS addedById";

                Value parameters = Values.parameters(
                        "playlistId", playlistId,
                        "songId", songId
                );

                Result result = tx.run(query, parameters);

                if (result.hasNext()) {
                    return result.single().get("addedById").asString();
                }
                return null;
            } catch (Neo4jException e) {
                logger.error("Neo4jDB transaction exception:", e);
                throw new DaoException("Failed to delete the playlist with the id " + playlistId, e);
            }
        } catch (Exception e) {
            logger.error("Neo4jDB session exception:", e);
        }
        return null;
    }
}




















