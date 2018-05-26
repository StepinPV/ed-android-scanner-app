package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class CheckResponse extends BaseResponse {

    @SerializedName("product")
    private Product product;

    public Product getProduct(){
        return this.product;
    }
}
