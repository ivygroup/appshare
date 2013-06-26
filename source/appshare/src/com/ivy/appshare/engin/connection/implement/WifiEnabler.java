package com.ivy.appshare.engin.connection.implement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.os.Handler;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


import java.util.concurrent.atomic.AtomicBoolean;

public class WifiEnabler{
    private final Context mContext;
    private static final String TAG = "WifiEnabler";
    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
    private WifiManagerHiddenAPI mWifiManagerHiddenAPI;
    private boolean mStarted = false;
    /*private final static Intent mWidgetEnabledIntent = 
            new Intent(WifiConnectionService.ACTION_WIDGET_WIFI_ENABLED);*/
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            }/* else if (WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION.equals(action)){
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManagerHiddenAPI.WifiHiddenAPI.EXTRA_WIFI_AP_STATE, WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_FAILED));
            }else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                //List<ScanResult> lists = mWifiManager.getScanResults();

            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }*/

        }
    };

    public WifiEnabler(Context context, CheckBox checkBox) {
        mContext = context;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiManagerHiddenAPI = WifiManagerHiddenAPI.getInstance(mContext);

        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION);
    }

    public void start() {
        mStarted = true;
        mContext.registerReceiver(mReceiver, mIntentFilter);
        tetherCheck(true);
    }

    public void stop() {
        mStarted = false;
        mContext.unregisterReceiver(mReceiver);
    }
    
    public void reStart(){
        tetherCheck(true);
    }

    public boolean isStarted(){
        return mStarted;
    }

    /* Don't update UI to opposite state until we're sure*/
    private void tetherCheck(boolean isChecked){
        // Disable tethering if enabling Wifi
        int wifiApState = mWifiManagerHiddenAPI.getWifiHiddenAPI().getWifiApState();
        if (isChecked && ((wifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED))) {
            mWifiManagerHiddenAPI.getWifiHiddenAPI().setWifiApEnabled(null, false);
        }

        mWifiManager.setWifiEnabled(isChecked);
    }


    private void handleWifiStateChanged(int state) {
        Log.d(TAG, "handleWifiStateChanged, state:" + state);
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                // mContext.sendBroadcast(mWidgetEnabledIntent); // TODO:
                mWifiManager.startScan();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_DISABLED:
            default:
                break;
        }
    }

    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

}
