package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GsmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkService.getInstance() == null) {
            context.startService(new Intent(context.getApplicationContext(), NetworkService.class));
        }
        NetworkService.getInstance().startLocationUpdate();
        AppGlobals.SCHEDULE_STATE = true;
        AppGlobals.CURRENT_STATE = AppGlobals.schedule;

    }
}
