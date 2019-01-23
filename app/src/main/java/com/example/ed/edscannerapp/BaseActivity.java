package com.example.ed.edscannerapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public AlertDialog.Builder getDialogBuilder(String message, String title, Integer templateId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.minilogo)
                .setCancelable(false);

        if(templateId != null){
            LayoutInflater inflater = this.getLayoutInflater();
            builder.setView(inflater.inflate(templateId, null));
        }

        return builder;
    }

    public void showMessage(String title, String message){
        if (this.isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = this.getDialogBuilder(message, title, null);
        builder.setPositiveButton("ОК", null);
        builder.create().show();
    }

    public void showErrorMessage(String message){
        showMessage("Ошибка!", message);
    }
}
