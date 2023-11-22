// Package declaration and imports for Neo4j, logging, and utility classes
package it.unipi.lsmd.spotify2.daos.impl.neo4j;

import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.models.Listener;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Class implementing the NeoListenerDao interface
public class NeoListenerDaoImpl implements NeoListenerDao {

    // Neo4j Driver for connecting to the Neo4j database
    private final Driver driver;

    // Logger for logging Neo4j-related exceptions
    private static final Logger logger = LoggerFactory.getLogger(NeoListenerDaoImpl.class);

    // Constructor initializes the Neo4j driver
    public NeoListenerDaoImpl(Driver driver) {
        this.driver = driver;
    }

    // Method to create a new listener node in Neo4j
    @Override
    public void createListener(Listener listener, Transaction transaction) {
        String createListenerQuery = "CREATE (l:Listener {username: $username})";
        Value parameters = Values.parameters("username", listener.getUsername());
        try {
            // Execute the Cypher query to create a new listener node
            transaction.run(createListenerQuery, parameters);
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to create the listener " + listener.getUsername(), e);
        }
    }

    // Method to retrieve all friends of a listener in Neo4j
    @Override
    public List<ListenerDTO> getAllFriends(String loggedUser) {
        try (Session session = driver.session()) {
            String cypherQuery = "MATCH (listener:Listener {username: $username})-[:FOLLOWS]->(friend:Listener) RETURN friend";

            return session.readTransaction(tx -> {
                List<ListenerDTO> friendList = new ArrayList<>();
                Result result = tx.run(cypherQuery, Values.parameters("username", loggedUser));
                while (result.hasNext()) {
                    Record record = result.next();
                    Node friendNode = record.get("friend").asNode();
                    ListenerDTO friend = new ListenerDTO(friendNode.get("username").asString());
                    friendList.add(friend);
                }
                return friendList;
            });
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to retrieve the friends list ", e);
        }
    }

    // Method to create a follows relationship between two listeners in Neo4j
    @Override
    public void followUser(String followerUsername, String followeeUsername) {
        try (Session session = driver.session()) {
            String cypherQuery = "MATCH (follower:Listener {username: $followerUsername}), " +
                    "(followee:Listener {username: $followeeUsername}) " +
                    "CREATE (follower)-[:FOLLOWS]->(followee)";
            session.writeTransaction(tx -> tx.run(cypherQuery,
                    Values.parameters("followerUsername", followerUsername, "followeeUsername", followeeUsername)));
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to follow the listener " + followeeUsername, e);
        }
    }

    // Method to remove the follows relationship between two listeners in Neo4j
    @Override
    public void unfollowUser(String followerUsername, String followeeUsername) {
        try (Session session = driver.session()) {
            String cypherQuery = "MATCH (follower:Listener {username: $followerUsername})-[r:FOLLOWS]->" +
                    "(followee:Listener {username: $followeeUsername}) DELETE r";
            session.writeTransaction(tx -> {
                tx.run(cypherQuery,
                        Values.parameters("followerUsername", followerUsername, "followeeUsername", followeeUsername));
                return null;
            });
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to unfollow the listener " + followeeUsername, e);
        }
    }

    // Method to check if a listener is following another listener in Neo4j
    @Override
    public boolean checkIfFollowing(String loggedUser, String friend) {
        try (Session session = driver.session()) {
            String cypherQuery = "MATCH (listener:Listener {username: $loggedUser})-[:FOLLOWS]->" +
                    "(friend:Listener {username: $friendUsername}) RETURN COUNT(friend) > 0 AS isFollowing";

            return session.readTransaction(tx -> {
                Result result = tx.run(cypherQuery,
                        Values.parameters("loggedUser", loggedUser, "friendUsername", friend));
                if (result.hasNext()) {
                    Record record = result.next();
                    return record.get("isFollowing").asBoolean();
                } else {
                    return false; // User not followed
                }
            });
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to check if I follow " + friend, e);
        }
    }

    // Method to update the username of a listener in Neo4j
    @Override
    public void updateListener(Listener listener, String oldUsername, Transaction tx) {
        String query = "MATCH (l:Listener {username: $oldUsername}) SET l.username = $newUsername";
        Value parameters = Values.parameters("oldUsername", oldUsername,
                "newUsername", listener.getUsername());
        try {
            // Execute the Cypher query to update the listener's username
            tx.run(query, parameters);
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to update the listener: " + oldUsername, e);
        }
    }

    // Method to delete a listener and its associated playlists in Neo4j
    @Override
    public void deleteListener(String username, Transaction tx) {
        String deleteListenerQuery = "MATCH (listener:Listener {username: $username})-[r:CREATED]->(playlist:Playlist)" +
                " DETACH DELETE listener, playlist";
        Value parameters = Values.parameters("username", username);
        try {
            // Execute the Cypher query to delete the listener and its playlists
            tx.run(deleteListenerQuery, parameters);
        } catch (Neo4jException e) {
            // Log Neo4j exception and throw custom DaoException
            logger.error("Neo4jDB exception:", e);
            throw new DaoException("Failed to delete the listener " + username, e);
        }
    }

    // Method to retrieve friends with similar tastes for a given listener in Neo4j
    @Override
    public List<ListenerDTO> getFriendsWithSimilarTastes(String listenerUsername) {
        try (Session session = driver.session()) {
            String query =
                    """
                            MATCH (listener: Listener {username: $username})-[:CREATED]->(playlist:Playlist)-[:CONTAINS]->(song:Song) WITH listener, COLLECT(DISTINCT song.genre) AS listenerGenres MATCH (listener)-[:FOLLOWS]->(:Listener)-[:FOLLOWS]->(fofFriend:Listener)-[:CREATED]->(playlist:Playlist)-[:CONTAINS]->(song:Song) WHERE NOT (listener)-[:FOLLOWS]->(fofFriend) WITH listenerGenres, fofFriend, COLLECT(DISTINCT song.genre) AS fofFriendGenres WITH fofFriend,
                                 SIZE([genre IN listenerGenres WHERE genre IN fofFriendGenres]) AS commonGenres,
                                 SIZE(listenerGenres + fofFriendGenres) AS totalGenres WITH fofFriend,
                                 commonGenres * 1.0 / totalGenres AS similarity WHERE similarity >= 0.5 RETURN fofFriend.username AS newFriendUsername""";

            Value parameters = Values.parameters("username", listenerUsername);

            try (Transaction transaction = session.beginTransaction()) {
                Result result = transaction.run(query, parameters);

                List<ListenerDTO> suggestedFriends = new ArrayList<>();

                while (result.hasNext()) {
                    String newFriendUsername = result.next().get("newFriendUsername").asString();
                    suggestedFriends.add(new ListenerDTO(newFriendUsername));
                }

                return suggestedFriends;
            }
        }
    }
}
