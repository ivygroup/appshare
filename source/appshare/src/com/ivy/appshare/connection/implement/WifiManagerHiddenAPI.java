package com.ivy.appshare.connection.implement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;


@SuppressLint("UseValueOf")
public class WifiManagerHiddenAPI {
    private static final String TAG = "WifiManagerHiddenAPI";
    private static WifiManagerHiddenAPI sWifiManagerHiddenAPI;
    
    private static Context sContext;
    private WifiHiddenAPI mWifiHiddenAPI;
//    private WifiP2pHiddenAPI mWifiP2pHiddenAPI;
    private static int sSDKVersion = Build.VERSION.SDK_INT;

    //private Class mKeyMgmtCls;
    //public static int WPA2_PSK;

    private WifiManagerHiddenAPI(){
        WifiManager wifiManager = (WifiManager) sContext.getSystemService(Context.WIFI_SERVICE);
        mWifiHiddenAPI = new WifiHiddenAPI(wifiManager);
        
        /*
        boolean p2pSupported = sContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
        if ((sSDKVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            && (p2pSupported)){
            WifiP2pManager wifiP2pManager = (WifiP2pManager) sContext.getSystemService(Context.WIFI_P2P_SERVICE);
            WifiP2pManager.Channel wifiP2pChannel = wifiP2pManager.initialize(sContext, sContext.getMainLooper(), null);
            mWifiP2pHiddenAPI = new WifiP2pHiddenAPI(wifiP2pManager, wifiP2pChannel);
        }
        */
    }

    public static synchronized WifiManagerHiddenAPI getInstance(Context context){
        if (sContext == null){
            sContext = context;
        }
        if (sWifiManagerHiddenAPI == null){
            sWifiManagerHiddenAPI = new WifiManagerHiddenAPI();
        }
        return sWifiManagerHiddenAPI;
    }
    
    public WifiHiddenAPI getWifiHiddenAPI(){
        return mWifiHiddenAPI;
    }
    
    /*
    public WifiP2pHiddenAPI getWifiP2pHiddenAPI(){
        return mWifiP2pHiddenAPI;
    }
    */
    
    /*
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static class WifiP2pHiddenAPI{
        private WifiP2pManager mWifiP2pManager;
        private WifiP2pManager.Channel mChannel;
        private Class<? extends WifiP2pManager> mWifiP2pManagerCls;
        private Class<? extends WifiP2pManager.Channel> mChannelCls;
        
        private Method mEnableP2p;
        private Method mDisableP2p;
        
        private WifiP2pHiddenAPI(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel){
            mWifiP2pManager = wifiP2pManager;
            mChannel = channel;
            mWifiP2pManagerCls = wifiP2pManager.getClass();
            mChannelCls = channel.getClass();
            findClass();
            findFields();
            findFunctions();
        }
        
        void findClass(){
            
        }
        
        void findFields(){
            
        }
        
        void findFunctions(){
            Class[] params = null;
            
            params = new Class[1];
            params[0] = mChannelCls;
            try {
                mEnableP2p = mWifiP2pManagerCls.getMethod("enableP2p", params);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            
            try {
                mDisableP2p = mWifiP2pManagerCls.getMethod("disableP2p", params);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            
        }
        
        public void enableP2p(WifiP2pManager.Channel channel){
            Object args[] = new Object[1];
            args[0] = channel;
            
            if (mEnableP2p == null){
                return;
            }
            
            try {
                mEnableP2p.invoke(mWifiP2pManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            
        }
        
        public void disableP2p(WifiP2pManager.Channel channel){
            Object args[] = new Object[1];
            args[0] = channel;
            if (mDisableP2p == null){
                return;
            }
            
            try {
                mDisableP2p.invoke(mWifiP2pManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            
        }
    }
*/
    public static class WifiHiddenAPI{
        static int WIFI_AP_STATE_DISABLING = -1;
        static int WIFI_AP_STATE_DISABLED = -1;
        static int WIFI_AP_STATE_ENABLING = -1;
        static int WIFI_AP_STATE_ENABLED = -1;
        static int WIFI_AP_STATE_FAILED = -1;
        public static String WIFI_AP_STATE_CHANGED_ACTION;
        public static String EXTRA_WIFI_AP_STATE;

        private Method mSetWifiApEnabled;
        private Method mGetWifiApState;
        private Method mIsWifiApEnabled;
        private Method mGetWifiApConfiguration;
        private Method mSetWifiApConfiguration;
        private Method mConnectNetworkWithConfig;
        private Method mConnectNetworkWithConfigJB;
        private Method mInitializeJB;
        private Method mConnectNetworkWithConfigJBMR1;
        private Method mAsyncConnect;
        private Method mForgetNetwork;

