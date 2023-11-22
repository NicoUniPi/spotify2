package it.unipi.lsmd.spotify2.daos.neo4j;

import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.models.Listener;
import org.neo4j.driver.Transaction;

import java.util.List;

public interface NeoListenerDao {
    void createListener(Listener listener, Transaction transaction);
    List<ListenerDTO> getAllFriends(String loggedUser);
    void updateListener(Listener listener, String oldUsername, Transaction transaction);
    void deleteListener(String username, Transaction transaction);
    void followUser(String followerUsername, String followeeUsername);
    void unfollowUser(String followerUsername, String followeeUsername);
    boolean checkIfFollowing(String loggedUser, String friend);
    //List<SongDTO> songRecommendation(String username);
    List<ListenerDTO> getFriendsWithSimilarTastes(String listenerUsername);

}
