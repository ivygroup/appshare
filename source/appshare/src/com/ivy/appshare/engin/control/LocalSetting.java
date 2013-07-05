package com.ivy.appshare.engin.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.data.ImSetting;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;

public class LocalSetting {
	private static final String TAG = LocalSetting.class.getSimpleName();
	private static final String LOCAL_DIR = Environment.getExternalStorageDirectory().getPath() + "/IvyAppShare/";

	private static LocalSetting instance = null;
    public static LocalSetting getInstance() {
        if (instance == null) {
            instance = new LocalSetting();
        }
        return instance;
    }


	private Person mSelfPerson;
	private UserIconEnvironment mUserIconEnvironment;

	private InetAddress mBoraAddress;
	private List<InetAddress> mMySelfIPs;    // my phone may be more than one IP.  used in filter myself.
	private boolean mRing;
	private boolean mVibrate;
	private boolean mFirstTime;
	private boolean mTraceAction;

	private boolean mInitialed;

	private LocalSetting() {
        mSelfPerson = new Person();
        mInitialed = false;
        mRing = true;
        mVibrate = false;
        mMySelfIPs = new ArrayList<InetAddress>();
        mUserIconEnvironment = new UserIconEnvironment(getLocalPath());
        init();
    }

	private int init() {
	    Log.d(TAG, "init called.");
		if (mInitialed) {
			return 0;
		}
		mInitialed = true;
		ImSetting imSetting = new ImSetting();

		mSelfPerson.mName = new String(android.os.Build.MODEL);
		mSelfPerson.mHost = "Android";
		mSelfPerson.mMsisdn = "";
		mSelfPerson.mImei = "";
//		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
//		if (tm != null) {
//		    mSelfPerson.mMsisdn = tm.getLine1Number();
//		    mSelfPerson.mImei = tm.getDeviceId();
//		}


		mSelfPerson.mNickName = imSetting.getNickName();
		if (mSelfPerson.mNickName.length() == 0) {
		    mSelfPerson.mNickName = mSelfPerson.mName;
		}

		mSelfPerson.mGroup = imSetting.getGroupName();
		if (mSelfPerson.mGroup.length() == 0) {
		    mSelfPerson.mGroup = "Default Group";
		}

		mSelfPerson.mSignature = imSetting.getSignature();

		updateMySelfHeadIconName(imSetting);

		mRing = imSetting.getRing();
		mVibrate = imSetting.getVibrate();
		mFirstTime = imSetting.getFirstTime();
		mTraceAction = imSetting.getTraceAction();

		return 0;
	}

	public  void updateMySelfHeadIconName() {
	    ImSetting imSetting = new ImSetting();
	    updateMySelfHeadIconName(imSetting);
	}

	private void updateMySelfHeadIconName(ImSetting imSetting) {
	    String savedHeadIconNameString = imSetting.getHeadIconName();
        if (mUserIconEnvironment.isExistHead(savedHeadIconNameString, -1)) {
            mUserIconEnvironment.setSelfHeadName(savedHeadIconNameString);
            mSelfPerson.mImage = mUserIconEnvironment.getSelfHeadName();
        } else {
            mUserIconEnvironment.setSelfHeadName(null);
            mSelfPerson.mImage = null;
            imSetting.setHeadIconName(null);
        }
        // Log.d(TAG, "headicon name = " + savedHeadIconNameString + ", " + mSelfPerson.mImage);
	}

	public void UpdateDefaultNickName() {
	    ImSetting imSetting = new ImSetting();
	    mSelfPerson.mNickName = imSetting.getNickName();
	    if (mSelfPerson.mNickName.length() == 0) {
	        mSelfPerson.mNickName = mSelfPerson.mName;
	        if (mSelfPerson.mIP != null) {
	            mSelfPerson.mNickName += " (" + mSelfPerson.mIP.getHostAddress() + ")";
	        }
	    }
	}
	
	public Person getMySelf() {
		return mSelfPerson;
	}
	
	public String getLocalPath() {
		File file = new File(LOCAL_DIR);
		if (!file.exists()) {
			file.mkdir();
		}
		return LOCAL_DIR;
	}

	public UserIconEnvironment getUserIconEnvironment() {
	    return mUserIconEnvironment;
	}

