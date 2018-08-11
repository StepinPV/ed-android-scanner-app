package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.Helper;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.Products;
import com.example.ed.edscannerapp.entities.User;

import java.util.List;
import java.util.Timer;

public class ProductsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    Manager manager = Manager.getInstance();
    SwipeRefreshLayout mSwipeRefreshLayout;
    ProductsListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.productsRefresh);
        final ListView listView = (ListView) findViewById(R.id.productListView);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                TextView userNameView = (TextView) findViewById(R.id.products_user_name);
                userNameView.setText(user.getFullName());
            }
        });

        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(com.example.ed.edscannerapp.entities.Products products){
                adapter = new ProductsListAdapter(ProductsActivity.this, products);
                listView.setAdapter(adapter);
            };

            @Override
            public void error(String message){
            };

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product = (Product) parent.getItemAtPosition(position);

                int packingQuantity = product.getPackingQuantity();
                if(packingQuantity > 0){
                    ProductsActivity.this.cancelProduct(product);
                }
                else {
                    ProductsActivity.this.exit(product.getId());
                }
            }

        });
    }

    public void exit(View w){
        this.exit("");
    }

    private void exit(String orderId){
        Intent intent = new Intent();
        intent.putExtra("product_id", orderId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void cancelProduct(final Product product){

        final int packingQuantity = product.getPackingQuantity();
        final boolean manyQuantity = packingQuantity > 1;

        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                manyQuantity ? "Введите количество товара" : "",
                "Отменить упаковку товара?", manyQuantity ? R.layout.barcode : null);

        final AlertDialog dialog;
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Да", null).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        int quantity = 1;
                        boolean isValid = true;
                        TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);

                        if(manyQuantity){
                            int inputQuantity = 1;

                            String input = textView.getText().toString();

                            if(input.equals("")){
                                isValid = false;
                            }
                            else {
                                inputQuantity = Integer.parseInt(input);

                                if(inputQuantity < 1 || inputQuantity > packingQuantity){
                                    isValid = false;
                                }
                            }

                            quantity = inputQuantity;
                        }

                        if(isValid){
                            dialog.dismiss();
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            manager.cancelProduct(product.getId(), quantity, new Manager.CancelProductCallback() {
                                @Override
                                public void success() {
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void error(String message) {
                                    Helper.showErrorMessage(ProductsActivity.this, message);
                                }
                            });
                        }
                        else {
                            textView.setError("Ввведите корректное значение!");
                        }


                    }
                });
            }
        });


        dialog.show();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(com.example.ed.edscannerapp.entities.Products products){
                adapter.setProducts(products);
                adapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            };

            @Override
            public void error(String message){
            };

        });

    }

    class ProductsListAdapter extends BaseAdapter implements ListAdapter {

        private Context context;
        private Products products;

        public ProductsListAdapter(Context context, Products products){
            this.context = context;
            this.products = products;
        }

        public void setProducts(Products products){
            this.products = products;
        }

        @Override
        public int getCount() {
            return products.getList().size();
        }

        @Override
        public Product getItem(int position) {
            return products.getList().get(position);
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
                    getString(R.string.product_packing_quantity,
                            String.valueOf(item.getPackingQuantity()), String.valueOf(item.getNeededQuantity()))
            );

            int color;
            switch (item.getStatus())
            {
                default:
                case Product.STATUS_UNSCANNED:
                    if(item.getPackingQuantity() > 0){
                        color = R.color.productListPartialScanned;
                    }
                    else {
                        color = R.color.productListUnScanned;
                    }
                    break;
                case Product.STATUS_SCANNED:
                    color = R.color.productListScanned;
                    break;
                case Product.STATUS_MANUAL_SCANNED:
                    color = R.color.productListManualScanned;
                    break;
            }
            convertView.setBackgroundResource(color);
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
