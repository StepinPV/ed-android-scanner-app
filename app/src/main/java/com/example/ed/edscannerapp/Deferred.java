package com.example.ed.edscannerapp;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class Deferred extends TimerTask {

    public interface Callback {
        void success();
    }

    private Activity activity;

    public Deferred(Activity activity){
        this.activity = activity;
    }

    private List<Callback> callbacks = new ArrayList<>();

    public void addCallback(final Callback callback){
        callbacks.add(0, callback);
    }

    @Override
    public void run() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Callback callback: callbacks) {
                    callback.success();
                }
            }
        });
    }
}
