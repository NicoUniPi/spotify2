package it.unipi.lsmd.spotify2.daos.impl.mongo.listener;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.utils.GenreDistribution;
import it.unipi.lsmd.spotify2.utils.PlaylistStats;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Sorts.descending;

// Class implementing the MongoPlaylistDao interface
public class MongoPlaylistDaoImpl implements MongoPlaylistDao {

    // Logger for logging MongoDB-related exceptions
    private static final Logger logger = LoggerFactory.getLogger(MongoPlaylistDaoImpl.class);

    // MongoDB's collection representing listeners
    private final MongoCollection<Document> listenerCollection;

    // Constructor initializes MongoDB connection and collection
    public MongoPlaylistDaoImpl() {
        try {
            MongoClient mongoClient = MongoDBConfig.getMongoClient();
            MongoDatabase database = mongoClient.getDatabase("spotify2");
            this.listenerCollection = database.getCollection("listeners");
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to initialize MongoDB resources", e);
        }
    }

    // Method to add a song to a playlist in MongoDB
    @Override
    public void addSongToPlaylist(String username, SongDTO song, ObjectId idPlaylist) {
        // Create a document representing the song to add
        Document songToAdd = new Document("songId", song.getId())
                .append("title", song.getTitle())
                .append("genre", song.getGenre())
                .append("duration", song.getDuration());

        // Define the filter to match the user and playlist
        Document filter = new Document("username", username)
                .append("playlists.idPlaylist", idPlaylist);

        // Create an update query to push the song to the playlist
        Document updateQuery = new Document("$push", new Document("playlists.$.songs", songToAdd));

        // Update the user's document to add the song to the playlist
        try {
            listenerCollection.updateOne(filter, updateQuery);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to add a song to the playlist " + idPlaylist.toString(), e);
        }
    }

    // Method to add a new playlist for a user in MongoDB
    @Override
    public void addPlaylist(String username, String playlistName) {
        // Create a new playlist document with a generated ID
        Document newPlaylist = new Document()
                .append("idPlaylist", new ObjectId())
                .append("name", playlistName)
                .append("createdAt", LocalDate.now());

        // Update the user's document to add the new playlist
        try {
            listenerCollection.updateOne(
                    new Document("username", username),
                    new Document("$push", new Document("playlists", newPlaylist))
            );
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to add the playlist " + playlistName, e);
        }
    }

    // Method to get songs from a playlist in MongoDB
    @Override
    public List<SongDTO> getSongsFromPlaylist(String username, ObjectId playlistId, ClientSession mongoSession) {
        List<SongDTO> songsInPlaylist = new ArrayList<>();

        // Create the aggregation pipeline to retrieve songs from the playlist
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(match(and(
                Filters.eq("username", username),
                Filters.eq("playlists.idPlaylist", playlistId)
        )));
        pipeline.add(unwind("$playlists"));
        pipeline.add(match(Filters.eq("playlists.idPlaylist", playlistId)));
        pipeline.add(Aggregates.project(new Document("songs", "$playlists.songs")));

        // Execute the aggregation based on whether a session is provided or not
        if (mongoSession == null) {
            try (MongoCursor<Document> cursor = listenerCollection.aggregate(pipeline).iterator()) {
                if (playlistScrolling(songsInPlaylist, cursor)) return songsInPlaylist;
            } catch (MongoException e) {
                // Log MongoDB exception and throw custom DaoException
                logger.error("MongoDB exception:", e);
                throw new DaoException("Failed to get songs from the playlist " + playlistId.toString(), e);
            }
        } else {
            try (MongoCursor<Document> cursor = listenerCollection.aggregate(mongoSession, pipeline).iterator()) {
                if (playlistScrolling(songsInPlaylist, cursor)) return songsInPlaylist;
            } catch (MongoException e) {
                // Log MongoDB exception and throw custom DaoException
                logger.error("MongoDB exception:", e);
                throw new DaoException("Failed to get songs from the playlist " + playlistId.toString(), e);
            }
        }
        return null;
    }

    // Helper method for scrolling through songs in a playlist
    private boolean playlistScrolling(List<SongDTO> songsInPlaylist, MongoCursor<Document> cursor) {
        if (cursor.hasNext()) {
            Document result = cursor.next();
            List<Document> songsList = result.getList("songs", Document.class);

            // Process the songs list and add to the result list
            if (songsList != null) {
                for (Document songDocument : songsList) {
                    scrollSongDto(songsInPlaylist, songDocument);
                }
            }
            return true;
        }
        return false;
    }

    static void scrollSongDto(List<SongDTO> songsInPlaylist, Document songDocument) {
        ObjectId songId = songDocument.getObjectId("songId");
        String title = songDocument.getString("title");
        String genre = songDocument.getString("genre");
        int duration = songDocument.getInteger("duration");
        SongDTO songDTO = new SongDTO(songId, title, genre, duration);
        songsInPlaylist.add(songDTO);
    }

