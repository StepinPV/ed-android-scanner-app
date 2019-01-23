package com.example.ed.edscannerapp;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Storage.initInstance(this.getApplicationContext());
        AccountManager.initInstance();
    }

}
