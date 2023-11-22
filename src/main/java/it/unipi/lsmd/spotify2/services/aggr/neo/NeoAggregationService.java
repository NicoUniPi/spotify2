package it.unipi.lsmd.spotify2.services.aggr.neo;

import it.unipi.lsmd.spotify2.daos.neo4j.NeoListenerDao;
import it.unipi.lsmd.spotify2.daos.neo4j.NeoSongDao;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.dtos.SongDTO;

import java.util.List;

public class NeoAggregationService {
    private final NeoSongDao neoSongDao;
    private  final NeoListenerDao neoListenerDao;

    public NeoAggregationService(NeoSongDao neoSongDao, NeoListenerDao neoListenerDao) {
        this.neoSongDao = neoSongDao;
        this.neoListenerDao = neoListenerDao;
    }

    public List<SongDTO> suggestedSongs(String userLogged) {
        return neoSongDao.suggestSongsToListener(userLogged);
    }

    public List<ListenerDTO> suggestFriend(String loggedListener) {
        return neoListenerDao.getFriendsWithSimilarTastes(loggedListener);
    }
}
