package com.ivy.appshare.engin.connection.implement;

import java.net.InetAddress;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;

import com.ivy.appshare.engin.connection.ConnectionState;



public class AccessPointInfo{
    public String mSSID;
    public String mBSSID;
    public InetAddress mIpAddress;
    public int mMask;
    public String mMacAddress;
    public int mNetworkId;
    public String mFriendlyName;
    private boolean mIsIvyWifiDirectDevice;
    public int mSecurity;
    public int mSignalLevel;
    private int mState;
    public AccessPointType mType = AccessPointType.UNKNOWN;
    //This field is only valuable when ivy-hotspot enabled
    public String mPassword;
    public byte mWpsInfo = WPS_INVALID;
    
    public static final byte WPS_DISPLAY = 0x01;
    public static final byte WPS_KEYPAD = 0x02;
    public static final byte WPS_LABEL = 0x04;
    public static final byte WPS_PBC = 0x08;
    public static final byte WPS_INVALID = 0x10;
    
    public AccessPointInfo(){
    	mState = ConnectionState.CONNECTION_UNKNOWN;
    	mIsIvyWifiDirectDevice = false;
    }


    public AccessPointInfo(WifiInfo wifiInfo){
        mSSID = removeDoubleQuotes(wifiInfo.getSSID());
        mBSSID = wifiInfo.getBSSID();
        mIpAddress = ConnectionManagement.intToInetAddress(wifiInfo.getIpAddress());
        mMacAddress = wifiInfo.getMacAddress();
        mNetworkId = wifiInfo.getNetworkId();
        mSignalLevel = wifiInfo.getRssi();
        try{
            mFriendlyName = mSSID.substring(0, mSSID.indexOf("-"));
        } catch(IndexOutOfBoundsException e){
            mFriendlyName = null;
        }
    }
    
    
    // This constructor is used for ivy-hotspot
    public AccessPointInfo(ScanResult result){
        mSSID = removeDoubleQuotes(result.SSID);
        mBSSID = result.BSSID;
        mSecurity = getSecurity(result);
        mSignalLevel = result.level;
        try{
            mFriendlyName = mSSID.substring(0, mSSID.lastIndexOf("-"));
        } catch(IndexOutOfBoundsException e){
            mFriendlyName = null;
        }
        mType = AccessPointType.IVY_HOTSPOT;
    }
    
    // This constructor is used for wifi-p2p peer
    public AccessPointInfo(String macAddr, String deviceName, byte wpsConfig){
        mMacAddress = macAddr;
        mFriendlyName = deviceName;
        mType = AccessPointType.WIFI_P2P_PEER;
        mWpsInfo = wpsConfig;
    }
    
    public String getSSID(){
        return mSSID;
    }

    public InetAddress getIpAddress() {
            return mIpAddress;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public String getFriendlyName(){
        return mFriendlyName;
    }

    public boolean getIsIvyWifiDirectDevice() {
        return mIsIvyWifiDirectDevice;
    }

    public int getConnectionState(){
        return mState;
    }
    
    public void setConnectionState(int connectionState) {
        mState = connectionState;
    }
    
    public String getIvyHotspotPassword(){
        return mPassword;
    }
    
    public void setFriendlyName(String name){
        mFriendlyName = name;
    }

    public void setIsIvyWifiDirectDevice(boolean b) {
        mIsIvyWifiDirectDevice = b;
    }

    public void update(ScanResult result){
        mSignalLevel = result.level;
    }
    
    public void update(WifiInfo info){
        if (info != null){
            mSSID = removeDoubleQuotes(info.getSSID());
            mBSSID = info.getBSSID();
            mIpAddress = ConnectionManagement.intToInetAddress(info.getIpAddress());
            mMacAddress = info.getMacAddress();
            mNetworkId = info.getNetworkId();
            mSignalLevel = info.getRssi();
            try{
                mFriendlyName = mSSID.substring(0, mSSID.indexOf("-"));
            } catch(IndexOutOfBoundsException e){
                mFriendlyName = null;
            }
        }
    }

    public void update(WifiConfiguration config)
	{
		if (null == config) {
			return;
		}

        mSSID = removeDoubleQuotes(config.SSID);
        mBSSID = config.BSSID;
        mPassword = config.preSharedKey;
 
		try{
			mFriendlyName = mSSID.substring(0, mSSID.indexOf("-"));
		} catch(IndexOutOfBoundsException e){
			mFriendlyName = null;
		}
	}
    
    public byte getWpsConfig(){
        return (byte)(mWpsInfo & 0xFF);
    }
    
    public void setWpsConfig(byte wpsConfig){
        mWpsInfo = wpsConfig;
    }
    
    public void reset(){
        mSSID = null;
        mBSSID = null;
        mIpAddress = null;
        mMacAddress = null;
        mNetworkId = -1;
        mFriendlyName = null;
        mSecurity = SECURITY_NONE;
        mSignalLevel = -1;
        mState = ConnectionState.CONNECTION_UNKNOWN;
    }
    
    public static String removeDoubleQuotes(String string) {
        if (string == null){
            return null;
        }
        
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }
    
    public enum AccessPointType{
        UNKNOWN,
        IVY_HOTSPOT,
        WIFI_P2P_PEER;
    }
    
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    
    static private int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    static private int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        sb.append("mSSID:").append(mSSID == null ? none : mSSID)
            .append(", mBSSID:").append(mBSSID == null ? none : mBSSID)
            .append(", mIpAddress:").append(mIpAddress == null ? none : mIpAddress.getHostAddress())
            .append(", mNetworkId:").append(mNetworkId)
            .append(", mFriendlyName:").append(mFriendlyName == null ? none : mFriendlyName)
            .append(", mSecurity:").append(mSecurity)
            .append(", mConnectionState:").append(mState)
            .append(", mType:").append(mType)
            .append(", mPassword:").append(mPassword == null ? none : mPassword)
            .append(", mWpsInfo:").append(mWpsInfo);
        
        return sb.toString();
    }
}
