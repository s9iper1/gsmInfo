package com.byteshaft.networkdetails;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by s9iper1 on 4/20/16.
 */
public class DataListener extends PhoneStateListener {

    private Context mContext;
    private String LOG_TAG = "LOG";

    public DataListener(Context context) {
        mContext = context;
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        super.onDataConnectionStateChanged(state, networkType);
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_DISCONNECTED");
                break;
            case TelephonyManager.DATA_CONNECTING:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTING");
                break;
            case TelephonyManager.DATA_CONNECTED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_CONNECTED");
                break;
            case TelephonyManager.DATA_SUSPENDED:
                Log.i(LOG_TAG, "onDataConnectionStateChanged: DATA_SUSPENDED");
                AppGlobals.CURRENT_STATE = AppGlobals.suspend;
                NetworkService.getInstance().getNetworkDetails();
                break;
            default:
                Log.w(LOG_TAG, "onDataConnectionStateChanged: UNKNOWN " + state);
                AppGlobals.CURRENT_STATE = AppGlobals.suspend;
                NetworkService.getInstance().getNetworkDetails();
                break;
        }
    }
}
