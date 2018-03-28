package com.example.ed.edscannerapp;

import android.content.Intent;
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

//Сканирование в ручную
