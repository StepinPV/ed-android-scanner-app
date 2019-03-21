package com.example.ed.edscannerapp.packing;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.ScannerActivity;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.User;

/**
 * Экран выбора заказа
 */
public class OrderActivity extends ScannerActivity {

    static public final int PRODUCT_ACTIVITY_CODE = 1;
    static public final int ORDERS_ACTIVITY_CODE = 2;
    static public final int PAUSE_ACTIVITY_CODE = 3;
    
    private Order selectedOrder;
    private boolean viewInitialized = false;

    //Менеджер для работы со сборкой заказа
    Manager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        manager = Manager.getInstance();
        
        //Загружаем заказ, сервер подберет необходимый
        loadOrderById("");
    }

    /**
     * Загрузить заказ с переданным id
     * @param orderId
     */
    private void loadOrderById(final String orderId){
        manager.getOrder(orderId, new Manager.GetOrderCallback(){
            @Override
            public void success(Order order, String message){
                if (!viewInitialized) {
                    setContentView(R.layout.activity_order);
                    viewInitialized = true;
                }
                
                updateOrder(order, message);
            };

            @Override
            public void error(String message){
                showConfirm(message, "Ошибка загрузки заказа!", "Повторить", "Отмена", new ConfirmDialogCallback() {
                    @Override
                    public void confirm() {
                        loadOrderById(orderId);
                    }

                    @Override
                    public void cancel() {
                        exit();
                    }
                });
            };
        });
    }

    /**
     * Обновляет экран и сканер
     * @param order
     * @param message
     */
    private void updateOrder(Order order, String message){
        selectedOrder = order;
        updateComponents(message);
        updateScanner();
    }

    /**
     * Обновить сканер в зависимости от выбранного заказа
     */
    private void updateScanner() {
        if(selectedOrder != null) {
            switch(selectedOrder.getStatus()){
                case Order.STATUS_UNSTARTED:
                case Order.STATUS_PAUSED:
                case Order.STATUS_PARTIAL:
                    this.initScanner();
                    break;
                case Order.STATUS_ACTIVE:
                    this.destroyScanner();
                    break;
            }
        } else {
            this.initScanner();
        }
    }

    public void exit(View w){
        exit();
    }

    private void exit() {
        this.destroyScanner();
        finish();
    }

    public void startOrder(View w){
        this.startOrder();
    }

    /**
     * Начать собирать выбранный заказ
     */
    private void startOrder() {
        manager.startOrder(selectedOrder.getId(), new Manager.GetOrderCallback(){
            @Override
            public void success(Order order, String message){
                openProductActivity();
            };
            @Override
            public void error(String message){

                showConfirm(message, "Ошибка запуска сборки!", "Повторить", "Отмена", new ConfirmDialogCallback() {
                    @Override
                    public void confirm() {
                        startOrder();
                    }

                    @Override
                    public void cancel() {
                        loadOrderById("");
                    }
                });
            };
        });
    }

    public void pauseButtonHandler(View w){

        showConfirm("Вы действительно хотите перейти к процессу заморозки заказа?", "",
                "Подтвердить", "Отмена", new ConfirmDialogCallback() {
            @Override
            public void confirm() {
                openPauseActivity(selectedOrder.getId());
            }

            @Override
            public void cancel() {

            }
        });
    }

    public void cancelButtonHandler(View w){

        showConfirm("Вы действительно хотите рассформировать сборку данного заказа?", "",
                "Подтвердить", "Отмена", new ConfirmDialogCallback() {
                    @Override
                    public void confirm() {
                        manager.cancelOrder(new Manager.GetOrderCallback(){
                            @Override
                            public void success(Order order, String message){
                                updateOrder(order, null);
                            };
                            @Override
                            public void error(String message){
                                showErrorMessage(message);
                            };
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });

    }

    public void openOrdersActivity(View w){
        this.openOrdersActivity();
    }

    public void openProductActivity(View w){
        this.openProductActivity();
    }

    /**
     * Открыть экран сборки товаров
     */
    private void openProductActivity(){
        this.destroyScanner();
        startActivityForResult(new Intent(this, ProductActivity.class), PRODUCT_ACTIVITY_CODE);
    }

    /**
     * Открыть экран списка заказов
     */
    private void openOrdersActivity(){
        this.destroyScanner();
        startActivityForResult(new Intent(this, OrdersActivity.class), ORDERS_ACTIVITY_CODE);
    }

    /**
     * Открыть экран заморозки заказа
     */
    private void openPauseActivity(String orderId){
        this.destroyScanner();
        Intent intent = new Intent(this, PauseActivity.class);
        intent.putExtra("orderId", orderId);
        startActivityForResult(intent, PAUSE_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){
            case ORDERS_ACTIVITY_CODE:
                String orderId = "";
                if(intent != null){
                    orderId = intent.getStringExtra("order_id");
                }
                loadOrderById(orderId);
                break;

            case PRODUCT_ACTIVITY_CODE:
                loadOrderById("");
                break;

            case PAUSE_ACTIVITY_CODE:
                loadOrderById("");
                break;

        }
    }

    /**
     * Обновить компоненты экрана
     */
    private void updateComponents(String message){

        //Подставляем информацию о пользователе
        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.order_user_name);
                    userNameView.setText(user.getFullName());
                }
            }
        });

        boolean hasOrder = selectedOrder != null;
        boolean isActive = hasOrder && selectedOrder.getStatus().equals(Order.STATUS_ACTIVE);

        ((LinearLayout) findViewById(R.id.order_client_block)).setVisibility(hasOrder ? LinearLayout.VISIBLE : LinearLayout.GONE);
        ((LinearLayout) findViewById(R.id.order_client_no_orders_block)).setVisibility(!hasOrder ? LinearLayout.VISIBLE : LinearLayout.GONE);

        if (!hasOrder) {
            ((TextView) findViewById(R.id.order_client_no_orders_message)).setText(message != null ? message : "Поздравляем! Все заказы собраны.");
        }

        if(hasOrder){
            //имя
            ((TextView) findViewById(R.id.order_client_name)).setText(selectedOrder.getName());
            //количество заказов
            ((TextView) findViewById(R.id.order_client_orders_count)).setText(getString(R.string.order_client_orders_count, selectedOrder.getOrderCount()));

            //Статус
            TextView statusView = (TextView) findViewById(R.id.order_client_status);
            if(selectedOrder.isVip()){
                statusView.setText("VIP");
                statusView.setVisibility(TextView.VISIBLE);
            }
            else {
                statusView.setText("");
                statusView.setVisibility(TextView.GONE);
            }

            //город
            ((TextView) findViewById(R.id.order_client_shipping_zone)).setText(selectedOrder.getShippingZone());
            //id
            ((TextView) findViewById(R.id.order_client_order_id)).setText("№" + selectedOrder.getId());
            //comment
            ((TextView) findViewById(R.id.order_client_comment)).setText(selectedOrder.getComment());
        }

        ((Button) findViewById(R.id.order_orders_button)).setVisibility(!hasOrder || !isActive ? Button.VISIBLE : Button.GONE);
        ((Button) findViewById(R.id.order_start_button)).setVisibility(hasOrder && !isActive ? Button.VISIBLE : Button.GONE);
        ((Button) findViewById(R.id.order_pause_button)).setVisibility(hasOrder && isActive ? Button.VISIBLE : Button.GONE);
        ((Button) findViewById(R.id.order_cancel_button)).setVisibility(hasOrder && isActive ? Button.VISIBLE : Button.GONE);
        ((Button) findViewById(R.id.order_scanning_button)).setVisibility(hasOrder && isActive ? Button.VISIBLE : Button.GONE);

        int bgColor = R.color.order_status_bg_default;
        int statusTitle = R.string.order_status_default;
        int statusTitleColor = getResources().getColor(R.color.order_status_title_default);

        if(hasOrder){
            switch(selectedOrder.getStatus()){
                case Order.STATUS_UNSTARTED:
                    bgColor = R.color.order_status_bg_unstarted;
                    statusTitle = R.string.order_status_unstarted;
                    statusTitleColor = getResources().getColor(R.color.order_status_title_unstarted);
                    break;
                case Order.STATUS_ACTIVE:
                    bgColor = R.color.order_status_bg_active;
                    statusTitle = R.string.order_status_active;
                    statusTitleColor = getResources().getColor(R.color.order_status_title_active);
                    break;
                case Order.STATUS_PAUSED:
                    bgColor = R.color.order_status_bg_pause;
                    statusTitle = R.string.order_status_pause;
                    statusTitleColor = getResources().getColor(R.color.order_status_title_pause);
                    break;
            }
        }

        ((LinearLayout) findViewById(R.id.order_container)).setBackgroundResource(bgColor);
        TextView statusTextView = (TextView) findViewById(R.id.order_status_title);
        statusTextView.setText(statusTitle);
        statusTextView.setTextColor(statusTitleColor);

    }

    @Override
    public void onDestroy(){
        Manager.destroyInstance();
        super.onDestroy();
    }

    public void handleScanner(String barcode) {
        playSound();

        if(selectedOrder != null && barcode.equals(selectedOrder.getId())) {
            showNotification("Данный заказ уже выбран!");
        } else {
            loadOrderById(barcode);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // По кнопке 82 открываем список заказов
        if(keyCode == 82) {
            if(selectedOrder == null || !selectedOrder.getStatus().equals(Order.STATUS_ACTIVE)){
                this.openOrdersActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}