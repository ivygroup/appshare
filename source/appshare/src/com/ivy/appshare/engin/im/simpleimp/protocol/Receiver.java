package com.ivy.appshare.engin.im.simpleimp.protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.ivy.appshare.connection.ConnectionState;
import com.ivy.appshare.connection.IvyNetService;
import com.ivy.appshare.connection.IvyNetwork;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Im;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.OnReceiveFileListener.RequestResult;
import com.ivy.appshare.engin.im.simpleimp.filetranslate.ReceiveFileInfo;
import com.ivy.appshare.engin.im.simpleimp.filetranslate.TcpFileReceiver;
import com.ivy.appshare.engin.im.simpleimp.filetranslate.TcpHeadIconReceiver;
import com.ivy.appshare.engin.im.simpleimp.network.Worker;
import com.ivy.appshare.engin.im.simpleimp.protocol.VersionControl.VersionType;
import com.ivy.appshare.engin.im.simpleimp.util.NotifactionEngin;

public class Receiver implements Worker.PackageListener {
	private static String TAG = "Receiver";
	private Sender mSender;
	private TcpFileReceiver mTcpFileRecvThread;
	private Person mMySelf;
	private NotifactionEngin mNotifactionEngin;

	public Receiver(Sender sender, NotifactionEngin engin) {
		mSender = sender;
		mNotifactionEngin = engin;
		mMySelf = LocalSetting.getInstance().getMySelf();
		Worker.getUdpWorker().registrPackageListener(this);
		Worker.getMultiCastWorker().registrPackageListener(this);
	}

