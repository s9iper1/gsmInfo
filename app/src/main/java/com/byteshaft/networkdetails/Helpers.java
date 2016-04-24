package com.byteshaft.networkdetails;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class Helpers {

    // ping the google server to check if internet is really working or not
    public static boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                AppGlobals.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String getTimeStamp() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }
    // get default sharedPreferences.
    private static SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(AppGlobals.getContext());
    }

    public static void saveGsmDetails(String key, String  value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getGsmDetails(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(key, "");
    }

    public static void removeGsmDetails(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        if (sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).apply();
        }

    }

    public static void saveHashSet(Set<String> value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putStringSet(AppGlobals.KEY, value).apply();
    }

    public static Set<String> getHashSet() {
        Set<String> set = new HashSet<>();
        set.add("");
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getStringSet(AppGlobals.KEY, set);
    }

    public static String getNetworkType(int type) {
        switch (type) {
            case 7:
                return  "1xRTT";
            case 4:
                return "CDMA";
            case 2:
                return "EDGE";
            case 14:
                return "eHRPD";
            case 5:
                return "EVDO rev. 0";
            case 6:
                return "EVDO rev. A";
            case 12:
                return "EVDO rev. B";
            case 1:
                return "GPRS";
            case 8:
                return "HSDPA";
            case 10:
                return "HSPA";
            case 15:
                return "HSPA+";
            case 9:
                return "HSUPA";
            case 11:
                return "iDen";
            case 13:
                return "LTE";
            case 3:
                return "UMTS";
            case 0:
                return "Unknown";
            default:
                return "Unknown";
        }
    }
}
