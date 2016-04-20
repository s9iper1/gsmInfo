package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GsmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppGlobals.SCHEDULE_STATE = true;
        NetworkService.getInstance().getNetworkDetails();
        AppGlobals.CURRENT_STATE = AppGlobals.schedule;

    }
}
