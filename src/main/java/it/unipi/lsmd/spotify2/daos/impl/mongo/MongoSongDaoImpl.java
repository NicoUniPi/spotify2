package it.unipi.lsmd.spotify2.daos.impl.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.models.Artist;
import it.unipi.lsmd.spotify2.models.Song;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Static import of utility class methods for mapping documents to Song and SongDTO objects
import static it.unipi.lsmd.spotify2.utils.Mappers.mapDocumentToSong;
import static it.unipi.lsmd.spotify2.utils.Mappers.mapDocumentToSongDTO;

// Class implementing the MongoSongDao interface
public class MongoSongDaoImpl implements MongoSongDao {

    // MongoDB's collection representing songs
    private final MongoCollection<Document> songCollection;

    // Logger for logging MongoDB-related exceptions
    private static final Logger logger = LoggerFactory.getLogger(MongoSongDaoImpl.class);

    // Constructor initializes MongoDB connection and collection
    public MongoSongDaoImpl() {
        MongoClient mongoClient = MongoDBConfig.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("spotify2");
        this.songCollection = database.getCollection("songs");
    }

    // Method to create a new song in MongoDB
    @Override
    public void createSong(Song song, ClientSession session) {
        // Convert artists to a list of documents
        List<Document> artistDocs = new ArrayList<>();
        for (Artist artist : song.getArtists()) {
            Document artistDoc = new Document()
                    .append("id", artist.getId())
                    .append("name", artist.getName());
            artistDocs.add(artistDoc);
        }

        // Create a document representing the song
        Document songDoc = new Document("_id", song.getId())
                .append("title", song.getTitle())
                .append("year", song.getReleasedYear())
                .append("artists", artistDocs)
                .append("duration", song.getDuration())
                .append("image", song.getSongImage())
                .append("genre", song.getGenre());

        try {
            // Insert the new song document into the collection
            songCollection.insertOne(session, songDoc);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to create a new song", e);
        }
    }

    // Method to search for songs by title in MongoDB
    @Override
    public List<SongDTO> getSongByTitle(String searchText) {
        List<SongDTO> songs = new ArrayList<>();

        // Create a regex pattern for a case-insensitive search that starts with searchText
        String regexPattern = "^" + searchText;
        Document query = new Document("title", new Document("$regex", regexPattern));

        // Perform the MongoDB query
        try (MongoCursor<Document> cursor = songCollection.find(query).limit(20).iterator()) {
            return mapDocumentToSongDTO(songs, cursor);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get a song by title", e);
        }
    }

    // Method to retrieve a song by its ID in MongoDB
    @Override
    public Song getSongById(ObjectId id) {
        Document query = new Document("_id", id);

        // Execute the query and retrieve the result
        try {
            Document result = songCollection.find(query).first();
            if (result != null) {
                return mapDocumentToSong(result);
            }
            return null;
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get a song by id", e);
        }
    }

    // Method to update song information in MongoDB
    @Override
    public void updateSong(Song song, ClientSession session) {
        // Define the filter for the document you want to update
        Document filter = new Document("_id", song.getId());

        // Define the update with new values for the attributes you want to change
        Document update = new Document("$set", new Document()
                .append("year", song.getReleasedYear())
                .append("duration", song.getDuration())
                .append("genre", song.getGenre())
                .append("title", song.getTitle()));

        try {
            // Perform the update while preserving the image attribute
            songCollection.updateOne(session, filter, update);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to update the song: " + song.getTitle(), e);
        }
    }

    // Method to delete a song by its ID in MongoDB
    @Override
    public void deleteSong(ObjectId id, ClientSession session) {
        Document filter = new Document("_id", id);

        try {
            // Delete the document with the specified ID
            songCollection.deleteOne(session, filter);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to delete the song: " + id.toString(), e);
        }
    }

    // Method to search for songs by artist name in MongoDB
    @Override
    public List<SongDTO> getSongByArtist(String searchText) {
        List<SongDTO> songs = new ArrayList<>();

        // Create a regex pattern for a case-insensitive search that matches the artist's name
        String regexPattern = "^" + searchText;
        Document query = new Document("artists.name", new Document("$regex", regexPattern));

        // Perform the MongoDB query
        try (MongoCursor<Document> cursor = songCollection.find(query).iterator()) {
            return mapDocumentToSongDTO(songs, cursor);
        } catch (MongoException e) {
            // Log MongoDB exception and throw custom DaoException
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get songs by artist", e);
        }
    }
}
