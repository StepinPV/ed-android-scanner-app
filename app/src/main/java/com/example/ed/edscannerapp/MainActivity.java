package com.example.ed.edscannerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ed.edscannerapp.packing.OrderActivity;
import com.example.ed.edscannerapp.packing.OrdersActivity;

public class MainActivity extends AppCompatActivity {

    static public final int LOGIN_ACTIVITY_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager.initInstance(this);
        AccountManager accountManager = AccountManager.getInstance();

        if(accountManager.isLogined()){
            initView();
        }
        else {
            this.openLoginActivity();
        }

    }

    private void initView(){
        setContentView(R.layout.activity_main);

        TextView userNameView = (TextView) findViewById(R.id.main_user_name);
        userNameView.setText(AccountManager.getInstance().getLogin());

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

    public void openAssemblyActivity(View w){
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void logout(View w){
        AccountManager.getInstance().logout();
        openLoginActivity();
    }

    private void openLoginActivity(){
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode){

            case LOGIN_ACTIVITY_CODE:
                this.initView();
                break;
        }
    }
}
