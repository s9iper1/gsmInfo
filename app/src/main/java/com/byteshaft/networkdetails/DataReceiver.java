package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class DataReceiver extends BroadcastReceiver {

    private TelephonyManager dataManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        dataManager.listen(new DataListener(context),
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_DATA_ACTIVITY);
    }
}
