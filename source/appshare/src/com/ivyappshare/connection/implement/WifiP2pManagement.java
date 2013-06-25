package com.ivyappshare.connection.implement;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.ivyappshare.connection.ConnectionState;
import com.ivyappshare.connection.implement.WifiP2pManagerHiddenAPI.WifiP2pHiddenAPI;
import com.ivyappshare.engin.control.LocalSetting;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WifiP2pManagement implements WifiP2pStateChangedListener, GroupInfoListener, ConnectionInfoListener, PeerListListener{
    
    private final static String TAG = "WifiP2pManagement";

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private Channel mWifiP2pChannel;
    private ConnectionHandler mConnectionHandler;
    private HandlerThread mHandlerThread;
    
    private WifiP2pInfo mWifiP2pInfo;
    private NetworkInfo mNetworkInfo;
    private int mWifiP2pDiscoveryStartedOrStopped;
    private WifiP2pDevice mThisDevice;
    private WifiP2pGroup mWifiP2pGroupInfo;
    private Collection<WifiP2pDevice> mWifiP2pPeers;
    private int mWifiP2pState = WifiP2pManager.WIFI_P2P_STATE_DISABLED;
    private Object mLock = new Object();
    private WifiP2pConnectionReceiver mReceiver;
    private boolean mRequestDiscovery;
    private WifiP2pManagerHiddenAPI mWifiP2pManagerHiddenAPI;
    private WifiP2pHiddenAPI mWifiP2pHiddenAPI;
    private String mPreviousDeviceName;
    private boolean mThisDeviceUpdatedFirstTime;

    private String mNickName;
    private HashMap<String, AccessPointInfo> mPeers = new HashMap<String, AccessPointInfo>();
    private HashMap<Object, ConnectionStateListener> mListeners = 
            new HashMap<Object, ConnectionStateListener>();
    private AccessPointInfo mThisDeviceInfo = new AccessPointInfo();
    private String mCurrentPeerDeviceAddress;

    
    private final static int Msg_WifiP2pConnectionChanged = 0;
    private final static int Msg_WifiP2pDiscoveryChanged = 1;
    private final static int Msg_WifiP2pPeersChanged = 2;
    private final static int Msg_WifiP2pThisDeviceChanged = 3;
    private final static int Msg_WifiP2pGroupInfoAvaliable = 4;
    private final static int Msg_WifiP2pPeersInfoAvailable = 5;
    private final static int Msg_WifiP2pStateChanged = 6;

    WifiP2pManagement(){
    }

    void initialize(Context context){
        Log.d(TAG, "initialize");
        mContext = context;
        
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);
        
        mWifiP2pManagerHiddenAPI = WifiP2pManagerHiddenAPI.getInstance(mContext);
        mWifiP2pHiddenAPI = mWifiP2pManagerHiddenAPI.getWifiP2pHiddenAPI();
        
        mHandlerThread = new HandlerThread("WifiP2pConnectionControler");
        mHandlerThread.start();
        mConnectionHandler = new ConnectionHandler(mHandlerThread.getLooper());

        initIntentReceiver();

        mNickName = generateDeviceName();
        mWifiP2pHiddenAPI.setDeviceName(mWifiP2pChannel, mNickName, null);
    }
    
    void unInitialize(){
        Log.d(TAG, "unInitialize");
        if (mHandlerThread != null){
            mHandlerThread.quit();
        }
        
        unInitIntentReceiver();
        mWifiP2pHiddenAPI.setDeviceName(mWifiP2pChannel, mPreviousDeviceName, null);
        
        mContext = null;
    }
    
    boolean startDiscovery() {
        mRequestDiscovery = true;
        mWifiP2pManager.discoverPeers(mWifiP2pChannel, null);
        return true;
    }
    
    private void startDiscovery_l() {
        if (mRequestDiscovery) {
            mWifiP2pHiddenAPI.setDeviceName(mWifiP2pChannel, mNickName, null);
            mWifiP2pManager.discoverPeers(mWifiP2pChannel, null);
        }
    }

    boolean stopDiscovery(){
        mRequestDiscovery = false;
        mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
        return true;
    }
    
    AccessPointInfo getConnectionInfo(){
        return mThisDeviceInfo;
    }
    
    boolean connect(String peerID){
        if (!mPeers.containsKey(peerID)){
            return false;
        }
        
        AccessPointInfo peer = mPeers.get(peerID);
        
        if (peer.getConnectionState() == ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED) {
            return false;
        }
        
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.mMacAddress;
        config.groupOwnerIntent = 8;

        switch (peer.getWpsConfig()){
            case AccessPointInfo.WPS_DISPLAY:
                config.wps.setup = WpsInfo.DISPLAY;
                break;
                
            case AccessPointInfo.WPS_PBC:
                config.wps.setup = WpsInfo.PBC;
                break;
                
            case AccessPointInfo.WPS_KEYPAD:
                config.wps.setup = WpsInfo.KEYPAD;
                break;
        }
        
        
        mWifiP2pManager.connect(mWifiP2pChannel, config, null);

        mCurrentPeerDeviceAddress = config.deviceAddress;
        return true;
    }

    public boolean cancelConnect() {
        if (mThisDeviceInfo.getConnectionState() == ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED
                || mThisDeviceInfo.getConnectionState() == ConnectionState.CONNECTION_STATE_WIFIP2P_DISABLED
                ) {
            return false;
        }

        mWifiP2pManager.cancelConnect(mWifiP2pChannel, null);
        return true;
    }

    public void removeGroup() {
        mWifiP2pManager.removeGroup(mWifiP2pChannel, null);
    }

    @Override
    public void onWifiP2pConnectionChanged(WifiP2pInfo info,
            NetworkInfo networkInfo) {
        synchronized(mLock){
            mWifiP2pInfo = info;
            mNetworkInfo = networkInfo;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pConnectionChanged);
    }


    @Override
    public void onWifiP2pDiscoveryChanged(int startedOrStopped) {
        synchronized(mLock){
            mWifiP2pDiscoveryStartedOrStopped = startedOrStopped;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pDiscoveryChanged);
    }


    @Override
    public void onWifiP2pPeersChanged() {
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pPeersChanged);
    }


    @Override
    public void onWifiP2pThisDeviceChanged(WifiP2pDevice p2pDevice) {
        synchronized(mLock){
            mThisDevice = p2pDevice;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pThisDeviceChanged);
    }
    
    @Override
    public void onWifiP2pStateChanged(int wifiP2pState) {
        synchronized (mLock){
            mWifiP2pState = wifiP2pState;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pStateChanged);
    }
    

    @Override
    public void onPeersAvailable(WifiP2pDeviceList deviceList) {
        synchronized (mLock){
            mWifiP2pPeers = deviceList.getDeviceList();
        }
        
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pPeersInfoAvailable);
    }


    @Override
    public void onGroupInfoAvailable(WifiP2pGroup info) {
        synchronized (mLock){
            mWifiP2pGroupInfo = info;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiP2pGroupInfoAvaliable);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // TODO Auto-generated method stub
        
    }
    
    void registerListener(Object o, ConnectionStateListener l){
        if (!mListeners.containsKey(o)){
            mListeners.put(o, l);
        }
    }
    
    void unregisterListener(Object o){
        if (mListeners.containsKey(o)){
            mListeners.remove(o);
        }
    }
    
    private void initIntentReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        mReceiver = new WifiP2pConnectionReceiver(this);
        mContext.registerReceiver(mReceiver, filter);
    }

    private void unInitIntentReceiver() {
        mContext.unregisterReceiver(mReceiver);
        mListeners.clear();
    }


    
    private static class TheResultIPAndMask {
        InetAddress mIpAddress;
        int mMask;
    }
    private static TheResultIPAndMask getInterfaceAddress(WifiP2pGroup info) {
        NetworkInterface iface;
        try {
            iface = NetworkInterface.getByName(info.getInterface());
        } catch (SocketException ex) {
            Log.w(TAG, "Could not obtain address of network interface "
                 + info.getInterface(), ex);              
            return null;
        }
        
        Log.d(TAG, "iface:" + iface.getDisplayName());

        List<InterfaceAddress> listInterfaceAddresses = iface.getInterfaceAddresses();
        for (InterfaceAddress ifAddress : listInterfaceAddresses) {
            InetAddress address = ifAddress.getAddress();

            if (address instanceof Inet4Address) {
                TheResultIPAndMask tmp = new TheResultIPAndMask();
                tmp.mIpAddress = (Inet4Address)address;
                int prefixLen = ifAddress.getNetworkPrefixLength();
                tmp.mMask = -1 << (32 - prefixLen);
                return tmp;
            }
        }

        Log.w(TAG, "Could not obtain address of network interface "
                + info.getInterface() + " because it had no IPv4 addresses."); 
        return null;
    }

    private static String getConnectedDeviceAddress(WifiP2pGroup groupInfo, HashMap<String, AccessPointInfo> nowPeers) {
        Collection<WifiP2pDevice> deviceList = groupInfo.getClientList();
        for (WifiP2pDevice device : deviceList) {
            if (nowPeers.containsKey(device.deviceAddress)) {
                return device.deviceAddress;
            }
        }
        return null;
    }
    
    private boolean isP2pMobileDevice(String primaryType){
        String type = null;
        
        if (primaryType == null){
            return false;
        }

        try{
            int firstcharIndex = primaryType.indexOf("-");
            type = primaryType.substring(0, firstcharIndex);

            if (Integer.parseInt(type)  == 10
                    || Integer.parseInt(type)  == 1){
                return true;
            }
        } catch (NumberFormatException ex){
            return false;
        } catch (IndexOutOfBoundsException ex){
            return false;
        }
        
        return false;
    }
    
    private void dispatchOnWifiP2pDiscoveredList(ArrayList<AccessPointInfo> list){
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiP2pDiscoveredList(list);
        }
    }
    
    private void dispatchOnWifiP2pConnected(AccessPointInfo info, String connectedPeerID){
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiP2pConnected(info, connectedPeerID);
        }
    }
    
    private void dispatchOnWifiP2pDisconnected(String id){
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiP2pDisconnected(id);
        }
    }
    
    private void dispatchOnWifiP2pStateChanged(String id, int state){
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiP2pStateChanged(id, ConnectionState.CONNECTION_TYPE_WIFIP2P, state);
        }
    }
    
    private class ConnectionHandler extends Handler {
        private ConnectionHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "msg:" + msg);
            
            switch (msg.what){
            case Msg_WifiP2pConnectionChanged:
                synchronized (mLock){
                    if (mNetworkInfo.isConnected() == true){
                        mWifiP2pManager.requestGroupInfo(mWifiP2pChannel, WifiP2pManagement.this);
                        // mWifiP2pManager.requestConnectionInfo(mWifiP2pChannel, WifiP2pManagement.this);
                        mThisDeviceInfo.setConnectionState(ConnectionState.CONNECTION_STATE_WIFIP2P_CONNECTED);
                        startDiscovery_l();
                    } else {
                        mThisDeviceInfo.setConnectionState(ConnectionState.CONNECTION_STATE_WIFIP2P_DISCONNECTED);
                        dispatchOnWifiP2pDisconnected(mCurrentPeerDeviceAddress);
                        mCurrentPeerDeviceAddress = null;
                    }
                }
                break;
                
            case Msg_WifiP2pDiscoveryChanged:
                synchronized (mLock) {
                    if (mWifiP2pDiscoveryStartedOrStopped == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                        Log.d(TAG, "Discovery started");
                    } else if (mWifiP2pDiscoveryStartedOrStopped == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                        Log.d(TAG, "Discovery stopped");
                        /*startDiscovery_l();*/
                    }
                }
                break;
                
            case Msg_WifiP2pPeersChanged:
                synchronized(mLock) {
                    mWifiP2pManager.requestPeers(mWifiP2pChannel, WifiP2pManagement.this);
                }
                break;
            
            case Msg_WifiP2pThisDeviceChanged:
                synchronized(mLock){
                    Log.d(TAG, "mThisDevice:" +mThisDevice.toString());
                    if (mThisDeviceUpdatedFirstTime == false){
                        mPreviousDeviceName = mThisDevice.deviceName;
                        mThisDeviceUpdatedFirstTime = true;
                    }
                    mThisDeviceInfo.mFriendlyName = mThisDevice.deviceName;
                    mThisDeviceInfo.mMacAddress = mThisDevice.deviceAddress;
                }
                
                break;
                
            case Msg_WifiP2pGroupInfoAvaliable:
                synchronized (mLock){
                    //Collection<WifiP2pDevice> groupDevices = mWifiP2pGroupInfo.getClientList();
                    TheResultIPAndMask ipAndMask = getInterfaceAddress(mWifiP2pGroupInfo);
                    // String connectedDeviceAddress = getConnectedDeviceAddress(mWifiP2pGroupInfo, mPeers);
                    String connectedDeviceAddress = mCurrentPeerDeviceAddress;
                    if (connectedDeviceAddress == null) {
                        Log.e(TAG, "can't find the current connected device");
                    }
                    if (ipAndMask == null) {
                        Log.e(TAG, "can't get the ip and address.");
                    } else {
                        Log.d(TAG, "IP:" + ipAndMask.mIpAddress.getHostAddress());

                        mThisDeviceInfo.mIpAddress = ipAndMask.mIpAddress;
                        mThisDeviceInfo.mMask = ipAndMask.mMask;

                        dispatchOnWifiP2pConnected(mThisDeviceInfo, connectedDeviceAddress);
                    }
                }
                break;
                 
            case Msg_WifiP2pPeersInfoAvailable:
                synchronized (mLock){
                    mPeers.clear();
                    for (WifiP2pDevice device : mWifiP2pPeers){
                        if (!isP2pMobileDevice(device.primaryDeviceType)){
                            continue;
                        }
                        
                        byte wpsConfig = AccessPointInfo.WPS_INVALID;
                        if (device.wpsPbcSupported()){
                            wpsConfig = AccessPointInfo.WPS_PBC;
                        } else if (device.wpsDisplaySupported()){
                            wpsConfig = AccessPointInfo.WPS_DISPLAY;
                        } else if (device.wpsKeypadSupported()){
                            wpsConfig = AccessPointInfo.WPS_KEYPAD;
                        }
                        
                        if (mPeers.containsKey(device.deviceAddress)){
                            boolean isIvyDevice = isIvyDevice(device.deviceName);
                            mPeers.get(device.deviceAddress).setIsIvyWifiDirectDevice(isIvyDevice);
                            if (isIvyDevice) {
                                String name = getNickNameFromDeviceName(device.deviceName);
                                mPeers.get(device.deviceAddress).setFriendlyName(name);
                            } else {
                                mPeers.get(device.deviceAddress).setFriendlyName(device.deviceName);
                            }

                            mPeers.get(device.deviceAddress).setWpsConfig(wpsConfig);
                        } else {
                            AccessPointInfo tmpAccessPointInfo =
                                    new AccessPointInfo(device.deviceAddress, device.deviceName, wpsConfig);

                            boolean isIvyDevice = isIvyDevice(device.deviceName);
                            tmpAccessPointInfo.setIsIvyWifiDirectDevice(isIvyDevice);
                            if (isIvyDevice) {
                                String name = getNickNameFromDeviceName(device.deviceName);
                                tmpAccessPointInfo.setFriendlyName(name);
                            } else {
                                tmpAccessPointInfo.setFriendlyName(device.deviceName);
                            }

                            mPeers.put(device.deviceAddress, tmpAccessPointInfo);
                        }
                    }
                    ArrayList<AccessPointInfo> list = new ArrayList<AccessPointInfo>(mPeers.values());
                    dispatchOnWifiP2pDiscoveredList(list);
                }
                break;
                
            case Msg_WifiP2pStateChanged:
                synchronized (mLock){
                    if (mWifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                        Log.d(TAG, "WifiP2p Enabled");
                        mThisDeviceInfo.setConnectionState(ConnectionState.CONNECTION_STATE_WIFIP2P_ENABLED);
                    } else if (mWifiP2pState == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                        mThisDeviceInfo.setConnectionState(ConnectionState.CONNECTION_STATE_WIFIP2P_DISABLED);
                        Log.d(TAG, "WifiP2p Disabled");
                    } else {
                        mThisDeviceInfo.setConnectionState(ConnectionState.CONNECTION_UNKNOWN);
                        Log.d(TAG, "mWifiP2pState:" + mWifiP2pState);
                    }
                }
                dispatchOnWifiP2pStateChanged(mThisDeviceInfo.mMacAddress, mThisDeviceInfo.getConnectionState());
                break;
            }
        }
    }



    private static String generateDeviceName() {
        StringBuffer deviceNameBuffer = new StringBuffer();
        deviceNameBuffer.append("IvyShare_");

        int x=(int)(Math.random()*1000);
        deviceNameBuffer.append(Integer.toString(x) + "_");
        
        LocalSetting localSetting = LocalSetting.getInstance();
        String nickname = localSetting.getMySelf().mNickName;
        deviceNameBuffer.append(nickname);

        return deviceNameBuffer.toString();
    }

    private static boolean isIvyDevice(String deviceName) {
        if (deviceName == null) {
            return false;
        }

        if (!deviceName.startsWith("IvyShare_")) {
            return false;
        }
        
        String arr[] = deviceName.split("_");
        if (arr.length < 3) {
            return false;
        }

        return true;
    }

    private static String getNickNameFromDeviceName(String deviceName) {
        if (deviceName == null) {
            return null;
        }

        if (!deviceName.startsWith("IvyShare_")) {
            return null;
        }
        
        String arr[] = deviceName.split("_");
        if (arr.length < 3) {
            return null;
        }

        return arr[2];        
    }
}
