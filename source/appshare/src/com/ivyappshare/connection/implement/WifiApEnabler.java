package com.ivyappshare.connection.implement;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;


//import java.lang.Long;


public class WifiApEnabler{
    private static final String TAG = "WifiApEnabler";

    private final Context mContext;

    private WifiManager mWifiManager;

    private final IntentFilter mIntentFilter;
    //Tracked to notify the user about wifi direct P2P being shut down
    //during wifi hotspot bring up
    private int mWifiApState = WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_FAILED;

    public static final int ERROR = -1;
    public static final int ENABLING = -2;
    public static final int DISABLING = -3;
    public static final int ENABLED = -4;
    public static final int DISABLED = -5;

    private String mMac;
    private boolean mStateMachineEvent;

    private static WifiConfiguration mWifiConfig;
    private WifiManagerHiddenAPI mWifiManagerHiddenAPI;

    private boolean mStarted;
    /*private final static Intent mWidgetEnabledIntent = 
            new Intent(WifiConnectionService.ACTION_WIDGET_WIFIAP_ENABLED);*/

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);
            if (WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                mWifiApState = intent.getIntExtra(
                        WifiManagerHiddenAPI.WifiHiddenAPI.EXTRA_WIFI_AP_STATE, WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_FAILED);
                handleWifiApStateChanged(mWifiApState);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                /*handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));*/
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }

        }
    };

    public WifiApEnabler(Context context, CheckBox switcher) {
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiManagerHiddenAPI = WifiManagerHiddenAPI.getInstance(context);
        mIntentFilter = new IntentFilter(WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);


        mWifiApState = mWifiManagerHiddenAPI.getWifiHiddenAPI().getWifiApState();
    }



    public void start( WifiConfiguration wifiConfig) {
        mStarted = true;
        mWifiConfig = wifiConfig;
        setSoftapEnabled(true);
        mContext.registerReceiver(mReceiver, mIntentFilter);

    }

    public void stop() {
        mStarted = false;
        mContext.unregisterReceiver(mReceiver);
    }
    
    public void reStart(WifiConfiguration wifiConfig){
        mWifiConfig = wifiConfig;
        setSoftapEnabled(true);
    }

    public boolean isStarted(){
        return mStarted;
    }

    private void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        int wifiState = mWifiManager.getWifiState();
        if(!isAirplaneMode &&
            (wifiState != WifiManager.WIFI_STATE_DISABLING) &&
            (wifiState != WifiManager.WIFI_STATE_ENABLING) &&
            (mWifiApState != WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLING) &&
            (mWifiApState != WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING)) {
        }

        if ((mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED)
                || (mWifiApState == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING)){
            setSoftapEnabled(true);
        } else {
            setSoftapEnabled(false);
        }
    }

    public void setSoftapEnabled(boolean menable) {
        if (mStateMachineEvent) return;

        final boolean enable = menable;


        int wifiState = mWifiManager.getWifiState();
        //Switcher On 
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
               (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
            controlWifiState(true);
        } else if (enable){
            controlWifiState(true);
        } else{
            controlWifiState(false);
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
    }

    public void controlWifiState(Boolean enable){
        Log.d(TAG, "mWifiConfig:" + mWifiConfig);
        if (mWifiApState  == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED){
            mWifiManagerHiddenAPI.getWifiHiddenAPI().setWifiApEnabled(null, false);
        }
        mWifiManagerHiddenAPI.getWifiHiddenAPI().setWifiApEnabled(mWifiConfig, enable);
    }

    private void handleWifiApStateChanged(int state) {
        Log.d(TAG, "handleWifiApStateChanged, state=" + state);

        if (state == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLING){
        } else if (state == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_ENABLED){
            // mContext.sendBroadcast(mWidgetEnabledIntent); // TODO:
        } else if (state == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLING){
        } else if (state == WifiManagerHiddenAPI.WifiHiddenAPI.WIFI_AP_STATE_DISABLED){
        } else {
        }

    }
}
