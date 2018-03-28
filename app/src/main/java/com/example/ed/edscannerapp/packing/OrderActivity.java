package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.Helper;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Order;

public class OrderActivity extends AppCompatActivity {

    static public final int PRODUCT_ACTIVITY_CODE = 1;
    static public final int ORDERS_ACTIVITY_CODE = 2;
    static public final int PAUSE_ACTIVITY_CODE = 3;
    private String selectedOrderId;

    Manager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = Manager.getInstance();
        manager.getOrder("", new Manager.GetOrderCallback(){
            @Override
            public void success(Order order){
                setContentView(R.layout.activity_order);
                updateOrder(order);
            };
            @Override
            public void error(String message){
                //TODO
                finish();
            };
        });
    }

    private void updateOrderById(String orderId){
        manager.getOrder(orderId, new Manager.GetOrderCallback(){
            @Override
            public void success(Order order){
                updateOrder(order);
            };
            @Override
            public void error(String message){
                Helper.showErrorMessage(OrderActivity.this, message);
            };
        });
    }

    private void updateOrder(Order order){
        updateComponents(order);

        if(order != null){
            selectedOrderId = order.getId();
        }
    }

    public void exitButtonHandler(View w){
        finish();
    }

    public void startButtonHandler(View w){
        manager.startOrder(selectedOrderId, new Manager.GetOrderCallback(){
            @Override
            public void success(Order order){
                OrderActivity.this.openScanningActivity();
            };
            @Override
            public void error(String message){
                //TODO
                AlertDialog.Builder builder = Helper.getDialogBuilder(OrderActivity.this,
                        message,
                        "", null);

                builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        manager.getOrder("", new Manager.GetOrderCallback(){
                            @Override
                            public void success(Order order){
                                updateOrder(order);
                            };
                            @Override
                            public void error(String message){
                                Helper.showErrorMessage(OrderActivity.this, message);
                            };
                        });
                    }
                });

                builder.create().show();
            };
        });
    }

    public void pauseButtonHandler(View w){

        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Вы действительно хотите заморозить сборку данного заказа?",
                "", null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                manager.pauseOrder(new Manager.GetOrderCallback(){
                    @Override
                    public void success(Order order){
                        openPauseActivity(order.getId());
                    };
                    @Override
                    public void error(String message){
                        Helper.showErrorMessage(OrderActivity.this, message);
                    };
                });
            }
        }).setNegativeButton("Отмена", null);

        builder.create().show();
    }

    public void cancelButtonHandler(View w){

        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Вы действительно хотите рассформировать сборку данного заказа?",
                "", null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                manager.cancelOrder(new Manager.GetOrderCallback(){
                    @Override
                    public void success(Order order){
                        updateOrder(order);
                    };
                    @Override
                    public void error(String message){
                        Helper.showErrorMessage(OrderActivity.this, message);
                    };
                });
            }
        }).setNegativeButton("Отмена", null);

        builder.create().show();

    }

    public void ordersButtonHandler(View w){
        startActivityForResult(new Intent(this, OrdersActivity.class), ORDERS_ACTIVITY_CODE);
    }

    public void scanningButtonHandler(View w){
        this.openScanningActivity();
    }

    private void openScanningActivity(){
        startActivityForResult(new Intent(this, ProductActivity.class), PRODUCT_ACTIVITY_CODE);
    }

    private void openPauseActivity(String orderId){
        Intent intent = new Intent(this, PauseActivity.class);
        intent.putExtra("orderId", orderId);
        startActivityForResult(intent, PAUSE_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){
            case ORDERS_ACTIVITY_CODE:
                updateOrderById(intent.getStringExtra("order_id"));
                break;

            case PRODUCT_ACTIVITY_CODE:
                updateOrderById("");
                break;

            case PAUSE_ACTIVITY_CODE:
                updateOrderById("");
                break;

        }
    }

    private void updateComponents(Order order){

        TextView userNameView = (TextView) findViewById(R.id.order_user_name);
        userNameView.setText(AccountManager.getInstance().getLogin());

        boolean hasOrder = order != null;
        boolean isActive = false;

        ((LinearLayout) findViewById(R.id.order_client_block)).setVisibility(hasOrder ? LinearLayout.VISIBLE : LinearLayout.GONE);
        ((TextView) findViewById(R.id.order_client_no_orders_message)).setVisibility(!hasOrder ? LinearLayout.VISIBLE : LinearLayout.GONE);

        if(hasOrder){
            //имя
            ((TextView) findViewById(R.id.order_client_name)).setText(order.getName());
            //количество заказов
            ((TextView) findViewById(R.id.order_client_orders_count)).setText(getString(R.string.order_client_orders_count, order.getOrderCount()));
            //Статус
            ((TextView) findViewById(R.id.order_client_status)).setText(getString(R.string.order_client_status, order.isVip() ? "VIP" : "Обычный"));
            //город
            ((TextView) findViewById(R.id.order_client_shipping_zone)).setText(order.getShippingZone());
            //id
            ((TextView) findViewById(R.id.order_client_order_id)).setText("№" + order.getId());
            //comment
            ((TextView) findViewById(R.id.order_client_comment)).setText(order.getComment());

            isActive = order.getStatus().equals(Order.STATUS_ACTIVE);
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
            switch(order.getStatus()){
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

}