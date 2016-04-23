package com.byteshaft.networkdetails;

import android.app.Application;
import android.content.Context;

public class AppGlobals  extends Application {

    private static Context sContext;
    public static String CURRENT_STATE = "";
    public static String schedule = "SCHEDULED";
    public static String call_dropped = "CALL_DROPPED";
    public static String suspend = "DATA_SUSPEND";
    public static final String KEY = "hash_set";
    public static String LOCATION = "null";
    public static boolean SCHEDULE_STATE = false;
    public static final String SEND_BROAD_CAST = "send_broad_cast";

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
