package com.byteshaft.networkdetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button clickMe;
    private ProgressBar progressBar;
    private TelephonyManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        dataManager.listen(new DataListener(this),
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_DATA_ACTIVITY);
        clickMe = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        startService(new Intent(getApplicationContext(), NetworkService.class));
        clickMe.setOnClickListener(this);
        AlarmHelpers alarmHelpers = new AlarmHelpers();
//        alarmHelpers.setAlarmForDetails();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                AppGlobals.CURRENT_STATE = AppGlobals.schedule;
                NetworkService.getInstance().getNetworkDetails();
                break;
        }

    }
}
