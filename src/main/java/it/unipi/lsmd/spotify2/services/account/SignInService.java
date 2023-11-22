package it.unipi.lsmd.spotify2.services.account;

import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.models.Listener;

public class SignInService {
    private final MongoListenerDao mongoListenerDao;
    public SignInService(MongoListenerDao mongoListenerDao) {
        this.mongoListenerDao = mongoListenerDao;
    }

    public int login(Listener listener) {
        return mongoListenerDao.usernameAndPassword(listener.getUsername(),
                listener.getPassword());// Username is already taken
    }

}
