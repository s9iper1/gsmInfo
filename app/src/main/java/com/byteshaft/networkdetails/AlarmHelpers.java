package com.byteshaft.networkdetails;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;


public class AlarmHelpers {

    private static AlarmManager mAlarmManager;
    private static PendingIntent mPendingIntent;

    public static void setAlarmForDetails() {
        mAlarmManager = getAlarmManager(AppGlobals.getContext());
        final int ONE_SECOND = 1000;
        final int ONE_MINUTE = ONE_SECOND * 60;
        final int TEN_MINUTES = ONE_MINUTE * 10;
        final int THIRTY_MINUTES= TEN_MINUTES * 3;
        setAlarm(THIRTY_MINUTES);
    }

    private static void setAlarm(long time) {
        Log.i("Alarm",
                String.format("Setting alarm for: %d", TimeUnit.MILLISECONDS.toMinutes(time)));
        Intent intent = new Intent("com.byteshaft.gsmDetails");
        mPendingIntent = PendingIntent.getBroadcast(AppGlobals.getContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + time, mPendingIntent);
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
