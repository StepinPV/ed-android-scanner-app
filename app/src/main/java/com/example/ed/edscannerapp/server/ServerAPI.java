package com.example.ed.edscannerapp.server;

import com.example.ed.edscannerapp.entities.BaseResponse;
import com.example.ed.edscannerapp.entities.CheckResponse;
import com.example.ed.edscannerapp.entities.OrdersResponse;
import com.example.ed.edscannerapp.entities.ProductsResponse;
import com.example.ed.edscannerapp.entities.OrderResponse;
import com.example.ed.edscannerapp.entities.VerificationResponse;
import com.example.ed.edscannerapp.entities.InfoResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ServerAPI {

    final String path = "index.php?route=api/terminal/";

    @FormUrlEncoded
    @POST(path + "packing_orders")
    Call<OrdersResponse> getOrders(@Field("login") String login, @Field("salt") String salt,
                                   @Field("sig") String sig);

    @FormUrlEncoded
    @POST(path + "order_products")
    Call<ProductsResponse> getProducts(@Field("login") String login, @Field("salt") String salt,
                                       @Field("sig") String sig, @Field("order_id") String orderId);

    @FormUrlEncoded
    @POST(path + "verification")
    Call<VerificationResponse> verification(@Field("login") String login,
                                            @Field("salt") String salt, @Field("sig") String sig);

    @FormUrlEncoded
    @POST(path + "info")
    Call<InfoResponse> info(@Field("login") String login,
                                            @Field("salt") String salt, @Field("sig") String sig);

    @FormUrlEncoded
    @POST(path + "product_search")
    Call<CheckResponse> checkProduct(@Field("login") String login,
                             @Field("salt") String salt, @Field("sig") String sig, @Field("code") String barcode);

    @FormUrlEncoded
    @POST(path + "order_get")
    Call<OrderResponse> getOrder(@Field("login") String login, @Field("salt") String salt,
                                 @Field("sig") String sig, @Field("order_id") String orderId, @Field("start") String start);

    @FormUrlEncoded
    @POST(path + "order_postpone")
    Call<OrderResponse> pauseOrder(@Field("login") String login, @Field("salt") String salt,
                                   @Field("sig") String sig, @Field("order_id") String orderId);

    @FormUrlEncoded
    @POST(path + "order_disband")
    Call<OrderResponse> cancelOrder(@Field("login") String login, @Field("salt") String salt,
                                    @Field("sig") String sig, @Field("order_id") String orderId);

    @FormUrlEncoded
    @POST(path + "order_complete")
    Call<OrderResponse> completeOrder(@Field("login") String login, @Field("salt") String salt,
                                     @Field("sig") String sig, @Field("order_id") String orderId);

    @FormUrlEncoded
    @POST(path + "product_confirm_auto")
    Call<BaseResponse> confirmProductAuto(@Field("login") String login, @Field("salt") String salt,
                                    @Field("sig") String sig, @Field("order_product_id") String productId);

    @FormUrlEncoded
    @POST(path + "product_confirm_manual")
    Call<BaseResponse> confirmProductManual(@Field("login") String login, @Field("salt") String salt,
                                           @Field("sig") String sig, @Field("order_product_id") String productId);

    @FormUrlEncoded
    @POST(path + "product_remove")
    Call<BaseResponse> cancelProduct(@Field("login") String login, @Field("salt") String salt,
                                     @Field("sig") String sig, @Field("order_product_id") String productId,
                                     @Field("quantity") String quantity);
}
