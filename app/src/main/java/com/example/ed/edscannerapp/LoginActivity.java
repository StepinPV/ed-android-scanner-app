package com.example.ed.edscannerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BaseActivity {

    private EditText loginText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginText = (EditText) findViewById(R.id.login_login);
        passwordText = (EditText) findViewById(R.id.login_password);

        Settings.setServerIP("1", null);
    }

    public void login(View w){
        if(checkValidation()){
            this.login();
        }
    }

    public void openSettings(View w){
        AlertDialog.Builder builder = this.getDialogBuilder("Введите код доступа", "", R.layout.barcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String barcode = textView.getText().toString();

                if(barcode.equals("2236")) {
                    startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                } else {
                    LoginActivity.this.showErrorMessage("Неверный код доступа");
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
                LoginActivity.this.showErrorMessage(message);
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
}
