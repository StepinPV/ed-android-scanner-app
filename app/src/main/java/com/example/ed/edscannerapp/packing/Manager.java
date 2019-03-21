package com.example.ed.edscannerapp.packing;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.entities.BaseResponse;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Products;
import com.example.ed.edscannerapp.entities.ProductsResponse;
import com.example.ed.edscannerapp.entities.OrderResponse;
import com.example.ed.edscannerapp.server.BL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Менеджер содержит методы для работы со сборкой заказа
 */
public class Manager {

    private static Manager instance;

    private String activeOrderId;
    private Products products;

    private Manager() {}

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public static void destroyInstance(){
        instance = null;
    }

    /**
     * Получить id активного заказа
     * @return
     */
    public String getActiveOrderId(){
        return activeOrderId;
    }

    public interface GetOrderCallback {
        void success(Order order, String message);
        void error(String message);
    }

    /**
     * Получить заказ по id
     * @return
     */
    public void getOrder(String orderId, final GetOrderCallback callback){
        AccountManager am = AccountManager.getInstance();
        this.callOrderMethod(BL.getOrder(am.getLogin(), am.getSalt(), am.getSig(), orderId), callback);
    }

    /**
     * Начать сборку заказа с переданным id
     * @return
     */
    public void startOrder(String orderId, final GetOrderCallback callback){
        AccountManager am = AccountManager.getInstance();
        this.callOrderMethod(BL.startOrder(am.getLogin(), am.getSalt(), am.getSig(), orderId), callback);
    }

    /**
     * Приостановить сборку заказа с переданным id
     * @return
     */
    public void pauseOrder(final GetOrderCallback callback){
        AccountManager am = AccountManager.getInstance();
        this.callOrderMethod(BL.pauseOrder(am.getLogin(), am.getSalt(), am.getSig(), activeOrderId), callback);
    }

    /**
     * Отменить сборку заказа с переданным id
     * @return
     */
    public void cancelOrder(final GetOrderCallback callback){
        AccountManager am = AccountManager.getInstance();
        this.callOrderMethod(BL.cancelOrder(am.getLogin(), am.getSalt(), am.getSig(), activeOrderId), callback);
    }

    /**
     * Завершить сборку заказа с переданным id
     * @return
     */
    public void completeOrder(final GetOrderCallback callback){
        AccountManager am = AccountManager.getInstance();
        this.callOrderMethod(BL.completeOrder(am.getLogin(), am.getSalt(), am.getSig(), activeOrderId), callback);
    }

    private void callOrderMethod(Call call, final GetOrderCallback callback){
        call.enqueue(new Callback<OrderResponse>() {

            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    OrderResponse orderResponse = response.body();

                    if(orderResponse.isSuccessful()){
                        Order order = response.body().getOrder();

                        //Устанавливаем заказ активным
                        activeOrderId = (order != null && order.getStatus().equals(Order.STATUS_ACTIVE)) ? order.getId() : null;

                        callback.success(order, orderResponse.getMessage());
                    }
                    else {
                        callback.error(orderResponse.getMessage());
                    }

                } else {
                    callback.error("Неизвестная ошибка");
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                //callback.error(t.getMessage());
                callback.error("Неизвестная ошибка!");
            }

        });
    }

    public interface GetProductsCallback {
        void success(Products products);
        void error(String message);
    }

    /**
     * Получить список товаров в текущем заказе
     * @return
     */
    public void getProducts(final GetProductsCallback callback){

        if(activeOrderId == null){
            callback.error(null);
        }

        AccountManager am = AccountManager.getInstance();
        BL.getProducts(am.getLogin(), am.getSalt(), am.getSig(), activeOrderId).enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                if (response.isSuccessful()) {
                    products = response.body().getProducts();
                    callback.success(products);
                } else {
                    //TODO
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                callback.error("Отсутствует соединение с сервером!");
            }
        });
    }

    /**
     * Получить список закешированных товаров в текущем заказе
     * @return
     */
    public Products getSavedProducts(){
        return products;
    }

    public interface ConfirmProductCallback {
        void success(BeforeProductsModifyCallback callback);
        void error(String message);
    }

    public interface BeforeProductsModifyCallback {
        void success();
    }

    /**
     * Подтвердить упаковку товар
     * @return
     */
    public void confirmProduct(final String productId, final boolean manual, final String weight, final ConfirmProductCallback callback){

        AccountManager am = AccountManager.getInstance();
        BL.confirmProduct(am.getLogin(), am.getSalt(), am.getSig(), productId, manual, weight).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    if(response.body().isSuccessful()){

                        BeforeProductsModifyCallback beforeCallback = new BeforeProductsModifyCallback() {
                            @Override
                            public void success() {
                                products.getProductById(productId).confirm(manual);
                            }
                        };


                        callback.success(beforeCallback);
                    }
                    else {
                        callback.error(response.body().getMessage());
                    }
                } else {
                    //TODO
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                callback.error("Отсутствует соединение с сервером!");
            }
        });
    }

    public interface CancelProductCallback {
        void success();
        void error(String message);
    }

    /**
     * Отменить упаковку товар
     * @return
     */
    public void cancelProduct(final String productId, final int quantity, final CancelProductCallback callback){

        AccountManager am = AccountManager.getInstance();
        BL.cancelProduct(am.getLogin(), am.getSalt(), am.getSig(), productId, String.valueOf(quantity)).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    if(response.body().isSuccessful()){
                        products.getProductById(productId).cancel(quantity);
                        callback.success();
                    }
                    else {
                        callback.error(response.body().getMessage());
                    }
                } else {
                    //TODO
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                callback.error("Отсутствует соединение с сервером!");
            }
        });
    }

}
