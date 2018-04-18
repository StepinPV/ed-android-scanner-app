package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Order {

    final public static String ID_FIELD = "id";

    @SerializedName("order_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("shipping_zone")
    private String shippingZone;

    @SerializedName("order_count")
    private String orderCount;

    @SerializedName("vip")
    private String vip;

    @SerializedName("packing_comment")
    private String comment;

    final public static String STATUS_UNSTARTED = "1";
    final public static String STATUS_PAUSED = "4";
    final public static String STATUS_ACTIVE = "5";
    final public static String STATUS_PARTIAL = "6";

    @SerializedName("packing_order_status_id")
    private String status;

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getShippingZone(){
        return this.shippingZone;
    }

    public String getOrderCount(){
        return this.orderCount;
    }

    public boolean isVip(){
        return this.vip != null && this.vip.equals("1");
    }

    public String getStatus(){
        return this.status;
    }

    public String getComment(){
        return this.comment;
    }

}