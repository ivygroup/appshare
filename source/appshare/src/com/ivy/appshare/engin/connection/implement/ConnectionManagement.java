package com.ivy.appshare.engin.connection.implement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.ivy.appshare.engin.connection.ConnectionState;
import com.ivy.appshare.engin.control.LocalSetting;


public class ConnectionManagement implements WifiStateChangedListener {

    private final static String TAG = "ConnectionManagement";
    
    private final static String SSID_PREFIX = "ivyappshare";
    private final static String SSID_PASSWORD = "ivyappshare";
    
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private HashMap<Object, ConnectionStateListener> mListeners = 
            new HashMap<Object, ConnectionStateListener>();
    private WifiManager mWifiManager;
    private WifiManagerHiddenAPI mWifiManagerHiddenAPI;
    private MulticastLock mMulticastLock;
    private Object mChannel;
    private HandlerThread mHandlerThread;
    private ConnectionHandler mConnectionHandler;
    private static final int sSDKVersion = Build.VERSION.SDK_INT;
    private String mNickName;
    private LocalSetting mLocalSetting;
    private boolean mP2pSupported;
    private ArrayList<String> mManufacturerList = new ArrayList<String>();
    
    private WifiManagerHiddenAPI.WifiHiddenAPI mWifiHiddenAPI;
//    private WifiManagerHiddenAPI.WifiP2pHiddenAPI mWifiP2pHiddenAPI;
    private WifiConfiguration mUserWifiAPConfiguration;
    private WifiP2pManagement mWifiP2pManagement;
    
