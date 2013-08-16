package com.ivy.appshare.engin.connection;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.connection.implement.AccessPointInfo;
import com.ivy.appshare.engin.connection.implement.ConnectionManagement;
import com.ivy.appshare.engin.connection.implement.ConnectionStateListener;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.im.Person;

public class IvyConnectionManager implements ConnectionStateListener {
    private static final String TAG = "IvyConnectionManager";
    private static final Boolean gIsOpenWifiP2p = false;

    private ConnectionState mCurrentState;
    private List<APInfo> mScanResult;
    private List<PeerInfo> mDiscoveredPeers;
    private ConnectionManagement mConnectionManagement;
    private InetAddress mMySelfIpOfWifiP2p;
    private int mMySelfNetMaskOfWifiP2p;


    public IvyConnectionManager() {
        Log.d(TAG, "IvyConnectionManager");
        mCurrentState = new ConnectionState();
        mScanResult = new ArrayList<APInfo>();
        mDiscoveredPeers = new ArrayList<PeerInfo>();

        mConnectionManagement = new ConnectionManagement();
        if (!mConnectionManagement.initialize(MyApplication.getInstance())) {
            mConnectionManagement = null;
        }
        if (mConnectionManagement != null) {
            mConnectionManagement.registerListener(this, this);

            int state = mConnectionManagement.getConnectionInfo().getConnectionState();
            updateCurrentState(state);
            if (state != ConnectionState.CONNECTION_UNKNOWN) {
                setMySelfInfo();
                String ssid = mConnectionManagement.getConnectionInfo().getSSID();
                IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_UNKNOWN, state, ssid);
            }

            List<AccessPointInfo> infos = mConnectionManagement.getScanResult();
            updateScanResult(infos, mScanResult);
        }

        if (mConnectionManagement != null) {
            mConnectionManagement.startScan();
            if (gIsOpenWifiP2p) {
                mConnectionManagement.wifiP2pStartDiscovery();
            }
        }
    }

    public void release() {
        Log.d(TAG, "release");

        if (mConnectionManagement != null) {
            mConnectionManagement.disconnectFromIvyNetwork();
            mConnectionManagement.disableHotspot();
            mConnectionManagement.stopScan();
            if (gIsOpenWifiP2p) {
                mConnectionManagement.wifiP2pStopDiscovery();
            }
            mConnectionManagement.unregisterListener(this);
            mConnectionManagement.unInitialize();
            mConnectionManagement = null;
        }
    }



  //==============================================================================

    // the interface of connection manager.
    public boolean isAirplanEnabled() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.isAirplanEnabled();
    }


    public ConnectionState getConnectionState() {
        return mCurrentState;
    }

    public InetAddress getBroadcastAddress() {
        if (mConnectionManagement == null) {
            return null;
        }
        return mConnectionManagement.getBroadcastAddress();
    }


    public boolean isWifiEnabled() {
        if (mConnectionManagement == null) {
            return true;
        }
        return mConnectionManagement.isWifiEnabled();
    }
    
    public void enableWifi() {
        if (mConnectionManagement == null) {
            return;
        }
        mConnectionManagement.enableWifi();
    }

    public APInfo getConnectionInfo() {
        if (mConnectionManagement == null) {
            return null;
        }
        APInfo info = new APInfo();
        info.mSSID = mConnectionManagement.getConnectionInfo().getSSID();
        info.mFriendlyName = mConnectionManagement.getConnectionInfo().getFriendlyName();
        info.mHotspotPassword = mConnectionManagement.getConnectionInfo().getIvyHotspotPassword();
        info.mShareAppCount = mConnectionManagement.getConnectionInfo().mShareAppCount;

        return info; 
    }

    public List<APInfo> getScanResult() {
        return mScanResult;
    }

    public boolean connectIvyNetwork(String ssid) {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.connectToIvyNetwork(ssid);
    }

    public boolean disconnectFromIvyNetwork() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.disconnectFromIvyNetwork();                
    }


    public boolean createHotspot() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.createHotspot();
    }

    public boolean createHotspot(int shareAppCount) {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.createHotspot(shareAppCount);
    }

    public boolean disableHotspot() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.disableHotspot();
    }

