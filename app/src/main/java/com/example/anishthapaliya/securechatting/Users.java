package com.example.anishthapaliya.securechatting;


import javax.crypto.Cipher;

public class Users {
    public String name;
    public String thumb_image;
    public String status;

    public Users(){

    }
    public String getName() {
        return name;
    }

    public String getImage() {
        return thumb_image;
    }

    public void setImage(String thumb_image) {

        this.thumb_image = thumb_image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {

        this.name = name;

    }



}