    private boolean mIsAirplaneEnabled;
    private WifiInfo mWifiInfo;
    private int mWifiApState;
    private int mWifiState;
    private int mPreviousWifiState;
    private List<ScanResult> mScanResults;
    private Object mLock = new Object();
    private final static int Msg_AirplaneModeChanged = 0;
    private final static int Msg_WifiConnected = 1;
    private final static int Msg_WifiDisconnected = 2;
    private final static int Msg_WifiHotspotStateChanged = 3;
    private final static int Msg_ScanResultAvailable = 4;
    private final static int Msg_StartScan = 5;
    private final static int Msg_GetHotspotIP = 6;
    private final static int Msg_WifiConnecting = 7;
    private final static int Msg_WifiStateChanged = 8;
    private boolean mRequestScan = false;
    private WifiConfiguration mIvyNetworkWifiConfiguration = new WifiConfiguration();


    
    private AccessPointInfo mConnectedAP = new AccessPointInfo();
    private HashMap<String, AccessPointInfo> mAccessPoints = new HashMap<String, AccessPointInfo>();
    private String mMac;
    private WifiConfiguration mIvyHotspotWifiConfigation;
    private static final ArrayList<String> PHONE_MATCHER = new ArrayList<String>();
    private SharedPreferences mPreferences;
    private static final String KEY_SSID = "SSID";
    private static final String VALUE_NO_SSID = "NONE";
    
    
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                
                if (action.equals(Intent.ACTION_SCREEN_ON)){
                    Log.d(TAG, "Screen On");
                    if (!mMulticastLock.isHeld()){
                        mMulticastLock.acquire();
                    }
                } else if (action.equals(Intent.ACTION_SCREEN_OFF)){
                    Log.d(TAG, "Screen Off");
                    if (mMulticastLock.isHeld()){
                        mMulticastLock.release();
                    }
                }
            }
    };
    
    public ConnectionManagement(){
        Log.d(TAG, "Constructor");
    }
    

    
    public boolean initialize(Context context){
        Log.d(TAG, "initialize");
        if (mContext != null){
            return false;
        }
        
        mContext = context;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiManagerHiddenAPI = WifiManagerHiddenAPI.getInstance(context);
        
        mWifiHiddenAPI = mWifiManagerHiddenAPI.getWifiHiddenAPI();
        //mWifiP2pHiddenAPI = mWifiManagerHiddenAPI.getWifiP2pHiddenAPI();
        
        mHandlerThread = new HandlerThread("WifiConnectionControler");
        mHandlerThread.start();
        mConnectionHandler = new ConnectionHandler(mHandlerThread.getLooper());
        
        
        if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
            mChannel = mWifiHiddenAPI.initialize(mContext, mContext.getMainLooper(), null);
        } else if ((sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            || (sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)){
            mWifiHiddenAPI.asyncConnect(mContext, mConnectionHandler);
        }
        
        mMulticastLock = mWifiManager.createMulticastLock("ConnectionManagement");
        if (!mMulticastLock.isHeld()){
            mMulticastLock.acquire();
        }
        
        initIntentReceiver();
        
        mLocalSetting = LocalSetting.getInstance();
        mUserWifiAPConfiguration = mWifiHiddenAPI.getWifiApConfiguration();
        
        
        mP2pSupported = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
        mP2pSupported = mP2pSupported && isP2pSupportedByUs();
        
        if (mP2pSupported){
            mWifiP2pManagement = new WifiP2pManagement();
        }
        
        if (mWifiP2pManagement != null){
            mWifiP2pManagement.initialize(mContext);
        }
        
        //mPreferences = mContext.getSharedPreferences("ConnectionMgmt", Context.MODE_PRIVATE);
        forgetNetworkIfPossible();

        return true;
    }
    
    public boolean unInitialize(){
        Log.d(TAG, "unInitialize");
        if (mContext == null){
            return false;
        }
        
        if ((mMulticastLock != null) && mMulticastLock.isHeld()){
            mMulticastLock.release();
        }
        
        mConnectionHandler.removeCallbacksAndMessages(null);
        if (mHandlerThread != null){
            mHandlerThread.quit();
        }
                
        if (mWifiP2pManagement != null){
            mWifiP2pManagement.unInitialize();
        }
        
        unInitIntentReceiver();
        mListeners.clear();

        mContext = null;
        
        return true;
    }
    
    public void registerListener(Object object, ConnectionStateListener l){
        Log.d(TAG, "registerListener, o:" + object);
        if (!mListeners.containsKey(object)){
            mListeners.put(object, l);
        }
        
        if (mP2pSupported){
            mWifiP2pManagement.registerListener(object, l);
        }
        
    }

    public void unregisterListener(Object object){
        Log.d(TAG, "unregisterListener, o:" + object);
        if (mListeners.containsKey(object)){
            mListeners.remove(object);
        }
        
        if (mP2pSupported){
            mWifiP2pManagement.unregisterListener(object);
        }
    }
        
    public boolean isAirplanEnabled(){
        boolean isAirplaneMode = (Settings.System.getInt(mContext.getContentResolver(), 
                Settings.System.AIRPLANE_MODE_ON, 0) != 0);
        return isAirplaneMode;
    }
    
    public AccessPointInfo getConnectionInfo(){
        Log.d(TAG, "mConnectedAP:" + mConnectedAP.toString());
        return mConnectedAP;
    }

    public AccessPointInfo getWifiP2pConnectionInfo(){
        if (!mP2pSupported){
            return null;
        }
        
        return mWifiP2pManagement.getConnectionInfo();
    }
    
    public ArrayList<AccessPointInfo> getScanResult(){
        synchronized (mLock){
            return new ArrayList<AccessPointInfo>(mAccessPoints.values());
        }
    }
    
    public boolean createHotspot(){
        mNickName = mLocalSetting.getMySelf().mNickName;
        Log.d(TAG, "createHotspot for " + mNickName);

        if ((mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED)
                || (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING)){
			Log.d(TAG, "Do NOT need to create hotspot. State: " + mWifiApState);
            return true;
        }
        
        mIvyHotspotWifiConfigation = new WifiConfiguration();
        if (false == generateIvyHotspotWifiConfigation()){
            return false;
        }
        
        setSoftapEnabled(true);
        
        return true;
    }

    public boolean createHotspot(int shareCount) {
        mNickName = mLocalSetting.getMySelf().mNickName;
        Log.d(TAG, "createHotspot for " + mNickName);

        if ((mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED)
                || (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING)){
            Log.d(TAG, "Do NOT need to create hotspot. State: " + mWifiApState);
            return true;
        }
        
        mIvyHotspotWifiConfigation = new WifiConfiguration();
        if (false == generateIvyHotspotWifiConfigation2(shareCount)){
            return false;
        }
        
        setSoftapEnabled(true);
        
        return true;        
    }

    public boolean disableHotspot(){
        Log.d(TAG, "disableHotspot");
        if ((mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLED)
                || (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLING)){
                return true;
        }
        
        WifiConfiguration currentConfig = mWifiHiddenAPI.getWifiApConfiguration();
        if (true == isWifiAPConfigurationChangedByUs(currentConfig)){
            mWifiHiddenAPI.setWifiApConfiguration(mUserWifiAPConfiguration);
        }
        
        mIvyHotspotWifiConfigation = null;
        setSoftapEnabled(false);
        return true;
    }
    
    public boolean connectToIvyNetwork(String ssid){
        Log.d(TAG, "connectToIvyNetwork");
        
        for (Entry<String, AccessPointInfo> info : mAccessPoints.entrySet()){
            if (info.getValue().mSSID.equals(ssid)){
                generateIvyNetworkWifiConfiguration(info.getValue());

                mWifiManager.disconnect();
                if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
                    mWifiManagerHiddenAPI.getWifiHiddenAPI().connect(mChannel, mIvyNetworkWifiConfiguration);
                } else if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN_MR1){
                    mWifiManagerHiddenAPI.getWifiHiddenAPI().connect(null, mIvyNetworkWifiConfiguration);
                } else{
                    mWifiManagerHiddenAPI.getWifiHiddenAPI().connectNetwork(mIvyNetworkWifiConfiguration);
                }
                //storeNetworkInfoToPreference(mIvyNetworkWifiConfiguration.SSID);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean disconnectFromIvyNetwork(){
        Log.d(TAG, "disconnectFromIvyNetwork");

        if ((mIvyNetworkWifiConfiguration == null)
            || (mIvyNetworkWifiConfiguration.SSID == null)
            || (mIvyNetworkWifiConfiguration.SSID.length() == 0)){
            return false;
        }
        
        
        //mConnectedAP will be reset when get WIFI_DISCONNECTED intent.
        //mWifiManager.disconnect();
        forgetNetwork(AccessPointInfo.removeDoubleQuotes(mIvyNetworkWifiConfiguration.SSID));
        //resetNetworkInfoFromPreference();
        return true;
    }
    
    public boolean startScan(){
        Log.d(TAG, "startScan");
        if (false == mWifiManager.isWifiEnabled()){
            return false;
        }
        synchronized (mLock){
            if (mRequestScan == true){
                return false;
            }
            
            mRequestScan = true;
        }
        mConnectionHandler.sendEmptyMessage(Msg_StartScan);
        return true;
    }
    
    public boolean isWifiEnabled(){
        return mWifiManager.isWifiEnabled();
    }

    public boolean enableWifi() {
        return mWifiManager.setWifiEnabled(true);
    }

    public boolean stopScan(){
        Log.d(TAG, "stopScan");
        if (false == mWifiManager.isWifiEnabled()){
            return false;
        }
        
        synchronized (mLock){
            if (mRequestScan == false){
                return false;
            }
            mRequestScan = false;
        }
        mConnectionHandler.removeMessages(Msg_StartScan);
        return true;
    }

    public boolean wifiP2pStartDiscovery(){
        Log.d(TAG, "wifiP2pStartDiscovery");
        if (mP2pSupported){
            mWifiP2pManagement.startDiscovery();
            return true;
        }
        return false;
    }
    
    public boolean wifiP2pStopDiscovery(){
        Log.d(TAG, "wifiP2pStopDiscovery");
        if (mP2pSupported){
            mWifiP2pManagement.stopDiscovery();
            return true;
        }
        return false;
    }
    
    public InetAddress getBroadcastAddress(){
        DhcpInfo dhcp = mWifiManager.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) (broadcast >> (k * 8));
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isWifiP2pSupported(){
        return mP2pSupported;
    }
    
    public boolean connectToWifiP2pPeer(String peerID){
        Log.d(TAG, "connectToWifiP2pPeer");
        if (!mP2pSupported){
            return false;
        }
        
        return mWifiP2pManagement.connect(peerID);
    }
    
    public boolean cancelConnectWifiP2p() {
        if (!mP2pSupported){
            return false;
        }

        return mWifiP2pManagement.cancelConnect();
    }

    public boolean removeGroupOfWifiP2p() {
        if (!mP2pSupported){
            return false;
        }

        mWifiP2pManagement.removeGroup();
        return true;
        
    }

    @Override
    public void onAirplaneModeChanged(boolean enabled) {
        synchronized(mLock){
            mIsAirplaneEnabled = enabled;
        }
        
        mConnectionHandler.sendEmptyMessage(Msg_AirplaneModeChanged);
    }
    
    @Override
    public void onWifiConnected(WifiInfo wifiInfo) {
        synchronized(mLock){
            mWifiInfo = wifiInfo;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiConnected);
    }

    @Override
    public void onWifiDisconnected() {
        mConnectionHandler.sendEmptyMessage(Msg_WifiDisconnected);
    }
    
    @Override
    public void onWifiHotspotStateChanged(int state) {
        synchronized(mLock){
            Log.d(TAG, "onWifiHotspotStateChanged:" + state);
            mWifiApState = state;
        }
        
        mConnectionHandler.sendEmptyMessage(Msg_WifiHotspotStateChanged);
    }
    
    @Override
    public void onWifiConnecting() {
        mConnectionHandler.sendEmptyMessage(Msg_WifiConnecting);
    }
    
    @Override
    public void onScanResultAvailable(List<ScanResult> results) {
        synchronized(mLock){
            if ((mRequestScan == false)
                || (results == null)) {
                return;
            }
            mScanResults = results;
        }
        mConnectionHandler.sendEmptyMessage(Msg_ScanResultAvailable);
    }
    
    @Override
    public void onWifiStateChanged(int state) {
        synchronized(mLock){
            mWifiState = state;
        }
        mConnectionHandler.sendEmptyMessage(Msg_WifiStateChanged);
    }
    
    private void initManufacturerList(){
        mManufacturerList.add("samsung");
    }
    
    private void forgetNetworkIfPossible() {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        if (configs == null) {
            return;
        }

        for(WifiConfiguration c : configs){
            if (isIvyHotspot(c)){
                mWifiHiddenAPI.forgetNetwork(mChannel, c.networkId);
            }
        }
        
     
        /*
        String ssid = mPreferences.getString(KEY_SSID, VALUE_NO_SSID);
        if (ssid.equals(VALUE_NO_SSID) == true){
            return;
        }
        
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if ((configs == null) || (configs.size() == 0)){
            return;
        }

        for (WifiConfiguration config : configs){
            if (AccessPointInfo.removeDoubleQuotes(config.SSID).equals(ssid)){
                mWifiHiddenAPI.forgetNetwork(mChannel, config.networkId);
            }
        }
        
        // No matter the saved SSID exist in wifi configuration list or not,
        // reset to "NONE" at start up.
        resetNetworkInfoFromPreference();
        */
    }
    
    private boolean forgetNetwork(String ssid){
        Log.d(TAG, "forgetNetwork, mConnectedAP.mSSID:" + mConnectedAP.mSSID + ", ssid:" + ssid);

        if ((mConnectedAP.mSSID != null) 
                && mConnectedAP.mSSID.equals(ssid)){
            Log.d(TAG, "forgetNetwork:" + ssid + ", networkId:" + mConnectedAP.mNetworkId);
            mWifiHiddenAPI.forgetNetwork(mChannel, mConnectedAP.mNetworkId);
            return true;
        }
        
        for (Entry<String, AccessPointInfo> info : mAccessPoints.entrySet()){
            if (info.getValue().mSSID.equals(ssid)){
                Log.d(TAG, "forgetNetwork:" + ssid + ", networkId:" + mConnectedAP.mNetworkId);
                mWifiHiddenAPI.forgetNetwork(mChannel, info.getValue().mNetworkId);
                return true;
            }
        }
        return false;
    }
    
    private void storeNetworkInfoToPreference(String ssid){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_SSID, AccessPointInfo.removeDoubleQuotes(ssid));
        editor.commit();
    }
    
    private void resetNetworkInfoFromPreference(){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_SSID, VALUE_NO_SSID);
        editor.commit();
    }
    
    private void setSoftapEnabled(boolean enable) {
        int wifiState = mWifiManager.getWifiState();
        
         
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
               (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mPreviousWifiState = wifiState;
            mWifiManager.setWifiEnabled(false);
        }
        
        mWifiHiddenAPI.setWifiApEnabled(mIvyHotspotWifiConfigation, enable);
        
        if ((enable == false) 
                && ((wifiState == WifiManager.WIFI_STATE_DISABLED)
                    || (wifiState == WifiManager.WIFI_STATE_DISABLING))                
                && (mPreviousWifiState == WifiManager.WIFI_STATE_ENABLED) 
                    || (mPreviousWifiState == WifiManager.WIFI_STATE_ENABLING)){
            mWifiManager.setWifiEnabled(true);
        }
    }
    
    private void initIntentReceiver(){
        
        mIntentFilter = new IntentFilter(WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);


        mReceiver = new ConnectionReceiver(mContext, this);
        mContext.registerReceiver(mReceiver, mIntentFilter);
        
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenStateFilter);
    }
    
    private boolean isP2pSupportedByUs(){
        /*if ((sSDKVersion >= Build.VERSION_CODES.JELLY_BEAN)
            && (PHONE_MATCHER.contains(Build.MANUFACTURER))){
            return true;
        }*/
        
        if (sSDKVersion >= Build.VERSION_CODES.JELLY_BEAN){
            return true;
        }
        return false;
    }
    
    private void unInitIntentReceiver() {
       
        mContext.unregisterReceiver(mReceiver);        
        mContext.unregisterReceiver(mScreenStateReceiver);
    }

    
    private void dispatchOnAirplaneModeChanged(boolean enabled){
        Log.d(TAG, "Airplane mode is " + ((enabled) ? "enabled" : "disabled"));
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onAirplaneModeChanged(enabled);
        }
    }
    
    private void dispatchOnWifiConnected(AccessPointInfo connectionInfo){
        Log.d(TAG, "WifiConnected:" + connectionInfo.getConnectionState());
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiConnected(connectionInfo);
        }
    }
    
    private void dispatchOnWifiDisconnected(String disconnectedSSID, int connectionType, int  wifiState){
        Log.d(TAG, "WifiDisconnected: ssid:" + disconnectedSSID + ", previousState:" + wifiState);
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiDisconnected(disconnectedSSID, connectionType, wifiState);
        }
    }
    
    private void dispatchOnWifiHotspotStateChanged(int connectionType, int state){
        Log.d(TAG, "WifiHotspotStateChanged:" + state);
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiHotspotStateChanged(connectionType, state);
        }
    }
    
    private void dispatchOnScanResultAvailable(ArrayList<AccessPointInfo> list){
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onScanResultAvailable(list);
        }
    }
    
    private void dispatchOnHotspotIPAvailable(InetAddress addr){
        if (addr == null) {
            return;
        }
        Log.d(TAG, "WifiHotspotIPAvailable:" + addr.getHostAddress());
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onIvyHotspotIPAvailable(addr);
        }
    }
    
    private void dispatchOnWifiConnecting(AccessPointInfo connetionInfo){
        Log.d(TAG, "WifiConnecting:" + connetionInfo.getConnectionState());
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiConnecting(connetionInfo);
        }
    }
    
    private void dispatchOnWifiEnabled(){
        Log.d(TAG, "WifiEnabled");
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiEnabled();
        }   
    }
    
    private void dispatchOnWifiDisabled(){
        Log.d(TAG, "WifiDisabled");
        for (Entry<Object, ConnectionStateListener> l : mListeners.entrySet()){
            l.getValue().onWifiDisabled();
        }
    }
    
    private boolean isWifiAPConfigurationChangedByUs(WifiConfiguration wifiApConfig){
        if (mIvyHotspotWifiConfigation == null || mIvyHotspotWifiConfigation.SSID == null) {
            return false;
        }
        if (wifiApConfig == null || wifiApConfig.SSID == null) {
            return false;
        }

        if ((mIvyHotspotWifiConfigation.SSID.equals(wifiApConfig.SSID))
            && (mIvyHotspotWifiConfigation.BSSID.equals(wifiApConfig.BSSID))
            && (wifiApConfig.allowedAuthAlgorithms.get(AuthAlgorithm.OPEN))
            && (wifiApConfig.allowedKeyManagement.get(4))
            && (mIvyHotspotWifiConfigation.preSharedKey.equals(wifiApConfig.preSharedKey))){
            return true;
        }
        
        return false;
    }
    
	private String getPrefixSSID(int maxNameLength) {
		int totalNum = 0;
		String prefixName = "";

		for (int i = 0; totalNum < maxNameLength && i < mNickName.length(); i ++) {
			String tempStr = mNickName.substring(i, i + 1);
			int num = tempStr.getBytes().length;

			if (totalNum + num > maxNameLength) {
				break;
			}

			prefixName += tempStr;
			totalNum += num;
		}

		return prefixName;
	}
    
    private boolean generateIvyHotspotWifiConfigation(){
        if (mMac == null){
            //Try to get device MAC address
            mMac = mWifiManager.getConnectionInfo().getMacAddress();
            if (mMac == null){
                //Only find one case that we can't get mac address.
                //After phone[bkb] booted, if wifi, wifi-hotspot are all disabled,
                //we can't get mac. If this happens, we will try enable
                //wlan at first, so do nothing here.
                return false;
            }
        }

        mIvyHotspotWifiConfigation.BSSID = mMac;

        String s = mMac.replaceAll(":", "");
        long l = Long.parseLong(s, 16);
        long ssid = l*2 + 1;
        long pass = (l + 10000000) * 3 ;

        //TODO: Adding battery info later
        mIvyHotspotWifiConfigation.SSID = getPrefixSSID(15) + "-" + Long.toString(ssid);// + String.format("%03d", mBatteryLevel);
        mIvyHotspotWifiConfigation.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
        mIvyHotspotWifiConfigation.allowedKeyManagement.clear();
        if (sSDKVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            mIvyHotspotWifiConfigation.allowedKeyManagement.set(4/*KeyMgmt.WPA_PSK*/);
        } else {
            mIvyHotspotWifiConfigation.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        mIvyHotspotWifiConfigation.preSharedKey= Long.toString(pass);
        
        Log.d(TAG, "Hotspot SSID: " + mIvyHotspotWifiConfigation.SSID
					+ ", Password: " + mIvyHotspotWifiConfigation.preSharedKey);
        
        return true;
    }
    
    private boolean generateIvyHotspotWifiConfigation2(int shareCount) {
        if (mMac == null){
            //Try to get device MAC address
            mMac = mWifiManager.getConnectionInfo().getMacAddress();
            if (mMac == null){
                //Only find one case that we can't get mac address.
                //After phone[bkb] booted, if wifi, wifi-hotspot are all disabled,
                //we can't get mac. If this happens, we will try enable
                //wlan at first, so do nothing here.
                return false;
            }
        }
        mIvyHotspotWifiConfigation.BSSID = mMac;

        StringBuilder mySSID = new StringBuilder();
        mySSID.append(SSID_PREFIX);
        mySSID.append("-");
        mySSID.append(shareCount);
        mySSID.append("-");
        mySSID.append(getPrefixSSID(30 - mySSID.length()));
        mIvyHotspotWifiConfigation.SSID = mySSID.toString();
        mIvyHotspotWifiConfigation.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
        mIvyHotspotWifiConfigation.allowedKeyManagement.clear();
        if (sSDKVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            mIvyHotspotWifiConfigation.allowedKeyManagement.set(4/*KeyMgmt.WPA_PSK*/);
        } else {
            mIvyHotspotWifiConfigation.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        mIvyHotspotWifiConfigation.preSharedKey= SSID_PASSWORD;
        
        Log.d(TAG, "Hotspot SSID: " + mIvyHotspotWifiConfigation.SSID
                    + ", Password: " + mIvyHotspotWifiConfigation.preSharedKey);
        
        return true;
    }

    private void generateIvyNetworkWifiConfiguration(AccessPointInfo selectedAp){
        String s = selectedAp.mBSSID.replaceAll(":", "");
        Long l = Long.parseLong(s, 16);
        long pwd = ((l + (10000000))) * 3 ;
        
        // mIvyNetworkWifiConfiguration.preSharedKey = '"' + Long.toString(pwd) + '"';
        mIvyNetworkWifiConfiguration.preSharedKey = '"' + SSID_PASSWORD + '"';
        mIvyNetworkWifiConfiguration.allowedKeyManagement.set(KeyMgmt.WPA_PSK);

        mIvyNetworkWifiConfiguration.SSID = '"' + selectedAp.mSSID + '"';
        mIvyNetworkWifiConfiguration.BSSID = selectedAp.mBSSID;
    } 
    

    
    public static InetAddress intToInetAddress(int hostAddress){
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                                (byte) (0xff & (hostAddress >> 8)),
                                (byte) (0xff & (hostAddress >> 16)),
                                (byte) (0xff & (hostAddress >> 24))};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private boolean isIvyHotspot(ScanResult result){
        String ssid = AccessPointInfo.removeDoubleQuotes(result.SSID);
        String bssid = result.BSSID;
        
        return isIvyHotspot(ssid, bssid);
    }
    
    
    private boolean isIvyHotspot(WifiInfo info){
        String ssid = AccessPointInfo.removeDoubleQuotes(info.getSSID());
        String bssid = info.getBSSID();
        
        return isIvyHotspot(ssid, bssid);
    }
    
    private boolean isIvyHotspot(WifiConfiguration config){
        String ssid = AccessPointInfo.removeDoubleQuotes(config.SSID);
        String bssid = config.BSSID;
        
        return isIvyHotspot(ssid, bssid);
    }
    
    /*
    private boolean isIvyHotspot(String ssid, String bssid){
        if (ssidIsInIvyRoom(ssid)) {
            return true;
        }

        long ssid_int = -1;
        long l = -1;
        
        
        if ((ssid == null) 
                || (ssid.length() == 0) 
                || (bssid == null) 
                || (bssid.length() == 0)){
            return false;
        }
        
        try{
            ssid_int = Long.parseLong(ssid.substring(ssid.lastIndexOf("-") + 1));
            String s = bssid.replace(":", "");
            l = Long.parseLong(s, 16);
        } catch (NumberFormatException ex){
            return false;
        }
        
        if (ssid_int == l*2 + 1){
            Log.d(TAG, "IvyNetwork found:" + ssid);
            return true;
        }
        
        return false;
    }
    */
    
    private boolean isIvyHotspot(String ssid, String bssid) {
        if (ssidIsInIvyRoom(ssid)) {
            return true;
        }

        /*
        if ((ssid == null) 
                || (ssid.length() == 0) 
                || (bssid == null) 
                || (bssid.length() == 0)){
            return false;
        } //*/
        
        if ((ssid == null) || (ssid.length() == 0)) {
            return false;
        }

        String[] arraySSID = ssid.split("-", 3);
        if (arraySSID.length < 3) {
            return false;
        }
        if (arraySSID[0].equals(SSID_PREFIX)) {
            return true;
        }

        return false;
    }

    private boolean ssidIsInIvyRoom(String ssid) {
        if (ssid == null) {
            return false;
        }

        return mAccessPoints.containsKey(ssid);
    }


    private class ConnectionHandler extends Handler {
        private ConnectionHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "msg:" + msg);
            
            switch (msg.what){
            case Msg_AirplaneModeChanged:
                boolean enabled = false;
                synchronized (mLock){
                    enabled = mIsAirplaneEnabled;
                }
                dispatchOnAirplaneModeChanged(enabled);
                break;
                
            case Msg_WifiConnected:
                synchronized (mLock){
                    mConnectedAP.update(mWifiInfo);
                    if (isIvyHotspot(mWifiInfo)){
                        mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_IVY_CONNECTED);
                    } else {
                        mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_PUBLIC_CONNECTED);
                    }
                }
                dispatchOnWifiConnected(mConnectedAP);
                break;
                
            case Msg_WifiDisconnected:
                String ssid = null;
                int newState = ConnectionState.CONNECTION_UNKNOWN;
                synchronized(mLock){
                	int wifiState = mConnectedAP.getConnectionState();
                    ssid = mConnectedAP.mSSID;
                    mConnectedAP.reset();
                    if (wifiState == ConnectionState.CONNECTION_STATE_WIFI_PUBLIC_CONNECTING
                    		|| wifiState == ConnectionState.CONNECTION_STATE_WIFI_PUBLIC_CONNECTED) {
                    	newState = ConnectionState.CONNECTION_STATE_WIFI_PUBLIC_DISCONNECTED;
                    } else if (wifiState == ConnectionState.CONNECTION_STATE_WIFI_IVY_CONNECTING
                    		|| wifiState == ConnectionState.CONNECTION_STATE_WIFI_IVY_CONNECTED) {
                    	newState = ConnectionState.CONNECTION_STATE_WIFI_IVY_DISCONNECTED;
                    } else {
                    	newState = ConnectionState.CONNECTION_STATE_WIFI_DISCONNECTED;
                    }

                    mConnectedAP.setConnectionState(newState);
                }
                dispatchOnWifiDisconnected(ssid, ConnectionState.CONNECTION_TYPE_WIFI, newState);
                break;
                
            case Msg_WifiHotspotStateChanged:
                int state = ConnectionState.CONNECTION_UNKNOWN;
                synchronized (mLock){
                    if (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED){
                        state = ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED;
                        WifiConfiguration wifiApConfig = mWifiHiddenAPI.getWifiApConfiguration();
                        if (mIvyHotspotWifiConfigation != null){
                            mConnectedAP.update(mIvyHotspotWifiConfigation);
                        } else if (wifiApConfig != null){
                            mConnectedAP.update(wifiApConfig);
                        }
                        
                        sendEmptyMessage(Msg_GetHotspotIP);
                    } else if (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING){
                        state = ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLING;
                    } else if (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLED){
                        state = ConnectionState.CONNECTION_STATE_HOTSPOT_DISABLED;
                    } else if (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLING){
                        state = ConnectionState.CONNECTION_STATE_HOTSPOT_DISABLING;
                    }

                    mConnectedAP.setConnectionState(state);
                }

                dispatchOnWifiHotspotStateChanged(ConnectionState.CONNECTION_TYPE_HOTSPOT, state);
                break;
                
            case Msg_ScanResultAvailable:
                synchronized (mLock){
                	mAccessPoints.clear();
                    for (ScanResult result : mScanResults){
                        if (result.SSID == null || result.SSID.length() == 0
                                || result.capabilities.contains("[IBSS]")
                                || !isIvyHotspot(result)){
                            continue;
                        }
                        
                        mAccessPoints.put(result.SSID, new AccessPointInfo(result));
                    }
                }

                ArrayList<AccessPointInfo> list = new ArrayList<AccessPointInfo>(mAccessPoints.values());
                dispatchOnScanResultAvailable(list);
                break;
                
            case Msg_StartScan:
                mWifiManager.startScan();
                sendEmptyMessageDelayed(Msg_StartScan, 5000);
                break;
                
            case Msg_GetHotspotIP:
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        int ip = native_getHotspotIp(null);
                        mConnectedAP.mIpAddress = intToInetAddress(ip);
                        Log.d(TAG, "Hotspot IP:" + mConnectedAP.mIpAddress.getHostAddress());
                        dispatchOnHotspotIPAvailable(mConnectedAP.mIpAddress);
                    }
                    
                }).start();
                break;
                
            case Msg_WifiConnecting:
                synchronized (mLock){
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    if ((wifiInfo == null) || (wifiInfo.getSSID() == null)){
                        Log.d(TAG, "I was told there is a connecting event, but I was not told who I am connecting with." +
                        		" So ignore this msg.");
                        return;
                    }
                    
                    if (isIvyHotspot(wifiInfo)){
                        mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_IVY_CONNECTING);
                        mConnectedAP.update(mIvyNetworkWifiConfiguration);
                    } else {
                    	mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_PUBLIC_CONNECTING);
                        mConnectedAP.update(wifiInfo);
                    }
                    dispatchOnWifiConnecting(mConnectedAP);
                }
                break;
                
            case Msg_WifiStateChanged:
                synchronized (mLock){
                    if (mWifiState == WifiManager.WIFI_STATE_ENABLED){
                    	mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_ENABLED);
                        dispatchOnWifiEnabled();
                    } else if (mWifiState == WifiManager.WIFI_STATE_DISABLED){
                    	mConnectedAP.setConnectionState(ConnectionState.CONNECTION_STATE_WIFI_DISABLED);
                        dispatchOnWifiDisabled();
                    }
                }
            }
        }
    }

    private native int native_getHotspotIp(String node);
    static{
        System.loadLibrary("appsharenative");
        
        PHONE_MATCHER.add("samsung");
    }

}
