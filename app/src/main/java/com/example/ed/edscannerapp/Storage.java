package com.example.ed.edscannerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Storage {

    static private Storage instance;

    private SharedPreferences sharedPref;

    private Storage(Context context){
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getString(String key){
        return sharedPref.getString(key, null);
    }

    public void setString(String key, String value){
        sharedPref.edit().putString(key, value).commit();
    }

    public static void initInstance(Context context) {
        if (instance == null) {
            instance = new Storage(context);
        }
    }

    public static Storage getInstance() {
        return instance;
    }

}
