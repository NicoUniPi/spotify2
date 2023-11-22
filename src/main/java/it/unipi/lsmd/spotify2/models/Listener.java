package it.unipi.lsmd.spotify2.models;
public class Listener extends RegisteredUser {
    private String country;
    private String email;
    private String listenerImage;

    public Listener(){

    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getListenerImage() {
        return listenerImage;
    }

    public void setListenerImage(String listenerImage) {
        this.listenerImage = listenerImage;
    }

    @Override
    public String toString() {
        return "Listener{" +
                ", country='" + country + '\'' +
                ", email='" + email +
                '}';
    }
}
