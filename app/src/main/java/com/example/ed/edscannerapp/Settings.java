package com.example.ed.edscannerapp;

import com.example.ed.edscannerapp.server.BL;

/**
 * Класс для работы с настройками приложения
 * Содержит в себе данные IP сервера и методы для его изменения
 */
public class Settings {

    public static String FIRST_SERVER = "https://esh-derevenskoe.ru/";
    public static String SECOND_SERVER = "https://backup.esh-derevenskoe.ru/";
    private static final String SERVER_KEY = "settings_server";

    /**
     * Возвращает выбранный сервер
     * @return
     */
    public static String getServer() {
        Storage storage = Storage.getInstance();
        String server = storage.getString(SERVER_KEY);

        return server != null ? server : FIRST_SERVER;
    }

    /**
     * Изменить сервер
     * @return
     */
    public static void setServer(String server) {
        Storage storage = Storage.getInstance();

        //Удаляем лишние пробелы
        server = server.trim().replaceAll("\\s+","");
        //Подставляем при необходимости, слэш в конце
        server = server.charAt(server.length() - 1) == '/' ? server : server + "/";
        //Подставляем протокол, если его нет
        server = server.indexOf("http") != 0 ? "https://" + server : server;

        storage.setString(SERVER_KEY, server);
        BL.updateServerAPIInst();
    }

    /**
     * Сбросить настройки сервера
     * @return
     */
    public static void resetServer() {
        setServer(FIRST_SERVER);
    }
}
