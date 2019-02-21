package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import com.example.ed.edscannerapp.CustomViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ed.edscannerapp.AccountManager;
import com.example.ed.edscannerapp.CheckActivity;
import com.example.ed.edscannerapp.Deferred;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.ScannerActivity;
import com.example.ed.edscannerapp.entities.Order;
import com.example.ed.edscannerapp.entities.Product;
import com.example.ed.edscannerapp.entities.Products;
import com.example.ed.edscannerapp.entities.User;

import java.util.Timer;

public class ProductActivity extends ScannerActivity {

    static final int PRODUCTS_ACTIVITY_CODE = 1;
    static final int CHECK_ACTIVITY_CODE = 2;

    private CustomViewPager viewPager;
    private ProductsPagerAdapter pagerAdapter;

    private Manager manager = Manager.getInstance();
    private int currentPagePosition = 0;
    private boolean hasBarcode = false;
    private SoundPool soundPool;
    private int soundID;
    private boolean confirmingProcess;

    private boolean weightInputProcess;
    private String activeProductId;
    private boolean activeProductInputManual;
    private AlertDialog weightInputDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initView();
    }

    private void initView(){
        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(Products products){
                setContentView(R.layout.activity_product);
                init();
            };

            @Override
            public void error(String message){
                AlertDialog.Builder builder = ProductActivity.this.getDialogBuilder("Отсутствует соединение с сервером", "", null);

                builder.setPositiveButton("Повторить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ProductActivity.this.initView();
                    }
                });

                if (!ProductActivity.this.isFinishing()) {
                    builder.create().show();
                }
            };

        });
    }

    private void init(){
        viewPager = (CustomViewPager) findViewById(R.id.activity_product_viewpager);
        pagerAdapter = new ProductsPagerAdapter(getSupportFragmentManager());

        Products products = manager.getSavedProducts();

        if (products != null) {
            pagerAdapter.setProducts(manager.getSavedProducts());
        }

        viewPager.setAdapter(pagerAdapter);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        soundID = soundPool.load(this, R.raw.scan,1);

        this.initScanner();

        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.product_user_name);
                    userNameView.setText(user.getFullName());
                }
            }
        });

        TextView orderIdView = (TextView) findViewById(R.id.product_order_id);
        orderIdView.setText("№" + manager.getActiveOrderId());

        viewPager.addOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {
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

    @Override
    public void handleScanner(String value) {
        if (this.weightInputProcess) {
            if (value.length() != 13 || value.charAt(0) != '2') {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Данный штрихкод не является весовым!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // Формат 2 000000 XXXXX 0
            String weight = value.substring(7, 12);

            successBarcode(this.activeProductId, this.activeProductInputManual, weight);

            soundPool.play(soundID, 1, 1,1,0, 1f);
            this.weightInputProcess = false;
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            weightInputDialog.cancel();

        } else {
            checkProduct(false, value, false);
        }
    }

    private void updateButtons(){
        Product currentProduct = ProductsHelper.getUnscannedByIndex(manager.getSavedProducts(), currentPagePosition);

        //TODO Должно быть не здесь
        hasBarcode = currentProduct.hasBarcode();

        ((ImageButton) findViewById(R.id.manualButton))
                .setVisibility((!hasBarcode || currentProduct.getRejectCount() >= 2) ? ImageButton.VISIBLE : ImageButton.GONE);
        ((ImageButton) findViewById(R.id.barcodeButton))
                .setVisibility(hasBarcode ? ImageButton.VISIBLE : ImageButton.GONE);
    }

    public void openProductsActivity(View w){
        this.openProductsActivity();
    }

    private void openProductsActivity(){
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

    public void openCheckActivity(View w){
        this.openCheckActivity();
    }

    private void openCheckActivity() {
        this.destroyScanner();
        startActivityForResult(new Intent(this, CheckActivity.class), CHECK_ACTIVITY_CODE);
    }

    public void completeOrder(View w) {
        manager.completeOrder(new Manager.GetOrderCallback() {
            @Override
            public void success(Order order, String message) {
                exit();
            }

            @Override
            public void error(String message) {
                ProductActivity.this.showErrorMessage(message);
            }
        });
    }

    public void confirmComment(View w) {
        Button completeButton = (Button) findViewById(R.id.product_complete_button);
        CheckBox checkbox = (CheckBox) findViewById(R.id.product_complete_checkbox);

        completeButton.setEnabled(checkbox.isChecked());
    }

    public void barcodeButtonHandler(View w){
        if(confirmingProcess){
            return;
        }

        AlertDialog.Builder builder = this.getDialogBuilder("Введите штрих код товара в поле ввода",
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

        if (!ProductActivity.this.isFinishing()) {
            dialog.show();
        }
    }

    public void inputProductWeight(final String productId, final boolean manual){
        AlertDialog.Builder builder = this.getDialogBuilder("Просканируйте или введите вес товара",
                "Ввод веса в формате x.xxx (кг)", R.layout.weight);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String weight = textView.getText().toString();

                if (weight.matches("^\\d\\d\\.\\d\\d\\d+$") || weight.matches("^\\d\\.\\d\\d\\d+$")) {
                    successBarcode(productId, manual, weight);

                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    ProductActivity.this.weightInputProcess = false;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Вес должен быть в формате x.xxx или xx.xxx", Toast.LENGTH_SHORT);
                    toast.show();
                    ProductActivity.this.inputProductWeight(productId, manual);
                }
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                ProductActivity.this.weightInputProcess = false;
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        dialog.setOnKeyListener(new AlertDialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if(keyCode == 139) {
                    if(event.getAction() == event.ACTION_DOWN && scanner != null && event.getRepeatCount() == 0) {
                        scanner.startScan();
                    }
                    if(event.getAction() == event.ACTION_UP && scanner != null && event.getRepeatCount() == 0) {
                        scanner.stopScan();
                    }
                    return true;
                }
                return false;
            }
        });

        if (!ProductActivity.this.isFinishing()) {
            this.weightInputProcess = true;
            this.weightInputDialog = dialog;
            this.activeProductId = productId;
            this.activeProductInputManual = manual;
            dialog.show();
        }
    }

    public void manualButtonHandler(View w){
        if(confirmingProcess){
            return;
        }

        AlertDialog.Builder builder = this.getDialogBuilder("Вы действительно хотите подтвердить товар вручную?",
                "Ручное подтверждение товара", null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                checkProduct(true, null, false);
            }
        }).setNegativeButton("Отмена", null);

        if (!ProductActivity.this.isFinishing()) {
            builder.create().show();
        }
    }

    public void checkProduct(boolean manual, String barcode, boolean manualInputBarcode){

        Product currentProduct = ProductsHelper.getUnscannedByIndex(manager.getSavedProducts(), currentPagePosition);

        if(currentProduct == null) {
            return;
        }

        if(manual){
            if (currentProduct.hasWeight()) {
                inputProductWeight(currentProduct.getId(), true);
            } else {
                successBarcode(currentProduct.getId(), true, null);
            }
        }
        else {
            if(currentProduct.checkBarcode(barcode)){
                soundPool.play(soundID, 1, 1,1,0, 1f);

                if (currentProduct.hasWeight()) {
                    inputProductWeight(currentProduct.getId(), false);
                } else {
                    successBarcode(currentProduct.getId(), false, null);
                }
            }
            else {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(300);
                }

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
        Deferred def = new Deferred(ProductActivity.this);

        def.addCallback(new Deferred.Callback(){
            @Override
            public void success(){
                confirmingProcess = false;
            };
        });

        showError(new Timer(), def);
    }

    public void successBarcode(String productId, boolean manual, String weight){
        confirmingProcess = true;
        viewPager.setPagingEnabled(false);
        manager.confirmProduct(productId, manual, weight, new Manager.ConfirmProductCallback() {
            @Override
            public void success(final Manager.BeforeProductsModifyCallback callback) {
                Deferred def = new Deferred(ProductActivity.this);

                def.addCallback(new Deferred.Callback(){

                    @Override
                    public void success(){
                        callback.success();
                        updateData();
                        viewPager.setPagingEnabled(true);
                        confirmingProcess = false;
                    };

                });

                showSuccess(new Timer(), def);

            }

            @Override
            public void error(String message) {
                ProductActivity.this.showErrorMessage(message);
                viewPager.setPagingEnabled(true);
                confirmingProcess = false;
            }
        });
    }

    public void updateData(){

        Products products = manager.getSavedProducts();

        if(products == null) {
            return;
        }

        int unScannedProducts = ProductsHelper.getUnscannedCount(products);
        boolean complete = unScannedProducts == 0;

        ((CustomViewPager) findViewById(R.id.activity_product_viewpager)).setVisibility(complete ? CustomViewPager.GONE : CustomViewPager.VISIBLE);
        ((LinearLayout) findViewById(R.id.product_counters)).setVisibility(complete ? LinearLayout.GONE : LinearLayout.VISIBLE);

        if(!complete){
            pagerAdapter.setProducts(manager.getSavedProducts());
            pagerAdapter.notifyChangeInPosition(0);
            pagerAdapter.notifyDataSetChanged();
            updateProgressCounter();
            updatePositionCounter();
            updateButtons();
            ((TextView) findViewById(R.id.product_complete_message)).setVisibility(LinearLayout.GONE);
            ((TextView) findViewById(R.id.product_complete_comment)).setVisibility(LinearLayout.GONE);
            ((Button) findViewById(R.id.product_complete_button)).setVisibility(LinearLayout.GONE);
        }
        else {
            manager.getOrder("", new Manager.GetOrderCallback(){
                @Override
                public void success(Order order, String message){
                    ((TextView) findViewById(R.id.product_complete_message)).setVisibility(LinearLayout.VISIBLE);

                    TextView commentView = (TextView) findViewById(R.id.product_complete_comment);
                    TextView commentViewTitle = (TextView) findViewById(R.id.product_complete_comment_title);
                    Button completeButton = (Button) findViewById(R.id.product_complete_button);
                    CheckBox checkbox = (CheckBox) findViewById(R.id.product_complete_checkbox);

                    completeButton.setVisibility(LinearLayout.VISIBLE);

                    String comment = order.getComment();
                    if(comment != null && !comment.equals("")){
                        commentView.setText(comment);
                        commentView.setMovementMethod(new ScrollingMovementMethod());
                        commentView.setVisibility(TextView.VISIBLE);
                        commentViewTitle.setVisibility(TextView.VISIBLE);
                        checkbox.setVisibility(CheckBox.VISIBLE);
                        checkbox.setChecked(false);
                        completeButton.setEnabled(false);
                    }
                    else {
                        commentView.setVisibility(LinearLayout.GONE);
                        commentViewTitle.setVisibility(TextView.GONE);
                        checkbox.setVisibility(CheckBox.GONE);
                        completeButton.setEnabled(true);
                    }

                };
                @Override
                public void error(String message){
                    ProductActivity.this.showErrorMessage(message);
                };
            });
        }

        if(complete) {
            this.destroyScanner();
        } else {
            this.initScanner();
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

        updateData();

        switch(requestCode){
            case PRODUCTS_ACTIVITY_CODE:

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

    private void showSuccess(Timer timer, Deferred def){
        ProductFragment f = getCurrentFragment();
        if(f != null) {
            f.showSuccess(timer, def);
        }
    }

    private void showError(Timer timer, Deferred def){
        ProductFragment f = getCurrentFragment();
        if(f != null) {
            f.showError(timer, def);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 82) {
            this.openProductsActivity();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean needScanning() {
        return !confirmingProcess && hasBarcode;
    }
}