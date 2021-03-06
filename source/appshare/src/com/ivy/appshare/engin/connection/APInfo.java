package com.ivy.appshare.engin.connection;

public class APInfo {
    String mSSID;
    String mFriendlyName;
    String mHotspotPassword;
    int mShareAppCount;
    int mSessionID;

    public String getSSID() {
        if (mSSID == null) {
            return new String();
        } else {
            return mSSID;
        }
    }

    public String getFriendlyName() {
        if (mFriendlyName == null) {
            return new String();
        } else {
            return mFriendlyName;
        }
    }

    public String getIvyHotspotPassword() {
        if (mHotspotPassword == null) {
            return new String();
        }
        return mHotspotPassword;
    }

    public int getShareAppCount() {
        return mShareAppCount;
    }

    public int getSessionID() {
        return mSessionID;
    }
}
