package it.unipi.lsmd.spotify2.services.aggr.mongo;

import it.unipi.lsmd.spotify2.daos.mongo.listener.LibraryDao;
import it.unipi.lsmd.spotify2.daos.mongo.listener.MongoPlaylistDao;
import it.unipi.lsmd.spotify2.dtos.SongDTO;
import it.unipi.lsmd.spotify2.utils.GenreDistribution;
import it.unipi.lsmd.spotify2.utils.PlaylistStats;

import java.time.LocalDate;
import java.util.List;

public class MongoAggregationService {
    private final LibraryDao libraryDao;
    private final MongoPlaylistDao mongoPlaylistDao;

    public MongoAggregationService(LibraryDao libraryDao, MongoPlaylistDao mongoPlaylistDao) {
        this.libraryDao = libraryDao;
        this.mongoPlaylistDao = mongoPlaylistDao;
    }
    public List<String> getDistinctCountries() {
        return libraryDao.getDistinctCountries();
    }
    public List<SongDTO> topLikedSongPerCountry(String country) {
        return libraryDao.topLikedSongPerCountry(country);
    }
    public List<GenreDistribution> getGenreDistribution() {
        return mongoPlaylistDao.genreDistribution();
    }

    public PlaylistStats getPlaylistsStatsOfUser(String username, LocalDate startDate, LocalDate endDate) {
        return mongoPlaylistDao.playlistStatistics(username, startDate, endDate);
    }
}
