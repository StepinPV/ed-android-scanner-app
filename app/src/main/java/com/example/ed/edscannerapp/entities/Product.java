package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Product {

    @SerializedName("order_product_id")
    private String id;

    @SerializedName("product_id")
    private String _id;

    @SerializedName("barcode")
    private String barcode;

    @SerializedName("sku")
    private String sku;

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

    @SerializedName("image")
    private String image;

    @SerializedName("quantity_needed")
    private int neededQuantity;

    @SerializedName("packing_quantity")
    private int packingQuantity;

    private int rejectCount = 0;

    final public static String STATUS_UNSCANNED = "1";
    final public static String STATUS_SCANNED = "2";
    final public static String STATUS_MANUAL_SCANNED = "3";

    @SerializedName("packing_product_status_id")
    private String status;

    public String getId(){
        return id;
    }

    //TODO
    public String getProductId(){
        return _id;
    }

    public boolean checkBarcode(String code){
        return barcode != null && !barcode.equals("") && barcode.equals(code) || sku != null && !sku.equals("") && sku.equals(code);
    }

    public boolean hasBarcode(){
        return barcode != null && !barcode.equals("") || sku != null && !sku.equals("");
    }

    public String getName(){
        return name.replaceAll("&quot;", "\"");
    }

    public String getSection(){
        return section;
    }

    public String getWeight(){
        return fmt(weight);
    }

    //Убираем лишние нули
    private static String fmt(String val) {
        float d = Float.parseFloat(val);
        return d == (long) d ? String.format("%d", (long) d) : String.format("%s", d);
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

    public String getImage(){
        return image;
    }

    public boolean isScanned(){
        return status.equals(STATUS_SCANNED) || status.equals(STATUS_MANUAL_SCANNED);
    }

    public void confirm(boolean manualMode){
        //Если последний, меняем статус
        packingQuantity++;

        if(neededQuantity == packingQuantity){
            status = manualMode ? STATUS_MANUAL_SCANNED : STATUS_SCANNED;
        }
    }

    public void cancel(int quantity){
        packingQuantity -= quantity;
        status = STATUS_UNSCANNED;
    }

    public void increaseRejectCounter(){
        rejectCount++;
    }

    public int getRejectCount(){
        return rejectCount;
    }

    public int getNeededQuantity(){
        return neededQuantity;
    }

    public int getPackingQuantity(){
        return packingQuantity;
    }
}