	public String getLocalFileReceivePath(FileType fileType) {
		StringBuilder PathBuilder = new StringBuilder(getLocalPath());
		switch(fileType) {
			case FileType_App:
				PathBuilder.append("App/");
				break;
			case FileType_Contact:
				PathBuilder.append("Contact/");
				break;
			case FileType_Picture:
				PathBuilder.append("Picture/");
				break;
			case FileType_Music:
				PathBuilder.append("Music/");
				break;
			case FileType_Video:
				PathBuilder.append("Video/");
				break;
			case FileType_OtherFile:
				PathBuilder.append("Other/");
				break;
			case FileType_Record:
				PathBuilder.append("Record/");
				break;
		}
		String path = PathBuilder.toString();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return path;
	}

	public boolean saveNickName(String content) {
		mSelfPerson.mNickName = content;
		ImSetting imSetting = new ImSetting();
        imSetting.setNickName(content);
		return true;
	}
	
	public boolean saveGroupName(String content) {
		mSelfPerson.mGroup = content;
		ImSetting imSetting = new ImSetting();
        imSetting.setGroupName(content);
		return true;
	}
	
	public boolean saveSignContent(String content) {
	    mSelfPerson.mSignature = content;
	    ImSetting imSetting = new ImSetting();
        imSetting.setSignature(content);
		return true;
	}

	public boolean saveImageName(String content) {
	    mUserIconEnvironment.setSelfHeadName(content);
	    mSelfPerson.mImage = mUserIconEnvironment.getSelfHeadName();
	    ImSetting imSetting = new ImSetting();
        imSetting.setHeadIconName(content);
		return true;
	}

	public void saveRing(boolean b) {
	    mRing = b;
	    ImSetting imSetting = new ImSetting();
        imSetting.setRing(b);
	}

	public void saveVibrate(boolean b) {
	    mVibrate = b;
	    ImSetting imSetting = new ImSetting();
        imSetting.setVibrate(b);
	}
	
	public void saveFirstTime(boolean b) {
	    mFirstTime = b;
	    ImSetting imSetting = new ImSetting();
        imSetting.setFirstTime(b);
	}

	public void saveTraceAction(boolean b) {
	    mTraceAction = b;
	    ImSetting imSetting = new ImSetting();
        imSetting.setTraceAction(b);
	}

	public void setBroadCastAddress(InetAddress address) {
	    mBoraAddress = address;
	}

	public InetAddress getBroadCastAddress() {
	    return mBoraAddress;
	}

	public void setMyIps(List<InetAddress> ips) {
	    synchronized (mMySelfIPs) {
	        if (ips == null) {
	            mMySelfIPs.clear();
	        } else {
	            mMySelfIPs.clear();
	            for (InetAddress tmp : ips) {
	                mMySelfIPs.add(tmp);
	            }
	        }
        }
	}

	public List<InetAddress> getMyIps() {
	    synchronized (mMySelfIPs) {
	        return new ArrayList<InetAddress>(mMySelfIPs);
	    }
	}

	public boolean getRing() {
	    return mRing;
	}

	public boolean getVibrate() {
	    return mVibrate;
	}

	public boolean getFirstTime() {
	    return mFirstTime;
	}

	public boolean getTraceAction() {
	    return mTraceAction;
	}

	public class UserIconEnvironment {
	    private String mHeadPath;
	    private String mMySelfHeadName;
	    private final String[] mDefaultHeads = {
	        "head_version_icon_feiq.png",
	        "head_version_icon_ipmsg.png",
	        "head_version_icon_iptux.png",
	        "avatar1.png","avatar2.png",
	        "avatar3.png","avatar4.png",
	        "avatar5.png","avatar6.png",
	        "avatar7.png","avatar8.png",
	        "avatar9.png"
	    };

	    private UserIconEnvironment(String localPath) {
	        mHeadPath = new String(localPath + "/heads/");
	        File file = new File(mHeadPath);
	        if (!file.exists()) {
	            file.mkdir();
	        }
	        exportDefaultHeads();
	    }

	    private void exportDefaultHeads() {
	        AssetManager assetManager = MyApplication.getInstance().getAssets();
	        for (int i = 0; i < mDefaultHeads.length; ++ i) {
	            String src = "default_heads/" + mDefaultHeads[i];
	            String dest = mHeadPath + mDefaultHeads[i];

	            File file = new File(dest);
	            if (file.exists()) {
	                continue;
	            }

	            try {
	                DataInputStream read;
	                DataOutputStream write;
	                read = new DataInputStream(new BufferedInputStream(assetManager.open(src)));
	                write = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
	                byte [] buf = new byte[1024*4];
	                int len = 0;
	                while((len = read.read(buf)) != -1) {
	                    write.write(buf, 0, len);
	                }
	                read.close();
	                write.close();
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            } catch (IOException e) {
                    e.printStackTrace();
                }
	        }
	    }

