package com.example.ed.edscannerapp.entities;

import java.util.List;

public class Products {

    protected List<Product> productList;

    public Products(List<Product> productList){
        this.productList = productList;
    }

    public List<Product> getList(){
        return productList;
    }

    public Product getProductById(String id){
        for (Product product: productList) {
            if (product.getId().equals(id)) {
                return product;
            }
        }

        return null;
    }

}