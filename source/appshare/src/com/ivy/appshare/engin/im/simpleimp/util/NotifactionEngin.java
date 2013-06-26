package com.ivy.appshare.engin.im.simpleimp.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Im.OnErrorListener;
import com.ivy.appshare.engin.im.Im.OnMessageListener;
import com.ivy.appshare.engin.im.Im.OnReceiveFileListener;
import com.ivy.appshare.engin.im.Im.OnSendFileListener;
import com.ivy.appshare.engin.im.Im.OnUserListener;
import com.ivy.appshare.engin.im.Im.OnReceiveFileListener.RequestResult;

@SuppressLint("HandlerLeak")
public class NotifactionEngin  implements OnUserListener, OnMessageListener, OnSendFileListener, OnReceiveFileListener, OnErrorListener  {
    private static final int MSG_NEWUSER = 10;
    private static final int MSG_SOMEONE_ABSENCE = 11;
    private static final int MSG_SOMEONE_EXIT = 13;
    private static final int MSG_CLEAR_ALL = 14;
    private static final int MSG_SOMEONE_HEADICON = 15;
    private static final int MSG_SOMEONE_SIGN = 16;
    
    private static final int MSG_RECEIVE_MESSAGE = 20;
    private static final int MSG_RECEIVE_GROUP_MESSAGE = 21;
    
    // private static final int MSG_REQUEST_FILETRANSLATE = 30;
    private static final int MSG_RECEIVEFILE_TRANSFERING = 31;
    private static final int MSG_RECEIVEFILE_COMPLETE = 32;
    private static final int MSG_RECEIVEFILE_ERROR = 33;
    
    private static final int MSG_RECEIVEFILE_TRANSFERING_GROUP = 36;
    private static final int MSG_RECEIVEFILE_COMPLETE_GROUP = 37;
    private static final int MSG_RECEIVEFILE_ERROR_GROUP = 38;
    
    private static final int MSG_SENDFILE_BEGIN = 40;
    private static final int MSG_SENDFILE_PROCESS = 41;
    private static final int MSG_SENDFILE_COMPLETE = 42;
    private static final int MSG_SENDFILE_TIMEOUT = 43;
    private static final int MSG_SENDFILE_ERROR = 44;
    
    private OnUserListener mOnUserListener;
    private OnMessageListener mOnMessageListener;
    private OnSendFileListener mOnSendFileListener;
    private OnReceiveFileListener mOnReceiveFileListener;
    private OnErrorListener mOnErrorListener;
    private HandlerThread mThread;
    private Handler mHandler;
    private boolean mIsInit;
    
    public NotifactionEngin() {
        mThread = new HandlerThread("NotifactionEngin");
        mThread.start();
        mHandler = new MyHandler(mThread.getLooper());
        mIsInit = true;
    }

    public synchronized void release() {
        mIsInit = false;
        mHandler = null;
        mThread.getLooper().quit();
    }

