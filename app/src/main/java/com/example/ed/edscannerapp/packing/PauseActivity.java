package com.example.ed.edscannerapp.packing;

import android.content.Context;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.Helper;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.Products;
import com.example.ed.edscannerapp.entities.ProductsResponse;
import com.example.ed.edscannerapp.entities.User;
import com.example.ed.edscannerapp.server.BL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PauseActivity extends AppCompatActivity {

    Manager manager = Manager.getInstance();
    ProductsListAdapter adapter;
    Boolean confirming = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause);

        String orderId = getIntent().getStringExtra("orderId");

        this.initList(orderId);

        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.products_user_name);
                    userNameView.setText(user.getFullName());
                }
            }
        });

        TextView orderIdView = (TextView) findViewById(R.id.pause_order_id);
        orderIdView.setText(getString(R.string.pause_order_id, orderId));
    }

    public void exit(View w){
        if(confirming) {
            return;
        }
        finish();
    }

    public void confirm(View w) {
        confirming = true;
        manager.pauseOrder(new Manager.GetOrderCallback(){
            @Override
            public void success(Order order, String message){
                confirming = false;
                finish();
            };
            @Override
            public void error(String message){
                confirming = false;
                Helper.showErrorMessage(PauseActivity.this, message);
            };
        });
    }

    public void initList(String orderId){

        AccountManager am = AccountManager.getInstance();
        BL.getProducts(am.getLogin(), am.getSalt(), am.getSig(), orderId).enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                if (response.isSuccessful()) {
                    Products products = response.body().getProducts();

                    ListView listView = (ListView) findViewById(R.id.productListView);
                    adapter = new ProductsListAdapter(PauseActivity.this, products);
                    listView.setAdapter(adapter);
                } else {
                    //TODO
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                //TODO
            }
        });

    }

    class ProductsListAdapter extends BaseAdapter implements ListAdapter {

        private Context context;
        private Products products;

        public ProductsListAdapter(Context context, Products products){
            this.context = context;
            this.products = products;
        }

        @Override
        public int getCount() {
            return ProductsHelper.getUnscannedCount(products);
        }

        @Override
        public Product getItem(int position) {
            return ProductsHelper.getUnscannedByIndex(products, position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Product item = getItem(position);
            convertView = LayoutInflater.from(context).inflate(R.layout.product, null);

            ((TextView)convertView.findViewById(R.id.product_section)).setText(item.getSection());
            ((TextView)convertView.findViewById(R.id.product_name)).setText(item.getName());

            ((TextView)convertView.findViewById(R.id.product_packingQuantity)).setText(
                    String.valueOf(item.getNeededQuantity() - item.getPackingQuantity())
            );

            return convertView;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 139) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == 139){
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