/*
    public boolean startScan() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.startScan();
    }

    public boolean stopScan() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.stopScan();
    }*/



    



/*
    public boolean isWifiP2pSupported() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.isWifiP2pSupported();
    }*/

    public List<PeerInfo> getWifiP2pPeers() {
        if (!gIsOpenWifiP2p) {
            return null;
        }
        synchronized (mDiscoveredPeers) {
            return new ArrayList<PeerInfo>(mDiscoveredPeers);
        }
    }

    public boolean connectToWifiP2pPeer(String peerID) {
        if (mConnectionManagement == null) {
            return false;
        }

        boolean re = mConnectionManagement.connectToWifiP2pPeer(peerID);
        if (re) {
            IvyMessages.sendNetworkStateChange(
                    ConnectionState.CONNECTION_TYPE_WIFIP2P,
                    ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTING,
                    peerID);
        }
        return re;
    }

    public boolean cancelConnectWifiP2p() {
        if (mConnectionManagement == null) {
            return false;
        }
        boolean b = mConnectionManagement.cancelConnectWifiP2p();
        if (b) {
            IvyMessages.sendNetworkStateChange(
                    ConnectionState.CONNECTION_TYPE_WIFIP2P,
                    ConnectionState.CONNECTION_STATE_WIFIP2P_DISCONNECTED,
                    null);
        }
        return b;
    }

    public boolean removeGroupOfWifiP2p() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.removeGroupOfWifiP2p();
    }

    public AccessPointInfo getWifiP2pConnectionInfo() {
        if (mConnectionManagement == null) {
            return null;
        }
        return mConnectionManagement.getWifiP2pConnectionInfo();
    }

    public InetAddress getMySelfIpOfWifiP2p() {
        return mMySelfIpOfWifiP2p;
    }

    public int getNetMaskOfWifiP2p() {
        return mMySelfNetMaskOfWifiP2p;
    }

/*
    public boolean wifiP2pStartDiscovery() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.wifiP2pStartDiscovery();
    }

    public boolean wifiP2pStopDiscovery() {
        if (mConnectionManagement == null) {
            return false;
        }
        return mConnectionManagement.wifiP2pStopDiscovery();
    }*/
    




