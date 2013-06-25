package com.ivyappshare.engin.im.simpleimp;

import java.util.List;

import android.util.Log;

import com.ivyappshare.engin.control.LocalSetting;
import com.ivyappshare.engin.control.PersonManager;
import com.ivyappshare.engin.im.Im;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.simpleimp.filetranslate.FileSendQueue;
import com.ivyappshare.engin.im.simpleimp.filetranslate.TcpFileServer;
import com.ivyappshare.engin.im.simpleimp.protocol.Receiver;
import com.ivyappshare.engin.im.simpleimp.protocol.Sender;
import com.ivyappshare.engin.im.simpleimp.protocol.VersionControl;
import com.ivyappshare.engin.im.simpleimp.util.KeepAlive;
import com.ivyappshare.engin.im.simpleimp.util.NotifactionEngin;

public class SimpleIm extends Im {
    private static String TAG = "SimpleIm";

    private Sender mSender;
    private Receiver mReceiver;
    private NotifactionEngin mNotifactionEngin;
    private KeepAlive mKeepAlive;

    // construct
    public SimpleIm() {

    }

    @Override
    public void init() {
        Log.d(TAG, "Called init");
        if (mSender != null) {
            return;
        }

        mNotifactionEngin = new NotifactionEngin();
        mSender = new Sender(mNotifactionEngin);
        mReceiver = new Receiver(mSender, mNotifactionEngin);
        mKeepAlive = new KeepAlive(mSender, mNotifactionEngin);
        mKeepAlive.start();
        TcpFileServer.createInstance(LocalSetting.getInstance().getMySelf().mPort, mNotifactionEngin);
        LocalSetting.getInstance().getMySelf().mState = State_OffLine;
        FileSendQueue.createInstance(mSender);
    }

    @Override
    public void release() {
        Log.d(TAG, "release in");

        LocalSetting.getInstance().getMySelf().mState = State_OffLine;

        if (mKeepAlive != null) {
            mKeepAlive.stopMe();
            mKeepAlive.interrupt();
            mKeepAlive = null;
        }

        if (mSender != null) {
            mSender.release();
            mSender = null;
        }

        if (mReceiver != null) {
            mReceiver = null;
        }

        FileSendQueue.destroyInstance();
        TcpFileServer.destroyInstance();

        mNotifactionEngin.release();
        mNotifactionEngin = null;
        Log.d(TAG, "release out");
    }

    @Override
    public void upLine() {
        if (mSender == null) {
            return;
        }
        PersonManager.getInstance().clearAll();
        if (mNotifactionEngin != null) {
            mNotifactionEngin.onClearAll();
        }
        LocalSetting.getInstance().getMySelf().mState = State_Active;
        mSender.upLine();
    }

    @Override
    public void upLine(Person to) {
        if (mSender == null) {
            return;
        }
        mSender.upLine(to);
    }
    
    @Override
    public void downLine() {
        if (mSender == null) {
            return;
        }

        LocalSetting.getInstance().getMySelf().mState = State_OffLine;

        List<Person> persons = PersonManager.getInstance().getPersonList();
        for (int i = 0; i<persons.size(); ++i) {
            Person tmpPerson = persons.get(i);
            mSender.downLine(tmpPerson);
        }

        mSender.downLine();

        PersonManager.getInstance().clearAll();
        if (mNotifactionEngin != null) {
            mNotifactionEngin.onClearAll();
        }
    }

    @Override
    public void getList() {
       if (mSender == null) {
           return;
       }
       mSender.isGetList();
    }

    @Override
    public void absence() {
        if (mSender == null) {
            return;
        }
        List<Person> persons = PersonManager.getInstance().getPersonList();
        for (int i = 0; i<persons.size(); ++i) {
            Person tmpPerson = persons.get(i);
            mSender.absence(tmpPerson);
        }
    }

    @Override
    public void sendHeadIcon() {
        if (mSender == null) {
            return;
        }

        List<Person> persons = PersonManager.getInstance().getPersonList();
        for (int i = 0; i<persons.size(); ++i) {
            Person tmpPerson = persons.get(i);
            mSender.sendMyIconNotify(tmpPerson);
        }
    }

    @Override
    public void sendMessage(long id, Person to, String msg) {
        mSender.sendMessage(id, to, msg);
    }

    @Override
    public void sendGroupMessage(long id, Person to, String message) {
        mSender.sendGroupMessage(id, to, message);
    }

    @Override
    public void sendFile(long id, Person to, String msg, String filename, FileType type) {
        FileSendQueue fileSendQueue = FileSendQueue.getInstance(to);
        if (fileSendQueue != null) {
            fileSendQueue.addTask(FileSendQueue.SenderType_Common, id, to, msg, filename, type);
        }

        // mSender.sendFile(id, to, msg, filename, type);
    }

    @Override
    public void sendGroupFile(long id, Person to, String msg, String filename, Im.FileType type) {
        FileSendQueue fileSendQueue = FileSendQueue.getInstance(to);
        if (fileSendQueue != null) {
            fileSendQueue.addTask(FileSendQueue.SenderType_Group, id, to, msg, filename, type);
        }
        // mSender.sendGroupFile(id, to, msg, filename, type);
    }

    @Override
    public void cancelFileTranslate(long id) {
        // TODO:
    }

    @Override
    public void changeUserState(int state) {
        LocalSetting.getInstance().getMySelf().mState = state;
        mSender.upLine();
    }

    @Override
    public void setOnUserListener(OnUserListener listener) {
        mNotifactionEngin.setOnUserListener(listener);
    }

    @Override
    public void setOnMessageListener(OnMessageListener listener) {
        mNotifactionEngin.setOnMessageListener(listener);
    }
    
    @Override
    public void setOnSendFileListener(OnSendFileListener listener) {
        mNotifactionEngin.setOnSendFileListener(listener);
    }

    @Override
    public void setOnFileListener(OnReceiveFileListener listener) {
        mNotifactionEngin.setOnReceiveFileListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mNotifactionEngin.setOnErrorListener(listener);
    }
}
