package com.ivy.appshare.engin.im.simpleimp.protocol;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.control.LocalSetting.UserIconEnvironment;
import com.ivy.appshare.engin.im.Im;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.simpleimp.filetranslate.FileTask;
import com.ivy.appshare.engin.im.simpleimp.filetranslate.TcpFileServer;
import com.ivy.appshare.engin.im.simpleimp.network.Worker;
import com.ivy.appshare.engin.im.simpleimp.util.NotifactionEngin;

@SuppressLint("HandlerLeak")
public class Sender {
	private static String TAG = "Sender";
	private Worker mWorkerUdp;
	private Worker mWorkerUdpMultiCast;
	private Person mMyself;
	private NotifactionEngin mNotifactionEngin;
	private HandlerThread mMyThread;
	private Handler mHandler;

	private static final int SEND_MSG_AGAIN_DURATION = 1500;

	private static final int MESSAGE_BROADCAST = 1;
	private static final int MESSAGE_SPECIAL_PERSON = 2;
	// private static final int MESSAGE_NEED_CHECK_REACHE = 3;
	private static final int MESSAGE_SEND_MSG = 3;
	private static final int MESSAGE_SEND_FILE = 4;
	private static final int MESSAGE_SEND_RAWDATA = 5;

	public Sender(NotifactionEngin engin) {
		// Log.v(TAG, "the thread name =" + Thread.currentThread().getName());
	    mWorkerUdp = Worker.getUdpWorker();
	    mWorkerUdpMultiCast = Worker.getMultiCastWorker();
		mMyself = LocalSetting.getInstance().getMySelf();
		mNotifactionEngin = engin;

		mMyThread = new HandlerThread("UdpSendThread");
		mMyThread.start();

		mHandler = new Handler(mMyThread.getLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_BROADCAST:
				{
				    PackageSend packageSend = (PackageSend)msg.obj;
				    // Log.d(TAG, "BroadCast msg = " + packageSend.mSendMessage);
				    mWorkerUdp.broadcast(packageSend.mSendMessage.toString(), mMyself);
				    mWorkerUdpMultiCast.broadcast(packageSend.mSendMessage.toString(), mMyself);
				}
					break;
					
				case MESSAGE_SPECIAL_PERSON:
				{
				    PackageSend packageSend = (PackageSend)msg.obj;
				    // Log.d(TAG, "Send to (" + packageSend.mToPerson.mIP.getHostAddress() + ") = " + packageSend.mSendMessage);
				    mWorkerUdp.send(packageSend.mSendMessage.toString(), mMyself, packageSend.mToPerson);
				}
					break;

				case MESSAGE_SEND_MSG:
				case MESSAGE_SEND_FILE:
				{
				    TheMsgOfSendMessage theMsgOfSendMessage = (TheMsgOfSendMessage)msg.obj;
				    Person to = theMsgOfSendMessage.packageSend.mToPerson;

			        long packetNumber = Long.parseLong(PackageSend.getPacketNo(theMsgOfSendMessage.packageSend.mSendMessage.toString()));
			        to.mDynamicStatus.rPacketNumber = packetNumber;

			        try {
			            int count = 0;
			            final int MAX_COUNT = 3;
			            do {
			                mWorkerUdp.send(theMsgOfSendMessage.packageSend.mSendMessage.toString(), mMyself, to);
			                ++count;
			                Thread.sleep(SEND_MSG_AGAIN_DURATION);
			            } while (to.mDynamicStatus.rPacketNumber == packetNumber && count < MAX_COUNT);
			        } catch (InterruptedException e) {
			            Log.e(TAG, "when send message occour a exception. " + e.toString());
			        }

			        if (to.mDynamicStatus.rPacketNumber == packetNumber) {
			            if (msg.what == MESSAGE_SEND_MSG) {
			                Log.i(TAG, "The message can't reach, userid = " + theMsgOfSendMessage.userid
	                                + ", msg = " + theMsgOfSendMessage.message);
			                mNotifactionEngin.onMsgSendFailed(theMsgOfSendMessage.userid, theMsgOfSendMessage.message);
			            } else if (msg.what == MESSAGE_SEND_FILE) {
			                Log.i(TAG, "The message can't reach, userid = " + theMsgOfSendMessage.userid
	                                + ", filename = " + theMsgOfSendMessage.filename);
			                // mNotifactionEngin.onSendFileTimeOut(theMsgOfSendMessage.userid, to, theMsgOfSendMessage.filename, theMsgOfSendMessage.fileType);
			            }
			        }
				}
				    break;
				    
				case MESSAGE_SEND_RAWDATA:
				{
                    PackageSend packageSend = (PackageSend)msg.obj;
                    Log.d(TAG, "Send rawdata to (" + packageSend.mToPerson.mIP.getHostAddress() + ") = " + packageSend.mSendMessage);
                    mWorkerUdp.sendRaw(packageSend.mSendMessage.toString(), packageSend.mSendRawData, mMyself, packageSend.mToPerson);
				}
				    break;

				default:
					break;
				}
			}
		};
	}
	
	public void release() {
	    mHandler = null;
		mMyThread.getLooper().quit();
		mWorkerUdp = null;
		// mWorkerUdpMultiCast = null;
		Worker.releaseUdpWorker();
		Worker.releaseUdpMultiCastWorker();
	}

	private StringBuffer generateIvyAdditionalInfo() {
	    StringBuffer additional = new StringBuffer();

	    // 1 standard ipmsg protocol.
	    additional.append(mMyself.mNickName.replace(':', ';') + "\0");
	    additional.append(mMyself.mGroup.replace(':', ';') + "\0");

	    // 2 ivyshare's struct.
	    //   (ipmsg protocol)\0(group)
	    //                            \0(image)\0(msisdn)\0(imei)\0(sign)\0(userstate)
	    if (mMyself.mImage != null) {
	        additional.append(mMyself.mImage);
	    }
	    additional.append("\0");

	    if (mMyself.mMsisdn != null) {
            additional.append(mMyself.mMsisdn);
        }
        additional.append("\0");

        if (mMyself.mImei != null) {
            additional.append(mMyself.mImei);
        }
        additional.append("\0");
        
        if (mMyself.mSignature != null) {
            additional.append(mMyself.mSignature);
        }
        additional.append("\0");
        additional.append(Integer.toString(mMyself.mState));

	    return additional;
	}

	public void upLine() {
	    if (mHandler == null) {
	        return;
	    }
		StringBuffer tmpBuffer = generateIvyAdditionalInfo();

		PackageSend packageSend = PackageSend.createCommand(mMyself, null,
		        ImMessages.IPMSG_BR_ENTRY | ImMessages.IPMSG_ABSENCEOPT,
		        tmpBuffer.toString());

		Message msg = mHandler.obtainMessage(MESSAGE_BROADCAST, packageSend);
		mHandler.sendMessage(msg);
	}
	
	public void upLine(Person to) {
	    if (mHandler == null) {
            return;
        }
	    StringBuffer tmpBuffer = generateIvyAdditionalInfo();

	    PackageSend packageSend = PackageSend.createCommand(mMyself,to,
	            ImMessages.IPMSG_BR_ENTRY | ImMessages.IPMSG_DIALUPOPT,
	            tmpBuffer.toString());

		Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
		mHandler.sendMessage(msg);
	}

	public void answerEntry(Person to) {
	    if (mHandler == null) {
            return;
        }
	    StringBuffer tmpBuffer = generateIvyAdditionalInfo();

	    PackageSend packageSend = PackageSend.createCommand(mMyself,to,
	            ImMessages.IPMSG_ANSENTRY | ImMessages.IPMSG_ABSENCEOPT,
	            tmpBuffer.toString());

	    Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void downLine() {
	    if (mHandler == null) {
            return;
        }
	    PackageSend packageSend = PackageSend.createCommand(mMyself, null,
	            ImMessages.IPMSG_BR_EXIT,
	            "\0");

	    Message msg = mHandler.obtainMessage(MESSAGE_BROADCAST, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void downLine(Person to) {
	    if (mHandler == null) {
            return;
        }
        PackageSend packageSend = PackageSend.createCommand(mMyself, to,
                ImMessages.IPMSG_BR_EXIT | ImMessages.IPMSG_ABSENCEOPT | ImMessages.IPMSG_DIALUPOPT,
                "\0");
        Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
        mHandler.sendMessage(msg);
	}

/*
	public void absence() {
	    if (mHandler == null) {
            return;
        }
	    StringBuffer tmpBuffer = new StringBuffer();
	    tmpBuffer.append(mMyself.mNickName.replace(':', ';'));
	    tmpBuffer.append("\0");
	    tmpBuffer.append(mMyself.mGroup.replace(':', ';'));
	    PackageSend packageSend = PackageSend.createCommand(mMyself, null,
	            ImMessages.IPMSG_BR_ABSENCE,
	            tmpBuffer.toString());

	    Message msg = mHandler.obtainMessage(MESSAGE_BROADCAST, packageSend);
	    mHandler.sendMessage(msg);
	} //*/
	public void absence(Person to) {
	    if (mHandler == null) {
            return;
        }
	    StringBuffer tmpBuffer = generateIvyAdditionalInfo();

	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_BR_ABSENCE | ImMessages.IPMSG_ABSENCEOPT,
	            tmpBuffer.toString());

	    Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void sendMyIcon(Person to) {
	    if (mHandler == null) {
            return;
        }
	    byte[] b = LocalSetting.getInstance().getUserIconEnvironment().getSelfHeadData();
	    if (b == null) {
	        return;
	    }

	    PackageSend packageSend = new PackageSend();
	    packageSend.mSendMessage = PackageSend.packHead(mMyself);
	    packageSend.mSendMessage.append(ImMessages.IPTUX_SENDICON);
	    packageSend.mSendMessage.append(":\0");
	    packageSend.mToPerson = to;
	    packageSend.mSendRawData = b;

        Message msg = mHandler.obtainMessage(MESSAGE_SEND_RAWDATA, packageSend);
        mHandler.sendMessage(msg);
	}

	public void sendMyIconNotify(Person to) {
	    if (mHandler == null) {
            return;
        }
	    UserIconEnvironment userIconEnvironment = LocalSetting.getInstance().getUserIconEnvironment();
	    if (!userIconEnvironment.isExistSelfHead()) {
	        return;
	    }

        PackageSend packageSend = PackageSend.createCommand(mMyself, to,
                ImMessages.IVY_SENDICON_NOTIFY,
                Long.toString(userIconEnvironment.getSelfHeadSize(), 16));

        Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);

        // add a task for tcp server.
        FileTask task = new FileTask();
        task.mTargetIP = null;
        task.mFileID = null;
        task.mPackID = null;
        task.mFilePathName = userIconEnvironment.getSelfHeadFullPath();
        task.mFileType = Im.FileType.FileType_HeadIcon;
        task.mUserID = 0;
        task.mToPerson = null;
        task.mFileSize = 0;
        TcpFileServer.getInstance().addTask(task);

        // send notify for other person to fetch the icon data.
        mHandler.sendMessage(msg);
	}

	public void isGetList() {
	    if (mHandler == null) {
            return;
        }
	    PackageSend packageSend = PackageSend.createCommand(mMyself, null,
	            ImMessages.IPMSG_BR_ISGETLIST,
	            null);

	    Message msg = mHandler.obtainMessage(MESSAGE_BROADCAST, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void okGetList(Person to) {
	    if (mHandler == null) {
            return;
        }
        PackageSend packageSend = PackageSend.createCommand(mMyself, to,
                ImMessages.IPMSG_OKGETLIST,
                null);

        Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
        mHandler.sendMessage(msg);
	}

	public void getList(Person to) {
	    if (mHandler == null) {
            return;
        }
        PackageSend packageSend = PackageSend.createCommand(mMyself, to,
                ImMessages.IPMSG_GETLIST | ImMessages.IPMSG_DIALUPOPT,
                null);

        Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
        mHandler.sendMessage(msg);
	}
	
	public void ansList(Person to) {
	    if (mHandler == null) {
            return;
        }
	    if (mNotifactionEngin == null) {
	        return;
	    }
	    StringBuffer stringBuffer = new StringBuffer();
	    List<Person> persons = PersonManager.getInstance().getPersonList();
	    for (int i = 0; i < persons.size(); ++i) {
	    	if (persons.get(i).mIP != null) {
		        stringBuffer.append(persons.get(i).mIP.getHostAddress().toString());
		        stringBuffer.append("\0");
	    	}
	    }

        PackageSend packageSend = PackageSend.createCommand(mMyself, to,
                ImMessages.IPMSG_ANSLIST,
                stringBuffer.toString());

        Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
        mHandler.sendMessage(msg);
	}

	private class TheMsgOfSendMessage {
	    PackageSend packageSend;
	    long userid;
	    String message;
	    String filename;
	    Im.FileType fileType;
	}
	public void sendMessage(long userid, Person to, String message) {
	    if (mHandler == null) {
            return;
        }
	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_SENDMSG | ImMessages.IPMSG_SENDCHECKOPT,
	            message);

	    TheMsgOfSendMessage theMsgOfSendMessage= new TheMsgOfSendMessage();
	    theMsgOfSendMessage.packageSend = packageSend;
	    theMsgOfSendMessage.userid = userid;
	    theMsgOfSendMessage.message = message;

	    Message msg = mHandler.obtainMessage(MESSAGE_SEND_MSG, theMsgOfSendMessage);
	    mHandler.sendMessage(msg);
	}

	public void sendGroupMessage(long userid, Person to, String message) {
	    if (mHandler == null) {
	        return;
	    }

	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_SENDMSG | ImMessages.IPMSG_BROADCASTOPT,
	            message);

	    Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void recvMsg(Person to, String pktno) {
	    if (mHandler == null) {
            return;
        }
	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_RECVMSG,
	            pktno);

	    Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void readMsg(Person to, String pktno) {
	    if (mHandler == null) {
            return;
        }
	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_READMSG,
	            pktno);

	    Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
	    mHandler.sendMessage(msg);
	}

	public void sendFile(long userid, Person to, String message, String filename, Im.FileType type) {
	    sendFile_l(userid, to, message, filename, type, false);
	}

	public void sendGroupFile(long userid, Person to, String message, String filename, Im.FileType type) {
	    if (VersionControl.getIvyVersion(to.mProtocolVersion) == 1) {
	        return;
	    }

	    sendFile_l(userid, to, message, filename, type, true);
	}

	private void sendFile_l(long userid, Person to, String message, String filename, Im.FileType type, boolean isGroupChat) {
	    if (mHandler == null) {
            return;
        }
		// send udp notify
	    PackageSend packageSend = PackageSend.createCommand(mMyself, to,
	            ImMessages.IPMSG_SENDMSG | ImMessages.IPMSG_FILEATTACHOPT,
	            null);

		// attachment
		// fileID:filename:size:mtime:fileattr
		//		[:extend-attr=val1[,val2...][:extend-attr2=...]]
		//		:\a:fileID...
		
		String fileID = String.valueOf(TcpFileServer.getNextFileID());
		
		StringBuffer additionInfoSb = new StringBuffer();
		File file = new File(filename);
		additionInfoSb.append(fileID + ":");  //ID
		additionInfoSb.append(file.getName() + ":"); // filename
		additionInfoSb.append(Long.toHexString(file.length()) + ":"); // size
		additionInfoSb.append(Long.toHexString(file.lastModified()) + ":"); // mtime
		additionInfoSb.append(Long.toHexString(ImMessages.IPMSG_FILE_REGULAR) + ":"); // fileattr  ?

		if (VersionControl.getIvyVersion(to.mProtocolVersion) >= 1) {
		    StringBuffer ivyFileTypeAttribute = new StringBuffer();
		    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_ATTR) + "="); 
		    switch (type) {
                case FileType_App:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_APP));
                    break;
                case FileType_Contact:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_CONTACT));
                    break;
                case FileType_Picture:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_PICTURE));
                    break;
                case FileType_Music:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_MUSIC));
                    break;
                case FileType_Video:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_VIDEO));
                    break;
                case FileType_OtherFile:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_OTHERFILE));
                    break;
                case FileType_Record:
                    ivyFileTypeAttribute.append(Long.toHexString(ImMessages.IVY_FILE_TYPE_VAL_RECORD));
                    break;
                default:
                    ivyFileTypeAttribute = null;
                    break;
            }

		    if (ivyFileTypeAttribute != null) {
		        ivyFileTypeAttribute.append(":");
		        additionInfoSb.append(ivyFileTypeAttribute);
		    }
		}

		if (VersionControl.getIvyVersion(to.mProtocolVersion) >= 2) {
		    additionInfoSb.append(Long.toHexString(ImMessages.IVY_GROUP_CHAT_ATTR));
		    additionInfoSb.append("=");
		    if (isGroupChat) {
		        additionInfoSb.append(Long.toHexString(ImMessages.IVY_GROUP_CHAT_VAL_TRUE));
		    } else {
		        additionInfoSb.append(Long.toHexString(ImMessages.IVY_GROUP_CHAT_VAL_FALSE));		        
		    }

		    additionInfoSb.append(":");
		}

		// byte[] bt = {0x07}; // TODO: why is 0x07?  the protocol say it is 0x0a, but iptux send 0x07.
		byte[] bt = {0x0a};
		String splitStr = new String(bt);
		additionInfoSb.append(splitStr);

		if (message != null) {
		    packageSend.mSendMessage.append(message + "\0" + additionInfoSb.toString() + "\0");
		} else {
		    packageSend.mSendMessage.append("\0" + additionInfoSb.toString() + "\0");
		}


		Message msg = mHandler.obtainMessage(MESSAGE_SPECIAL_PERSON, packageSend);
		/*TheMsgOfSendMessage theMsgOfSendFile= new TheMsgOfSendMessage();
		theMsgOfSendFile.packageSend = packageSend;
		theMsgOfSendFile.userid = userid;
		theMsgOfSendFile.message = message;
		theMsgOfSendFile.filename = filename;
		theMsgOfSendFile.fileType = type;
		Message msg = mHandler.obtainMessage(MESSAGE_SEND_FILE, theMsgOfSendFile);
	    */

		// 1 		// add a task for tcp listener.
		FileTask task = new FileTask();
		task.mTargetIP = to.mIP.getHostAddress().toString();
		task.mFileID = fileID;
		task.mPackID = PackageSend.getPacketNo(packageSend.mSendMessage.toString());
		task.mFilePathName = filename;
		task.mFileType = type;
		task.mUserID = userid;
		task.mToPerson = to;
		task.mFileSize = file.length();
		task.mIsGroupSend = isGroupChat?true:false;
		TcpFileServer.getInstance().addTask(task);

		// 2
		mHandler.sendMessage(msg);
	}
}
