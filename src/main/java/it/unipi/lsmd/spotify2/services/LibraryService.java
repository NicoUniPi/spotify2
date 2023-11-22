package it.unipi.lsmd.spotify2.services;

import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class LibraryService {
    private final LibraryDao libraryDao;
    public LibraryService(LibraryDao libraryDao) {
        this.libraryDao = libraryDao;
    }
    public List<SongDTO> getLibrary(String username) {
        return libraryDao.getLibrary(username);
    }
    public void addToLibrary(String username, SongDTO song){
        libraryDao.addSongToLibrary(username, song);
    }
    public void removeFromLibrary(String username, ObjectId songId) {
        libraryDao.removeSongFromLibrary(username, songId);
    }
}
