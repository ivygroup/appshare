package com.ivyappshare.connection;

public class ConnectionState {
	public static final int CONNECTION_UNKNOWN = 0;
	// Connection Type
	public static final int CONNECTION_TYPE_WIFI = 1;
	public static final int CONNECTION_TYPE_HOTSPOT = 2;
	public static final int CONNECTION_TYPE_WIFIP2P = 3;
	public static final int CONNECTION_TYPE_BLUETOOTH = 4;

	// WIFI state
	public static final int CONNECTION_STATE_WIFI_MIN = 100;
    public static final int CONNECTION_STATE_WIFI_ENABLED = 100;
    public static final int CONNECTION_STATE_WIFI_DISABLED = 101;
    public static final int CONNECTION_STATE_WIFI_DISCONNECTED = 102;

    public static final int CONNECTION_STATE_WIFI_PUBLIC_CONNECTING = 110;
    public static final int CONNECTION_STATE_WIFI_PUBLIC_CONNECTED = 111;
    public static final int CONNECTION_STATE_WIFI_PUBLIC_DISCONNECTING = 112;
    public static final int CONNECTION_STATE_WIFI_PUBLIC_DISCONNECTED = 102;

    public static final int CONNECTION_STATE_WIFI_IVY_CONNECTING = 120;
    public static final int CONNECTION_STATE_WIFI_IVY_CONNECTED = 121;
    public static final int CONNECTION_STATE_WIFI_IVY_DISCONNECTING = 122;
    public static final int CONNECTION_STATE_WIFI_IVY_DISCONNECTED = 102;

    public static final int CONNECTION_STATE_WIFI_MAX = 199;

    // HOTSPOT state
    public static final int CONNECTION_STATE_HOTSPOT_MIN = 200;
    public static final int CONNECTION_STATE_HOTSPOT_ENABLING = 200;
    public static final int CONNECTION_STATE_HOTSPOT_ENABLED = 201;
    public static final int CONNECTION_STATE_HOTSPOT_DISABLING = 202;
    public static final int CONNECTION_STATE_HOTSPOT_DISABLED = 203;

    public static final int CONNECTION_STATE_HOTSPOT_MAX = 299;

    // wifi p2p state
    public static final int CONNECTION_STATE_WIFIP2P_MIN = 300;
    public static final int CONNECTION_STATE_WIFIP2P_ENABLED = 300;
    public static final int CONNECTION_STATE_WIFIP2P_DISABLED = 301;
    public static final int CONNECTION_STATE_WIFIP2P_CONNECTING = 302;
    public static final int CONNECTION_STATE_WIFIP2P_CONNECTED = 303;
    public static final int CONNECTION_STATE_WIFIP2P_DISCONNECTING = 304;
    public static final int CONNECTION_STATE_WIFIP2P_DISCONNECTED = 305;

    public static final int CONNECTION_STATE_WIFIP2P_MAX = 399;

    //
    private int mWifiState;
    private int mHotspotState;
    private int mWifiP2pState;

    private int mLastState;
    private int mLastType;


    public ConnectionState() {
    	reset();
    }

    public void reset() {
    	mWifiState = CONNECTION_UNKNOWN;
    	mHotspotState = CONNECTION_UNKNOWN;
    	mWifiP2pState = CONNECTION_UNKNOWN;
    	mLastState = CONNECTION_UNKNOWN;
    	mLastType = CONNECTION_UNKNOWN;
    }

    public void setState(int connectionType, int state) {
    	switch (connectionType) {
    	case CONNECTION_TYPE_WIFI:
    		mWifiState = state;
    		break;

    	case CONNECTION_TYPE_HOTSPOT:
    		mHotspotState = state;
    		break;

    	case CONNECTION_TYPE_WIFIP2P:
    		mWifiP2pState = state;
    		break;

    	case CONNECTION_TYPE_BLUETOOTH:
    		break;
    	default:
    		break;
    	}

    	mLastState = state;
    	mLastType = connectionType;
    }

    public void setWifiState(int state) {
    	setState(CONNECTION_TYPE_WIFI, state);
    }

    public void setHotspotState(int state) {
    	setState(CONNECTION_TYPE_HOTSPOT, state);
    }

    public void setWifiP2pState(int state) {
    	setState(CONNECTION_TYPE_WIFIP2P, state);
    }

    public int getState(int connectionType) {
    	switch (connectionType) {
    	case CONNECTION_TYPE_WIFI:
    		return mWifiState;

    	case CONNECTION_TYPE_HOTSPOT:
    		return mHotspotState;

    	case CONNECTION_TYPE_WIFIP2P:
    		return mWifiP2pState;

    	case CONNECTION_TYPE_BLUETOOTH:
    		break;
    	default:
    		break;
    	}
    	return CONNECTION_UNKNOWN;
    }

    public int getLastStateByFast() {
    	return mLastState;
    }

    public int getLastType() {
    	return mLastType;
    }

    public int getWifiState() {
    	return mWifiState;
    }

    public int getHotspotState() {
    	return mHotspotState;
    }

    public int getWifiP2pState() {
    	return mWifiP2pState;
    }

    public boolean isConnected() {
    	if (mWifiState == CONNECTION_STATE_WIFI_PUBLIC_CONNECTED
    			|| mWifiState == CONNECTION_STATE_WIFI_IVY_CONNECTED) {
    		return true;
    	}

    	if (mHotspotState == CONNECTION_STATE_HOTSPOT_ENABLED) {
    		return true;
    	}
    	
    	if (mWifiP2pState == CONNECTION_STATE_WIFIP2P_CONNECTED) {
    		return true;
    	}
    	
    	return false;
    }

    public static boolean isConnected(int state) {
    	if (state == CONNECTION_STATE_WIFI_PUBLIC_CONNECTED
    			|| state == CONNECTION_STATE_WIFI_IVY_CONNECTED
    			|| state == CONNECTION_STATE_HOTSPOT_ENABLED
    			|| state == CONNECTION_STATE_WIFIP2P_CONNECTED) {
    		return true;
    	}

    	return false;
    }

    public static boolean isBusy(int state) {
    	if (state == CONNECTION_STATE_WIFI_PUBLIC_CONNECTING
    			|| state == CONNECTION_STATE_WIFI_IVY_CONNECTING
    			|| state == CONNECTION_STATE_WIFI_PUBLIC_DISCONNECTING
    			|| state == CONNECTION_STATE_WIFI_IVY_DISCONNECTING
    			|| state == CONNECTION_STATE_HOTSPOT_ENABLING
    			|| state == CONNECTION_STATE_HOTSPOT_DISABLING
    			|| state == CONNECTION_STATE_WIFIP2P_CONNECTING) {
    		return true;
    	}

            return false;
    }
    
    public static int getConnectionTypeByStatus(int state) {
    	if (state >= CONNECTION_STATE_WIFI_MIN && state <= CONNECTION_STATE_WIFI_MAX) {
    		return CONNECTION_TYPE_WIFI;
    	}

    	if (state >= CONNECTION_STATE_HOTSPOT_MIN && state <= CONNECTION_STATE_HOTSPOT_MAX) {
    		return CONNECTION_TYPE_HOTSPOT;
    	}

    	if (state >= CONNECTION_STATE_WIFIP2P_MIN && state <= CONNECTION_STATE_WIFIP2P_MAX) {
    		return CONNECTION_TYPE_WIFIP2P;
    	}

    	return CONNECTION_UNKNOWN;
    }
}
