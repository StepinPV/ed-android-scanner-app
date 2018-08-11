package com.example.ed.edscannerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.example.ed.edscannerapp.entities.CheckResponse;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.User;
import com.example.ed.edscannerapp.packing.Manager;
import com.example.ed.edscannerapp.server.BL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckActivity extends AppCompatActivity {

    private Manager manager = Manager.getInstance();
    private BarcodeScanner barcodeScanner = null;
    private SoundPool soundPool;
    private int soundID;
    private boolean isFetch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check);
        init();
    }

    private void init(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        soundID = soundPool.load(this, R.raw.scan,1);

        initScanner();

        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                TextView userNameView = (TextView) findViewById(R.id.check_user_name);
                userNameView.setText(user.getFullName());
            }
        });
    }

    public void exit(View w) {
        destroyScanner();
        finish();
    }

    private void initScanner() {
        barcodeScanner = new BarcodeScanner(this, new BarcodeScanner.ScanCallback() {
            @Override
            public void success(String barcode) {
                checkProduct(barcode);
            }
        });
    }

    private void destroyScanner() {
        if(barcodeScanner != null) {
            barcodeScanner.destroy();
            barcodeScanner = null;
        }
    }

    public void barcodeButtonHandler(View w){
        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Введите штрих код товара в поле ввода",
                "Ручной ввод штрихкода", R.layout.barcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String barcode = textView.getText().toString();
                checkProduct(barcode);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        dialog.show();
    }

    public void checkProduct(final String barcode){

        AccountManager am = AccountManager.getInstance();

        this.isFetch = true;
        BL.checkProduct(am.getLogin(), am.getSalt(), am.getSig(), barcode).enqueue(new Callback<CheckResponse>() {

            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                if (response.isSuccessful()) {
                    Product product = response.body().getProduct();

                    if(product == null){
                        ((Vibrator)getSystemService(CheckActivity.VIBRATOR_SERVICE)).vibrate(300);
                    }
                    else {
                        soundPool.play(soundID, 1, 1,1,0, 1f);
                    }
                    updateData(product, barcode);

                }
                else {
                    //updateData(null, barcode);
                }

                CheckActivity.this.isFetch = false;
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                //updateData(null, barcode);
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
            new Helper.ImageLoader(imageView).execute(product.getImage());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 139) {
            if(barcodeScanner != null && !this.isFetch && event.getRepeatCount() == 0) {
                barcodeScanner.startScan();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==139){
            if(barcodeScanner != null && event.getRepeatCount() == 0) {
                barcodeScanner.stopScan();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        this.destroyScanner();
        super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        this.destroyScanner();
        super.onDestroy();
    }
}