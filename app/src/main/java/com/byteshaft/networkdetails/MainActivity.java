package com.byteshaft.networkdetails;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout left;
    private RelativeLayout right;
    private TelephonyManager telephonyManager;
    private TextView lcId;
    private TextView cid;
    private TextView lac;
    private TextView psc;
    private TextView rnc;
    private TextView type;
    private TextView net;
    private short RNCID_C;
    private short CID_C;
    private TextView rxl;
    private TextView qual;
    private TextView rsrp;
    private TextView rsrq;
    private TextView ecno;
    private TextView dist;
    private TextView bear;
    private int strength = 0;
    public ViewHolder viewHolder;
    private HashMap<Integer, Integer[]> neighbouringData;
    private ArrayList<Integer> idsList;
    private ListView neighbouringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lcId = (TextView) findViewById(R.id.lcId);
        cid = (TextView) findViewById(R.id.cId);
        lac = (TextView) findViewById(R.id.laC);
        psc = (TextView) findViewById(R.id.pSc);
        rnc = (TextView) findViewById(R.id.rNc);
        type = (TextView) findViewById(R.id.type);
        net = (TextView) findViewById(R.id.net);
        rxl = (TextView) findViewById(R.id.rxl);
        qual = (TextView) findViewById(R.id.qual);
        rsrp = (TextView) findViewById(R.id.rsrp);
        rsrq = (TextView) findViewById(R.id.rsrq);
        ecno = (TextView) findViewById(R.id.ecio);
        dist = (TextView) findViewById(R.id.dist);
        bear = (TextView) findViewById(R.id.bear);
        neighbouringList = (ListView) findViewById(R.id.neighbouring_details);
        AppGlobals.APP_FOREGROUND = true;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Intent intent = new Intent(getApplicationContext(), NetworkService.class);
        intent.putExtra(AppGlobals.SEND_BROAD_CAST, true);
        if (NetworkService.getInstance() == null) {
            startService(intent);
        }
        displayData();
    }

    private void displayData() {
        CellLocation cellLocation = telephonyManager.getCellLocation();
        GsmCellLocation gsmLoc = null;
        CdmaCellLocation cdmaLoc;
        int lcid = 0;
        try {
            gsmLoc = (GsmCellLocation) cellLocation;
            System.out.println(gsmLoc.getCid());
            lcid = gsmLoc.getCid();
        } catch (ClassCastException e) {
            cdmaLoc = (CdmaCellLocation) cellLocation;
            System.out.println("Base station ID - " + cdmaLoc.getBaseStationId());
            lcid = cdmaLoc.getBaseStationId();
        }
        lcId.setText("LCID " + ": " + String.valueOf(lcid));
        lac.setText("LAC " + ": " + String.valueOf(gsmLoc.getLac()));
        psc.setText("PSC " + ": " + String.valueOf(gsmLoc.getPsc()));
        //        rnc.setText(String.valueOf());
        type.setText("TYPE " + ":" + Helpers.getNetworkType(telephonyManager.getNetworkType()));
        net.setText("NET " + ":");
        byte[] bytearray = new byte[4];
        bytearray = convertByteArray(lcid);
        int RNCID = getRNCIDorCID(bytearray, RNCID_C);
        Log.i("RNC", String.valueOf(RNCID));
        int realCID = getRNCIDorCID(bytearray, CID_C);
        cid.setText("CID " + ": " + String.valueOf(realCID));
        StrengthListener strengthListener = new StrengthListener();
        telephonyManager.listen(strengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                    rsrp.setText("RSRP : " + String.valueOf(((CellInfoLte) cellInfo)
                            .getCellSignalStrength().getDbm()));
                    rsrq.setText("RSRQ: " + String.valueOf(((CellInfoLte) cellInfo).getCellSignalStrength()));
                } else {
                    rsrp.setText("RSRP : " + "0");
                    rsrq.setText("RSRQ: -1");
                }
            }
        } else {
            rsrp.setText("RSRP : " + "0");
            rsrq.setText("RSRQ: -1");
        }
    }

    private class StrengthListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            strength = signalStrength.getGsmSignalStrength();
            strength = (2 * strength) - 113;
            rxl.setText("RXL : " + String.valueOf(strength) + "dBm");
            qual.setText("QUAL : " + String.valueOf(signalStrength.getGsmBitErrorRate()));
            ecno.setText("ECNO : " + "");
            final List<CellInfo> cellInfos;
            idsList = new ArrayList<>();
            neighbouringData = new HashMap<>();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cellInfos = telephonyManager.getAllCellInfo();
                for (CellInfo cellInfo : cellInfos) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                        if (!idsList.contains(cellIdentityGsm.getCid())) {
                            idsList.add(cellIdentityGsm.getCid());
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
                            Integer[] networkData = {cellIdentityGsm.getLac(), cellSignalStrengthGsm.getDbm()};
                            neighbouringData.put(cellIdentityGsm.getCid(), networkData);
                        }
                    }
                }
                Adapter adapter = new Adapter(getApplicationContext(), R.layout.neighbouring_delegate,
                        idsList);
                neighbouringList.setAdapter(adapter);
                System.out.println(idsList);
                System.out.println(neighbouringData);

            }
        }
    }

    public int getRNCIDorCID(byte[] bytes, short which){
        int MASKc = 0xFF;
        int result = 0;
        if (which == CID_C) {
            result = bytes[0] & MASKc ;
            result = result + ((bytes[1] & MASKc ) << 8);
        } else if (which == RNCID_C){
            result = bytes[2] & MASKc ;
            result = result + ((bytes[3] & MASKc ) << 8);
        } else {
//            g_FileHandler.putLog__p('E', "getRNCIDorCID invalid parameter");
        }
        return result;
    }

    public static byte[] convertByteArray(int number){
        byte[] bytearray = new byte[4];
        int MASK_c = 0xFF;
        for (short i=0; i<=3; i++){
            bytearray[i] = (byte) ((number >> (8*i)) & MASK_c);
        }
        return bytearray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.upload:
                new NetworkService.UploadDataTask().execute();
                return true;

        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppGlobals.APP_FOREGROUND = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppGlobals.APP_FOREGROUND = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppGlobals.APP_FOREGROUND = false;
    }

    class Adapter extends ArrayAdapter<String> {

        private ArrayList<Integer> list;

        public Adapter(Context context, int resource, ArrayList<Integer> list) {
            super(context, resource);
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater layoutInflater = getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.neighbouring_delegate, parent, false);
                viewHolder.textViewCid = (TextView) convertView.findViewById(R.id.text_view_cid);
                viewHolder.textViewLac = (TextView) convertView.findViewById(R.id.text_view_lac);
                viewHolder.textViewrXl = (TextView) convertView.findViewById(R.id.text_view_rxl);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textViewCid.setText(String.valueOf(list.get(position)));
            viewHolder.textViewLac.setText(String.valueOf(neighbouringData.get(list.get(position))[0]));
            viewHolder.textViewrXl.setText(String.valueOf(neighbouringData.get(list.get(position))[1]));
            return convertView;
        }
    }

    class ViewHolder {

        public TextView textViewCid;
        public TextView textViewLac;
        public TextView textViewrXl;
    }
}
