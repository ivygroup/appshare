package com.ivy.appshare.connection.implement;

import java.util.List;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectionReceiver";
    WifiStateChangedListener mListener;
    private State mNetworkState;
    private WifiManager mWifiManager;
    private Context mContext;
    private int mWifiApState;
    private int mWifiState;
    
    public ConnectionReceiver(Context context, WifiStateChangedListener listener){
        mContext = context;
        mListener = listener;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action:" + action);
        
        if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
            boolean enabled = intent.getBooleanExtra("state", false);
            mListener.onAirplaneModeChanged(enabled);
            
        } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
            List<ScanResult> list = mWifiManager.getScanResults();
            mListener.onScanResultAvailable(list);
            
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo networkInfo = intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mNetworkState = networkInfo.getState();
            DetailedState detailState = networkInfo.getDetailedState();
            
            Log.d(TAG, "State:" + mNetworkState + ", DetailState:" + detailState);
            if (mNetworkState == NetworkInfo.State.CONNECTED){
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                
                mListener.onWifiConnected(wifiInfo);
            } else if (mNetworkState == NetworkInfo.State.DISCONNECTED){
                
                mListener.onWifiDisconnected();
            } else if (mNetworkState == NetworkInfo.State.CONNECTING){
                mListener.onWifiConnecting();
            }
            
        } else if (action.equals(WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION)){
            mWifiApState = intent.getIntExtra(
                    WifiManagerHiddenAPI.WifiHiddenAPI.EXTRA_WIFI_AP_STATE, WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_FAILED);
            Log.d(TAG, "mWifiApState:" + mWifiApState);
            mListener.onWifiHotspotStateChanged(mWifiApState);
            
        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            mListener.onWifiStateChanged(mWifiState);
        } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            Log.d(TAG, "SupplicantState:" + state);
            if (state == SupplicantState.DISCONNECTED){
                mListener.onWifiDisconnected();
            }
        }
    }

}
