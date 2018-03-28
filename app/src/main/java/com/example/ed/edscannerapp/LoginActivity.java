package com.example.ed.edscannerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.ed.edscannerapp.entities.Order;

public class LoginActivity extends AppCompatActivity {

    private EditText loginText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginText = (EditText) findViewById(R.id.login_login);
        passwordText = (EditText) findViewById(R.id.login_password);
    }

    public void login(View w){
        if(checkValidation()){
            this.login();
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
            public void error(){
                showError();
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

    private void showError(){
        AlertDialog.Builder builder = Helper.getDialogBuilder(this,
                "Не правильный логин или пароль!",
                "", null);

        builder.setPositiveButton("ОК", null);

        builder.create().show();
    }
}
