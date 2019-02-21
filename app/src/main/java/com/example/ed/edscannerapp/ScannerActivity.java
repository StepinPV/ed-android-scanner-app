package com.example.ed.edscannerapp;

import android.os.Bundle;
import android.view.KeyEvent;

public abstract class ScannerActivity extends BaseActivity {

    public BarcodeScanner scanner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 139) {
            if(scanner != null && event.getRepeatCount() == 0 && this.needScanning()) {
                scanner.startScan();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == 139){
            if(scanner != null && event.getRepeatCount() == 0) {
                scanner.stopScan();
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDestroy(){
        this.destroyScanner();
        super.onDestroy();
    }

    public void initScanner() {
        if(scanner == null) {
            scanner = new BarcodeScanner(this, new BarcodeScanner.ScanCallback() {
                @Override
                public void success(String barcode) {
                    ScannerActivity.this.handleScanner(barcode);
                }
            });
        }
    }

    public void destroyScanner() {
        if(scanner != null) {
            scanner.destroy();
            scanner = null;
        }
    }

    public void handleScanner(String barcode){};
    public boolean needScanning(){ return true; }
}
