package it.unipi.lsmd.spotify2.services.account;

import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.models.Listener;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Class responsible for managing account-related operations such as updating and deleting listener accounts.
public class ManageAccountService {

    // Instance variables for DAOs and logger.
    private final MongoListenerDao mongoListenerDao;
    private final NeoListenerDao neoListenerDao;
    private static final Logger logger = LoggerFactory.getLogger(ManageAccountService.class);

    // Constructor to initialize ManageAccountService with necessary DAOs.
    public ManageAccountService(MongoListenerDao mongoListenerDao, NeoListenerDao neoListenerDao) {
        this.mongoListenerDao = mongoListenerDao;
        this.neoListenerDao = neoListenerDao;
    }

    // Method to update a listener's account information.
    public void updateAccount(Listener newListener, String oldUsername) {
        try (ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            // Configuring transaction options for MongoDB.
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();

            try (Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start MongoDB and Neo4j transactions.
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();

                try {
                    // Update listener information in both MongoDB and Neo4j.
                    mongoListenerDao.updateListener(newListener, oldUsername, mongoSession);
                    neoListenerDao.updateListener(newListener, oldUsername, neoTransaction);

                    // Commit both transactions.
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    // Handle exceptions by aborting MongoDB transaction and rolling back Neo4j transaction.
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (SessionExpiredException e) {
                // Handle Neo4j session expiration exception.
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            // Handle MongoDB session exception.
            logger.error("MongoDB session exception:", e);
        }
    }

    // Method to delete a listener's account.
    public void deleteAccount(String listenerUsername) {
        try (ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            // Configuring transaction options for MongoDB.
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();

            try (Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start MongoDB and Neo4j transactions.
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();

                try {
                    // Delete listener from both MongoDB and Neo4j.
                    mongoListenerDao.deleteListener(listenerUsername, mongoSession);
                    neoListenerDao.deleteListener(listenerUsername, neoTransaction);

                    // Commit both transactions.
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    // Handle exceptions by aborting MongoDB transaction and rolling back Neo4j transaction.
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (SessionExpiredException e) {
                // Handle Neo4j session expiration exception.
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            // Handle MongoDB session exception.
            logger.error("MongoDB session exception:", e);
        }
    }
}
