package com.example.ed.edscannerapp.packing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.ed.edscannerapp.CustomViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private boolean confirmingProcess;

    private String activeProductId;
    private boolean activeProductInputManual;

    private boolean weightScanningProcess;
    private AlertDialog weightScanningDialog;
    private int weightScanningCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loadProducts();
    }

    private void loadProducts(){
        manager.getProducts(new Manager.GetProductsCallback(){

            @Override
            public void success(Products products){
                setContentView(R.layout.activity_product);
                init(products);
            };

            @Override
            public void error(String message){
                showConfirm("Отсутствует соединение с сервером", "",
                        "Повторить", "Отмена", new ConfirmDialogCallback() {
                            @Override
                            public void confirm() {
                                loadProducts();
                            }

                            @Override
                            public void cancel() {
                                exit();
                            }
                        });
            };

        });
    }

    private void init(Products products){
        viewPager = (CustomViewPager) findViewById(R.id.activity_product_viewpager);
        pagerAdapter = new ProductsPagerAdapter(getSupportFragmentManager());

        if (products != null) {
            pagerAdapter.setProducts(products);
        }

        viewPager.setAdapter(pagerAdapter);

        this.initScanner();

        //Заполняем информацию о пользователе
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
                updatePositionCounter();
                updateButtons();
            }
        });

        //Обновляем компоненты на экране
        updateData();
    }

    @Override
    public void handleScanner(String value) {
        if (weightScanningProcess) {
            if (value.length() != 13 || value.charAt(0) != '2' || !value.substring(1, 7).equals("000000")) {
                showNotification("Данный штрихкод не является весовым!");
                playVibrate();

                weightScanningCount += 1;

                if (weightScanningCount >= 2) {
                    ((AlertDialog) weightScanningDialog).findViewById(R.id.scanningMessage_manual).setEnabled(true);
                }
                return;
            }

            // Формат 2 000000 XXXXX 0
            String weight = value.substring(7, 9) + "." + value.substring(9, 12);

            successBarcode(this.activeProductId, this.activeProductInputManual, weight);

            playSound();
            weightScanningDialog.cancel();

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

        showNumberInputDialog("Введите штрих код товара в поле ввода", "Ручной ввод штрихкода",
                null, null, new NumberInputDialogCallback() {
                    @Override
                    public void confirm(String value) {
                        checkProduct(false, value, true);
                    }

                    @Override
                    public void cancel() {}
                });
    }

    public void scanWeight(final String productId, final boolean manual) {
        AlertDialog.Builder builder = this.getDialogBuilder(
                "Номер штрихкода должен быть в формате: 2 000000 XXXXXX",
                "Отсканируйте вес товара", R.layout.scanning_message);

        AlertDialog dialog = builder.create();

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

        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnCancelListener(new AlertDialog.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                ProductActivity.this.weightScanningProcess = false;
                ProductActivity.this.weightScanningDialog = null;
                ProductActivity.this.weightScanningCount = 0;
            }
        });

        if (!ProductActivity.this.isFinishing()) {
            this.weightScanningProcess = true;
            this.weightScanningDialog = dialog;
            this.weightScanningCount = 0;

            this.activeProductId = productId;
            this.activeProductInputManual = manual;
            dialog.show();
        }
    }

    public void inputWeight(View w) {
        this.inputWeight();
    }

    public void inputWeight(){
        AlertDialog.Builder builder = this.getDialogBuilder("Формат ввода: x.xxx (кг)",
                "Введите вес товара", R.layout.weight);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.weight_dialog_text);
                String weight = textView.getText().toString();

                if ((weight.matches("^\\d\\d\\.\\d\\d\\d$") || weight.matches("^\\d\\.\\d\\d\\d$")) &&
                        !weight.equals("0.000") && !weight.equals("00.000")) {
                    successBarcode(activeProductId, activeProductInputManual, weight);

                    toggleKeyBoard(false);
                    ProductActivity.this.weightScanningDialog.cancel();
                } else {
                    showNotification("Вес должен быть в формате x.xxx или xx.xxx");
                    playVibrate();

                    ProductActivity.this.inputWeight();
                }
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                toggleKeyBoard(false);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                toggleKeyBoard(true);
            }
        });

        if (!ProductActivity.this.isFinishing()) {
            dialog.show();
        }
    }

    public void manualButtonHandler(View w){
        if(confirmingProcess){
            return;
        }

        showConfirm("Вы действительно хотите подтвердить товар вручную?", "Ручное подтверждение товара",
                "Подтвердить", "Отмена", new ConfirmDialogCallback() {
                    @Override
                    public void confirm() {
                        checkProduct(true, null, false);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    public void checkProduct(boolean manual, String barcode, boolean manualInputBarcode){

        Product currentProduct = ProductsHelper.getUnscannedByIndex(manager.getSavedProducts(), currentPagePosition);

        if(currentProduct == null) {
            return;
        }

        if(manual){
            if (currentProduct.hasWeight()) {
                scanWeight(currentProduct.getId(), true);
            } else {
                successBarcode(currentProduct.getId(), true, null);
            }
        }
        else {
            if(currentProduct.checkBarcode(barcode)){
                playSound();

                if (currentProduct.hasWeight()) {
                    scanWeight(currentProduct.getId(), false);
                } else {
                    successBarcode(currentProduct.getId(), false, null);
                }
            }
            else {
                playVibrate();

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

    /**
     * Обновить компоненты на экране
     */
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