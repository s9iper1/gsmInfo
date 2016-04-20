package com.byteshaft.networkdetails;

import android.app.Application;
import android.content.Context;

public class AppGlobals  extends Application {

    private static Context sContext;
    public static final String SCHEDULE  = "SCHEDULED";
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
