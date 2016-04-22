package com.byteshaft.networkdetails;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataListener extends PhoneStateListener {

    private Context mContext;
    private String LOG_TAG = "DataListener";
    private boolean dataState = false;

    public DataListener(Context context) {
        mContext = context;
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        super.onDataConnectionStateChanged(state, networkType);
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                if (NetworkService.getInstance() == null) {
                    mContext.startService(new Intent(mContext.getApplicationContext(), NetworkService.class));
                }
                AppGlobals.CURRENT_STATE = AppGlobals.suspend;
                if (NetworkService.getInstance() != null) {
                    if (dataState) {
                        NetworkService.getInstance().startLocationUpdate();
                        dataState = false;
                    }
                }
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_DISCONNECTED");
                break;
            case TelephonyManager.DATA_CONNECTING:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTING");
                break;
            case TelephonyManager.DATA_CONNECTED:
                dataState = true;
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTED");
                break;
            case TelephonyManager.DATA_SUSPENDED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_SUSPENDED");
                AppGlobals.CURRENT_STATE = AppGlobals.suspend;
                NetworkService.getInstance().startLocationUpdate();
                break;
            default:
                Log.w(LOG_TAG, "onDataConnectionStateChanged: UNKNOWN " + state);
                AppGlobals.CURRENT_STATE = AppGlobals.suspend;
                NetworkService.getInstance().startLocationUpdate();
                break;
        }
    }
}
