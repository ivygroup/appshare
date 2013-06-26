package com.ivy.appshare.engin.connection.implement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WifiP2pManagerHiddenAPI {

    private static final String TAG = "WifiManagerHiddenAPI";
    private static WifiP2pManagerHiddenAPI sWifiP2pManagerHiddenAPI;
    
    private static Context sContext;
    private WifiP2pHiddenAPI mWifiP2pHiddenAPI;
    private static int sSDKVersion = Build.VERSION.SDK_INT;
    
    private WifiP2pManagerHiddenAPI(){
        WifiP2pManager wifiP2pManager = (WifiP2pManager) sContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pHiddenAPI = new WifiP2pHiddenAPI(wifiP2pManager);
        

    }

    public static synchronized WifiP2pManagerHiddenAPI getInstance(Context context){
        if (sContext == null){
            sContext = context;
        }
        if (sWifiP2pManagerHiddenAPI == null){
            sWifiP2pManagerHiddenAPI = new WifiP2pManagerHiddenAPI();
        }
        return sWifiP2pManagerHiddenAPI;
    }
    
    public WifiP2pHiddenAPI getWifiP2pHiddenAPI(){
        return mWifiP2pHiddenAPI;
    }
    
    public static class WifiP2pHiddenAPI{
        private WifiP2pManager mWifiP2pManager;
        private Class<? extends WifiP2pManager> mWifiP2pManagerCls;

        private Method mSetDeviceName;

        private WifiP2pHiddenAPI(WifiP2pManager wifiP2pManager){
            mWifiP2pManager = wifiP2pManager;
            mWifiP2pManagerCls = wifiP2pManager.getClass();
            findClass();
            findFields();
            findFunctions();
        }
        
        private void findClass(){


        }
        
        private void findFields(){
            
        }
        
        private void findFunctions(){
            Class[] params = null;
            
            params = new Class[3];
            params[0] = WifiP2pManager.Channel.class;
            params[1] = String.class;
            params[2] = WifiP2pManager.ActionListener.class;
            
            try {
                mSetDeviceName = mWifiP2pManagerCls.getMethod("setDeviceName", params);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        
        public void setDeviceName(Channel c, String devName, ActionListener listener){
            if (mSetDeviceName == null) {
                return;
            }

            Object[] args = new Object[3];
            args[0] = c;
            args[1] = devName;
            args[2] = listener;
            
            try {
                mSetDeviceName.invoke(mWifiP2pManager, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
    }
}
