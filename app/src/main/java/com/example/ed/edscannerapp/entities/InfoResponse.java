package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class InfoResponse extends BaseResponse {

    @SerializedName("user")
    private User user;

    public User getUser(){
        return user;
    }
}