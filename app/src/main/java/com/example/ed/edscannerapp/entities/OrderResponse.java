package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class OrderResponse extends BaseResponse {

    @SerializedName("order")
    private Order order;

    @SerializedName("hold")
    private boolean hold;

    public Order getOrder(){
        return order;
    }

    public boolean getHold(){
        return hold;
    }
}
