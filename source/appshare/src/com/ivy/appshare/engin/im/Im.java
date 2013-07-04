package com.ivy.appshare.engin.im;

public abstract class Im {
    public static final int State_Active = 1;
    public static final int State_Idle = 2;
    public static final int State_Screen_Off = 3;
    public static final int State_Sleep = 4;
    public static final int State_OffLine = 99;

    // some operations.
    public enum FileType {
        FileType_App,
        FileType_Contact,
        FileType_Picture,
        FileType_Music,
        FileType_Video,
        FileType_OtherFile,
        FileType_Record,
        FileType_CommonMsg,     // not a file, but a common message. it used in ImData.java
        FileType_HeadIcon,      // the SimpleIm engin use this type, trans the user head icon.
    }
    public abstract void upLine();
    public abstract void upLine(Person to); // only for this user send upline message.
    public abstract void downLine();
    public abstract void getList();
    public abstract void absence();   // call this method when myself info has changed. eg: nickname, group and so on.
    public abstract void sendHeadIcon();
    public abstract void sendMessage(long id, Person to, String msg);   // id will be used in onErrorListener.
    public abstract void sendGroupMessage(long id, Person to, String message);
    public abstract void sendFile(long id, Person to, String msg, String filename, FileType type);
    public abstract void sendGroupFile(long id, Person to, String message, String filename, Im.FileType type);
    public abstract boolean cancelFileTranslate(Person to, long id);
    public abstract void clearAllFileTranslates();
    public abstract void changeUserState(int state);

    // release resources.
    public abstract void init();
    public abstract void release();

    // listeners
    public interface OnUserListener {
        public void onNewUser(Person p);
        public void onSomeoneAbsence(Person p);
        public void onSomeoneExit(Person p);
        public void onClearAll();
        public void onSomeoneHeadIcon(Person p);
    }

    public interface OnMessageListener {
        public void onReceiveMessage(Person from, String msg);
        public void onReceiveGroupMessage(Person from, String msg);
    }

    public interface OnSendFileListener {
        public void onBeginSend(long id, Person person, String name, FileType fileType);
        public void onTranslateProcess(long id, Person person, String name, FileType fileType, long pos, long total);
        public void onCompleteSend(long id, Person person, String name, FileType fileType);
        public void onSendFileTimeOut(long id,  Person person, String filename, FileType fileType);
        public void onSendFileError(long id,  Person person, String filename, FileType fileType);
    }

    public interface OnReceiveFileListener {
        public class RequestResult {
            public boolean bIsSaveThisfile;
            public String strSavePath;
        }

        public RequestResult requestFileTranslate(Person from, String id, String name, long size, long time, FileType fileType);
        public void onTranslateProcess(Person from, String id, String name, FileType fileType, long pos, long total);
        public void onCompleteFileReceive(Person from, String id, String name, FileType fileType);
        public void onReceiveFileError(Person from, String id, String name, FileType fileType);

        public RequestResult requestFileTranslate_Group(Person from, String id, String name, long size, long time, FileType fileType);
        public void onTranslateProcess_Group(Person from, String id, String name, FileType fileType, long pos, long total);
        public void onCompleteFileReceive_Group(Person from, String id, String name, FileType fileType);
        public void onReceiveFileError_Group(Person from, String id, String name, FileType fileType);
    }

    public interface OnErrorListener {
        public void onMsgSendFailed(long id, String msg);
    }

    public abstract void setOnUserListener(OnUserListener listener);
    public abstract void setOnMessageListener(OnMessageListener listener);
    public abstract void setOnSendFileListener(OnSendFileListener listener);
    public abstract void setOnFileListener(OnReceiveFileListener listener);
    public abstract void setOnErrorListener (OnErrorListener listener);
}