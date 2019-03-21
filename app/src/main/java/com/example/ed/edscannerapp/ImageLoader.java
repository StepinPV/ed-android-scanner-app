package com.example.ed.edscannerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/*
* Загрузчик изображений
* Загружает изображение и внедряет его в переданный view
* */
public class ImageLoader extends AsyncTask<String, String, Bitmap> {
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
            // e.printStackTrace();
        }

        return image;
    }

    protected void onPostExecute(Bitmap image) {
        if(image != null){
            imageView.setImageBitmap(image);
        }
    }
}

