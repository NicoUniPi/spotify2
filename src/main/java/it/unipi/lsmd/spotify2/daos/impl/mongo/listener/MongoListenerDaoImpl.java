package it.unipi.lsmd.spotify2.daos.impl.mongo.listener;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.models.Listener;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.unipi.lsmd.spotify2.utils.Mappers.mapDocumentToListener;
import static it.unipi.lsmd.spotify2.utils.PasswordSecurity.generateSalt;
import static it.unipi.lsmd.spotify2.utils.PasswordSecurity.hashWithSHA256;

// Class declaration for MongoListenerDaoImpl implementing MongoListenerDao interface
public class MongoListenerDaoImpl implements MongoListenerDao {

    // Logger for logging MongoDB exceptions
    private static final Logger logger = LoggerFactory.getLogger(MongoListenerDaoImpl.class);

    // MongoDB's collection for listeners
    private final MongoCollection<Document> listenerCollection;

    // Constructor to initialize MongoDB resources
    public MongoListenerDaoImpl() {
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

    // Method to create a new listener
    @Override
    public void createListener(Listener listener, ClientSession mongoSession) throws NoSuchAlgorithmException {
        // Generate a random salt
        String salt = generateSalt();

        // Hash the password with SHA-256 and the salt
        String hashedPassword = hashWithSHA256(listener.getPassword() + salt);

        // Start a transaction
        Document listenerDoc = new Document("username", listener.getUsername())
                .append("password", listener.getPassword())
                .append("hashedPassword", hashedPassword)
                .append("salt", salt)
                .append("email", listener.getEmail())
                .append("country", listener.getCountry())
                .append("picture", listener.getListenerImage())
                .append("isAdmin", false);

        try {
            listenerCollection.insertOne(mongoSession, listenerDoc);
        } catch (MongoException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to create a listener", e);
        }
    }

    // Method to check if a username is already taken
    @Override
    public boolean isUsernameTaken(String username) {
        Document query = new Document("username", username);
        try {
            return listenerCollection.countDocuments(query) > 0;
        } catch (MongoException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to check if a username is taken", e);
        }
    }

    // Method to authenticate a listener based on username and password
    @Override
    public int usernameAndPassword(String username, String password) {
        Document query = new Document("username", username);
        try {
            // Execute the query and check if a matching user exists
            Document user = listenerCollection.find(query).first();

            if (user != null) {
                String storedSalt = user.getString("salt");

                // Hash the input password with the retrieved salt
                String hashedPassword = hashWithSHA256(password + storedSalt);

                if(!Objects.equals(hashedPassword, user.getString("hashedPassword")))
                    return 0;
                // Check if the user is an administrator
                if(user.getBoolean("isAdmin"))
                    return -1;
                return 1;
            }
            return 0;
        } catch (MongoException | NoSuchAlgorithmException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to check if username and password are correct", e);
        }
    }

    // Method to retrieve listeners by name (case-insensitive)
    @Override
    public List<ListenerDTO> getListenerByName(String name, String loggedUser) {
        List<ListenerDTO> listeners = new ArrayList<>();
        // Create a regex pattern for a case-insensitive search that starts with searchText
        String regexPattern = "^" + name;

        // Create the aggregation pipeline
        List<Bson> pipeline = new ArrayList<>();

        // Stage 1: Match documents with the desired username pattern
        pipeline.add(Aggregates.match(Filters.regex("username", regexPattern)));

        // Stage 2: Exclude the specific username
        pipeline.add(Aggregates.match(Filters.not(Filters.eq("username", loggedUser))));

        // Stage 3: Exclude listeners with "isAdmin" set to true
        pipeline.add(Aggregates.match(Filters.or(
                Filters.exists("isAdmin", false),
                Filters.eq("isAdmin", false)
        )));

        // Perform the MongoDB aggregation query
        try (MongoCursor<Document> cursor = listenerCollection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document listenerDocument = cursor.next();
                listeners.add(new ListenerDTO(listenerDocument.getString("username")));
            }
            return listeners;
        } catch (MongoException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get a listener by name", e);
        }
    }

    // Method to update listener information
    @Override
    public void updateListener(Listener listener, String oldUsername, ClientSession session) {
        // Define the filter for the document you want to update
        Document filter = new Document("username", oldUsername);

        // Define the update with new values for the attributes you want to change
        Document update = new Document("$set", new Document()
                .append("username", listener.getUsername())
                .append("email", listener.getEmail())
                .append("country", listener.getCountry()));
        if(session != null) {
            try {
                // Perform the update while preserving the image attribute
                listenerCollection.updateOne(session, filter, update);
            } catch (MongoException e) {
                logger.error("MongoDB exception:", e);
                throw new DaoException("Failed to update the listener: " + oldUsername, e);
            }
        } else {
            try {
                // Perform the update while preserving the image attribute
                listenerCollection.updateOne(filter, update);
            } catch (MongoException e) {
                logger.error("MongoDB exception:", e);
                throw new DaoException("Failed to update the listener: " + oldUsername, e);
            }
        }
    }

    // Method to delete a listener account
    @Override
    public void deleteListener(String username, ClientSession mongoSession) {
        Bson filter = Filters.eq("username", username);
        try {
            listenerCollection.deleteOne(mongoSession, filter);
        } catch (MongoException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to delete the account of " + username, e);
        }
    }

    // Method to get a listener by username
    @Override
    public Listener getListenerByUsername(String username) {
        Document query = new Document("username", username);
        try {
            Document result = listenerCollection.find(query).first();
            if(result != null) {
                return mapDocumentToListener(result);
            }
        } catch (MongoException e) {
            logger.error("MongoDB exception:", e);
            throw new DaoException("Failed to get the listener " + username, e);
        }
        return null;
    }
}
