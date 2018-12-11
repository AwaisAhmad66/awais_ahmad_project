package com.example.awais.chatapp;

public class User {
    String name,phone,image,email, uId;

    public User(String name, String phone, String image, String email, String uId) {
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.email = email;
        this.uId = uId;
    }

    public User(){

    }


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
