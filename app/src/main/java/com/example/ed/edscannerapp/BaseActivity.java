package com.example.ed.edscannerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Класс базового экрана
 * содержит в себе методы для показа диалоговых и информационных окон
 * содержит в себе метод для показа/скрытия клавиатуры
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Показать сообщение
     */
    public void showMessage(String title, String message){
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = this.getDialogBuilder(message, title, null);
        builder.setPositiveButton("ОК", null);
        builder.create().show();
    }

    /**
     * Показать сообщение об ошибке
     */
    public void showErrorMessage(String message){
        showMessage("Ошибка!", message);
    }

    /**
     * Показать нотификацию
     */
    public void showNotification(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Изменить видимость клавиатуры
     */
    public void toggleKeyBoard(boolean show) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;

        imm.toggleSoftInput(show ? InputMethodManager.SHOW_FORCED : InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public interface NumberInputDialogCallback {
        void confirm(String value);
        void cancel();
    }

    /**
     * Показать диалог ввода числа
     */
    public void showNumberInputDialog(String message, String title, String positiveText, String negativeText, final NumberInputDialogCallback callback){
        AlertDialog.Builder builder = this.getDialogBuilder(message, title, R.layout.barcode);

        builder.setPositiveButton(positiveText == null ? "Подтвердить" : positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TextView textView = ((AlertDialog) dialog).findViewById(R.id.activity_product_barcode);
                String value = textView.getText().toString();

                callback.confirm(value);

                toggleKeyBoard(false);
            }
        }).setNegativeButton(negativeText == null ? "Отмена" : negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                callback.cancel();

                toggleKeyBoard(false);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                toggleKeyBoard(true);
            }
        });

        if (!isFinishing()) {
            dialog.show();
        }
    }

    public interface ConfirmDialogCallback {
        void confirm();
        void cancel();
    }

    /**
     * Показать диалог подтверждения
     */
    public void showConfirm(String message, String title, String positiveText, String negativeText, final ConfirmDialogCallback callback) {
        AlertDialog.Builder builder = this.getDialogBuilder(message, title, null);

        if (positiveText != null) {
            builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    callback.confirm();
                }
            });
        }

        if (negativeText != null) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    callback.cancel();
                }
            });
        }

        if (!isFinishing()) {
            builder.create().show();
        }
    }

    /**
     * Получить диалог билдер
     */
    public AlertDialog.Builder getDialogBuilder(String message, String title, Integer templateId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.minilogo)
                .setCancelable(false);

        if(templateId != null){
            LayoutInflater inflater = this.getLayoutInflater();
            builder.setView(inflater.inflate(templateId, null));
        }

        return builder;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Перехватыем клавиши включения сканера, чтобы он не включался там, где не нужно
        return keyCode == 139 || super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Перехватыем клавиши включения сканера, чтобы он не включался там, где не нужно
        return keyCode == 139 || super.onKeyUp(keyCode, event);

    }
}
