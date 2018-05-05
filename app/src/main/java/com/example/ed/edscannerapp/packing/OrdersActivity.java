package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Orders;
import com.example.ed.edscannerapp.entities.OrdersResponse;
import com.example.ed.edscannerapp.server.BL;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView listView;
    OrdersListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ordersRefresh);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        TextView userNameView = (TextView) findViewById(R.id.orders_user_name);
        userNameView.setText(AccountManager.getInstance().getLogin());

        listView = (ListView) findViewById(R.id.orders_list);
        TextView emptyText = (TextView) findViewById(R.id.orders_no_orders_message);
        listView.setEmptyView(emptyText);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = (Order) parent.getItemAtPosition(position);
                OrdersActivity.this.exit(order.getId());
            }

        });

        AccountManager am = AccountManager.getInstance();
        BL.getOrders(am.getLogin(), am.getSalt(), am.getSig()).enqueue(new Callback<OrdersResponse>() {
            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                if (response.isSuccessful()) {
                    OrdersActivity.this.updateData(response.body().getOrders().getOrderList());
                } else {
                    //TODO
                    Log.d("123", "response code " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                //TODO
                Log.d("123", "failure " + t);
            }
        });
    }

    public void exit(View w){
        this.exit("");
    }

    private void exit(String orderId){
        Intent intent = new Intent();
        intent.putExtra("order_id", orderId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void updateData(List<Order> orders){
        boolean hasOrders = orders != null;

        if(hasOrders){
            if(adapter == null){
                OrdersListAdapter adapter = new OrdersListAdapter(OrdersActivity.this, orders);
                listView.setAdapter(adapter);
            }
            else {
                adapter.setOrders(orders);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        AccountManager am = AccountManager.getInstance();
        BL.getOrders(am.getLogin(), am.getSalt(), am.getSig()).enqueue(new Callback<OrdersResponse>() {

            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                if (response.isSuccessful()) {
                    OrdersActivity.this.updateData(response.body().getOrders().getOrderList());
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    //TODO
                    Log.d("123", "response code " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                //TODO
                Log.d("123", "failure " + t);
            }
        });

    }

    class OrdersListAdapter extends BaseAdapter implements ListAdapter {

        private Context context;
        private List<Order> items;

        public OrdersListAdapter(Context context, List<Order> items){
            this.context = context;
            this.items = items;
        }

        public void setOrders(List<Order> items){
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Order getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Order item = getItem(position);
            convertView = LayoutInflater.from(context).inflate(R.layout.order, null);

            ((TextView)convertView.findViewById(R.id.order_id)).setText(item.getId());
            ((TextView)convertView.findViewById(R.id.order_client_shipping_zone)).setText(item.getShippingZone());

            int color;
            String secondColumnValue;
            switch (item.getStatus())
            {
                case Order.STATUS_UNSTARTED:
                    color = R.color.orderListUnstarted;
                    secondColumnValue = item.getName();
                    break;
                case Order.STATUS_PAUSED:
                    color = R.color.orderListPaused;
                    secondColumnValue = item.getName();
                    break;
                case Order.STATUS_PARTIAL:
                    color = R.color.orderListPartial;
                    secondColumnValue = item.getName();
                    break;
                case Order.STATUS_ACTIVE:
                    color = R.color.orderListActive;
                    secondColumnValue = "Заказ собирает:\n" + item.getUserName();
                    break;
                default:
                    color = R.color.orderListDefault;
                    secondColumnValue = item.getName();
                    break;
            }
            convertView.setBackgroundResource(color);
            ((TextView)convertView.findViewById(R.id.order_client_name)).setText(secondColumnValue);

            return convertView;
        }
    }
}
