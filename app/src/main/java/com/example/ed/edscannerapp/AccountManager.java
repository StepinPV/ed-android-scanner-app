package com.example.ed.edscannerapp;

import com.example.ed.edscannerapp.entities.InfoResponse;
import com.example.ed.edscannerapp.entities.User;
import com.example.ed.edscannerapp.entities.VerificationResponse;
import com.example.ed.edscannerapp.server.BL;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.ed.edscannerapp.Helper.sha256;

public class AccountManager {
    static private AccountManager instance;
    private Storage storage;

    private String login = null;
    private String password = null;
    private String salt = null;
    private String sig = null;
    private User user = null;

    private AccountManager(){
        storage = Storage.getInstance();

        String login = storage.getString(R.string.account_manager_login);

        if(login != null){
            this.login = login;
            this.password = storage.getString(R.string.account_manager_password);
            this.salt = storage.getString(R.string.account_manager_salt);
            this.sig = storage.getString(R.string.account_manager_sig);
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

    public boolean isLogined(){
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
                        saveData(login, password, salt, sig);
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
                callback.error("Отсутствует соединение с сервером!");
            }
        });
    }

    public interface UserCallback {
        void success(User user);
    }

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
                        saveUser(info.getUser());
                        callback.success(info.getUser());
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
        saveData(null, null, null, null);
    }

    private void saveUser(User user){
        this.user = user;
    }

    private void saveData(String login, String password, String salt, String sig){
        this.setLogin(login);
        this.setPassword(password);
        this.setSalt(salt);
        this.setSig(sig);
    }

    private void setLogin(String login){
        this.login = login;
        storage.setString(R.string.account_manager_login, login);
    }

    private void setSalt(String salt){
        this.salt = salt;
        storage.setString(R.string.account_manager_salt, salt);
    }

    private void setSig(String sig){
        this.sig = sig;
        storage.setString(R.string.account_manager_sig, sig);
    }

    private void setPassword(String password){
        this.password = password;
        storage.setString(R.string.account_manager_password, password);
    }

}
