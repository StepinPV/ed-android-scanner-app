package com.example.ed.edscannerapp;

import com.example.ed.edscannerapp.entities.InfoResponse;
import com.example.ed.edscannerapp.entities.User;
import com.example.ed.edscannerapp.entities.VerificationResponse;
import com.example.ed.edscannerapp.server.BL;

import java.security.MessageDigest;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Менеджер по работе с пользователем
 * Хранит информацию о пользователе
 */
public class AccountManager {
    static private AccountManager instance;
    private Storage storage;

    //Ключи, для хранения данных в storage
    private final String LOGIN_KEY = "account_manager_login";
    private final String PASSWORD_KEY = "account_manager_password";
    private final String SALT_KEY = "account_manager_salt";
    private final String SIG_KEY = "account_manager_sig";

    private String login = null;
    private String password = null;
    private String salt = null;
    private String sig = null;
    private User user = null;

    private AccountManager(){
        storage = Storage.getInstance();

        String login = storage.getString(LOGIN_KEY);

        if(login != null){
            this.login = login;
            this.password = storage.getString(PASSWORD_KEY);
            this.salt = storage.getString(SALT_KEY);
            this.sig = storage.getString(SIG_KEY);
        }
    }

    public static void initInstance() {
        if (instance == null) {
            instance = new AccountManager();
        }
    }

    public static AccountManager getInstance() {
        return instance;
    }

    public boolean isAuthorized(){
        return login != null;
    }

    public String getLogin(){
        return login;
    }

    public String getSalt(){
        return salt;
    }

    public String getSig(){
        return sig;
    }

    interface LoginCallback {
        void success();
        void error(String message);
    }

    /**
     * Функция авторизации
     * @param login
     * @param password
     * @param callback
     */
    public void login(final String login, final String password, final LoginCallback callback){

        Random rand = new Random();

        Integer _salt = rand.nextInt(98000) + 1000;

        final String salt = _salt.toString();
        final String sig = sha256(salt + login + password);

        BL.verification(login, salt, sig).enqueue(new Callback<VerificationResponse>() {
            @Override
            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                if (response.isSuccessful()) {
                    VerificationResponse verification = response.body();

                    if(verification.isSuccessful()){
                        setLogin(login);
                        setPassword(password);
                        setSalt(salt);
                        setSig(sig);

                        callback.success();
                    }
                    else {
                        callback.error(verification.getMessage());
                    }

                } else {
                    callback.error("Отсутствует соединение с сервером!");
                }
            }

            @Override
            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                callback.error(t.getMessage() != null ? t.getMessage() : "Отсутствует соединение с сервером!");
            }
        });
    }

    public interface UserCallback {
        void success(User user);
    }

    /**
     * Получить информацию о авторизованном пользователе.
     * @param callback
     */
    public void getUser(final UserCallback callback){

        if(this.user != null) {
            callback.success(user);
        }

        BL.info(this.login, this.salt, this.sig).enqueue(new Callback<InfoResponse>() {
            @Override
            public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                if (response.isSuccessful()) {
                    InfoResponse info = response.body();

                    if(info.isSuccessful()){
                        User user = info.getUser();

                        //Сохраняем пользователя в кэш
                        AccountManager.this.user = user;

                        callback.success(user);
                    }
                    else {
                        callback.success(null);
                    }

                } else {
                    callback.success(null);
                }
            }

            @Override
            public void onFailure(Call<InfoResponse> call, Throwable t) {
                callback.success(null);
            }
        });
    }

    public void logout(){
        this.setLogin(null);
        this.setPassword(null);
        this.setSalt(null);
        this.setSig(null);
    }

    private void setLogin(String login){
        this.login = login;
        storage.setString(LOGIN_KEY, login);
    }

    private void setSalt(String salt){
        this.salt = salt;
        storage.setString(SALT_KEY, salt);
    }

    private void setSig(String sig){
        this.sig = sig;
        storage.setString(SIG_KEY, sig);
    }

    private void setPassword(String password){
        this.password = password;
        storage.setString(PASSWORD_KEY, password);
    }

    private String sha256(String value) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        md.update(value.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
