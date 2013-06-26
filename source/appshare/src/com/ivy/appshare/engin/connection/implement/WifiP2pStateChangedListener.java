package com.ivy.appshare.engin.connection.implement;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

public interface WifiP2pStateChangedListener {
    void onWifiP2pConnectionChanged(WifiP2pInfo info, NetworkInfo networkInfo);
    void onWifiP2pDiscoveryChanged(int startedOrStopped);
    void onWifiP2pPeersChanged();
    void onWifiP2pThisDeviceChanged(WifiP2pDevice p2pDevice);
    void onWifiP2pStateChanged(int wifiP2pState);
}
