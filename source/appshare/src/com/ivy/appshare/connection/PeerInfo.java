package com.ivy.appshare.connection;


public class PeerInfo {
    String mID;
    String mFriendlyName;
    boolean mIsIvyDevice;

    public String getID() {
        if (mID == null) {
            return new String();
        } else {
            return mID;
        }
    }

    public String getFriendlyName() {
        if (mFriendlyName == null) {
            return new String();
        } else {
            return mFriendlyName;
        }
    }

    public boolean isIvyDevice() {
        return mIsIvyDevice;
    }

    @Override
    public String toString() {
        return "ID = " + mID + ", name = " + mFriendlyName + ", isivydevice = " + mIsIvyDevice;
    }
}
