package it.unipi.lsmd.spotify2.services.social;

import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;

import java.util.List;

public class FollowingSocialService {
    private final NeoListenerDao neoListenerDao;

    public FollowingSocialService(NeoListenerDao neoListenerDao) {
        this.neoListenerDao = neoListenerDao;
    }

    public void followUser(String followerUsername, String followeeUsername) {
        neoListenerDao.followUser(followerUsername, followeeUsername);
    }

    public List<ListenerDTO> getListFriends(String loggedUser) {
        return neoListenerDao.getAllFriends(loggedUser);
    }

    public boolean checkIfFollowing(String loggedUser, String friend) {
        return neoListenerDao.checkIfFollowing(loggedUser, friend);
    }

    public void unfollowUser(String followerUsername, String followeeUsername) {
        neoListenerDao.unfollowUser(followerUsername, followeeUsername);
    }
}