//==============================================================================
    // ConnectionStateListener interface
    @Override
    public void onAirplaneModeChanged(boolean enabled) {
        if (enabled) {
            clearMySelfInfo();
            // int state = mConnectionManagement.getConnectionInfo().getConnectionState().getWifiState();
            int type = mCurrentState.getLastType();
            if (type == ConnectionState.CONNECTION_TYPE_HOTSPOT) {
                IvyMessages.sendNetworkStateChange(type, ConnectionState.CONNECTION_STATE_HOTSPOT_DISABLED, null);
            } else if (type == ConnectionState.CONNECTION_TYPE_WIFI) {
                IvyMessages.sendNetworkStateChange(type, ConnectionState.CONNECTION_STATE_WIFI_DISABLED, null);
            }
        }
    }

    @Override
    public void onWifiConnected(AccessPointInfo info) {
        setMySelfInfo();
        int state = info.getConnectionState();
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFI, state);
        if (state == ConnectionState.CONNECTION_STATE_WIFI_IVY_CONNECTED) {
            boolean hasThisAP = false;
            for (APInfo tmp : mScanResult) {
                if (tmp.getSSID().equals(info.getSSID())) {
                    hasThisAP = true;
                    break;
                }
            }
            if (hasThisAP == false) {
                addOneApToScanResult(info, mScanResult);
            }
        }

        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_WIFI, state, info.getSSID());
    }

    @Override
    public void onWifiDisconnected(String disconnectedSSID, int connectionType, int wifiState) {
        clearMySelfInfo();
        updateCurrentState(connectionType, wifiState);
        IvyMessages.sendNetworkStateChange(connectionType, wifiState, disconnectedSSID);
    }

    @Override
    public void onWifiConnecting(AccessPointInfo info) {
        int state = info.getConnectionState();
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFI, state);
        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_WIFI, state, info.getSSID());
    }

    @Override
    public void onWifiHotspotStateChanged(int connectionType, int state) {
        updateCurrentState(connectionType, state);
        String ssid= mConnectionManagement.getConnectionInfo().getSSID();
        IvyMessages.sendNetworkStateChange(connectionType, state, ssid);
    }

    @Override
    public void onScanResultAvailable(ArrayList<AccessPointInfo> lists) {
        updateScanResult(lists, mScanResult);
        IvyMessages.sendNetworkFinishScanIvyRoom();
    }

    @Override
    public void onIvyHotspotIPAvailable(InetAddress addr) {
        setMySelfInfo();
        updateCurrentState(ConnectionState.CONNECTION_TYPE_HOTSPOT, ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED);
        String ssid= mConnectionManagement.getConnectionInfo().getSSID();
        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_HOTSPOT,
                ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED, ssid);
    }

    @Override
    public void onWifiP2pConnected(AccessPointInfo info, String connectedPeerID) {
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFIP2P, ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED);

        int state = info.getConnectionState();
        /*if (state == ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED && connectedPeerID != null) {
            boolean hasThisPeer = false;
            for (PeerInfo tmp : mDiscoveredPeers) {
                if (tmp.getID().equals(info.getMacAddress())) {
                    hasThisPeer = true;
                    break;
                }
            }
            if (hasThisPeer == false) {
                addOnePeerToDiscrovedPeers(info, mDiscoveredPeers);     // TODO, not info but instead of connectedPeerID.
            }
        }*/

        mMySelfIpOfWifiP2p = info.getIpAddress();
        mMySelfNetMaskOfWifiP2p = info.mMask;

        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_WIFIP2P,
                ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED, connectedPeerID);
    }

    @Override
    public void onWifiP2pDisconnected(String id) {
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFIP2P, ConnectionState.CONNECTION_STATE_WIFIP2P_DISCONNECTED);
        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_WIFIP2P,
                ConnectionState.CONNECTION_STATE_WIFIP2P_DISCONNECTED, id);
    }

    @Override
    public void onWifiP2pStateChanged(String id, int connectionType, int state) {
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFIP2P, state);
        IvyMessages.sendNetworkStateChange(connectionType, state, id);
    }

    @Override
    public void onWifiP2pDiscoveredList(ArrayList<AccessPointInfo> lists) {
        synchronized (mDiscoveredPeers) {
            updateDiscrovedPeers(lists, mDiscoveredPeers);
        }

        IvyMessages.sendNetworkDiscoveryWifiP2p();
    }

    @Override
    public void onWifiEnabled() {
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFI, ConnectionState.CONNECTION_STATE_WIFI_ENABLED);
        if (mConnectionManagement != null) {
            mConnectionManagement.startScan();
        }
    }

    @Override
    public void onWifiDisabled() {
        clearMySelfInfo();
        updateCurrentState(ConnectionState.CONNECTION_TYPE_WIFI, ConnectionState.CONNECTION_STATE_WIFI_DISABLED);
        IvyMessages.sendNetworkStateChange(ConnectionState.CONNECTION_TYPE_WIFI,
                ConnectionState.CONNECTION_STATE_WIFI_DISABLED, null);
        IvyMessages.sendNetworkClearIvyRoom();
    }




