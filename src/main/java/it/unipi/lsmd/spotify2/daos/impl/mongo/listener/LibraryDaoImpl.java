package it.unipi.lsmd.spotify2.daos.impl.mongo.listener;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class declaration for LibraryDaoImpl implementing LibraryDao interface
public class LibraryDaoImpl implements LibraryDao {

    // Logger for logging MongoDB exceptions
    private static final Logger logger = LoggerFactory.getLogger(LibraryDaoImpl.class);

    // MongoDB's collection for listeners
    private final MongoCollection<Document> listenerCollection;

    // Constructor to initialize MongoDB resources
    public LibraryDaoImpl() {
        try {
            MongoClient mongoClient = MongoDBConfig.getMongoClient();
            MongoDatabase database = mongoClient.getDatabase("spotify2");
            this.listenerCollection = database.getCollection("listeners");
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to initialize MongoDB resources", e);
        }
    }

    // Method to get the library for a given username
    @Override
    public List<SongDTO> getLibrary(String username) {
        List<SongDTO> songs = new ArrayList<>();

        // Create the aggregation pipeline
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("username", username)),
                Aggregates.unwind("$library"), // Unwind the "library" array
                Aggregates.replaceRoot("$library") // Replace the root with the "library" array
        );

        // Execute the aggregation
        try (MongoCursor<Document> cursor = listenerCollection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document songDocument = cursor.next();
                ObjectId idSong = songDocument.getObjectId("songId");
                String title = songDocument.getString("title");
                String genre = songDocument.getString("genre");
                int duration = songDocument.getInteger("duration");
                songs.add(new SongDTO(idSong, title, genre, duration));
            }
            return songs;
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the library", e);
        }
    }

    // Method to check if a song is in the library for a given username
    @Override
    public boolean checkSongFromLibrary(String username, ObjectId songId) {
        // Create a query to find the user with the given username and library containing the song ID
        Document query = new Document("username", username)
                .append("library", new Document("$elemMatch", new Document("songId", songId)));
        // Check if any matching document exists
        try {
            return listenerCollection.countDocuments(query) > 0;
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to check if the song " + songId.toString() + " is in the library", e);
        }
    }

    // Method to remove a song from the library for a given username and song ID
    @Override
    public void removeSongFromLibrary(String username, ObjectId songId) {
        // Create a query to find the user by username and remove the song from the library
        Document query = new Document("username", username);
        Document update = new Document("$pull", new Document("library", new Document("songId", songId)));
        // Check if any matching document exists
        try {
            listenerCollection.updateOne(query, update);
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to remove the song " + songId.toString() + " from the library", e);
        }
    }

    // Method to add a song to the library for a given username
    @Override
    public void addSongToLibrary(String username, SongDTO song) {
        Document songDocument = new Document("songId", song.getId())
                .append("title", song.getTitle())
                .append("addedAt", LocalDate.now())
                .append("genre", song.getGenre())
                .append("duration", song.getDuration());
        try {
            // Update the user's library with the new song
            listenerCollection.updateOne(
                    new Document("username", username),
                    new Document("$push", new Document("library", songDocument))
            );
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to add the song " + song.getTitle() + " to the library", e);
        }
    }

    // Method to get the top ten liked songs for a given country
    @Override
    public List<SongDTO> topLikedSongPerCountry(String country) {
        List<SongDTO> topLikedSongs = new ArrayList<>();
        // Aggregation pipeline stages
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("country", country)),
                new Document("$unwind", "$library"),
                new Document("$group", new Document("_id", new Document("songId", "$library.songId")
                        .append("title", "$library.title"))
                        .append("count", new Document("$sum", 1))
                ),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 10)
        );
        try {
            AggregateIterable<Document> result = listenerCollection.aggregate(pipeline);

            // Iterate over the result and create SongDTO objects
            for (Document document : result) {
                Document compoundKey = (Document) document.get("_id");
                MongoPlaylistDaoImpl.scrollSongDto(topLikedSongs, compoundKey);
            }
            return topLikedSongs;
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the top ten liked songs: ", e);
        }
    }

    // Method to get a list of distinct countries from the database
    @Override
    public List<String> getDistinctCountries() {
        try {
            return listenerCollection.distinct("country", String.class).into(new ArrayList<>());
        } catch (MongoException e) {
            // Log the exception and throw a custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the list of countries: ", e);
        }
    }
}
