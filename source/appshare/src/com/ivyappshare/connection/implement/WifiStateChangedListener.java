package com.ivyappshare.connection.implement;

import java.util.List;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

interface WifiStateChangedListener {
    void onAirplaneModeChanged(boolean enabled);
    void onWifiConnected(WifiInfo wifiInfo);
    void onWifiDisconnected();
    void onWifiHotspotStateChanged(int state);
    void onScanResultAvailable(List<ScanResult> results);
    void onWifiConnecting();
    void onWifiStateChanged(int state);

}
