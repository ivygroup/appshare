package com.ivy.appshare.engin.im.simpleimp.filetranslate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivy.appshare.engin.im.simpleimp.protocol.PackageSend;
import com.ivy.appshare.engin.im.simpleimp.util.NotifactionEngin;

public class TcpFileReceiver extends Thread {
	private final static String TAG = "TcpFileReceiver";
	private static final int BUFFE_LENGTH = ImMessages.DEFAULT_BUFFER_LENGTH;

	private String mHexPacketID;
	private Person mSender;
	private ReceiveFileInfo mRecvFileInfo;
	private String mHexFileID;
	private String mSavePath;
	private NotifactionEngin mNotifactionEngin;

	private Person mMyself;

	private Socket mSocket;
	private BufferedInputStream mBis;
	private BufferedOutputStream mBos;
	private BufferedOutputStream mFbos;
	private byte[] mOnceReadBuffer = new byte[BUFFE_LENGTH];


	public TcpFileReceiver(String packetno, 
						Person sender,
						ReceiveFileInfo recvfileinfo,
						String savePath,
						NotifactionEngin engin) {
	    this.mHexPacketID = dec2Hex(packetno);
	    this.mSender = sender;
		this.mRecvFileInfo = recvfileinfo;
		mHexFileID = dec2Hex(mRecvFileInfo.mRecvFileId);
		this.mSavePath = savePath;
		this.mNotifactionEngin = engin;
		this.mMyself = LocalSetting.getInstance().getMySelf();

		File fileDir = new File(mSavePath);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}
	}

	private String dec2Hex(String data) {
	    return Long.toHexString(Long.parseLong(data));
	}

	@Override
	public void run() {
		boolean isSuccessfulFlag = false;
		try {
			mSocket = new Socket(mSender.mIP.getHostAddress(), LocalSetting.getInstance().getMySelf().mPort);
			Log.d(TAG, "Connect the sender successfully, sender ip = " + mSender.mIP.getHostAddress());
			mBos = new BufferedOutputStream(mSocket.getOutputStream());		

			// Receive file
			File receiveFile = new File(mSavePath + mRecvFileInfo.mRecvFileName);

			//TODO: Need to add the rename design.
			//Now: If the file is exist, delete it.
			if (receiveFile.exists()) {
				receiveFile.delete();
			}

	        //Send the Tcp Msg to receive file data.
			String additionStr = mHexPacketID + ":" + mHexFileID + ":" + "0:";
			PackageSend packageSend = PackageSend.createCommand(mMyself, null, ImMessages.IPMSG_GETFILEDATA, additionStr);

			byte []sendmsg = packageSend.mSendMessage.toString().getBytes();
			mBos.write(sendmsg, 0, sendmsg.length);
			mBos.flush();

			Log.d(TAG, "Begin to receive file....");
			mFbos = new BufferedOutputStream(new FileOutputStream(
					receiveFile));
			mBis = new BufferedInputStream(mSocket.getInputStream());

			int len = 0;
			long addr = 0;
			while ((len = mBis.read(mOnceReadBuffer)) != -1) {
				// Log.d(TAG, "read len = " + len);
				mFbos.write(mOnceReadBuffer, 0, len);
				mFbos.flush();
				
				addr += len;
				if (mRecvFileInfo.mIsGroupSend) {
				    mNotifactionEngin.onTranslateProcess_Group(mSender,
                            mRecvFileInfo.mRecvFileId,
                            mRecvFileInfo.mRecvFileName,
                            mRecvFileInfo.mFileType,
                            addr,
                            mRecvFileInfo.mRecvFileSize);
				} else {
				    mNotifactionEngin.onTranslateProcess(mSender,
				            mRecvFileInfo.mRecvFileId,
				            mRecvFileInfo.mRecvFileName,
				            mRecvFileInfo.mFileType,
				            addr,
				            mRecvFileInfo.mRecvFileSize);
				}
			}

			if (mRecvFileInfo.mIsGroupSend) {
			    mNotifactionEngin.onCompleteFileReceive_Group(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
			} else {
			    mNotifactionEngin.onCompleteFileReceive(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
			}
			isSuccessfulFlag = true;
			Log.d(TAG, "receive file successfully, filename = " + mRecvFileInfo.mRecvFileName);
		} catch (UnsupportedEncodingException e) {
		    sendReceiveFileErrorNotify();
			e.printStackTrace();
		} catch (UnknownHostException e) {
		    sendReceiveFileErrorNotify();
			e.printStackTrace();
			Log.e(TAG, "remote ip address error");
		} catch (FileNotFoundException e) {
		    sendReceiveFileErrorNotify();
			e.printStackTrace();
			Log.e(TAG, "Create file failed");
		} catch (IOException e) {
		    sendReceiveFileErrorNotify();
			e.printStackTrace();
			Log.e(TAG, "IO ERROR");
		} finally {
			if (mBos != null) {
				try {
					mBos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mBos = null;
				}

				if (mFbos != null) {
					try {
						mFbos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mFbos = null;
				}

				if (mBis != null) {
					try {
						mBis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mBis = null;
				}
				
				if (mSocket != null) {
					try {
						mSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mSocket = null;
				}
			}

		if (!isSuccessfulFlag) {
		    if (mRecvFileInfo.mIsGroupSend) {
		        mNotifactionEngin.onReceiveFileError_Group(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
		    } else {
		        mNotifactionEngin.onReceiveFileError(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
		    }
		}

	} // end run

	private void sendReceiveFileErrorNotify() {
        if (mRecvFileInfo.mIsGroupSend) {
            mNotifactionEngin.onReceiveFileError_Group(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
        } else {
            mNotifactionEngin.onReceiveFileError(mSender, mRecvFileInfo.mRecvFileId, mRecvFileInfo.mRecvFileName, mRecvFileInfo.mFileType);
        }
	}
}
