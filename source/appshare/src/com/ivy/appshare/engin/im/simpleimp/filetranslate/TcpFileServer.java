package com.ivy.appshare.engin.im.simpleimp.filetranslate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivy.appshare.engin.im.simpleimp.protocol.PackageReceive;
import com.ivy.appshare.engin.im.simpleimp.protocol.ProtocolUnPack;
import com.ivy.appshare.engin.im.simpleimp.protocol.VersionControl;
import com.ivy.appshare.engin.im.simpleimp.util.NotifactionEngin;

public class TcpFileServer extends Thread {
	private final static String TAG = "TcpFileServer";
	private final static int BUFFER_LENGTH = ImMessages.DEFAULT_BUFFER_LENGTH;

	private final int TIMEOUT_IVY = 5000;
	private final int TIMEOUT_OTHER = 1000 * 60 * 2;   // 2min

	//
	private static TcpFileServer gInstance = null;
	public static TcpFileServer getInstance() {
		return gInstance;
	}

	public static void createInstance(int port, NotifactionEngin notifactionEngin) {
	    if (gInstance != null) {
	        return;
	    }

	    gInstance = new TcpFileServer(port, notifactionEngin);
	    if (gInstance.mStatus == RunningStatus.Ready) {
	        gInstance.start();
	    }
	}

	public static void destroyInstance() {
		if (gInstance == null) {
			return;
		}

		gInstance.stopServer();
		try {
			gInstance.join();
		} catch (Exception e) {
			Log.i(TAG, "when join fileserver stop, occore a error. " + e.getMessage());
		}
		gInstance = null;
	}

	//
	private static int gFileIDIndex = 0;
	public static int getNextFileID() {
		return ++gFileIDIndex;
	}

	//
	private HashMap<String, FileTask> mTasks;	// key = mTargetIP + FileID
	private FileTask mHeadIconTask;
	private ServerSocket mServerSocket;
	private enum RunningStatus {
		Ready,
		Running,
		WantStop,
	}
	private RunningStatus mStatus;
	private NotifactionEngin mNotifactionEngin;
	private TaskTimeOutProtection mTaskTimeOutProtection;

	private TcpFileServer(int port, NotifactionEngin notifactionEngin) {
		mTasks = new HashMap<String, FileTask>();
		try {
			mServerSocket = new ServerSocket(port);
		} catch (IOException e) {
			Log.e(TAG, "create serversocket failed");
			e.printStackTrace();
		}

		mStatus = RunningStatus.Ready;
		mNotifactionEngin = notifactionEngin;
		mTaskTimeOutProtection = new TaskTimeOutProtection();
		mTaskTimeOutProtection.start();
	}

	public void addTask(FileTask task) {
	    if (task.mFileType == FileType.FileType_HeadIcon) {
	        if (mHeadIconTask == null) {
	            mHeadIconTask = task;
	        } else {
	            if (mHeadIconTask.mFilePathName.compareTo(task.mFilePathName) != 0) {
	                mHeadIconTask = task;
	            }
	        }
	        return;
	    }

	    synchronized (mTasks) {
	        task.mTaskTime = System.currentTimeMillis();
	        String key = task.mTargetIP+task.mFileID; 
	        mTasks.put(key, task);
	    }
	}

	private void stopServer() {
		mStatus = RunningStatus.WantStop;
		mTaskTimeOutProtection.interrupt();
		try {
		    mTaskTimeOutProtection.join();
		} catch (Exception e) {
		    Log.i(TAG, "when join fileserver stop, occore a error. " + e.getMessage());
		}

		ServerSocket tmpServerSocket = mServerSocket;
		mServerSocket = null;
		closeServerSocket(tmpServerSocket);
	}

