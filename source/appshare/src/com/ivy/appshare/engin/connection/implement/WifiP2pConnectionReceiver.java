package com.ivy.appshare.engin.connection.implement;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class WifiP2pConnectionReceiver extends BroadcastReceiver{

    WifiP2pStateChangedListener mListener;
    
    WifiP2pConnectionReceiver(WifiP2pStateChangedListener listener){
        mListener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            WifiP2pInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            mListener.onWifiP2pConnectionChanged(info, networkInfo);
            
        } else if (action.equals(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
            mListener.onWifiP2pDiscoveryChanged(state);
            
        } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
            mListener.onWifiP2pPeersChanged();
            
        } else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
            WifiP2pDevice p2pDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mListener.onWifiP2pThisDeviceChanged(p2pDevice);
        } else if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)){
            int wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
            mListener.onWifiP2pStateChanged(wifiP2pState);
        }
    }

}
