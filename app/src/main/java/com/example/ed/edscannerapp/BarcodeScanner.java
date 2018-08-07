package com.example.ed.edscannerapp;

import android.app.Activity;
import android.os.AsyncTask;

import com.zebra.adc.decoder.Barcode2DWithSoft;

public class BarcodeScanner {

    private Barcode2DWithSoft barcode2DWithSoft;
    private Activity activity;
    private ScanCallback scanCallback;
    private boolean scanning = false;

    public interface ScanCallback {
        void success(String barcode);
    }

    public BarcodeScanner(Activity activity, ScanCallback scanCallback){
        this.activity = activity;
        this.scanCallback = scanCallback;

        new InitTask().execute();
    }

    public void startScan(){
        if(barcode2DWithSoft!=null) {
            barcode2DWithSoft.scan();
            barcode2DWithSoft.setScanCallback(scanBack);
            scanning = true;
        }
    }

    public void stopScan() {
        if (barcode2DWithSoft != null && scanning){
            barcode2DWithSoft.stopScan();
            scanning = false;
        }
    }

    public void destroy(){
        if (barcode2DWithSoft != null){
            barcode2DWithSoft.stopScan();
            barcode2DWithSoft.close();
        }
    }

    public Barcode2DWithSoft.ScanCallback scanBack = new Barcode2DWithSoft.ScanCallback(){
        @Override
        public void onScanComplete(int i, int length, byte[] bytes) {
            if (length < 1) {
                if (length == -1) {
                    //Scan cancel
                } else if (length == 0) {
                    //Scan TimeOut
                } else {
                    //Scan fail
                }
            }else{
                String barCode = new String(bytes, 0, length);
                scanCallback.success(barCode);
            }
        }
    };

    public class InitTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            if(barcode2DWithSoft == null){
                barcode2DWithSoft = Barcode2DWithSoft.getInstance();
            }

            boolean result = false;

            if(barcode2DWithSoft != null) {
                result = barcode2DWithSoft.open(activity);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                barcode2DWithSoft.setParameter(324, 1);
                barcode2DWithSoft.setParameter(300, 0); // Snapshot Aiming
                barcode2DWithSoft.setParameter(361, 0); // Image Capture Illumination

                // interleaved 2 of 5
                barcode2DWithSoft.setParameter(6, 1);
                barcode2DWithSoft.setParameter(22, 0);
                barcode2DWithSoft.setParameter(23, 55);

            }else{
                //TODO
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }
}
