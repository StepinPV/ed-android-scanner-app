package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.util.List;
import java.util.Timer;

public class ProductsActivity extends AppCompatActivity {

    Manager manager = Manager.getInstance();
    ProductsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        this.initList();

        TextView userNameView = (TextView) findViewById(R.id.products_user_name);
        userNameView.setText(AccountManager.getInstance().getLogin());
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

    public void initList(){

        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(com.example.ed.edscannerapp.entities.Products products){
                ListView listView = (ListView) findViewById(R.id.productListView);
                adapter = new ProductsListAdapter(ProductsActivity.this, products);
                listView.setAdapter(adapter);

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

            };

            @Override
            public void error(String message){
            };

        });

    }

    private void cancelProduct(final Product product){

        final int packingQuantity = product.getPackingQuantity();
        final boolean manyQuantity = packingQuantity > 1;

        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                manyQuantity ? "Введите количество товара" : "",
                "Отминить упаковку товара?", manyQuantity ? R.layout.barcode : null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                int quantity = 1;

                if(manyQuantity){
                    TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                    int inputQuantity = Integer.parseInt(textView.getText().toString());

                    if(inputQuantity > packingQuantity){
                        inputQuantity = packingQuantity;
                    }
                    else if(packingQuantity < 1){
                        inputQuantity = 1;
                    }
                    quantity = inputQuantity;
                }

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
        }).setNegativeButton("Отмена", null);

        builder.setNegativeButton("Отмена", null);

        builder.create().show();
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
                    color = R.color.productListUnScanned;
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
}