    @Override
    public synchronized void receivedPackage(PackageReceive packet) {
        if (isMySelf(packet.mPersonOther)) {
            return;
        }

        Person person = PersonManager.getInstance().getPerson(packet.mPersonOther);
        long packetNumber = Long.parseLong(packet.mPacketNo);
        if (person != null) {
            if ((person.mDynamicStatus.packetNumber != 0)
                    && person.mDynamicStatus.packetNumber == packetNumber) {
                /*Log.i(TAG, "received some message. person name = "
                    + person.mNickName
                    + ", command = " + packet.mCommand);    //*/
                return;
            }

            person.mDynamicStatus.packetNumber = packetNumber;
        }

        /* Log.d(TAG, "receivedPackage (" + packet.mPersonOther.mIP.getHostAddress()
                + ") command = 0X" + Long.toHexString(packet.mCommand)); //*/

        long command = ImMessages.GET_MODE(packet.mCommand);
        
        switch ((int)command) {
        case (int) ImMessages.IPMSG_BR_ENTRY:
        {
            brEntry(packet);
            break;
        }
            
        case (int) ImMessages.IPMSG_ANSENTRY:
        {
            ansEntry(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_BR_ABSENCE:
        {
            brAbsence(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_BR_EXIT:
        {
            brExit(packet);
            break;
        }

        case (int) ImMessages.IPTUX_SENDICON:
        {
            sendMyIcon(packet);
            break;
        }

        case (int) ImMessages.IVY_SENDICON_NOTIFY:
        {
            sendMyIconNotify(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_BR_ISGETLIST:
        {
            isGetList(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_OKGETLIST:
        {
            okGetList(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_GETLIST:
        {
            getList(packet);
            break;
        }

        case (int) ImMessages.IPMSG_ANSLIST:
        {
            ansList(packet);
            break;
        }

        case (int) ImMessages.IPMSG_SENDMSG:
        {
            sendMsg(packet);
            break;
        }
            
        case (int) ImMessages.IPMSG_RECVMSG:
        {
            recvMsg(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_READMSG:
        {
            readMsg(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_DELMSG:
        {
            delMsg(packet);
            break;
        }
        
        case (int) ImMessages.IPMSG_ANSREADMSG:
        {
            ansReadMsg(packet);
            break;
        }
        
        }//end switch
    }

    // if the package is ivy protocol, return true, else return false.
    private boolean dealEntryInfo(PackageReceive packet) {
        boolean isIvyProtocol = false;
        String[]  additioninfo = packet.mAdditionalSection.split("\0", 7);
        VersionType type = VersionControl.getVersionType(packet.mPersonOther.mProtocolVersion);
        switch (type) {
            case IPMSG_STANDARD:
            case IPMSG_IPTUX:
            case IPMSG_FEIQ:
            case IPMSG_OTHER_EXTENDS:
            {
                isIvyProtocol = false;
                packet.mPersonOther.mNickName = additioninfo[0];
                if (additioninfo.length > 1) {
                    packet.mPersonOther.mGroup = additioninfo[1];
                }
                packet.mPersonOther.mState = Im.State_Active;
                packet.mPersonOther.mImage = VersionControl.getDefaultVersionIcon(type);
            }
                break;
            case IVY_CURRENT_VERSION:
            case IVY_SMALLER_THAN_CURVERSION:
            case IVY_BIGGER_THAN_CURVERSION:
            {
                /*if (additioninfo.length > 5) {
                    Log.d(TAG, "dealEntryInfo the additional = " + packet.mAdditionalSection
                            + ", count = " + additioninfo.length
                            + ", 0 = " + additioninfo[0]
                            + ", 1 = " + additioninfo[1]
                            + ", 2 = " + additioninfo[2]
                            + ", 3 = " + additioninfo[3]
                            + ", 4 = " + additioninfo[4]
                            + ", 5 = " + additioninfo[5]
                            );
                    if (additioninfo[4] == null) {
                        Log.d(TAG, "additional 4 is null");
                    } else {
                        Log.d(TAG, "additional 4 is not null");
                    }
                } else {
                    Log.d(TAG, "dealEntryInfo the additional = " + packet.mAdditionalSection
                            + ", count = " + additioninfo.length);
                }*/
                isIvyProtocol = true;
                packet.mPersonOther.mNickName = additioninfo[0];
                if (additioninfo.length > 1) {
                    packet.mPersonOther.mGroup = additioninfo[1];
                }
                if (additioninfo.length > 2) {
                    packet.mPersonOther.mImage = additioninfo[2];
                }
                if (additioninfo.length > 3) {
                    packet.mPersonOther.mMsisdn = additioninfo[3];
                }
                if (additioninfo.length > 4) {
                    packet.mPersonOther.mImei = additioninfo[4];
                }
                if (additioninfo.length > 5) {
                    packet.mPersonOther.mSignature = additioninfo[5];
                }
                if (additioninfo.length > 6) {
                    packet.mPersonOther.mState = Integer.valueOf(additioninfo[6]);
                } else {
                    packet.mPersonOther.mState = Im.State_Active;
                }
            }
                break;

            default:
                break;
        }

        return isIvyProtocol;
    }

	private void brEntry(PackageReceive packet) {
		// Log.d(TAG, "Message IPMSG_BR_ENTRY");
		if (mMySelf.mState == Im.State_OffLine) {
		    Log.d(TAG, "Now we are in offline state, not deal this brEntry msg");
		    return;
		}

		boolean isIvyProtocol = dealEntryInfo(packet);

		boolean isSamePersonStates = false;
		{
		    Person person = PersonManager.getInstance().getPerson(packet.mPersonOther);
		    if (person != null && packet.mPersonOther.isSameState(person)) {
		        isSamePersonStates = true;
		    }
		}

		Person person = PersonManager.getInstance().addPerson(packet.mPersonOther);
		person.mDynamicStatus.lastActiveTime = System.currentTimeMillis();
		mSender.answerEntry(person);

        if (isIvyProtocol) {
            // mSender.sendMyIcon(person);
            mSender.sendMyIconNotify(person);
        }

		if (isSamePersonStates) {
		    return;
		}

        mNotifactionEngin.onNewUser(person);
	}

	private void ansEntry(PackageReceive packet) {
		// Log.d(TAG, "Message IPMSG_ANSENTRY");		
		if (mMySelf.mState == Im.State_OffLine) {
            Log.d(TAG, "Now we are in offline state, not deal this ansEntry msg");
            return;
        }

		long option = ImMessages.GET_OPT(packet.mCommand);
        if ((option & ImMessages.IPMSG_ABSENCEOPT) == 0) {
            mSender.answerEntry(packet.mPersonOther);
        }

        boolean isIvyProtocol = dealEntryInfo(packet);

        boolean isSamePersonStates = false;
        {
            Person person = PersonManager.getInstance().getPerson(packet.mPersonOther);
            if (person != null && packet.mPersonOther.isSameState(person)) {
                isSamePersonStates = true;
            }
        }

        Person person = PersonManager.getInstance().addPerson(packet.mPersonOther);
        person.mDynamicStatus.lastActiveTime = System.currentTimeMillis();

        if (isIvyProtocol) {
            // mSender.sendMyIcon(person);
            mSender.sendMyIconNotify(person);
        }

        if (isSamePersonStates) {
            return;
        }

        mNotifactionEngin.onNewUser(person);
	}

	private void brAbsence(PackageReceive packet) {
		// Log.d(TAG, "Message IPMSG_BR_ABSENCE");
        if (mMySelf.mState == Im.State_OffLine) {
            Log.d(TAG, "Now we are in offline state, not deal this brEntry msg");
            return;
        }

        dealEntryInfo(packet);

        Person person = PersonManager.getInstance().addPerson(packet.mPersonOther);
        mNotifactionEngin.onSomeoneAbsence(person);
	}

	private void brExit(PackageReceive packet) {
		Log.d(TAG, "Message IPMSG_BR_EXIT");
		Person p = PersonManager.getInstance().removePerson(packet.mPersonOther);
		mNotifactionEngin.onSomeoneExit(p);
	}

	private void sendMyIcon(PackageReceive packet) {
	    if (!VersionControl.isIvyVersion(packet.mPersonOther.mProtocolVersion)) {
	        return;
	    }

	    Person fromPerson = PersonManager.getInstance().getPerson(packet.mPersonOther);
	    if (fromPerson == null) {
	        return;
	    }

	    if (LocalSetting.getInstance().getUserIconEnvironment().saveFriendHead(fromPerson.mImage, packet.mAdditionalRawData)) {
	        mNotifactionEngin.onSomeoneHeadIcon(fromPerson);
	    }
	}

	private void sendMyIconNotify(PackageReceive packet) {
	    if (!VersionControl.isIvyVersion(packet.mPersonOther.mProtocolVersion)) {
            return;
        }

	    Person fromPerson = PersonManager.getInstance().getPerson(packet.mPersonOther);
	    if (fromPerson == null) {
	        return;
	    }

	    if (fromPerson.mImage == null || fromPerson.mImage.length() <= 0) {
	        return;
	    }

	    long size = -1;
	    if (packet.mAdditionalSection != null && packet.mAdditionalSection.length() > 0) {
	        size = Long.parseLong(packet.mAdditionalSection, 16);
	    }

	    if (LocalSetting.getInstance().getUserIconEnvironment().isExistHead(fromPerson.mImage, size)) {
	        return;
	    }

	    if (TcpHeadIconReceiver.isDownloading(fromPerson)) {
	        Log.d(TAG, "now we are downloading " + fromPerson.mNickName + ", ip = " + fromPerson.mIP.getHostAddress());
	        return;
	    }

	    TcpHeadIconReceiver iconReceiver = new TcpHeadIconReceiver(fromPerson, mNotifactionEngin);
	    iconReceiver.start();
	}

	private void isGetList(PackageReceive packet) {
	    if (isMySelf(packet.mPersonOther)) {
	        return;
	    }

	    VersionType type = VersionControl.getVersionType(packet.mPersonOther.mProtocolVersion);
	    switch (type) {
	        case IVY_CURRENT_VERSION:
	        case IVY_SMALLER_THAN_CURVERSION:
	        case IVY_BIGGER_THAN_CURVERSION:
	        {
	            mSender.okGetList(packet.mPersonOther);
	        }
	        break;

	        default:
	            break;
	    }
	}

	private void okGetList(PackageReceive packet) {
	    if (isMySelf(packet.mPersonOther)) {
	        return;
	    }

	    VersionType type = VersionControl.getVersionType(packet.mPersonOther.mProtocolVersion);
	    switch (type) {
	        case IVY_CURRENT_VERSION:
	        case IVY_SMALLER_THAN_CURVERSION:
	        case IVY_BIGGER_THAN_CURVERSION:
	        {
	            mSender.getList(packet.mPersonOther);
	        }
	        break;

	        default:
	            break;
	    }
	}

	private void getList(PackageReceive packet) {
	    if (isMySelf(packet.mPersonOther)) {
	        return;
	    }

	    VersionType type = VersionControl.getVersionType(packet.mPersonOther.mProtocolVersion);
	    switch (type) {
	        case IVY_CURRENT_VERSION:
	        case IVY_SMALLER_THAN_CURVERSION:
	        case IVY_BIGGER_THAN_CURVERSION:
	        {
	            mSender.ansList(packet.mPersonOther);
	        }
	        break;

	        default:
	            break;
	    }
	}

	private void ansList(PackageReceive packet) {
	    if (isMySelf(packet.mPersonOther)) {
	        return;
	    }

	    // Log.d(TAG, "ansList in");
	    VersionType type = VersionControl.getVersionType(packet.mPersonOther.mProtocolVersion);
	    switch (type) {
	        case IVY_CURRENT_VERSION:
	        case IVY_SMALLER_THAN_CURVERSION:
	        case IVY_BIGGER_THAN_CURVERSION:
	        {
	            String[]  additioninfo = packet.mAdditionalSection.split("\0");
	            for (int i = 0; i < additioninfo.length; ++i) {
	                String str = additioninfo[i];
	                // Log.d(TAG, "list person: " + additioninfo[i]);
	                try {
	                    Person person = new Person();
	                    person.mIP = InetAddress.getByName(str);                        
	                    mSender.upLine(person);
	                } catch (UnknownHostException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        break;

	        default:
	            break;
	    }
	    // Log.d(TAG, "ansList out");
	}

	private void sendMsg(PackageReceive packet) {
	    Log.d(TAG, "Message IPMSG_SENDMSG. " + packet.mPersonOther.mNickName
	            + "(" + packet.mPersonOther.mIP.getHostAddress() + ") "
	            + " package ID = " + packet.mPacketNo + ", addation = " + packet.mAdditionalSection);

	    Person tmpPerson = PersonManager.getInstance().getPerson(packet.mPersonOther);
	    if (tmpPerson != null) {
	        packet.mPersonOther = tmpPerson;
	    } else {
	        PersonManager.getInstance().addPerson(packet.mPersonOther);
	        mSender.upLine(packet.mPersonOther);
	    }

        long option = ImMessages.GET_OPT(packet.mCommand);

        if ((option & ImMessages.IPMSG_SENDCHECKOPT) != 0) {
            mSender.recvMsg(packet.mPersonOther,
                    packet.mPacketNo);
        }

        if ((option & ImMessages.IPMSG_FILEATTACHOPT) != 0) {
            sendMsg_SignleFile(packet);
        } else if ((option & ImMessages.IPMSG_BROADCASTOPT) != 0) {
            sendMsg_GroupTalk(packet);
        } else {
            sendMsg_OnlyTalk(packet);
        }
	}

	private void sendMsg_OnlyTalk(PackageReceive packet) {
	    String additioninfo[] = packet.mAdditionalSection.split("\0");
	    if (additioninfo[0] != null) {
	        mNotifactionEngin.onReceiveMessage(packet.mPersonOther, additioninfo[0]);
	    }

        long option = ImMessages.GET_OPT(packet.mCommand);

        if ((option & ImMessages.IPMSG_AUTORETOPT) != 0) {
            // TODO: we not auto-ret this message for protect pingpang.
        }

        if ((option & ImMessages.IPMSG_SECRETOPT) != 0) {
            mSender.readMsg(packet.mPersonOther, packet.mPacketNo);
        }

        if ((option & ImMessages.IPMSG_NOADDLISTOPT) != 0) {
            // TODO: now, we not do this.
        }
	}

	private void sendMsg_GroupTalk(PackageReceive packet) {
	    String additioninfo[] = packet.mAdditionalSection.split("\0");
	    if (additioninfo[0] != null) {
	        mNotifactionEngin.onReceiveGroupMessage(packet.mPersonOther, additioninfo[0]);
	    }

	    long option = ImMessages.GET_OPT(packet.mCommand);
	    if ((option & ImMessages.IPMSG_SECRETOPT) != 0) {
	        mSender.readMsg(packet.mPersonOther, packet.mPacketNo);
	    }
	}

	private void sendMsg_SignleFile(PackageReceive packet) {
	    Log.d(TAG, "IPMSG_FILEATTACHOPT");

	    String additioninfo[] = packet.mAdditionalSection.split("\0");
	    if (additioninfo[0] != null && !additioninfo[0].equals("")) {
	        mNotifactionEngin.onReceiveMessage(packet.mPersonOther, additioninfo[0]);
	    }

	    ReceiveFileInfo recvfileinfo = new ReceiveFileInfo(packet.mPersonOther.mProtocolVersion, additioninfo[1]);
	    RequestResult isAllowAcceptFile = null;
	    
	    if (recvfileinfo.mIsGroupSend) {
	        isAllowAcceptFile = mNotifactionEngin.requestFileTranslate_Group(packet.mPersonOther, 
                    recvfileinfo.mRecvFileId,
                    recvfileinfo.mRecvFileName, 
                    recvfileinfo.mRecvFileSize,
                    recvfileinfo.mTime,
                    recvfileinfo.mFileType);
	    } else {
	        isAllowAcceptFile =  mNotifactionEngin.requestFileTranslate(packet.mPersonOther, 
	                recvfileinfo.mRecvFileId,
	                recvfileinfo.mRecvFileName, 
	                recvfileinfo.mRecvFileSize,
	                recvfileinfo.mTime,
	                recvfileinfo.mFileType);
	    }

	    if (isAllowAcceptFile.bIsSaveThisfile) {
	        mTcpFileRecvThread = new TcpFileReceiver(packet.mPacketNo,
	                packet.mPersonOther,
	                recvfileinfo,
	                isAllowAcceptFile.strSavePath,
	                mNotifactionEngin);
	        mTcpFileRecvThread.start();
	    } else {
	        // TODO: send a message to other person?
	    }
	}

	private void recvMsg(PackageReceive packet) {
		Person tmpPerson = PersonManager.getInstance().getPerson(packet.mPersonOther);
        if (tmpPerson != null) {
            packet.mPersonOther = tmpPerson;
        } else {
            PersonManager.getInstance().addPerson(packet.mPersonOther);
            mSender.upLine(packet.mPersonOther);
        }

        if (packet.mAdditionalSection != null) {
            String number[] = packet.mAdditionalSection.split("\0");
            Log.d(TAG, "rPacketNumber = " + packet.mPersonOther.mDynamicStatus.rPacketNumber
                    + ", received number = " + number[0]);
            if (packet.mPersonOther.mDynamicStatus.rPacketNumber == (Long.parseLong(number[0]))) {
                packet.mPersonOther.mDynamicStatus.rPacketNumber = 0;
            }
        }
	}

	private void readMsg(PackageReceive packet) {
		Log.d(TAG, "Messasge IPMSG_READMSG");
		// because we not send msg with IPMSG_SECRETOPT, so we can't receive this message.
	}
	
	private void delMsg(PackageReceive packet) {
		Log.d(TAG, "Message IPMSG_DELMSG");
		// because we not send msg with IPMSG_SECRETOPT, so we can't receive this message.
	}
	
	private void ansReadMsg(PackageReceive packet) {
		Log.d(TAG, "Message IPMSG_ANSREADMSG");
		// because we not send msg with IPMSG_SECRETOPT, so we can't receive this message.
	}


	private boolean isMySelf(Person person) {
	    if (person == null || person.mIP == null) {
	        return false;
	    }

	    if (mMySelf != null && mMySelf.mIP != null) {
	        if (person.mIP.getHostAddress().equals(mMySelf.mIP.getHostAddress())) {
	            return true;
	        }
	    }

	    byte[] byte1 = person.mIP.getAddress();
	    List<InetAddress> ips = LocalSetting.getInstance().getMyIps();
	    if (ips != null) {
	        for (InetAddress ip: ips) {
	            if (ip != null) {
	                byte[] byte2 = ip.getAddress();
	                if (Arrays.equals(byte1, byte2)) {
	                    return true;
	                }
	            }
	        }
	    }

	    return false;
	}

	private class ThePersonKeys {
	    String p1Key;
	    String p2Key;
	    boolean isSuccessful = false;
	    boolean isHotSpot = false;
	}
	private boolean isSamePerson(Person p1 , Person p2) {
		if (p1 == null || p2 == null) {
			return false;
		}

		ThePersonKeys keys = getPersonKey(p1, p2);
		if (keys.isSuccessful == false) {
		    if (keys.isHotSpot) {
		        return false;
		    } else {
		        return true;
		    }
		}
		String p1Key = keys.p1Key;
		String p2Key = keys.p2Key;

		if (p1Key.compareTo(p2Key) == 0) {
			return true;
		} else {
			return false;
		}
	}

	private ThePersonKeys getPersonKey(Person p1, Person p2) {
	    ThePersonKeys keys = new ThePersonKeys();

	    keys.isSuccessful = true;
	    if (p1.mIP != null && p2.mIP != null) {
	        keys.p1Key = p1.mIP.getHostAddress().toString();
	        keys.p2Key = p2.mIP.getHostAddress().toString();
	        return keys;
	    }

	    Log.i(TAG, "the person's IP is null. so, we use mac address to compare two person.");
	    keys.p1Key = PersonManager.getPersonKey(p1);
        keys.p2Key = PersonManager.getPersonKey(p2);
        if (keys.p1Key != null && keys.p2Key != null) {
            return keys;
        }

        Log.e(TAG, "the person's mac and ip is null, please check the code.");
        keys.isSuccessful = false;
        IvyNetService myService = IvyNetwork.getInstance().getIvyNetService();
        if (myService != null) {
            if (myService.getConnectionState().getHotspotState() == ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED) {
                keys.isHotSpot = true;
            } else {
                keys.isHotSpot = false;
            }
        } else {
            keys.isHotSpot = false;
        }
        return keys;
	}
}
