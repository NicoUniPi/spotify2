package it.unipi.lsmd.spotify2.models;

public class Artist {
    private int id;
    private String name;

    public Artist(){

    }

    public Artist(String name){
        this.name = name;
    }
    public Artist(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Artist{" +
                //"id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
