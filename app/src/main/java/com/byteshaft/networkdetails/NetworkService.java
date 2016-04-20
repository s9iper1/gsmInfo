package com.byteshaft.networkdetails;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class NetworkService extends Service {

    public static final String TAG = NetworkService.class.getSimpleName();
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    private boolean mDone = false;
    private String mTextStr = "";
    private TelephonyManager mManager;
    private final String SPACE = " ";
    private final String COMMA = ",";
    private static NetworkService sInstance;

    public static NetworkService getInstance() {
        return sInstance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstance = this;
        mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        DropReceiver dropReceiver = new DropReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelecomManager.EXTRA_CALL_DISCONNECT_CAUSE);
        intentFilter.addAction(TelecomManager.EXTRA_CALL_DISCONNECT_MESSAGE);
        registerReceiver(dropReceiver, intentFilter);
        return START_STICKY;
    }

    public void getNetworkDetails() {
        mManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                        PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final PhoneStateListener mListener = new PhoneStateListener() {
        boolean process = false;

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            System.out.println(state);

        }

        @Override
        public void onCellLocationChanged(CellLocation mLocation) {
            Log.d(TAG, "Cell location obtained.");
            mCellLocation = mLocation;
            process = true;
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength sStrength) {
            Log.d(TAG, "Signal strength obtained.");
            mSignalStrength = sStrength;
            if (process) {
                update();
            }
        }
    };

    // AsyncTask to avoid an ANR.
    private class ReflectionTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... mVoid) {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String imei =  telephonyManager.getDeviceId();
            mTextStr = AppGlobals.SCHEDULE + COMMA + telephonyManager.getSubscriberId()+
                    COMMA + imei + COMMA + telephonyManager.getSimSerialNumber() +COMMA + Helpers.getTimeStamp()
                    + COMMA + mManager.getNetworkOperatorName()+ COMMA + ReflectionUtils.dumpClass(SignalStrength.class,
                    mSignalStrength) +
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
            mManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
            mManager.listen(mListener, PhoneStateListener.LISTEN_SERVICE_STATE);
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

    public static void sendRequestData(HttpURLConnection connection, String body) throws IOException {
        byte[] outputInBytes = body.getBytes("UTF-8");
        OutputStream os = connection.getOutputStream();
        os.write(outputInBytes);
        os.close();
        System.out.println("tag" + connection.getResponseCode() + "" + connection.getResponseMessage());
    }

    private static String getJsonObjectString(String data) {
        return String.format("{\"scriptName\": \"%s\"}", data);
    }

    class UploadDataTask extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "uploading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            System.out.println(mTextStr);
            int response = 0;
            if (Helpers.isNetworkAvailable() && Helpers.isInternetWorking()) {
                URL url;
                try {
                    url = new URL("http://ba3.aga.my/claritybqm/reportFetch/?scriptName=StringDigest");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestMethod("POST");
                    String jsonFormattedData = getJsonObjectString(mTextStr);
                    Log.i("TAG" , jsonFormattedData);
                    sendRequestData(connection, jsonFormattedData);
                    response = connection.getResponseCode();
                    Log.i("TAG", connection.getResponseMessage());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            System.out.println(mTextStr);
            mTextStr = "";
            Log.i("TAG", " "+integer);
            if (integer == HttpURLConnection.HTTP_OK) {
                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "error with code " + integer,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}

