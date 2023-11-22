package it.unipi.lsmd.spotify2.utils;

import java.time.LocalDate;

public class PlaylistStats {
    private String username;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalPlaylists;
    private int totalSongs;
    private  int totalDuration;
    private double averageSongLength;

    public  PlaylistStats(){}

    public PlaylistStats(String username, int totalSongs, int totalDuration, double averageSongLength) {
        this.username = username;
        this.totalSongs = totalSongs;
        this.totalDuration = totalDuration;
        this.averageSongLength = averageSongLength;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTotalPlaylists(int totalPlaylists) {
        this.totalPlaylists = totalPlaylists;
    }

    public int getTotalPlaylists(){
        return totalPlaylists;
    }

    public void setTotalSongs(int totalSongs) {
        this.totalSongs = totalSongs;
    }

    public int getTotalSongs(){
        return totalSongs;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getTotalDuration(){
        return totalDuration;
    }

    public void setAverageSongLength(double averageSongLength) {
        this.averageSongLength = averageSongLength;
    }

    public double getAverageNumber() {
        return averageSongLength;
    }
}
