package com.example.ed.edscannerapp;

import com.example.ed.edscannerapp.server.BL;

import java.util.regex.Pattern;

public class Settings {

    private static final String IP_TYPE_KEY = "settings_server_ip_type";
    private static final String IP_KEY = "settings_server_ip_ip";
    private static final String PORT_KEY = "settings_server_ip_port";

    private static String FIRST_SERVER_IP = "https://esh-derevenskoe.ru/";
    private static String SECOND_SERVER_IP = "https://backup.esh-derevenskoe.ru/";
    private static String IP_ADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static String getServerAddress() {
        Storage storage = Storage.getInstance();
        String serverIPType = storage.getString(IP_TYPE_KEY);

        if (serverIPType == null || serverIPType.equals("1")) {
            return FIRST_SERVER_IP;
        }

        if (serverIPType.equals("2")) {
            return SECOND_SERVER_IP;
        }

        String serverIP = storage.getString(IP_KEY);
        String serverPort = storage.getString(PORT_KEY);

        return "http://" + serverIP + ":" + serverPort + "/";
    }

    public static String getServerIP() {
        Storage storage = Storage.getInstance();
        return storage.getString(IP_KEY);
    }

    public static String getServerPort() {
        Storage storage = Storage.getInstance();
        return storage.getString(PORT_KEY);
    }

    public static String getServerIPType() {
        Storage storage = Storage.getInstance();

        String serverIPType = storage.getString(IP_TYPE_KEY);

        if (serverIPType == null) {
            return "1";
        }

        return serverIPType;
    }

    public static void setServerIP(String type, String serverIP, String serverPort) {
        Storage storage = Storage.getInstance();

        if (type == null || type.equals("1")) {
            storage.setString(IP_TYPE_KEY, "1");
        } else if (type.equals("2")) {
            storage.setString(IP_TYPE_KEY, "2");
        } else if (serverIP != null){
            Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

            if (pattern.matcher(serverIP).matches()) {
                storage.setString(IP_TYPE_KEY, "3");
                storage.setString(IP_KEY, serverIP);
                storage.setString(PORT_KEY, (serverPort.equals("") || serverPort == null) ? "80" : serverPort);
            }
        }

        BL.updateServerAPIInst();
    }
}
