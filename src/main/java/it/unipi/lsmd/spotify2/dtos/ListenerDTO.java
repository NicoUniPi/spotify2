package it.unipi.lsmd.spotify2.dtos;

public class ListenerDTO {
    String username;

    public ListenerDTO(String username) {
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
