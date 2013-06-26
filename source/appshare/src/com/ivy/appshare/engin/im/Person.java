package com.ivy.appshare.engin.im;

import java.net.InetAddress;
import java.util.HashMap;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;

public class Person {
	public String mProtocolVersion; // 1 should be a const.
	public String mName;		// 1 should be get from phone.
	public String mHost;		// 1 should be get from phone.
	public InetAddress mIP;		// 1 should be get from phone.
	public String mNickName;// 2 should be set by user.
	public String mImage;	// 2 should be set by user.
	public String mGroup;	// 2 should be set by user.
	public String mSignature;// 2 should be set by user.
	public String mMac;			// 1 should be get from phone.
	public String mMsisdn;		// 1 should be get from phone.
	public String mImei;		// 1 should be get from phone.
	public String mRsaPub;			// 3 should be get from network.
	public int mPort;		// 2 should be set by user.	
	public String mNetworkCoding;	// 2 should be set by user.
	public int mState;

	public boolean mIsFakePerson; // this is a empty person, only a placeholder

	public PersonDynamicStatus mDynamicStatus;


	// 
	public Person() {
		mProtocolVersion = ImMessages.IPMSG_VERSION;
		mName = "default user";
		mHost = "default host";
//		mNickName = MyApplication.getInstance().getResources().getString(R.string.default_nickname);
		mGroup = "android";
		mPort = ImMessages.IPMSG_DEFAULT_PORT;
		// mNetworkCoding = "GB18030";
		mNetworkCoding = ImMessages.DEFAULT_ENCODE;
		mState = Im.State_OffLine;

		mIsFakePerson = false;

		mDynamicStatus = new PersonDynamicStatus();

		mDynamicStatus.myEncodes = new MyEncodes();
		mDynamicStatus.myEncodes.mHasDetected = false;
		mDynamicStatus.myEncodes.mHasDetectFirstMsg = false;
		mDynamicStatus.myEncodes.mDecode = ImMessages.DEFAULT_ENCODE;
		mDynamicStatus.myEncodes.mMapEncodes = new HashMap<String, Integer>();
	}

	public void copyFromOther(Person other) {
	    this.mProtocolVersion = other.mProtocolVersion;
	    this.mName = other.mName;
	    this.mHost = other.mHost;
	    this.mIP = other.mIP;
	    this.mNickName = other.mNickName;
	    this.mImage = other.mImage;
	    this.mGroup = other.mGroup;
	    this.mSignature = other.mSignature;
	    this.mMac = other.mMac;
	    this.mMsisdn = other.mMsisdn;
	    this.mImei = other.mImei;
	    this.mRsaPub = other.mRsaPub;
	    this.mPort = other.mPort;
	    this.mNetworkCoding = other.mNetworkCoding;
	    this.mState = other.mState;
	}

	public boolean isSameState(Person other) {
	    if (!compareTwoString(this.mName, other.mName)) {
	        return false;
	    }
	    if (!compareTwoString(this.mHost, other.mHost)) {
            return false;
        }
	    if (!compareTwoString(this.mNickName, other.mNickName)) {
            return false;
        }
	    if (!compareTwoString(this.mImage, other.mImage)) {
            return false;
        }
	    if (!compareTwoString(this.mGroup, other.mGroup)) {
            return false;
        }
	    if (!compareTwoString(this.mSignature, other.mSignature)) {
            return false;
        }

	    if (this.mState != other.mState) {
	        return false;
	    }

	    return true;
	}

	private boolean compareTwoString(String str1, String str2) {
	    if (str1 == null && str2 == null) {
	        return true;
	    } else if (str1 != null && str2 != null) {
	        return (str1.compareTo(str2) == 0);
	    } else {
	        return false;
	    }
	}
/*
	public boolean isActiveForSend() {
	    switch (mState) {
            case Im.State_OnLine:
            case Im.State_Idle:
            case Im.State_Screen_Off:
                return true;
            case Im.State_Sleep:
            case Im.State_OffLine:
                return false;
            default:
                return false;
        }
	}
*/
	
	public boolean isOnline() {
	       switch (mState) {
	            case Im.State_Active:
	            case Im.State_Idle:
	            case Im.State_Screen_Off:
	            case Im.State_Sleep:
	                return true;
	            case Im.State_OffLine:
	                return false;
	            default:
	                return false;
	        }
	}

	public String getStateString() {
	    int stateID = 1;

//	    switch (mState) {
//	        case Im.State_Active:
//	            stateID = R.string.state_active;
//	            break;
//	        case Im.State_OffLine:
//	            stateID = R.string.state_offline;
//                break;
//	        case Im.State_Idle:
//	            stateID = R.string.state_idle;
//                break;
//	        case Im.State_Screen_Off:
//	            stateID = R.string.state_screen_off;
//                break;
//	        case Im.State_Sleep:
//	            stateID = R.string.state_sleep;
//                break;
//	    }

	    return MyApplication.getInstance().getString(stateID);
	}

	public class PersonDynamicStatus {
        public int unReadMsgCount;     // unread message
        public int MessageCount;     // message count
        public long packetNumber;      // have received max packet's number, for filter repeat message 
        public long rPacketNumber;     // need to be checked packet's number
        public long lastActiveTime;    // for keep alive
        public MyEncodes myEncodes;
    }

	public class MyEncodes {
	    public boolean mHasDetectFirstMsg;
	    public HashMap<String, Integer> mMapEncodes;

	    public boolean mHasDetected;
	    public String mDecode;
	}
}
