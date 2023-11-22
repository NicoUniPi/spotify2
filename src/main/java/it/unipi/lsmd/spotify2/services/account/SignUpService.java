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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUpService {
    private final MongoListenerDao mongoListenerDao;
    private final NeoListenerDao neolistenerDao;
    private static final Logger logger = LoggerFactory.getLogger(SignUpService.class);

    public SignUpService(MongoListenerDao mongoListenerDao, NeoListenerDao neolistenerDao) {
        this.mongoListenerDao = mongoListenerDao;
        this.neolistenerDao = neolistenerDao;
    }

    public boolean signup(Listener listener) {
        if (!mongoListenerDao.isUsernameTaken(listener.getUsername())) {
            try (ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
                TransactionOptions txnOptions = TransactionOptions.builder()
                        .readPreference(ReadPreference.primary())
                        .build();
                try (Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {

                    // Start the transactions
                    mongoSession.startTransaction(txnOptions);
                    Transaction neoTransaction = neo4jSession.beginTransaction();
                    try {
                        // Perform operations within the Transactions
                        mongoListenerDao.createListener(listener, mongoSession);
                        neolistenerDao.createListener(listener, neoTransaction);

                        // Commit both transactions
                        mongoSession.commitTransaction();
                        neoTransaction.commit();
                        return true;
                    } catch (DaoException e) {
                        mongoSession.abortTransaction();
                        neoTransaction.rollback();
                        return false;
                    }
                } catch (Exception e) {
                    logger.error("Neo4j session exception:", e);
                }
            } catch (Exception e) {
                logger.error("MongoDB session exception:", e);
            }
        }
        return false; // Username is already taken
    }
}
