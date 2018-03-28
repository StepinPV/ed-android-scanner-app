package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class OrderResponse extends BaseResponse {

    @SerializedName("order")
    private Order order;

    public Order getOrder(){
        return order;
    }
}
