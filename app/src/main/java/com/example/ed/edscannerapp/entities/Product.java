package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Product {

    @SerializedName("order_product_id")
    private String id;

    @SerializedName("barcode")
    private String barcode;

    @SerializedName("name")
    private String name;

    @SerializedName("item_section")
    private String section;

    @SerializedName("weight")
    private String weight;

    @SerializedName("unit")
    private String unit;

    @SerializedName("manufacturer")
    private String manufacturer;

    private int rejectCount = 0;

    final public static String STATUS_UNSCANNED = "1";
    final public static String STATUS_SCANNED = "2";
    final public static String STATUS_MANUAL_SCANNED = "3";

    @SerializedName("packing_product_status_id")
    private String status;

    public String getId(){
        return id;
    }

    public String getBarcode(){
        return barcode;
    }

    public String getName(){
        return name;
    }

    public String getSection(){
        return section;
    }

    public String getWeight(){
        return weight;
    }

    public String getUnit(){
        return unit;
    }

    public String getManufacturer(){
        return manufacturer;
    }

    public String getStatus(){
        return status;
    }

    public boolean isScanned(){
        return status.equals(STATUS_SCANNED) || status.equals(STATUS_MANUAL_SCANNED);
    }

    public void confirm(boolean manualMode){
        status = manualMode ? STATUS_MANUAL_SCANNED : STATUS_SCANNED;
    }

    public void cancel(){
        status = STATUS_UNSCANNED;
    }

    public void increaseRejectCounter(){
        rejectCount++;
    }

    public int getRejectCount(){
        return rejectCount;
    }
}