package com.example.ed.edscannerapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Storage {

    static private Storage instance;

    private SharedPreferences sharedPref;
    private Activity activity;

    private Storage(Activity activity){
        this.activity = activity;
        this.sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
    }

    public String getString(int id){
        return sharedPref.getString(activity.getString(id), null);
    }

    public void setString(int id, String value){
        sharedPref.edit().putString(activity.getString(id), value).commit();
    }

    public static void initInstance(Activity activity) {
        if (instance == null) {
            instance = new Storage(activity);
        }
    }

    public static Storage getInstance() {
        return instance;
    }

}
