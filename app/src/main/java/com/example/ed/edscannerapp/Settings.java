package com.example.ed.edscannerapp;

import com.example.ed.edscannerapp.server.BL;

import java.util.regex.Pattern;

public class Settings {

    private static String FIRST_SERVER_IP = "https://esh-derevenskoe.ru/";
    private static String SECOND_SERVER_IP = "https://backup.esh-derevenskoe.ru/";
    private static String IP_ADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static String getServerAddress() {
        Storage storage = Storage.getInstance();
        String serverIPType = storage.getString(R.string.settings_server_ip_type);

        if (serverIPType == null || serverIPType.equals("1")) {
            return FIRST_SERVER_IP;
        }

        if (serverIPType.equals("2")) {
            return SECOND_SERVER_IP;
        }

        String serverIP = storage.getString(R.string.settings_server_ip_ip);
        String serverPort = storage.getString(R.string.settings_server_ip_port);

        return "http://" + serverIP + ":" + serverPort + "/";
    }

    public static String getServerIP() {
        Storage storage = Storage.getInstance();
        return storage.getString(R.string.settings_server_ip_ip);
    }

    public static String getServerPort() {
        Storage storage = Storage.getInstance();
        return storage.getString(R.string.settings_server_ip_port);
    }

    public static String getServerIPType() {
        Storage storage = Storage.getInstance();

        String serverIPType = storage.getString(R.string.settings_server_ip_type);

        if (serverIPType == null) {
            return "1";
        }

        return serverIPType;
    }

    public static void setServerIP(String type, String serverIP, String serverPort) {
        Storage storage = Storage.getInstance();

        if (type == null || type.equals("1")) {
            storage.setString(R.string.settings_server_ip_type, "1");
        } else if (type.equals("2")) {
            storage.setString(R.string.settings_server_ip_type, "2");
        } else if (serverIP != null){
            Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

            if (pattern.matcher(serverIP).matches()) {
                storage.setString(R.string.settings_server_ip_type, "3");
                storage.setString(R.string.settings_server_ip_ip, serverIP);
                storage.setString(R.string.settings_server_ip_port, (serverPort.equals("") || serverPort == null) ? "80" : serverPort);
            }
        }

        BL.updateServerAPIInst();
    }
}
