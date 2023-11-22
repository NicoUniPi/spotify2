package it.unipi.lsmd.spotify2.utils;

public class UserSession {

    private static UserSession instance;
    private String username;

    private UserSession() {
        // Private constructor to enforce singleton pattern
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getLoggedInUser() {
        return username;
    }

    public void setLoggedInUser(String username) {
        this.username = username;
    }
}

