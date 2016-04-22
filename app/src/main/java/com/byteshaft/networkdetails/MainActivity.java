package com.byteshaft.networkdetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button clickMe;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        clickMe = (Button) findViewById(R.id.button);
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra(AppGlobals.SEND_BROAD_CAST, true);
        startService(intent);
        clickMe.setOnClickListener(this);
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
