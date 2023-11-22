package it.unipi.lsmd.spotify2.daos.mongo.listener;

import com.mongodb.client.ClientSession;
import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import it.unipi.lsmd.spotify2.models.Listener;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface MongoListenerDao {

    void createListener(Listener listener, ClientSession mongoSession) throws NoSuchAlgorithmException;

    boolean isUsernameTaken(String username);

    int usernameAndPassword(String username, String password);

    List<ListenerDTO> getListenerByName(String name, String loggedUser);

    void updateListener(Listener listener, String oldUsername, ClientSession session);

    void deleteListener(String username, ClientSession mongoSession);

    Listener getListenerByUsername(String username);
}
