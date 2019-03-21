package com.example.ed.edscannerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ed.edscannerapp.entities.CheckResponse;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.User;
import com.example.ed.edscannerapp.server.BL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Экран определения товара по штрихкоду
 * Разметка экрана хранится в файле res/drawable/activity_check
 */
public class CheckActivity extends ScannerActivity {

    private boolean isFetch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check);
        init();
    }

    private void init(){
        initScanner();

        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.check_user_name);
                    userNameView.setText(user.getFullName());
                }
            }
        });
    }

    public void exit(View w) {
        destroyScanner();
        finish();
    }

    @Override
    public void handleScanner(String barcode) {
        checkProduct(barcode);
    }

    public void barcodeButtonHandler(View w){
        showNumberInputDialog("Введите штрих код товара в поле ввода", "Ручной ввод штрихкода",
                null, null, new NumberInputDialogCallback() {
                    @Override
                    public void confirm(String value) {
                        checkProduct(value);
                    }

                    @Override
                    public void cancel() {}
                });
    }

    public void checkProduct(final String barcode){

        AccountManager am = AccountManager.getInstance();

        this.isFetch = true;
        BL.checkProduct(am.getLogin(), am.getSalt(), am.getSig(), barcode).enqueue(new Callback<CheckResponse>() {

            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                if (response.isSuccessful()) {
                    Product product = response.body().getProduct();

                    //Продукт по считанному штрихкоду не найден, включаем вибрацию
                    if(product == null){
                        playVibrate();
                    }
                    else {
                        playSound();
                    }

                    updateData(product, barcode);

                }

                CheckActivity.this.isFetch = false;
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                CheckActivity.this.isFetch = false;
            }
        });
    }

    public void updateData(Product product, String barcode){

        ((LinearLayout) findViewById(R.id.activity_check_result)).setVisibility(product == null ? LinearLayout.GONE : LinearLayout.VISIBLE);
        TextView notFoundView = (TextView) findViewById(R.id.activity_check_not_found);
        notFoundView.setVisibility(product == null ? TextView.VISIBLE : TextView.GONE);

        if(product == null) {
            notFoundView.setText("Товар с номером штрихкода: " + barcode + " не найден!");
        }
        else {
            ((TextView) findViewById(R.id.activity_check_name)).setText(product.getName());
            ((TextView) findViewById(R.id.activity_check_manufacturer)).setText(product.getManufacturer());
            ((TextView) findViewById(R.id.activity_check_id)).setText("ID: " + product.getProductId());

            ((TextView) findViewById(R.id.activity_check_section)).setText(
                    getString(R.string.activity_product_packing_section, product.getSection())
            );

            ((TextView) findViewById(R.id.activity_check_amount)).setText(
                    getString(R.string.activity_product_packing_amount,
                            product.getWeight(),
                            product.getUnit())
            );

            ImageView imageView = (ImageView) findViewById(R.id.activity_check_image);
            new ImageLoader(imageView).execute(product.getImage());
        }
    }

    /*
    * см ScannerActivity
    * */
    @Override
    public boolean needScanning() {
        return !this.isFetch;
    }

    @Override
    public void onBackPressed() {
        this.destroyScanner();
        super.onBackPressed();
    }
}