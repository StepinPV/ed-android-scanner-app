package com.example.ed.edscannerapp;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context appContext = this.getApplicationContext();

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(appContext));

        //Инстанцируем синглтоны
        Storage.initInstance(appContext);
        AccountManager.initInstance();
    }

}
