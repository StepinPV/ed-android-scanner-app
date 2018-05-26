package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("firstname")
    private String firstName;

    @SerializedName("lastname")
    private String lastName;

    @SerializedName("name")
    private String name;

    @SerializedName("num_packed_orders")
    private String numPackedOrders;

    @SerializedName("num_packed_orders_shift")
    private String numPackedOrdersShift;

    public String getFullName() {
        return this.firstName + ' ' + this.lastName;
    }
}