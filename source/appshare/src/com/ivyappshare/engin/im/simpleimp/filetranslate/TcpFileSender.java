package com.ivyappshare.engin.im.simpleimp.filetranslate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import android.util.Log;

import com.ivyappshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivyappshare.engin.im.simpleimp.protocol.PackageReceive;
import com.ivyappshare.engin.im.simpleimp.util.NotifactionEngin;

public class TcpFileSender extends Thread {
	private static final String TAG = "TcpFileSender";
	private static final int BUFFER_LENGTH = ImMessages.DEFAULT_BUFFER_LENGTH;

	private Socket mSocket;
	private FileTask mFileTask;
	private PackageReceive mRequestInfo;
	private NotifactionEngin mNotifactionEngin;

	public TcpFileSender(Socket socket, FileTask task, PackageReceive requestInfo, NotifactionEngin notifactionEngin) {
		mSocket = socket;
		mFileTask = task;
		mRequestInfo = requestInfo;
		mNotifactionEngin = notifactionEngin;
	}

	@Override
	public void run() {
	    if (!mFileTask.mIsGroupSend) {
	        mNotifactionEngin.onBeginSend(mFileTask.mUserID, mFileTask.mToPerson, mFileTask.mFilePathName, mFileTask.mFileType);
	    }

	    Log.d(TAG, "TcpFileSender Thread run. task.mFileID = " + mFileTask.mFileID 
                + ", filename = " + mFileTask.mFilePathName);

		// 1 we should check the translate command, now, we only translate one file.
		if (ImMessages.GET_MODE(mRequestInfo.mCommand) != ImMessages.IPMSG_GETFILEDATA) {
			closeTempSocket(mSocket, null, null);
			finishTask();
			return;
		}
		
		// 2. translate one file.
		// String additional = mRequestInfo.mAdditionalSection;
		// String[] fileNoArray = additional.split(":");
		// // packetID:filedID:offset.
		// long offset = Long.parseLong(fileNoArray[2], 16);
		long offset = 0;

		BufferedOutputStream outputStream = null;
		BufferedInputStream inputStream = null;
		try {
			outputStream = new BufferedOutputStream(mSocket.getOutputStream());

			File sendFile = new File(mFileTask.mFilePathName);
			inputStream = new BufferedInputStream(new FileInputStream(sendFile));
			inputStream.skip(offset);

			Log.d(TAG, "nputstream = " + inputStream);
			
			int rlen = 0;
			long sumLength = 0;
			byte[] readBuffer = new byte[BUFFER_LENGTH];
			while((rlen = inputStream.read(readBuffer)) != -1) {
				outputStream.write(readBuffer, 0, rlen);
				sumLength += rlen;
				if (!mFileTask.mIsGroupSend) {
				    mNotifactionEngin.onTranslateProcess(mFileTask.mUserID,
				            mFileTask.mToPerson,
				            mFileTask.mFilePathName,
				            mFileTask.mFileType,
				            sumLength,
				            mFileTask.mFileSize);
				}
			}
			outputStream.flush();
			if (!mFileTask.mIsGroupSend) {
			    mNotifactionEngin.onCompleteSend(mFileTask.mUserID, mFileTask.mToPerson, mFileTask.mFilePathName, mFileTask.mFileType);
			}
			Log.d(TAG, "finish task " + mFileTask.mFileID + ", path =" + mFileTask.mFilePathName);
		} catch (IOException e) {
		    if (!mFileTask.mIsGroupSend) {
                mNotifactionEngin.onSendFileError(mFileTask.mUserID, mFileTask.mToPerson, mFileTask.mFilePathName, mFileTask.mFileType);
            }
			e.printStackTrace();
		} finally {
			closeTempSocket(mSocket, outputStream, inputStream);			
			mSocket = null;
		}
		
		Log.d(TAG, "TcpFileSender Thread will exit. task.mFileID = " + mFileTask.mFileID 
				+ ", filename = " + mFileTask.mFilePathName);

		finishTask();
	}
	
	private void closeTempSocket(Socket socket, BufferedOutputStream outputStream, BufferedInputStream inputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        if(socket != null) {
            try {
                socket.shutdownInput();
            } catch (IOException e1) {
            }
            try {
                socket.shutdownOutput();
            } catch (IOException e1) {
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	private void finishTask() {
	    /*if (mFileTask.mIsGroupSend) {
	        return;
	    }

	    if (!VersionControl.isIvyVersion(mFileTask.mToPerson.mProtocolVersion)) {
	        return;
	    }*/

	    FileSendQueue fileSendQueue = FileSendQueue.getInstance(mFileTask.mToPerson);
	    if (fileSendQueue != null) {
	        fileSendQueue.finishTask(mFileTask.mUserID);
	    }
	}
}
