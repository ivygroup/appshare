package com.ivy.appshare.engin.connection.implement;

import java.net.InetAddress;
import java.util.ArrayList;


public interface ConnectionStateListener {

    public void onAirplaneModeChanged(boolean enabled);

    public void onWifiEnabled();
    public void onWifiDisabled();

    public void onWifiConnected(AccessPointInfo info);
    public void onWifiConnecting(AccessPointInfo info);
    public void onWifiDisconnected(String disconnectedSSID, int ConnectionType, int state);

    public void onWifiHotspotStateChanged(int ConnectionType, int state);
    public void onIvyHotspotIPAvailable(InetAddress addr);
    public void onScanResultAvailable(ArrayList<AccessPointInfo> lists);

    public void onWifiP2pDiscoveredList(ArrayList<AccessPointInfo> lists);
    public void onWifiP2pConnected(AccessPointInfo info, String connectedPeerID);
    public void onWifiP2pDisconnected(String id);            // now the id is the macAddress.
    public void onWifiP2pStateChanged(String id, int ConnectionType, int state);

    }