    private boolean isCanWork() {
        return mIsInit;
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper lp) {
            super(lp);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEWUSER:
                    mOnUserListener.onNewUser((Person)msg.obj);
                    break;
                case MSG_SOMEONE_ABSENCE:
                    mOnUserListener.onSomeoneAbsence((Person)msg.obj);
                    break;
                case MSG_SOMEONE_EXIT:
                    mOnUserListener.onSomeoneExit((Person)msg.obj);
                    break;
                case MSG_CLEAR_ALL:
                    mOnUserListener.onClearAll();
                    break;
                case MSG_SOMEONE_HEADICON:
                    mOnUserListener.onSomeoneHeadIcon((Person)msg.obj);
                    break;
                case MSG_RECEIVE_MESSAGE:
                {
                    ReceiveMessagePackage packet = (ReceiveMessagePackage)msg.obj;
                    mOnMessageListener.onReceiveMessage(packet.fromPerson, packet.msg);
                }
                    break;
                case MSG_RECEIVE_GROUP_MESSAGE:
                {
                    ReceiveMessagePackage packet = (ReceiveMessagePackage)msg.obj;
                    mOnMessageListener.onReceiveGroupMessage(packet.fromPerson, packet.msg);
                }
                    break;
                case MSG_RECEIVEFILE_TRANSFERING:
                {
                	ReceiveFilePackage recvfile = (ReceiveFilePackage)msg.obj;
                	mOnReceiveFileListener.onTranslateProcess(recvfile.fromPerson, recvfile.fileid, recvfile.filename, recvfile.fileType, recvfile.position, recvfile.totalsize);
                }
                	break;
                case MSG_RECEIVEFILE_COMPLETE:
                {
                	ReceiveFilePackage finishrecv = (ReceiveFilePackage)msg.obj; 
                	mOnReceiveFileListener.onCompleteFileReceive(finishrecv.fromPerson, finishrecv.fileid, finishrecv.filename, finishrecv.fileType);
                }
                	break;
                case MSG_RECEIVEFILE_ERROR:
                {
                    ReceiveFilePackage finishrecv = (ReceiveFilePackage)msg.obj; 
                    mOnReceiveFileListener.onReceiveFileError(finishrecv.fromPerson, finishrecv.fileid, finishrecv.filename, finishrecv.fileType);
                }
                    break;

                case MSG_RECEIVEFILE_TRANSFERING_GROUP:
                {
                    ReceiveFilePackage recvfile = (ReceiveFilePackage)msg.obj;
                    mOnReceiveFileListener.onTranslateProcess_Group(recvfile.fromPerson, recvfile.fileid, recvfile.filename, recvfile.fileType, recvfile.position, recvfile.totalsize);
                }
                    break;
                case MSG_RECEIVEFILE_COMPLETE_GROUP:
                {
                    ReceiveFilePackage finishrecv = (ReceiveFilePackage)msg.obj; 
                    mOnReceiveFileListener.onCompleteFileReceive_Group(finishrecv.fromPerson, finishrecv.fileid, finishrecv.filename, finishrecv.fileType);
                }
                    break;
                case MSG_RECEIVEFILE_ERROR_GROUP:
                {
                    ReceiveFilePackage finishrecv = (ReceiveFilePackage)msg.obj; 
                    mOnReceiveFileListener.onReceiveFileError_Group(finishrecv.fromPerson, finishrecv.fileid, finishrecv.filename, finishrecv.fileType);
                }
                    break;

                case MSG_SENDFILE_BEGIN:
                {
                    SendFile tmpFile = (SendFile)msg.obj;
                    mOnSendFileListener.onBeginSend(tmpFile.id, tmpFile.person, tmpFile.name, tmpFile.fileType);
                }
                    break;
                case MSG_SENDFILE_PROCESS:
                {
                    SendFile tmpFile = (SendFile)msg.obj;
                    mOnSendFileListener.onTranslateProcess(tmpFile.id, tmpFile.person, tmpFile.name, tmpFile.fileType, tmpFile.pos, tmpFile.totle);
                }
                    break;
                case MSG_SENDFILE_COMPLETE:
                {
                    SendFile tmpFile = (SendFile)msg.obj;
                    mOnSendFileListener.onCompleteSend(tmpFile.id, tmpFile.person, tmpFile.name, tmpFile.fileType);
                }
                    break;
                case MSG_SENDFILE_TIMEOUT:
                {
                    SendFile tmpFile = (SendFile)msg.obj;
                    mOnSendFileListener.onSendFileTimeOut(tmpFile.id, tmpFile.person, tmpFile.name, tmpFile.fileType);
                }
                    break;
                case MSG_SENDFILE_ERROR:
                {
                    SendFile tmpFile = (SendFile)msg.obj;
                    mOnSendFileListener.onSendFileError(tmpFile.id, tmpFile.person, tmpFile.name, tmpFile.fileType);
                }
                    break;
                default:
                    break;
            }
        }
    }

    public synchronized void setOnUserListener(OnUserListener listener) {
        mOnUserListener = listener;
    }

    public synchronized void setOnMessageListener(OnMessageListener listener) {
        mOnMessageListener = listener;
    }

    public synchronized void setOnSendFileListener(OnSendFileListener listener) {
        mOnSendFileListener = listener;
    }

    public synchronized void setOnReceiveFileListener(OnReceiveFileListener listener) {
        mOnReceiveFileListener = listener;
    }

    public synchronized void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }


    // onUserListener
    @Override
    public synchronized void onNewUser(Person p) {
        if (!isCanWork()) {
            return;
        }
        if (mOnUserListener == null) {
            return;
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_NEWUSER, p));
    }

    @Override
    public synchronized void onSomeoneAbsence(Person p) {
        if (!isCanWork()) {
            return;
        }
        if (mOnUserListener == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SOMEONE_ABSENCE, p));
    }

    @Override
    public synchronized void onSomeoneExit(Person p) {
        if (!isCanWork()) {
            return;
        }
        if (mOnUserListener == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SOMEONE_EXIT, p));
    }

    @Override
    public synchronized void onClearAll() {
        if (!isCanWork()) {
            return;
        }
        if (mOnUserListener == null) {
            return;
        }
        mHandler.sendEmptyMessage(MSG_CLEAR_ALL);
    }

    @Override
    public synchronized void onSomeoneHeadIcon(Person p) {
        if (!isCanWork()) {
            return;
        }
        if (mOnUserListener == null) {
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SOMEONE_HEADICON, p));
    }



    // onMessageListener
    private class ReceiveMessagePackage {
        public Person fromPerson;
        public String msg;
    }

    @Override
    public synchronized void onReceiveMessage(Person from, String msg) {
        if (!isCanWork()) {
            return;
        }
        if (mOnMessageListener == null) {
            return;
        }
        ReceiveMessagePackage packet = new ReceiveMessagePackage();
        packet.fromPerson = from;
        packet.msg = msg;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_MESSAGE, packet));
    }

    @Override
    public synchronized void onReceiveGroupMessage(Person from, String msg) {
        if (!isCanWork()) {
            return;
        }
        if (mOnMessageListener == null) {
            return;
        }
        ReceiveMessagePackage packet = new ReceiveMessagePackage();
        packet.fromPerson = from;
        packet.msg = msg;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_GROUP_MESSAGE, packet));
    }

    // onSendFileListener
    private class SendFile {
        public long id;
        public Person person;
        public String name;
        public FileType fileType;
        public long pos;
        public long totle;
    }
    @Override
    public synchronized void onBeginSend(long id, Person person, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnSendFileListener == null) {
            return;
        }
        
        SendFile tmpFile = new SendFile();
        tmpFile.id = id;
        tmpFile.person = person;
        tmpFile.name = name;
        tmpFile.fileType = fileType;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SENDFILE_BEGIN, tmpFile));
    }

    @Override
    public synchronized void onTranslateProcess(long id, Person person, String name, FileType fileType, long pos, long total) {
        if (!isCanWork()) {
            return;
        }
        if (mOnSendFileListener == null) {
            return;
        }
        SendFile tmpFile = new SendFile();
        tmpFile.id = id;
        tmpFile.person = person;
        tmpFile.name = name;
        tmpFile.fileType = fileType;
        tmpFile.pos = pos;
        tmpFile.totle = total;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SENDFILE_PROCESS, tmpFile));
    }

    @Override
    public synchronized void onCompleteSend(long id, Person person, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnSendFileListener == null) {
            return;
        }
        SendFile tmpFile = new SendFile();
        tmpFile.id = id;
        tmpFile.person = person;
        tmpFile.name = name;
        tmpFile.fileType = fileType;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SENDFILE_COMPLETE, tmpFile));
    }

    public synchronized void onSendFileTimeOut(long id,  Person person, String filename, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnSendFileListener == null) {
            return;
        }
        SendFile tmpFile = new SendFile();
        tmpFile.id = id;
        tmpFile.person = person;
        tmpFile.name = filename;
        tmpFile.fileType = fileType;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SENDFILE_TIMEOUT, tmpFile));
    }

    public synchronized void onSendFileError(long id,  Person person, String filename, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnSendFileListener == null) {
            return;
        }
        SendFile tmpFile = new SendFile();
        tmpFile.id = id;
        tmpFile.person = person;
        tmpFile.name = filename;
        tmpFile.fileType = fileType;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SENDFILE_ERROR, tmpFile));
    }


    // OnReceiveFileListener
    @Override
    public synchronized RequestResult requestFileTranslate(Person from, String id, String name, long size, long time, FileType fileType) {
        RequestResult defaultRequestResult = new RequestResult();
        defaultRequestResult.bIsSaveThisfile = true;
        defaultRequestResult.strSavePath = LocalSetting.getInstance().getLocalPath();

        if (!isCanWork()) {
            return defaultRequestResult;
        }
        if (mOnReceiveFileListener == null) {
            
            return defaultRequestResult;
        }

        return mOnReceiveFileListener.requestFileTranslate(from, id, name, size, time, fileType);
    }

    private class ReceiveFilePackage {
    	public Person fromPerson;
    	public String fileid;
    	public String filename;
    	public FileType fileType;
    	public long position;
    	public long totalsize;
    }
    @Override
    public synchronized void onTranslateProcess(Person from, String id, String name, FileType fileType, long pos, long total) {
        if (!isCanWork()) {
            return;
        }
    	if (mOnReceiveFileListener == null) {
    		return;
    	}
    	
    	ReceiveFilePackage recvfile = new ReceiveFilePackage();
    	recvfile.fromPerson = from;
    	recvfile.fileid = id;
    	recvfile.filename = name;
    	recvfile.fileType = fileType;
    	recvfile.position = pos;
    	recvfile.totalsize = total;
    	
    	mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_TRANSFERING, recvfile));
    }
    @Override
    public synchronized void onCompleteFileReceive(Person from, String id, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
    	if (mOnReceiveFileListener == null) {
    		return;
    	}

    	ReceiveFilePackage recvfile = new ReceiveFilePackage();
    	recvfile.fromPerson = from;
    	recvfile.fileid = id;
    	recvfile.filename = name;
    	recvfile.fileType = fileType;

    	mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_COMPLETE, recvfile));
    }
    @Override
    public synchronized void onReceiveFileError(Person from, String id, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnReceiveFileListener == null) {
            return;
        }

        ReceiveFilePackage recvfile = new ReceiveFilePackage();
        recvfile.fromPerson = from;
        recvfile.fileid = id;
        recvfile.filename = name;
        recvfile.fileType = fileType;

        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_ERROR, recvfile));
    }

    @Override
    public RequestResult requestFileTranslate_Group(Person from, String id, String name, long size, long time, FileType fileType) {
        RequestResult defaultRequestResult = new RequestResult();
        defaultRequestResult.bIsSaveThisfile = true;
        defaultRequestResult.strSavePath = LocalSetting.getInstance().getLocalPath();

        if (!isCanWork()) {
            return defaultRequestResult;
        }
        if (mOnReceiveFileListener == null) {
            
            return defaultRequestResult;
        }

        return mOnReceiveFileListener.requestFileTranslate_Group(from, id, name, size, time, fileType);
    }
    @Override
    public void onTranslateProcess_Group(Person from, String id, String name, FileType fileType, long pos, long total) {
        if (!isCanWork()) {
            return;
        }
        if (mOnReceiveFileListener == null) {
            return;
        }
        
        ReceiveFilePackage recvfile = new ReceiveFilePackage();
        recvfile.fromPerson = from;
        recvfile.fileid = id;
        recvfile.filename = name;
        recvfile.fileType = fileType;
        recvfile.position = pos;
        recvfile.totalsize = total;
        
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_TRANSFERING_GROUP, recvfile));
    }
    @Override
    public void onCompleteFileReceive_Group(Person from, String id, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnReceiveFileListener == null) {
            return;
        }

        ReceiveFilePackage recvfile = new ReceiveFilePackage();
        recvfile.fromPerson = from;
        recvfile.fileid = id;
        recvfile.filename = name;
        recvfile.fileType = fileType;

        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_COMPLETE_GROUP, recvfile));
    }
    @Override
    public void onReceiveFileError_Group(Person from, String id, String name, FileType fileType) {
        if (!isCanWork()) {
            return;
        }
        if (mOnReceiveFileListener == null) {
            return;
        }

        ReceiveFilePackage recvfile = new ReceiveFilePackage();
        recvfile.fromPerson = from;
        recvfile.fileid = id;
        recvfile.filename = name;
        recvfile.fileType = fileType;

        mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVEFILE_ERROR_GROUP, recvfile));
    }

    // onErrorListener
    @Override
    public synchronized void onMsgSendFailed(long id, String msg) {
        if (!isCanWork()) {
            return;
        }
        if (mOnErrorListener == null) {
            return;
        }
        mOnErrorListener.onMsgSendFailed(id, msg);
    }
}
