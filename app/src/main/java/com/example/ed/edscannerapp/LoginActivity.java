package com.example.ed.edscannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/*
* Окно авторизации
* */
public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // При первом открытии окна авторизации,
        // сбрасываем значение адреса сервера на значение по умолчанию
        Settings.resetServer();
    }

    public void login(View w){
        if(validate()){
            this.login();
        }
    }

    private void login(){
        String login = ((EditText) findViewById(R.id.login_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.login_password)).getText().toString();

        AccountManager accountManager = AccountManager.getInstance();

        accountManager.login(login, password, new AccountManager.LoginCallback(){

            @Override
            public void success(){
                finish();
            };

            @Override
            public void error(String message){
                showErrorMessage(message);
            };

        });
    }

    public void openSettings(View w){
        showNumberInputDialog("Введите код доступа", "",
                null, null, new NumberInputDialogCallback() {
                    @Override
                    public void confirm(String value) {
                        if(value.equals(getString(R.string.settings_code))) {
                            startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                        } else {
                            showErrorMessage("Неверный код доступа");
                        }
                    }

                    @Override
                    public void cancel() {}
                });
    }

    private boolean validate(){
        EditText loginText = ((EditText) findViewById(R.id.login_login));
        EditText passwordText = ((EditText) findViewById(R.id.login_password));

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