//==============================================================================
    // private function.
    private void updateCurrentState(int state) {
        int type = ConnectionState.getConnectionTypeByStatus(state);
        mCurrentState.setState(type, state);
    }

    private void updateCurrentState(int connectionType, int state) {
        mCurrentState.setState(connectionType, state);
    }

    private void updateScanResult(List<AccessPointInfo>src, List<APInfo> resultApInfos) {
        resultApInfos.clear();
        if (src != null) {
            for (AccessPointInfo info : src) {
                addOneApToScanResult(info, resultApInfos);
            }
        }
    }

    private void addOneApToScanResult(AccessPointInfo info, List<APInfo> resultApInfos) {
        if (info == null) {
            return;
        }

        APInfo apInfo = new APInfo();
        apInfo.mSSID = info.getSSID();
        apInfo.mFriendlyName = info.getFriendlyName();
        apInfo.mHotspotPassword = info.getIvyHotspotPassword();
        apInfo.mShareAppCount = info.mShareAppCount;

        try{
            String[] arr = apInfo.mSSID.split("-", 4);  // ivyappshare-count-sessioID-name
            if (arr.length==4) {
                apInfo.mShareAppCount = Integer.valueOf(arr[1]);
                apInfo.mSessionID = Integer.valueOf(arr[2]);
                apInfo.mFriendlyName = arr[3];
            }
        } catch(IndexOutOfBoundsException e){
            // nothing to do.
        } catch(NumberFormatException e) {
            // nothing to do.
        }

        resultApInfos.add(apInfo);
    }

    private void updateDiscrovedPeers(List<AccessPointInfo> lists, List<PeerInfo> discrovedPeers) {
        discrovedPeers.clear();
        if (lists != null) {
            Log.d(TAG, "onWifiP2pDiscoveredList Size = " + lists.size());
            for (AccessPointInfo info: lists) {
                addOnePeerToDiscrovedPeers(info, discrovedPeers);
            }
        }
    }

    private void addOnePeerToDiscrovedPeers(AccessPointInfo info, List<PeerInfo> discrovedPeers) {
        if (info == null || info.getMacAddress() == null) {
            return;
        }
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.mID = info.getMacAddress();
        peerInfo.mFriendlyName = info.getFriendlyName();
        peerInfo.mIsIvyDevice = info.getIsIvyWifiDirectDevice();

        // Log.d(TAG, "addOnePeerToDiscrovedPeers: " + peerInfo.toString());

        discrovedPeers.add(peerInfo);
    }

    private PeerInfo getPeerInfoByDeviceID(String peerID) {
        for (PeerInfo info : mDiscoveredPeers) {
            if (info.mID.equals(peerID)) {
                return info;
            }
        }

        return null;
    }

    private void setMySelfInfo() {
        Person mySelf = LocalSetting.getInstance().getMySelf();
        mySelf.mIP = mConnectionManagement.getConnectionInfo().getIpAddress();
        mySelf.mMac = mConnectionManagement.getConnectionInfo().getMacAddress();
        LocalSetting.getInstance().setBroadCastAddress(mConnectionManagement.getBroadcastAddress());

        new Thread(new Runnable(){
            @Override
            public void run() {
                getMyIps();
            }
        }).start();
    }

    private void clearMySelfInfo() {
        Person mySelf = LocalSetting.getInstance().getMySelf();
        mySelf.mIP = null;
        mySelf.mMac = null;
        LocalSetting.getInstance().setBroadCastAddress(null);
        LocalSetting.getInstance().setMyIps(null);
    }

    private void getMyIps() {
        List<InetAddress> outIps = new ArrayList<InetAddress>();

        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Log.e(TAG, "cant get my ips. " + e.getMessage());
            return;
        } catch (Exception ex) {
        	Log.e(TAG, "get my ips error. exception="+ex);
        	return;
        }

        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            getIpsFromIpInterface(netInterface, outIps);
        }

        LocalSetting.getInstance().setMyIps(outIps);
    }

    private void getIpsFromIpInterface(NetworkInterface netInterface, List<InetAddress> out) {
        InetAddress ip = null;

        Enumeration addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            ip = (InetAddress) addresses.nextElement();
            if (ip != null) {
                if (out != null) {
                    out.add(ip);
                }
            }
        }
    }
}
