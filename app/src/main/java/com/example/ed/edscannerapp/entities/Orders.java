package com.example.ed.edscannerapp.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Orders {

    private List<Order> orderList;

    public Orders(List<Order> orderList){
        this.orderList = orderList;
    }

    public List<Order> getOrderList(){
        return orderList;
    }

    public Order getOrderById(String id){
        int orderCount = this.orderList.size();
        for(int i = 0; i < orderCount; i++){
            Order order = orderList.get(i);
            if(order.getId().equals(id)){
                return order;
            };
        }
        return null;
    }
}