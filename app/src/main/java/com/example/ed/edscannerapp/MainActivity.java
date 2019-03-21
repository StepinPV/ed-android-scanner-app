package com.example.ed.edscannerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ed.edscannerapp.entities.User;

import com.example.ed.edscannerapp.packing.OrderActivity;

/**
 * Экран главной страницы (разводящей)
 */
public class MainActivity extends BaseActivity {

    static public final int LOGIN_ACTIVITY_CODE = 3;
    static public final int SETTINGS_ACTIVITY_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAuthorized();
    }

    private void checkAuthorized(){
        AccountManager accountManager = AccountManager.getInstance();

        if(accountManager.isAuthorized()){
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
                    showConfirm("Отсутствует соединение с сервером", "",
                            "Перейти в настройки", "Повторить", new ConfirmDialogCallback() {
                                @Override
                                public void confirm() {
                                    openSettingsActivity();
                                }

                                @Override
                                public void cancel() {
                                    updateView();
                                }
                            });
                }
            }
        });

        checkPermission();
    }

    public void openOrderActivity(View w){
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void openCheckActivity(View w){
        startActivity(new Intent(this, CheckActivity.class));
    }

    private void openLoginActivity(){
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY_CODE);
    }

    public void openSettingsActivity(View w){
        this.openSettingsActivity();
    }

    public void openSettingsActivity() {
        showNumberInputDialog("Введите код доступа", "",
                null, null, new NumberInputDialogCallback() {
                    @Override
                    public void confirm(String value) {
                        if(value.equals(getString(R.string.settings_code))) {
                            startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY_CODE);
                        } else {
                            updateView();
                            showErrorMessage("Неверный код доступа");
                        }
                    }

                    @Override
                    public void cancel() {}
                });
    }

    public void logout(View w){

        showConfirm("Вы действительно хотите выйти?", "",
                "Подтвердить", "Отмена", new ConfirmDialogCallback() {
                    @Override
                    public void confirm() {
                        AccountManager.getInstance().logout();
                        openLoginActivity();
                    }

                    @Override
                    public void cancel() {
                        updateView();
                    }
                });
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
                checkAuthorized();
                break;

            case SETTINGS_ACTIVITY_CODE:
                updateView();
                break;
        }
    }
}
