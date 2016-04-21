package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCallListener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkService.getInstance() == null) {
            context.startService(new Intent(context.getApplicationContext(), NetworkService.class));
        }

    }

}
