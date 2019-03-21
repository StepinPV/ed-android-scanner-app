package com.example.ed.edscannerapp;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;

/**
 * Класс экрана со сканнером
 * содержит методы управления сканером
 * содержит методы для запуска вибрации и звука, успешного сканирования
 */
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

    /**
     * Запустить вибрацию, обычно используется при ошибке сканирования
     */
    public void playVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(ScannerActivity.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(300);
        }
    }

    /**
     * Запустить звук, обычно используется при успешном сканировании
     */
    public void playSound() {
        SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        int soundID = soundPool.load(this, R.raw.scan,1);

        soundPool.play(soundID, 1, 1,1,0, 1f);
    }
}
