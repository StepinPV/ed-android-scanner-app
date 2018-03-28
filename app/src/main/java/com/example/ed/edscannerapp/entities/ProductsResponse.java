package com.example.ed.edscannerapp.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductsResponse extends BaseResponse {

    @SerializedName("order_products")
    private List<Product> productList;

    public Products getProducts(){
        return new Products(productList);
    }
}
