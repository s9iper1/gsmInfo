package com.byteshaft.networkdetails;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class NetworkService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = NetworkService.class.getSimpleName();
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    private boolean mDone = false;
    private String mTextStr = "";
    private TelephonyManager mManager;
    private final String SPACE = " ";
    private final String COMMA = ",";
    private static NetworkService sInstance;
    private AlarmHelpers alarmHelpers;
    private GoogleApiClient mGoogleApiClient;
    private android.location.Location mLocation;
    private int mLocationRecursionCounter;
    private int mLocationChangedCounter;
    private LocationRequest mLocationRequest;
    private Handler mHandler;
    private boolean locationCannotBeAcquired = false;
    private String neighbouringInfo = "";
    private String SERVICE_STATE = "";
    private TelephonyManager dataManager;
    private boolean serviceRunning = false;

    public static NetworkService getInstance() {
        return sInstance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstance = this;
        dataManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        dataManager.listen(new DataListener(this),
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_DATA_ACTIVITY);
        alarmHelpers = new AlarmHelpers();
        if ( intent != null && intent.getExtras() != null && intent.getBooleanExtra(AppGlobals.SEND_BROAD_CAST, false)) {
            sendBroadcast(new Intent("com.byteshaft.gsmDetails"));
        }
        mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return START_STICKY;
    }

    public void getNetworkDetails() {
        mManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SERVICE_STATE);
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final PhoneStateListener mListener = new PhoneStateListener() {
        boolean process = false;

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            SERVICE_STATE = "";
            switch (serviceState.getState()) {
                case ServiceState.STATE_EMERGENCY_ONLY:
                    SERVICE_STATE = "EMERGENCY_ONLY";
                    break;
                case ServiceState.STATE_IN_SERVICE:
                    SERVICE_STATE = "IN_SERVICE";
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                    SERVICE_STATE = "OUT_OF_SERVICE";
                    break;
                case ServiceState.STATE_POWER_OFF:
                    SERVICE_STATE = "POWER_OFF";
                    break;
                default:
                    SERVICE_STATE = "Unknown";
                    break;
            }
            Log.i("STATE", SERVICE_STATE);
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
            List<NeighboringCellInfo> infoList = mManager.getNeighboringCellInfo();
            System.out.println(infoList);
            for (NeighboringCellInfo info : infoList) {
                neighbouringInfo = info.getCid()+ COMMA +info.getLac()+ COMMA + info.getPsc()+ COMMA
                        +info.getNetworkType() + COMMA +
                        info.getRssi() + " ";

            }
            String imei = mManager.getDeviceId();
            mTextStr = AppGlobals.CURRENT_STATE + COMMA + mManager.getSubscriberId() +
                    COMMA + imei + COMMA + mManager.getSimSerialNumber() + COMMA + Helpers.getTimeStamp()
                    + COMMA + mManager.getNetworkOperatorName() + COMMA + AppGlobals.LOCATION + COMMA
                    +SERVICE_STATE + COMMA + ReflectionUtils.dumpClass(SignalStrength.class,
                    mSignalStrength) +
                    ReflectionUtils.dumpClass(mCellLocation.getClass(), mCellLocation) + SPACE
                    + getWimaxDump() + neighbouringInfo;
            mTextStr = mTextStr.replace("[-1,-1,-1]", "[-1|-1|-1]");
            Log.i("TAG", mTextStr);
            Log.i("INFO",  "this" +String.valueOf(neighbouringInfo));
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
            Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
            String date = Helpers.getTimeStamp();
            Helpers.saveGsmDetails(date, mTextStr);
            Set<String> set = Helpers.getHashSet();
            set.add(date);
            Helpers.saveHashSet(set);
            if (AppGlobals.CURRENT_STATE.equals(AppGlobals.schedule)) {
                new UploadDataTask().execute();

            }

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
        System.out.println("TAG " + connection.getResponseCode() + "" + connection.getResponseMessage());
    }

    private static String getJsonObjectString(String data) {
        return String.format("%s", data);
    }

    class UploadDataTask extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "uploading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int response = 0;
            if (Helpers.isNetworkAvailable() && Helpers.isInternetWorking()) {
                URL url;
                try {
                    url = new URL("http://ba3.aga.my/claritybqm/reportFetch/?scriptName=StringDigest");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestMethod("POST");
                    Set<String> strings = Helpers.getHashSet();
                    Log.i("strings", String.valueOf(strings));
                    StringBuilder totalData = new StringBuilder();
                    Object[] array = strings.toArray();
                    for (Object singleGsm : array) {
                        String gsmData = Helpers.getGsmDetails(singleGsm.toString());
                        Log.i("single", gsmData);
                        totalData.append(gsmData).append("\n");
                        strings.remove(singleGsm.toString());
                    }
                    String jsonFormattedData = getJsonObjectString(totalData.toString());
                    Log.i("total", totalData.toString());
                    sendRequestData(connection, jsonFormattedData);
                    Helpers.saveHashSet(strings);
                    response = connection.getResponseCode();
                    Log.i("TAG", connection.getResponseMessage());
                    connection.disconnect();
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
            Log.i("TAG", " " + integer);
            if (integer == HttpURLConnection.HTTP_OK) {
                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "error with code " + integer,
                        Toast.LENGTH_SHORT).show();
            }
            alarmHelpers.setAlarmForDetails();
        }
    }

    private Runnable mLocationRunnable = new Runnable() {
        @Override
        public void run() {
            String LOG_TAG = "Location";
            if (mLocation == null && mLocationRecursionCounter > 24) {
                mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLocation != null) {
                    Log.w(LOG_TAG, "Failed to get location current location, saving last known location");
                    locationCannotBeAcquired = true;
                    stopLocationUpdate();
                } else {
                    Log.e(LOG_TAG, "Failed to get location");
                    locationCannotBeAcquired = true;
                    stopLocationUpdate();
                }
            } else if (mLocation == null) {
                acquireLocation();
                mLocationRecursionCounter++;
                Log.i(LOG_TAG, "Tracker Thread Running: " + mLocationRecursionCounter);
            } else {
                stopLocationUpdate();
            }
            if (locationCannotBeAcquired) {
                getNetworkDetails();
                getHandler().removeCallbacks(mLocationRunnable);
            }
        }
    };

    public void startLocationUpdate() {
        Log.i("TAG", "update" + serviceRunning);
        if (!serviceRunning) {
            connectGoogleApiClient();
            serviceRunning = true;
        }
    }

    private void stopLocationUpdate() {
        reset();
    }

    private void reset() {
        serviceRunning = false;
        mLocationChangedCounter = 0;
        mLocationRecursionCounter = 0;
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mLocation = null;
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("TAG", "connected");
        startLocationUpdates();
        acquireLocation();
        serviceRunning = false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        mLocationChangedCounter++;
        System.out.println(mLocationChangedCounter);
        if (mLocationChangedCounter == 3) {
            mLocation = location;
            AppGlobals.LOCATION = "Lat " + getLatitudeAsString(location) + ",Long " + getLongitudeAsString(location);
            getHandler().removeCallbacks(mLocationRunnable);
            reset();
            getNetworkDetails();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(AppGlobals.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = getLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    public static String getLongitudeAsString(Location location) {
        return String.valueOf(location.getLongitude());
    }

    public static String getLatitudeAsString(Location location) {
        return String.valueOf(location.getLatitude());
    }

    private void acquireLocation() {
        Handler handler = getHandler();
        handler.postDelayed(mLocationRunnable, 800);
    }

    public LocationRequest getLocationRequest() {
        long INTERVAL = 0;
        long FASTEST_INTERVAL = 0;
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        return mLocationRequest;
    }
}

