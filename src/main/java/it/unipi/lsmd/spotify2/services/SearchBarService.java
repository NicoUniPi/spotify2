package it.unipi.lsmd.spotify2.services;

import it.unipi.lsmd.spotify2.daos.mongo.MongoSongDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoListenerDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;

import java.util.List;

public class SearchBarService {
    private final MongoSongDao mongoSongDao;
    private final MongoListenerDao mongoListenerDao;

    public SearchBarService(MongoSongDao mongoSongDao, MongoListenerDao mongoListenerDao) {
        this.mongoSongDao = mongoSongDao;
        this.mongoListenerDao = mongoListenerDao;
    }

    public List<SongDTO> searchSongByTitle(String title) {
        return mongoSongDao.getSongByTitle(title);
    }

    public List<SongDTO> searchSongByArtist(String artist) {
        return mongoSongDao.getSongByArtist(artist);
    }
    
    public List<ListenerDTO> searchListener(String name, String loggedUser) {
        return mongoListenerDao.getListenerByName(name, loggedUser);
    }
}
