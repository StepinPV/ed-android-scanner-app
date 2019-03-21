package com.example.ed.edscannerapp.server;

import com.example.ed.edscannerapp.Settings;
import com.example.ed.edscannerapp.entities.BaseResponse;
import com.example.ed.edscannerapp.entities.CheckResponse;
import com.example.ed.edscannerapp.entities.InfoResponse;
import com.example.ed.edscannerapp.entities.OrdersResponse;
import com.example.ed.edscannerapp.entities.ProductsResponse;
import com.example.ed.edscannerapp.entities.OrderResponse;
import com.example.ed.edscannerapp.entities.VerificationResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BL {

    static private ServerAPI EP = getServerAPIInst(Settings.getServer());

    static public void updateServerAPIInst() {
        EP = getServerAPIInst(Settings.getServer());
    }

    static private ServerAPI getServerAPIInst(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ServerAPI.class);
    }

    static public Call<OrdersResponse> getOrders(String login, String salt, String sig){
        return EP.getOrders(login, salt, sig);
    }

    static public Call<ProductsResponse> getProducts(String login, String salt, String sig, String orderId){
        return EP.getProducts(login, salt, sig, orderId);
    }

    static public Call<VerificationResponse> verification(String login, String salt, String sig){
        return EP.verification(login, salt, sig);
    }

    static public Call<InfoResponse> info(String login, String salt, String sig){
        return EP.info(login, salt, sig);
    }

    static public Call<CheckResponse> checkProduct(String login, String salt, String sig, String barcode){
        return EP.checkProduct(login, salt, sig, barcode);
    }

    static public Call<OrderResponse> startOrder(String login, String salt, String sig, String orderId){
        return EP.getOrder(login, salt, sig, orderId, "1");
    }

    static public Call<OrderResponse> getOrder(String login, String salt, String sig, String orderId){
        return EP.getOrder(login, salt, sig, orderId, "0");
    }

    static public Call<OrderResponse> pauseOrder(String login, String salt, String sig, String orderId){
        return EP.pauseOrder(login, salt, sig, orderId);
    }

    static public Call<OrderResponse> cancelOrder(String login, String salt, String sig, String orderId){
        return EP.cancelOrder(login, salt, sig, orderId);
    }

    static public Call<OrderResponse> completeOrder(String login, String salt, String sig, String orderId){
        return EP.completeOrder(login, salt, sig, orderId);
    }

    static public Call<BaseResponse> confirmProduct(String login, String salt, String sig, String productId, boolean manual, String weight){
        return manual ? EP.confirmProductManual(login, salt, sig, productId, weight) : EP.confirmProductAuto(login, salt, sig, productId, weight);
    }

    static public Call<BaseResponse> cancelProduct(String login, String salt, String sig, String productId, String quantity){
        return EP.cancelProduct(login, salt, sig, productId, quantity);
    }

}
