package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallListener extends BroadcastReceiver {

    private boolean inComingCall = false;
    private boolean outGoingCall = false;
    private boolean calledAttended = false;
    private boolean calledOnce = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkService.getInstance() == null) {
            context.startService(new Intent(context.getApplicationContext(), NetworkService.class));
        }
            TelephonyManager tmgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        //Create Listner
        MyPhoneStateListener PhoneListener = new MyPhoneStateListener();

        // Register listener for LISTEN_CALL_STATE
//        tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    inComingCall = true;
                    Log.i("Ringing: ", "New Phone Call Event. Incomming Number : " + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (inComingCall) {
                        calledAttended = true;
                    }
                    outGoingCall = true;
                    Log.i("OFFHOOK: ", "New Phone Call Event. Incomming Number : " + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (inComingCall && calledAttended || outGoingCall) {
                        if (NetworkService.getInstance() != null) {
                            if (!calledOnce) {
                                calledOnce = true;
                                Log.i("IDLE: ", "state idle ");
                                AppGlobals.CURRENT_STATE = AppGlobals.call_dropped;
                                NetworkService.getInstance().startLocationUpdate();
                                inComingCall = false;
                                outGoingCall = false;
                                calledAttended = false;
                            }
                        }
                    }
                    break;
            }
        }
    }
}