    // Method to remove a song from a playlist in MongoDB
    @Override
    public void removeSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId) {
        // Create the filter to identify the user and playlist
        Document filter = new Document("username", username)
                .append("playlists.idPlaylist", playlistId);

        // Create the update operation to remove the song from the playlist
        Document update = new Document("$pull", new Document("playlists.$.songs", new Document("songId", songId)))
                .append("$inc", new Document("playlists.$.numberOfSongs", -1));

        try {
            // Perform the update operation
            listenerCollection.updateOne(filter, update);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to remove the song " + songId.toString() +
                    " from the playlist " + playlistId.toString(), e);
        }
    }

    // Method to check if a song is in a playlist in MongoDB
    @Override
    public boolean checkSongFromPlaylist(String username, ObjectId playlistId, ObjectId songId) {
        // Create the aggregation pipeline to check if the song is in the playlist
        List<Bson> pipeline = Arrays.asList(
                match(Filters.eq("username", username)),
                match(Filters.eq("playlists.idPlaylist", playlistId)),
                unwind("$playlists"),
                match(Filters.eq("playlists.idPlaylist", playlistId)),
                unwind("$playlists.songs"),
                match(Filters.eq("playlists.songs.songId", songId))
        );

        try {
            // Execute the aggregation
            Document result = listenerCollection.aggregate(pipeline).first();

            // Return true if the result is not null (song is in the playlist)
            return result != null;
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to check if the song " + songId.toString() + " is in the playlist " + playlistId.toString(), e);
        }
    }

    // Method to remove a playlist for a user in MongoDB
    @Override
    public void removePlaylist(String username, ObjectId idPlaylist) {
        // Create the update query to remove the playlist
        Document updateQuery = new Document("$pull", new Document("playlists",
                new Document("idPlaylist", idPlaylist)));

        // Perform the update operation
        try {
            listenerCollection.updateOne(new Document("username", username), updateQuery);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to remove the playlist " + idPlaylist.toString(), e);
        }
    }

    // Method to get all playlists for a user in MongoDB
    @Override
    public List<PlaylistDTO> getPlaylists(String username) {
        List<PlaylistDTO> playlists = new ArrayList<>();

        // Create the aggregation pipeline to retrieve all playlists
        List<Bson> pipeline = Arrays.asList(
                match(Filters.eq("username", username)),
                unwind("$playlists"), // Unwind the "playlists" array
                Aggregates.replaceRoot("$playlists") // Replace the root with the "playlists" array
        );

        // Execute the aggregation
        try (MongoCursor<Document> cursor = listenerCollection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document playlistDocument = cursor.next();
                playlists.add(new PlaylistDTO(playlistDocument.getObjectId("idPlaylist"), playlistDocument.getString("name")));
            }
            return playlists;
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get all the playlists", e);
        }
    }

    // Method to retrieve playlist statistics (e.g., total playlists, total songs) in MongoDB
    @Override
    public PlaylistStats playlistStatistics(String username, LocalDate startDate, LocalDate endDate) {
        // Create the aggregation pipeline for playlist statistics
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document("username", username)),
                new Document("$match", new Document("playlists.createdAt", new Document("$gte", startDate)
                        .append("$lte", endDate))),
                new Document("$unwind", "$playlists"),
                new Document("$unwind", "$playlists.songs"),
                new Document("$group", new Document()
                        .append("_id", null)
                        .append("totalPlaylists", new Document("$addToSet", "$playlists.idPlaylist"))
                        .append("totalSongs", new Document("$sum", 1))
                        .append("totalDuration", new Document("$sum", "$playlists.songs.duration"))
                        .append("averageDuration", new Document("$avg", "$playlists.songs.duration"))
                ),
                new Document("$project", new Document()
                        .append("_id", 0)
                        .append("totalPlaylists", new Document("$size", "$totalPlaylists"))
                        .append("totalSongs", 1)
                        .append("totalDuration", 1)
                        .append("averageDuration", 1)
                )
        );

        // Initialize PlaylistStats object to store the result
        PlaylistStats playlistStats = new PlaylistStats();

        try {
            // Execute the aggregation and get the result
            AggregateIterable<Document> result = listenerCollection.aggregate(pipeline);

            for (Document document : result) {
                // Map the result to a PlaylistStats object
                playlistStats.setUsername(username);
                playlistStats.setStartDate(startDate);
                playlistStats.setEndDate(endDate);
                playlistStats.setTotalPlaylists(document.getInteger("totalPlaylists"));
                playlistStats.setTotalSongs(document.getInteger("totalSongs"));
                playlistStats.setTotalDuration(document.getInteger("totalDuration"));
                playlistStats.setAverageSongLength(document.getDouble("averageDuration"));
                return playlistStats;
            }
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the playlists statistics", e);
        }
        return playlistStats;
    }

    // Method to retrieve genre distribution of songs in playlists in MongoDB
    @Override
    public List<GenreDistribution> genreDistribution() {
        // Create the aggregation pipeline for genre distribution
        List<Bson> pipeline = Arrays.asList(
                unwind("$playlists"),
                unwind("$playlists.songs"),
                group("$playlists.songs.genre", sum("count", 1)),
                sort(descending("count")),
                limit(10)
        );

        // Initialize a list to store GenreDistribution objects
        List<GenreDistribution> genreDistributions = new ArrayList<>();

        // Execute the aggregation
        try {
            AggregateIterable<Document> result = listenerCollection.aggregate(pipeline);

            // Process the result and add to the list
            for (Document document : result) {
                String genre = document.getString("_id");
                int count = document.getInteger("count");
                genreDistributions.add(new GenreDistribution(genre, count));
            }
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the genre distribution", e);
        }

        return genreDistributions;
    }
}
