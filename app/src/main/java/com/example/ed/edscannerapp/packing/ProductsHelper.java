package com.example.ed.edscannerapp.packing;

import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.Products;

public class ProductsHelper {

    static public Product getUnscannedByIndex(Products products, int index){
        int counter = 0;

        for (Product product: products.getList()){
            if(!product.isScanned()){

                if(counter == index){
                    return product;
                }

                counter++;
            }
        }

        return null;
    }

    static public int getUnscannedIndex(Products products, String productId){
        int index = 0;

        for (Product product: products.getList()){
            if(!product.isScanned()){

                if(product.getId().equals(productId)){
                    return index;
                }

                index++;
            }
        }

        return -1;
    }

    static public int getCount(Products products){
        return products.getList().size();
    }

    static public int getUnscannedCount(Products products){
        int count = 0;

        for (Product product: products.getList()){
            if(!product.isScanned()){
                count++;
            }
        }

        return count;
    }

}
