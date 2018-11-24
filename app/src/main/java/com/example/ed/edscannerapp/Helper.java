package com.example.ed.edscannerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.example.ed.edscannerapp.packing.ProductActivity;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class Helper {
    private Helper(){}

    static public String sha256(String val) {
        String password = val;
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        md.update(password.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    static public AlertDialog.Builder getDialogBuilder(Activity activity, String message, String title, Integer templateId){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.minilogo)
                .setCancelable(false);

        if(templateId != null){
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(templateId, null));
        }

        return builder;
    }

    static public void showMessage(Activity activity, String title, String message){
        if (activity.isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = Helper.getDialogBuilder(activity,
                message,
                title, null);

        builder.setPositiveButton("ОК", null);

        builder.create().show();
    }

    static public void showErrorMessage(Activity activity, String message){
        showMessage(activity, "Ошибка!", message);
    }

    static public class ImageLoader extends AsyncTask<String, String, Bitmap> {
        ImageView imageView;

        public ImageLoader(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            Bitmap image = null;

            try {
                image = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return image;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                imageView.setImageBitmap(image);
            }
        }
    }

    public interface DeferredCallback {
        void success();
    }

    static public class Deferred extends TimerTask {

        private Activity activity;

        public Deferred(Activity activity){
            this.activity = activity;
        }

        private List<DeferredCallback> callbacks = new ArrayList<>();

        public void addCallback(final DeferredCallback callback){
            callbacks.add(0, callback);
        }

        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (DeferredCallback callback: callbacks) {
                        callback.success();
                    }
                }
            });
        }
    }
}
