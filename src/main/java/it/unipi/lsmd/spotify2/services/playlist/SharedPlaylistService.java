package it.unipi.lsmd.spotify2.services.playlist;

import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.PlaylistDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import org.bson.types.ObjectId;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SharedPlaylistService {
    private final MongoPlaylistDao mongoPlaylist;
    private final NeoPlaylistDao neoPlaylist;
    private static final Logger logger = LoggerFactory.getLogger(SharedPlaylistService.class);

    public SharedPlaylistService(MongoPlaylistDao mongoPlaylist, NeoPlaylistDao neoPlaylist) {
        this.mongoPlaylist = mongoPlaylist;
        this.neoPlaylist = neoPlaylist;
    }

    public void createPlaylist(PlaylistDTO newPlaylist, String ownerPlaylist) {
        try (ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try (Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {

                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    List<SongDTO> playlistSongs = mongoPlaylist.getSongsFromPlaylist(ownerPlaylist, newPlaylist.getIdPlaylist(), mongoSession);
                    List<String> idSongs = new ArrayList<>();
                    for (SongDTO song : playlistSongs) {
                        String idSong = song.getId().toString();
                        idSongs.add(idSong);
                    }
                    neoPlaylist.createPlaylist(newPlaylist, ownerPlaylist, idSongs, neoTransaction);
                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (Exception e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (Exception e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void addSongToPlaylist(SongDTO song, ObjectId idPlaylist, String loggedUser) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    mongoPlaylist.addSongToPlaylist(loggedUser, song, idPlaylist);
                    neoPlaylist.addSongToPlaylist(song.getId().toString(), idPlaylist.toString(), loggedUser, neoTransaction);
                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void removeSongFromPlaylist(ObjectId songId, ObjectId idPlaylist, String loggedUser, String friend) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    mongoPlaylist.removeSongFromPlaylist(loggedUser, idPlaylist, songId);
                    if(friend != null)
                        neoPlaylist.removeSongFromFriendPlaylist(songId.toString(), idPlaylist.toString(), friend, neoTransaction);
                    else
                        neoPlaylist.removeSongFromPlaylist(songId.toString(), idPlaylist.toString(), neoTransaction);
                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void addSongToFriendPlaylist(SongDTO song, ObjectId idPlaylist, String friendUsername, String loggedUser) {
        try(ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .build();
            try(Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {
                // Start the transactions
                mongoSession.startTransaction(txnOptions);
                Transaction neoTransaction = neo4jSession.beginTransaction();
                try {
                    mongoPlaylist.addSongToPlaylist(friendUsername, song, idPlaylist);
                    neoPlaylist.addSongToFriendPlaylist(song.getId().toString(), song.getTitle(),
                            idPlaylist.toString(), loggedUser, neoTransaction);
                    // Commit both transactions
                    mongoSession.commitTransaction();
                    neoTransaction.commit();
                } catch (DaoException e) {
                    mongoSession.abortTransaction();
                    neoTransaction.rollback();
                }
            } catch (SessionExpiredException e) {
                logger.error("Neo4j session exception:", e);
            }
        } catch (DaoException e) {
            logger.error("MongoDB session exception:", e);
        }
    }

    public void deletePlaylist(ObjectId idPlaylist, String owner) {
        if (!neoPlaylist.checkPlaylistPresence(idPlaylist.toString())) {
            mongoPlaylist.removePlaylist(owner, idPlaylist);
        } else {
            try (ClientSession mongoSession = MongoDBConfig.getMongoClient().startSession()) {
                TransactionOptions txnOptions = TransactionOptions.builder()
                        .readPreference(ReadPreference.primary())
                        .build();
                try (Session neo4jSession = Neo4jDBConfig.getInstance().getDriver().session()) {

                    // Start the transactions
                    mongoSession.startTransaction(txnOptions);
                    Transaction neoTransaction = neo4jSession.beginTransaction();
                    try {
                        mongoPlaylist.removePlaylist(owner, idPlaylist);
                        neoPlaylist.deletePlaylist(idPlaylist.toString(), neoTransaction);
                        // Commit both transactions
                        mongoSession.commitTransaction();
                        neoTransaction.commit();
                    } catch (DaoException e) {
                        mongoSession.abortTransaction();
                        neoTransaction.rollback();
                    }
                } catch (Exception e) {
                    logger.error("Neo4j session exception:", e);
                }
            } catch (Exception e) {
                logger.error("MongoDB session exception:", e);
            }
        }
    }
}