	@Override
	public void run() {
		mStatus = RunningStatus.Running;
		while (mStatus != RunningStatus.WantStop) {
			Socket socket = null;
			BufferedInputStream bis = null;

			try {
			    socket = mServerSocket.accept();
			    Log.d(TAG, "build a tcp link with (" + socket.getInetAddress().getHostAddress() + ")");

			    synchronized (mTasks) {
			        // 1 read the request command.
			        bis = new BufferedInputStream(socket.getInputStream());

			        byte[] readBuffer = new byte[BUFFER_LENGTH];
			        int len = bis.read(readBuffer);

			        byte[] ipmsgData = new byte[len];
			        System.arraycopy(readBuffer, 0, ipmsgData, 0, len);

			        // 2 switch the command.
			        PackageReceive requestInfo = ProtocolUnPack.unPack(socket, ipmsgData);
			        Log.d(TAG, "command = " + requestInfo.mCommand + ", additional = " + requestInfo.mAdditionalSection);
			        long command = ImMessages.GET_MODE(requestInfo.mCommand);
			        if (command == ImMessages.IPMSG_GETFILEDATA) {
			            doFileTranslate(requestInfo, socket, bis);
			        } else if (command == ImMessages.IVY_GETHEADICON) {
			            doHeadIconTranslate(requestInfo, socket);
			        } else {
			            // TODO: add folder and multi files translate
			            Log.e(TAG, "we only translate one file. command = " + requestInfo.mCommand);
			            closeMyTempSocket(socket, bis);
			            continue;
			        }
			    }
			} catch(SocketException e) {
			    Log.i(TAG, "catch socketexception. " + e.getMessage());
			    if(e.getMessage().indexOf("socket closed")!=-1) {
			        Log.d(TAG, "is socket closed");
			        mServerSocket = null;
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    closeMyTempSocket(socket, bis);
			    socket = null;
			    bis = null;
			}
		} // end while.

		Log.d(TAG, "exit the while loop.");
		closeServerSocket(mServerSocket);
		mServerSocket = null;

		mStatus = RunningStatus.Ready;
	}

	private void closeMyTempSocket(Socket socket, BufferedInputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void closeServerSocket(ServerSocket serverSocket) {
		if (serverSocket == null) {
			return;
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
		    Log.d(TAG, "when closeServerSocket crash. " + e.getMessage());
		}
	}
	
	private void doFileTranslate(PackageReceive requestInfo, Socket socket, BufferedInputStream bis) {
	    // 1 check the additional data.
        String tmp[] = requestInfo.mAdditionalSection.split("\0");
        requestInfo.mAdditionalSection = tmp[0];
        String additional = requestInfo.mAdditionalSection;
        String[] fileNoArray = additional.split(":");
        Log.d(TAG, "array size = " + fileNoArray.length);
        if (fileNoArray.length < 3) {
            Log.e(TAG, "The additional data not correct, expect 3 parameter, but only " + fileNoArray.length);
            closeMyTempSocket(socket, bis);
            return;
        }

        // 2 check the task is correct or not.
        // packetID:filedID:offset.
        int fileID = Integer.parseInt(fileNoArray[1], 16);
        String key = socket.getInetAddress().getHostAddress().toString() + String.valueOf(fileID);
        FileTask task = mTasks.get(key);

        if (task == null) {
            try {
                fileID = Integer.parseInt(fileNoArray[1]);
                key = socket.getInetAddress().getHostAddress().toString() + String.valueOf(fileID);
                task = mTasks.get(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (task == null) {
            Log.i(TAG, "can't find the translate task. key = " + key);
            closeMyTempSocket(socket, bis);
            return;
        }

        // 3 to start thread ....
        Log.d(TAG, "will start a new thread to send the file.");
        TcpFileSender sender = new TcpFileSender(socket, task, requestInfo, mNotifactionEngin);
        sender.start();
        mTasks.remove(key);
	}

	private void doHeadIconTranslate(PackageReceive requestInfo, Socket socket) {
	    if (mHeadIconTask == null) {
	        return;
	    }

	    TcpHeadIconSender sender = new TcpHeadIconSender(mHeadIconTask.mFilePathName, socket);
	    sender.start();
	}




	private class TaskTimeOutProtection extends Thread {
	    @Override
	    public void run() {
	        while (mStatus != RunningStatus.WantStop) {
	            synchronized (mTasks) {
	                Iterator<FileTask> it = mTasks.values().iterator();
	                while (it.hasNext()) {
	                    FileTask task = (FileTask)it.next();
	                    int timeout = TIMEOUT_IVY;
                        if (VersionControl.isIvyVersion(task.mToPerson.mProtocolVersion)) {
                            timeout = TIMEOUT_IVY;
                        } else {
                            timeout = TIMEOUT_OTHER;
                        }
                        long curTime = System.currentTimeMillis();
                        if (curTime - task.mTaskTime > timeout) {
                            finishTask(task);
                            mNotifactionEngin.onSendFileTimeOut(task.mUserID, task.mToPerson, task.mFilePathName, task.mFileType);
                            it.remove();
                        }
	                }
                }

	            try {
                    Thread.sleep(TIMEOUT_IVY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
	        }
	    }  // end run
	}

	private void finishTask(FileTask task) {
	    /*if (mFileTask.mIsGroupSend) {
	            return;
	        }

	        if (!VersionControl.isIvyVersion(mFileTask.mToPerson.mProtocolVersion)) {
	            return;
	        }*/

	    FileSendQueue fileSendQueue = FileSendQueue.getInstance(task.mToPerson);
	    if (fileSendQueue != null) {
	        fileSendQueue.finishTask(task.mUserID);
	    }
	}
}
