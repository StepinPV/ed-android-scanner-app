package com.example.ed.edscannerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ed.edscannerapp.packing.ProductsActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText loginText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginText = (EditText) findViewById(R.id.login_login);
        passwordText = (EditText) findViewById(R.id.login_password);

        Settings.setServerIP("1", null, null);
    }

    public void login(View w){
        if(checkValidation()){
            this.login();
        }
    }

    public void openSettings(View w){
        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Введите код доступа", "", R.layout.barcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String barcode = textView.getText().toString();

                if(barcode.equals("2236")) {
                    startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                } else {
                    Helper.showErrorMessage(LoginActivity.this, "Неверный код доступа");
                }

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

        if (!LoginActivity.this.isFinishing()) {
            dialog.show();
        }
    }

    private void login(){
        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();

        AccountManager accountManager = AccountManager.getInstance();

        accountManager.login(login, password, new AccountManager.LoginCallback(){

            @Override
            public void success(){
                finish();
            };

            @Override
            public void error(String message){
                showError(message);
            };

        });
    }

    private void exit(){
        finish();
    }

    private boolean checkValidation(){
        if(loginText.getText().toString().isEmpty()){
            loginText.setError("Поле не может быть пустым!");
            return false;
        }

        if(passwordText.getText().toString().isEmpty()){
            passwordText.setError("Поле не может быть пустым!");
            return false;
        }

        return true;
    }

    private void showError(String message){
        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                message, "", null);

        builder.setPositiveButton("ОК", null);



        if (!LoginActivity.this.isFinishing()) {
            builder.create().show();
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
