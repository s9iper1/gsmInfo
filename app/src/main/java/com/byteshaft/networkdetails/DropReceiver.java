package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by s9iper1 on 4/20/16.
 */
public class DropReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("called");

    }
}
