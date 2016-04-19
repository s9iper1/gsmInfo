package com.byteshaft.networkdetails;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    private boolean mDone = false;
    private Button clickMe;
    private String mTextStr = "";
    private TelephonyManager mManager;
    private final String SPACE = " ";
    private final String UNDERSCORE = "_";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickMe = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        clickMe.setOnClickListener(this);
        mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    final PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onCellLocationChanged(CellLocation mLocation) {
            if (mDone) return;
            Log.d(TAG, "Cell location obtained.");
            mCellLocation = mLocation;
            update();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength sStrength) {
            if (mDone) return;
            Log.d(TAG, "Signal strength obtained.");
            mSignalStrength = sStrength;
            update();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                        PhoneStateListener.LISTEN_CELL_LOCATION);
                break;
        }

    }

    // AsyncTask to avoid an ANR.
    private class ReflectionTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... mVoid) {
            String imsiNumber = "";
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String imei =  telephonyManager.getDeviceId();
            Process ifc = null;
            try {
                ifc = Runtime.getRuntime().exec("getprop ro.hardware");
                BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
                imsiNumber = bis.readLine();
            } catch (java.io.IOException e) {
            }
            ifc.destroy();
            mTextStr = mManager.getNetworkOperatorName() + SPACE + UNDERSCORE  + SPACE+ imsiNumber+
                    SPACE+  UNDERSCORE + imei + SPACE + UNDERSCORE + Helpers.getTimeStamp()
                    + UNDERSCORE +  SPACE + ReflectionUtils.dumpClass(SignalStrength.class,
                    mSignalStrength) + UNDERSCORE +
                    ReflectionUtils.dumpClass(mCellLocation.getClass(), mCellLocation) + SPACE
                    + getWimaxDump();
            Log.i("TAG" , mTextStr);
            return null;
        }

        protected void onProgressUpdate(Void... progress) {
            // Do nothing...
        }

        protected void onPostExecute(Void result) {
            complete();
        }
    }

    private final void complete() {
        try {
            // Stop listening.
            mTextStr = "";
            mManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
            Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
            new UploadDataTask().execute();

        } catch (Exception e) {
            Log.e(TAG, "ERROR!!!", e);
        }
    }

    private final void update() {
        if (mSignalStrength == null || mCellLocation == null) return;

        final ReflectionTask mTask = new ReflectionTask();
        mTask.execute();
    }

    private static final String[] mServices = {
            "WiMax", "wimax", "wimax", "WIMAX", "WiMAX"
    };

    /**
     * @return A String containing a dump of any/ all WiMax
     * classes/ services loaded via {@link Context}.
     */
    public final String getWimaxDump() {
        String mStr = "";

        for (final String mService : mServices) {
            final Object mServiceObj = getApplicationContext()
                    .getSystemService(mService);
            if (mServiceObj != null) {
                mStr += "getSystemService(" + mService + ")\n\n";
                mStr += ReflectionUtils.dumpClass(mServiceObj.getClass(), mServiceObj);
            }
        }
        return mStr;
    }

    class UploadDataTask extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            int response = 0;
            if (Helpers.isNetworkAvailable() && Helpers.isInternetWorking()) {
                HttpURLConnection connection;
                URL url;
                try {
                    url = new URL("http://ba3.aga.my/claritybqm/reportFetch/?scriptName=FileDigest");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("charset", "utf-8");

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    String data = mTextStr;
                    out.writeBytes(data);
                    out.flush();
                    out.close();
                    response = connection.getResponseCode();
                    System.out.println(connection.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressBar.setVisibility(View.INVISIBLE);
            if (integer == HttpURLConnection.HTTP_OK) {
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "error with code " + integer,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