	    public String generateRandName() {
	        StringBuffer name = new StringBuffer();
	        name.append(System.currentTimeMillis());
	        name.append(".");
	        if (mSelfPerson.mMac != null) {
	            String mac[] = mSelfPerson.mMac.split(":");
	            for (int i = 0; i < mac.length; ++i) {
	                name.append(Integer.valueOf(mac[i], 16));
	            }
	        }

	        return name.toString();
	    }

	    public boolean isExistSelfHead() {
	        if (mMySelfHeadName == null) {
	            return false;
	        }

	        if (mMySelfHeadName.compareTo("null") == 0) {
	            // we have a bug, so we filter this case.
	            return false;
	        }

	        if (mMySelfHeadName.length() <= 0) {
	            return false;
	        }

	        String str = mHeadPath + mMySelfHeadName;

	        File file = new File(str);
	        if (!file.exists()) {
	            return false;
	        }
	        if (file.length() <=0 ) {
	            return false;
	        }

	        return true;
	    }

	    public void setSelfHeadName(String name) {
	        if (name == null || name.length() <= 0) {
	            return;
	        }
	        mMySelfHeadName = name;
	    }

	    public String getSelfHeadName() {
	        return mMySelfHeadName;
	    }

	    public String getSelfHeadFullPath() {
	        return mHeadPath + mMySelfHeadName;
	    }

	    public long getSelfHeadSize() {
	        if (mMySelfHeadName == null) {
	            return 0;
	        }

	        File file = new File(getSelfHeadFullPath());
	        if (!file.exists()) {
	            return 0;
	        }

	        long len = file.length();
	        return len;
	    }
	    
	    public byte[] getSelfHeadData() {
	        if (mMySelfHeadName == null) {
	            return null;
	        }

	        File file = new File(getSelfHeadFullPath());
	        if (!file.exists()) {
	            return null;
	        }

	        long len = file.length();
	        if (len > 8096) {
	            return null;
	        }

	        byte[] returnBuffer = null;
	        try {
	            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

	            byte []readBuffer = new byte[(int)len];
	            int dataLength = 0;
	            if ((dataLength = in.read(readBuffer)) != -1) {
	                Log.d(TAG, "read successful!");
	                returnBuffer = new byte[dataLength];
	                System.arraycopy(readBuffer, 0, returnBuffer, 0, dataLength);
	                
	            } else {
	                Log.e(TAG, "read myself icon data failed");
	            }
	        } catch (FileNotFoundException e) {
	            Log.e(TAG, e.toString());
	        } catch (IOException e) {
	            Log.e(TAG, e.toString());
	        }

	        return returnBuffer;
	    }

	    public boolean isExistHead(String headname, long size) {
	        if (headname == null) {
	            return false;
	        }

            if (headname.compareTo("null") == 0) {
                // we have a bug, so we filter this case.
                return false;
            }

            if (headname.length() <= 0) {
                return false;
            }

            if (isDefaultHead(headname)) {
                return true;
            }

            String str = mHeadPath + headname;

            File file = new File(str);
            if (!file.exists()) {
                return false;
            }
            if (file.length() <=0 ) {
                return false;
            }
            if (size > 0) {
                if (file.length() != size) {
                    return false;
                }
            }

            return true;
	    }
	    
	    private boolean isDefaultHead(String headname) {
	        for (int i = 0; i < mDefaultHeads.length; ++i) {
	            if (headname.equals(mDefaultHeads[i])) {
	                return true;
	            }
	        }
	        return false;
	    }

	    public String getFriendHeadFullPath(String headname) {
            return mHeadPath + headname;
        }

	    public boolean saveFriendHead(String name, byte[] data) {
	        String filenameString = mHeadPath + name;

	        File file = new File(filenameString);
	        if (file.exists()) {
	            return false;
	        }

	        try {
	            file.createNewFile();
	            FileOutputStream out = new FileOutputStream(file);
	            out.write(data);
	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "when saveFriendHead crash. " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "when saveFriendHead crash. " + e.getMessage());
	        }
	        return true;
	    }
	}
}
