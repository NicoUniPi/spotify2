package it.unipi.lsmd.spotify2.services.song;

import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.models.Song;
import org.bson.types.ObjectId;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageSongService {
    private final NeoSongDao neoSongDao;
    private final MongoSongDao mongoSongDao;
    private static final Logger logger = LoggerFactory.getLogger(ManageSongService.class);


    public ManageSongService(NeoSongDao neoSongDao, MongoSongDao mongoSongDao) {
        this.neoSongDao = neoSongDao;
        this.mongoSongDao = mongoSongDao;
    }

    public void createNewSong(Song song) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    ObjectId songId = new ObjectId();
                    song.setId(songId);

                    mongoSongDao.createSong(song, mongoSession);
                    neoSongDao.createSong(song, neoTransaction);

                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                    logger.error("Exception occurred:", e);
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void updateSong(Song song) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    mongoSongDao.updateSong(song, mongoSession);
                    neoSongDao.updateSong(song, neoTransaction);

                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                    logger.error("Exception occurred:", e);
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void deleteSong(ObjectId songId) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    mongoSongDao.deleteSong(songId, mongoSession);
                    neoSongDao.deleteSong(songId.toString(), neoTransaction);

                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                    logger.error("Exception occurred:", e);
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }
}