        private Class<? extends WifiManager> mWifiManagerCls;
        private WifiManager mWifiManager;
        private Class<?> mWifiConfigurationCls;
        private Class<?> mActionListenerCls;
        private Class<?> mWifiChannelCls;
        private Class<?> mChannelListenerCls;


        private WifiHiddenAPI(WifiManager wifiManager){
            mWifiManager = wifiManager;
            mWifiManagerCls = wifiManager.getClass();
            findClass();
            findFields();
            findFunctions();
        }


        private void findClass(){
            try {
                mWifiConfigurationCls = Class.forName("android.net.wifi.WifiConfiguration");
                if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
                    mWifiChannelCls = Class.forName("android.net.wifi.WifiManager$Channel");
                    mChannelListenerCls = Class.forName("android.net.wifi.WifiManager$ChannelListener");
                    mActionListenerCls = Class.forName("android.net.wifi.WifiManager$ActionListener");
                }else if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN_MR1){
                    mActionListenerCls = Class.forName("android.net.wifi.WifiManager$ActionListener");
                }
                //mKeyMgmtCls = Class.forName("android.net.wifi.WifiConfiguration.KeyMgmt");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void findFields(){
            Field field = null;
            Object o = null;
            try {
                field = mWifiManagerCls.getField("WIFI_AP_STATE_DISABLING");
                WIFI_AP_STATE_DISABLING = field.getInt(o);
                field = mWifiManagerCls.getField("WIFI_AP_STATE_DISABLED");
                WIFI_AP_STATE_DISABLED = field.getInt(o);
                field = mWifiManagerCls.getField("WIFI_AP_STATE_ENABLING");
                WIFI_AP_STATE_ENABLING = field.getInt(o);
                field = mWifiManagerCls.getField("WIFI_AP_STATE_ENABLED");
                WIFI_AP_STATE_ENABLED = field.getInt(o);
                field = mWifiManagerCls.getField("WIFI_AP_STATE_FAILED");
                WIFI_AP_STATE_FAILED = field.getInt(o);
                field = mWifiManagerCls.getField("WIFI_AP_STATE_CHANGED_ACTION");
                WIFI_AP_STATE_CHANGED_ACTION = (String)field.get(o);
                field = mWifiManagerCls.getField("EXTRA_WIFI_AP_STATE");
                EXTRA_WIFI_AP_STATE = (String)field.get(o);
    
                //field = mKeyMgmtCls.getField("WPA2_PSK");
                //WPA2_PSK = field.getInt(o);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
    
        private void findFunctions(){
            Class[] params = null;
            try {
                params = new Class[2];
                params[0] = mWifiConfigurationCls;
                params[1] = Boolean.TYPE;
                Log.d(TAG, "params[0]:" + params[0].toString());
                mSetWifiApEnabled = mWifiManagerCls.getMethod("setWifiApEnabled", params);
    
                mGetWifiApState = mWifiManagerCls.getMethod("getWifiApState");
                mIsWifiApEnabled = mWifiManagerCls.getMethod("isWifiApEnabled");
                mGetWifiApConfiguration = mWifiManagerCls.getMethod("getWifiApConfiguration");
    
                params = new Class[1];
                params[0] = mWifiConfigurationCls;
                
                
                if (sSDKVersion == Build.VERSION_CODES.FROYO){
                    // For froyo and older verision, android doesn't provide this function to app.
                    // Since our app doesn't need this api up to now, so ignore it.
                    
                } else {
                    mSetWifiApConfiguration = mWifiManagerCls.getMethod("setWifiApConfiguration", params);
                }
                Log.d(TAG, "sSDKVersion:" + sSDKVersion + ", Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
                if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
                    params = new Class[3];
                    params[0] = Context.class;
                    params[1] = Looper.class;
                    params[2] = mChannelListenerCls;
                    mInitializeJB = mWifiManagerCls.getMethod("initialize", params);
                    
                    params = new Class[3];
                    params[0] = mWifiChannelCls;
                    params[1] = mWifiConfigurationCls;
                    params[2] = mActionListenerCls;
                    mConnectNetworkWithConfigJB = mWifiManagerCls.getMethod("connect", params);
                    
                    params = new Class[3];
                    params[0] = mWifiChannelCls;
                    params[1] = Integer.TYPE;
                    params[2] = mActionListenerCls;
                    mForgetNetwork = mWifiManagerCls.getMethod("forget", params);
                    
                    
                } else if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN_MR1){
                    params = new Class[2];
                    params[0] = mWifiConfigurationCls;
                    params[1] = mActionListenerCls;
                    mConnectNetworkWithConfigJBMR1 = mWifiManagerCls.getMethod("connect", params);
                    
                    params = new Class[2];
                    params[0] = Integer.TYPE;
                    params[1] = mActionListenerCls;
                    mForgetNetwork = mWifiManagerCls.getMethod("forget", params);
                } else if ((sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                        || (sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)){
                    params = new Class[2];
                    params[0] = Context.class;
                    params[1] = Handler.class;
                    mAsyncConnect = mWifiManagerCls.getMethod("asyncConnect", params);
                    
                    params = new Class[1];
                    params[0] = mWifiConfigurationCls;
                    mConnectNetworkWithConfig = mWifiManagerCls.getMethod("connectNetwork", params);
                    
                    params = new Class[1];
                    params[0] = Integer.TYPE;
                    mForgetNetwork = mWifiManagerCls.getMethod("forgetNetwork", params);
                } else{
                    
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    
    
    
        public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled){
            Object args[] = new Object[2];
            args[0] = wifiConfig;
            args[1] = enabled;
            Object ret = null;
            try {
                ret = mSetWifiApEnabled.invoke(mWifiManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ret == null) ? false : ((Boolean)ret).booleanValue();
        }
    
        public int getWifiApState(){
            Object ret = null;
    
            try {
                ret = mGetWifiApState.invoke(mWifiManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ret == null) ? -1 : ((Integer)ret).intValue();
        }
    
        public boolean isWifiApEnabled(){
            Object ret = null;
            try {
                ret = mIsWifiApEnabled.invoke(mWifiManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ret == null) ? false : ((Boolean)ret).booleanValue();
        }
    
        public WifiConfiguration getWifiApConfiguration(){
            Object ret = null;
            try {
                ret = mGetWifiApConfiguration.invoke(mWifiManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ret == null) ? null : (WifiConfiguration)ret;
        }
    
        public boolean setWifiApConfiguration(WifiConfiguration wifiConfig){
            Object ret = null;
            Object[] args = new Object[1];
            args[0] = wifiConfig;
            
            if (sSDKVersion == Build.VERSION_CODES.FROYO){
                boolean isWpa = false;
                final ContentResolver cr = sContext.getContentResolver();
                Settings.Secure.putString(cr, "wifi_ap_ssid", wifiConfig.SSID);
                isWpa = wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK);
                Settings.Secure.putInt(cr,
                        "wifi_ap_security", 
                        isWpa ? KeyMgmt.WPA_PSK : KeyMgmt.NONE);
                if (isWpa){
                    Settings.Secure.putString(cr, "wifi_ap_passwd", wifiConfig.preSharedKey);
                }
                
                return true;
            }
            
            try {
                ret = mSetWifiApConfiguration.invoke(mWifiManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (ret == null) ? false: ((Boolean)ret).booleanValue();
        }

        public void connectNetwork(WifiConfiguration config){
            Object[] args = new Object[1];
            args[0] = config;
            
            if ((sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                || (sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)){
                //For ics
                try {
                    Log.d(TAG, "mWifiManager:" + mWifiManager);
                    mConnectNetworkWithConfig.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    e.getTargetException();
                }
            } else {
                //For froyo and gingerbread
                int networkId = mWifiManager.addNetwork(config);
                if (networkId != -1){
                    //mWifiManager.updateNetwork(config);
                    config.networkId = networkId;
                    mWifiManager.enableNetwork(networkId, true);
                    mWifiManager.reconnect();
                }
            }    

        }
        
        public  Object initialize(Context context, Looper looper, Object listener){
            Object ret = null;
            Object[] args = new Object[3];
            args[0] = context;
            args[1] = looper;
            args[2] = listener;
            
            try {
                ret = mInitializeJB.invoke(mWifiManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            
            
            return (ret == null) ? null : ret;
        }
        
        public void asyncConnect(Context context, Handler srcHandler){
            Object[] args = new Object[2];
            args[0] = context;
            args[1] = srcHandler;
            
            try {
                mAsyncConnect.invoke(mWifiManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public void connect(Object channel, WifiConfiguration config){
            if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
                Object[] args = new Object[3];

                args[0] = channel;
                args[1] = config;
                args[2] = null;
                try {
                    mConnectNetworkWithConfigJB.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    e.getTargetException();
                }
                    

            } else if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN_MR1){
                Object[] args = new Object[2];
                args[0] = config;
                args[1] = null;
                try {
                    mConnectNetworkWithConfigJBMR1.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void forgetNetwork(Object channel, int networkId){
            if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN){
                Object[] args = new Object[3];
                args[0] = channel;
                args[1] = networkId;
                args[2] = null;
                
                try {
                    mForgetNetwork.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (sSDKVersion == Build.VERSION_CODES.JELLY_BEAN_MR1){
                Object[] args = new Object[2];
                args[0] = networkId;
                args[1] = null;
                
                try {
                    mForgetNetwork.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if ((sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    || (sSDKVersion == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)){
                Object[] args = new Object[1];
                args[0] = networkId;
                
                try {
                    mForgetNetwork.invoke(mWifiManager, args);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                mWifiManager.removeNetwork(networkId);
                mWifiManager.saveConfiguration();
            }
        }
    }
}
