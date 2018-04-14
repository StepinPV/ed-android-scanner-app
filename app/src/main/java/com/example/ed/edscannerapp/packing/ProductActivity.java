package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.BarcodeScanner;
import com.example.ed.edscannerapp.Helper;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.Products;

import java.util.Timer;

public class ProductActivity extends AppCompatActivity {

    static final int PRODUCTS_ACTIVITY_CODE = 1;

    private ViewPager viewPager;
    private ProductsPagerAdapter pagerAdapter;

    private Manager manager = Manager.getInstance();
    private int currentPagePosition = 0;
    private boolean hasBarcode = false;
    private BarcodeScanner barcodeScanner = null;
    private SoundPool soundPool;
    private int soundID;
    private boolean confirmingProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(Products products){
                setContentView(R.layout.activity_product);
                init();
            };

            @Override
            public void error(String message){
                //TODO
                finish();
            };

        });
    }

    private void init(){
        viewPager = (ViewPager) findViewById(R.id.activity_product_viewpager);
        pagerAdapter = new ProductsPagerAdapter(getSupportFragmentManager());

        pagerAdapter.setProducts(manager.getSavedProducts());
        viewPager.setAdapter(pagerAdapter);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        soundID = soundPool.load(this, R.raw.scan,1);

        barcodeScanner = new BarcodeScanner(this, new BarcodeScanner.ScanCallback() {
            @Override
            public void success(String barcode) {
                checkProduct(false, barcode, false);
            }
        });

        TextView userNameView = (TextView) findViewById(R.id.product_user_name);
        userNameView.setText(AccountManager.getInstance().getLogin());

        TextView orderIdView = (TextView) findViewById(R.id.product_order_id);
        orderIdView.setText("№" + manager.getActiveOrderId());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                currentPagePosition = position;
                ProductActivity.this.updatePositionCounter();
                ProductActivity.this.updateButtons();
            }
        });

        updateData();
    }

    private void updateButtons(){
        Product currentProduct = ProductsHelper.getUnscannedByIndex(manager.getSavedProducts(), currentPagePosition);

        String barcode = currentProduct.getBarcode();
        //TODO Должно быть не здесь
        hasBarcode = barcode != null && !barcode.equals("");

        ((ImageButton) findViewById(R.id.manualButton))
                .setVisibility((!hasBarcode || currentProduct.getRejectCount() >= 2) ? ImageButton.VISIBLE : ImageButton.GONE);
        ((ImageButton) findViewById(R.id.barcodeButton))
                .setVisibility(hasBarcode ? ImageButton.VISIBLE : ImageButton.GONE);
    }

    public void openProductsActivity(View w){
        Intent intent = new Intent(this, ProductsActivity.class);
        startActivityForResult(intent, PRODUCTS_ACTIVITY_CODE);
    }

    public void exit(View w) {
        exit();
    }

    public void exit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void completeOrder(View w) {
        manager.completeOrder(new Manager.GetOrderCallback() {
            @Override
            public void success(Order order, boolean hold) {
                exit();
            }

            @Override
            public void error(String message) {
                Helper.showErrorMessage(ProductActivity.this, message);
            }
        });
    }

    public void barcodeButtonHandler(View w){
        if(confirmingProcess){
            return;
        }

        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Введите штрих код товара в поле ввода",
                "Ручной ввод штрихкода", R.layout.barcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String barcode = textView.getText().toString();
                checkProduct(false, barcode, true);
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

    public void manualButtonHandler(View w){
        if(confirmingProcess){
            return;
        }
        
        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Вы действительно хотите подтвердить товар вручную?",
                "Ручное подтверждение товара", null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                checkProduct(true, null, false);
            }
        }).setNegativeButton("Отмена", null);

        builder.create().show();
    }

    public void checkProduct(boolean manual, String barcode, boolean manualInputBarcode){

        Product currentProduct = ProductsHelper.getUnscannedByIndex(manager.getSavedProducts(), currentPagePosition);

        if(manual){
            successBarcode(currentProduct.getId(), true);
        }
        else {
            if(barcode.equals(currentProduct.getBarcode())){
                soundPool.play(soundID, 1, 1,1,0, 1f);
                successBarcode(currentProduct.getId(), false);
            }
            else {
                ((Vibrator)getSystemService(ProductActivity.VIBRATOR_SERVICE)).vibrate(300);

                //TODO Надо в колбеке
                if(manualInputBarcode){
                    currentProduct.increaseRejectCounter();
                }

                updateButtons();

                errorBarcode();
            }
        }
    }

    public void errorBarcode(){
        confirmingProcess = true;
        Helper.Deferred def = new Helper.Deferred(ProductActivity.this);

        def.addCallback(new Helper.DeferredCallback(){
            @Override
            public void success(){
                confirmingProcess = false;
            };
        });

        showError(new Timer(), def);
    }

    public void successBarcode(String productId, boolean manual){
        confirmingProcess = true;
        manager.confirmProduct(productId, manual, new Manager.ConfirmProductCallback() {
            @Override
            public void success(final Manager.BeforeProductsModifyCallback callback) {
                Helper.Deferred def = new Helper.Deferred(ProductActivity.this);

                def.addCallback(new Helper.DeferredCallback(){

                    @Override
                    public void success(){
                        callback.success();
                        updateData();
                        confirmingProcess = false;
                    };

                });

                showSuccess(new Timer(), def);

            }

            @Override
            public void error(String message) {
                Helper.showErrorMessage(ProductActivity.this, message);
                confirmingProcess = false;
            }
        });
    }

    public void updateData(){

        int unScannedProducts = ProductsHelper.getUnscannedCount(manager.getSavedProducts());
        boolean complete = unScannedProducts == 0;

        ((ViewPager) findViewById(R.id.activity_product_viewpager)).setVisibility(complete ? ViewPager.GONE : ViewPager.VISIBLE);
        ((LinearLayout) findViewById(R.id.product_counters)).setVisibility(complete ? LinearLayout.GONE : LinearLayout.VISIBLE);
        ((TextView) findViewById(R.id.product_complete_message)).setVisibility(complete ? LinearLayout.VISIBLE : LinearLayout.GONE);
        ((Button) findViewById(R.id.product_complete_button)).setVisibility(complete ? LinearLayout.VISIBLE : LinearLayout.GONE);

        if(!complete){
            pagerAdapter.setProducts(manager.getSavedProducts());
            pagerAdapter.notifyChangeInPosition(0);
            pagerAdapter.notifyDataSetChanged();
            updateProgressCounter();
            updatePositionCounter();
            updateButtons();
        }

    }
    
    private void updatePositionCounter(){
        int unScannedCount = ProductsHelper.getUnscannedCount(manager.getSavedProducts());
        ((TextView) findViewById(R.id.product_position)).setText((currentPagePosition + 1) + "/" + unScannedCount);
    }
    
    private void updateProgressCounter(){
        Products products = manager.getSavedProducts();
        int allCount = ProductsHelper.getCount(products);
        int scannedCount = allCount - ProductsHelper.getUnscannedCount(products);
        ((TextView) findViewById(R.id.product_progress)).setText(scannedCount + "/" + allCount);
    }

    private void selectProduct(String productId){
        viewPager.setCurrentItem(ProductsHelper.getUnscannedIndex(manager.getSavedProducts(), productId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){
            case PRODUCTS_ACTIVITY_CODE:

                updateData();

                if(intent != null){
                    String productId = intent.getStringExtra("product_id");
                    if(!productId.equals("")){
                        this.selectProduct(productId);
                    }
                }

                break;

        }
    }

    private ProductFragment getCurrentFragment(){
        return (ProductFragment)getSupportFragmentManager()
                .findFragmentByTag(
                        "android:switcher:" +
                        R.id.activity_product_viewpager + ":" +
                        pagerAdapter.getItemId(viewPager.getCurrentItem())
                );
    }

    private void showSuccess(Timer timer, Helper.Deferred def){
        getCurrentFragment().showSuccess(timer, def);
    }

    private void showError(Timer timer, Helper.Deferred def){
        getCurrentFragment().showError(timer, def);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==139 && !confirmingProcess && hasBarcode){
            if(event.getRepeatCount() == 0) {
                barcodeScanner.startScan();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==139){
            if(event.getRepeatCount() == 0) {
                barcodeScanner.stopScan();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDestroy(){
        barcodeScanner.destroy();
        super.onDestroy();
    }
}