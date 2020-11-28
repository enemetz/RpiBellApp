package com.example.rpibell;

public class Guest {
    public String name;
    public String email;
    //public String password;
    //public int imageID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String setEmail) {
        this.email = email;
    }

    /**public int getImageID() {
        return imageID;
    }**/

    Guest(String name, String email) {
        this.name = name;
        this.email = email;
        //this.imageID = imageID;
    }

    @Override
    public String toString() {
        return String.format(name + ", " + email);
    }

}
