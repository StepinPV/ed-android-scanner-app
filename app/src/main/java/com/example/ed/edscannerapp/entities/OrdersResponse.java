package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrdersResponse extends BaseResponse {

    @SerializedName("packing_status")
    private List<Order> orderList;

    public Orders getOrders(){
        return new Orders(orderList);
    }
}
