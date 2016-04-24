package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GsmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkService.getInstance() == null) {
            Intent intent1 = new Intent(context, NetworkService.class);
            intent1.putExtra(AppGlobals.SEND_BROAD_CAST, true);
            context.startService(intent1);
        }
        if (NetworkService.getInstance() != null) {
            NetworkService.getInstance().startLocationUpdate();
        }
        AppGlobals.SCHEDULE_STATE = true;
        AppGlobals.CURRENT_STATE = AppGlobals.schedule;

    }
}
