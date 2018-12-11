package com.example.awais.chatapp;

public class Contacts {
    public String phone , image ;

    public Contacts(){

    }


    public Contacts(String phone, String image) {
        this.phone = phone;
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
