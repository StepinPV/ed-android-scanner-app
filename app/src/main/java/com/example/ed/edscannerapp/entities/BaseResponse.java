package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("error")
    private String error;

    public boolean isSuccessful(){
        return error == null;
    }

    public String getMessage(){
        return isSuccessful() ? success : error;
    }

}
