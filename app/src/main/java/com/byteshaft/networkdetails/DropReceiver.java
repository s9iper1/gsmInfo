package com.byteshaft.networkdetails;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DropReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("called");

    }
}
