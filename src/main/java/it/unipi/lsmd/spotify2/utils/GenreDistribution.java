package it.unipi.lsmd.spotify2.utils;

public class GenreDistribution {
    private String genre;
    private int distribution;

    public GenreDistribution(String genre, int distribution) {
        this.genre = genre;
        this.distribution = distribution;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    public void setDistribution(int distribution) {
        this.distribution = distribution;
    }

    public int getDistribution() {
        return distribution;
    }
}
