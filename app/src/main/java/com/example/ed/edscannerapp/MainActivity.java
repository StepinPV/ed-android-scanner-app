package com.example.ed.edscannerapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.ed.edscannerapp.entities.User;

import com.example.ed.edscannerapp.packing.OrderActivity;

public class MainActivity extends BaseActivity {

    static public final int LOGIN_ACTIVITY_CODE = 3;
    static public final int SETTINGS_ACTIVITY_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLogined();

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(MainActivity.this));
    }

    private void checkLogined(){
        AccountManager accountManager = AccountManager.getInstance();

        if(accountManager.isLogined()){
            setContentView(R.layout.activity_main);
            updateView();
        }
        else {
            this.openLoginActivity();
        }
    }

    private void updateView(){
        AccountManager accountManager = AccountManager.getInstance();

        accountManager.getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.main_user_name);
                    userNameView.setText(user.getFullName());
                } else {
                    AlertDialog.Builder builder = MainActivity.this.getDialogBuilder("Отсутствует соединение с сервером", "", null);

                    builder.setPositiveButton("Перейти в настройки", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.openSettings();
                        }
                    });

                    builder.setNegativeButton("Повторить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.updateView();
                        }
                    });

                    if (!MainActivity.this.isFinishing()) {
                        builder.create().show();
                    }
                }
            }
        });

        checkPermission();
    }

    public void openAssemblyActivity(View w){
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void logout(View w){

        AlertDialog.Builder builder = this.getDialogBuilder("Вы действительно хотите выйти?", "", null);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                AccountManager.getInstance().logout();
                openLoginActivity();
            }
        }).setNegativeButton("Отмена", null);

        if (!MainActivity.this.isFinishing()) {
            builder.create().show();
        }
    }

    public void checkProduct(View w){
        startActivity(new Intent(this, CheckActivity.class));
    }

    public void openSettings() {
        AlertDialog.Builder builder = this.getDialogBuilder("Введите код доступа", "", R.layout.barcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String barcode = textView.getText().toString();

                if(barcode.equals("2236")) {
                    startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY_CODE);
                } else {
                    MainActivity.this.updateView();
                    MainActivity.this.showErrorMessage("Неверный код доступа");
                }

                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                MainActivity.this.updateView();
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

        if (!MainActivity.this.isFinishing()) {
            dialog.show();
        }
    }

    public void openSettings(View w){
        this.openSettings();
    }

    private void openLoginActivity(){
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY_CODE);
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, },1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){

            case LOGIN_ACTIVITY_CODE:
                checkLogined();
                break;

            case SETTINGS_ACTIVITY_CODE:
                updateView();
                break;
        }
    }
}
