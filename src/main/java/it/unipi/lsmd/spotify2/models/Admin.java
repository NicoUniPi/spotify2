package it.unipi.lsmd.spotify2.models;

import java.util.Date;

public class Admin extends RegisteredUser {
    private Date hiredDate;

    //for future implementations
    public Admin() {
    }

    public Date getHiredDate() {
        return hiredDate;
    }

    public void setHiredDate(Date hiredDate) {
        this.hiredDate = hiredDate;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "hiredDate=" + hiredDate +
                '}';
    }
}
