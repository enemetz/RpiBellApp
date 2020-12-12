package com.example.rpibell;

/**
 * This is the Guest class. This will be used as a data structure to store Guest info
 * for the admin to view.
 */
public class Guest {
    // class variables
    public String name;     // name of the account
    public String email;    // email of the account
    public String ID;       // ID of the account

    // getter and setter methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    // constructor
    Guest(String name, String email, String ID) {
        this.name = name;
        this.email = email;
        this.ID = ID;
    }

    // toString() method
    @Override
    public String toString() {
        return String.format(name + ", " + email + " , " + ID);
    }
} // ends the Guest class