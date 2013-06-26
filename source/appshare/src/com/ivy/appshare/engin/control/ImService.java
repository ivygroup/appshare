package com.ivy.appshare.engin.control;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ivy.appshare.constdefines.IvyMessages;
import com.ivy.appshare.engin.control.SessionMessages.SessionMessage;
import com.ivy.appshare.engin.data.ImData;
import com.ivy.appshare.engin.data.Table_Message;
import com.ivy.appshare.engin.im.Im;
import com.ivy.appshare.engin.im.ImFactory;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;
public class ImService extends Service {
    private static final String TAG = "ImService";

    private Im mIm;
    private ImData mImData;
    private PersonMessages mPersonMessages;
    private PersonManager mPersonManager;
    private GroupMessages mGroupMessages;
    private SessionMessages mSessionMessages;
    private ImListener mImListener;
    private DaemonNotifaction mDaemonNotifaction;

    // This is the object that receives interactions from clients.    See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private UserStateMonitor mUserStateMonitor;

    public class LocalBinder extends Binder {
        public LocalBinder() {
            Log.d(TAG, "LocalBinder construct");
        }

        public ImService getService() {
            Log.d(TAG, "get Service called.");
            return ImService.this;
        }
    }

    @Override 
    public void onCreate() { 
        Log.d(TAG, "onCreate");

        // Init ImData here, make sure it's the first time init it
        mImData = new ImData();
        mImData.ResetUnEndMessage();

        mPersonMessages = new PersonMessages(mImData);
        mGroupMessages = new GroupMessages(mImData);
        mSessionMessages = new SessionMessages(mImData, mGroupMessages);
        mDaemonNotifaction = new DaemonNotifaction(mImData, mPersonMessages, mGroupMessages);
        mPersonManager = PersonManager.getInstance();

        mIm = ImFactory.getSimpleIm();
        mIm.init();

        mImListener = new ImListener(mImData, mPersonMessages, mGroupMessages);
        mIm.setOnUserListener(mImListener);
        mIm.setOnSendFileListener(mImListener);
        mIm.setOnMessageListener(mImListener);
        mIm.setOnFileListener(mImListener);
        mIm.setOnErrorListener(mImListener);

        mUserStateMonitor = new UserStateMonitor(this);

    }

    @Override 
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDaemonNotifaction.release();
        mDaemonNotifaction = null;

        mImData.ResetUnEndMessage();
        mImData.release();
        mImData = null;

        mIm.release();
        mIm = null;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return 0;
    }

    @Override 
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder; 
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind");
        return true;
    }


    // interface for Im
    public void upLine() {
        Log.i(TAG, "upLine");
        mIm.upLine();
        // mIm.getList();   //temp close this feature.
    }

    public void upLine(Person to) {
        mIm.upLine(to);
    }

    public void downLine() {
        mIm.downLine();
    }

    public void absence() {
        mIm.absence();
    }

    public void sendHeadIcon() {
        mIm.sendHeadIcon();
    }

    public void changeUserState(int state) {
        mIm.changeUserState(state);
    }

    // for Data read and write.
    public Person getPerson(String key) {
        if (mImData == null) {
            return null;
        }
        return mPersonManager.getPerson(key);
    }

    public List<Person> getPersonListClone() {
        return mPersonManager.getPersonList();
    }

    public List<SessionMessage> getSessionMessageListClone() {
        return mSessionMessages.getSessionMessageList();
    }

    public List<GroupMessage> getGroupMessageListClone(boolean isBroadcast, String groupName) {
        return mGroupMessages.getGroupMessageListClone(isBroadcast, groupName);
    }

    public Cursor getPersonHistoryMessage(Person person) {
        return mImData.getMsgHistory(person);
    }

    public void saveFreeShare(FileType type, String content) {
        mImData.addFreeShare(type, content, System.currentTimeMillis());
    }

    public Cursor getFreeShareHistory() {
        return mImData.getFreeShareHistory();
    }
    public void clearFreeShareHistory() {
        mImData.clearFreeShareHistory();
    }

    // for person message------------------------------------------------------
    public int sendMessage(Person to, String msg) {
        Log.i(TAG, "Send Message " + msg);
        int ret = mImData.addMessage(to, FileType.FileType_CommonMsg, msg, true, System.currentTimeMillis(), Table_Message.STATE_OK);
        mIm.sendMessage(ret, to, msg);

        mPersonMessages.addMessage(to);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_NEW, Table_Message.STATE_OK, ret, FileType.FileType_CommonMsg.ordinal(), msg, true, to);
        return ret;
    }

    public int sendFile(Person to, String msg, String filename, FileType fileType) {
        Log.i(TAG, "Send File " + filename);
        int ret = mImData.addMessage(to, fileType, filename, true, System.currentTimeMillis(), Table_Message.STATE_WAITING);
        mIm.sendFile(ret, to, msg, filename, fileType);
        mPersonMessages.addMessage(to);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_NEW, Table_Message.STATE_WAITING, ret, fileType.ordinal(), filename, true, to);
        return ret;
    }

    public void clearUnReadMessage(Person person) {
        Log.i(TAG, "clear UnRead Message");
        mImData.clearUnReadMessage(person);
        mPersonMessages.clearUnReadMessage(person);
        IvyMessages.sendPersonBroadCast(IvyMessages.VALUE_PERSONTYPE_UNREAD_MESSAGECHANGE, null);
    }

    public void deleteMessage(Person person, int id) {
        Log.i(TAG, "Delete One Person Message");
        mImData.RemoveMessage(id);
        mPersonMessages.deleteOneMessage(person);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_DELETE, Table_Message.STATE_OK, id, -1, null, true, person);
    }

    public void recoverMessage(Person person, FileType type, String msg, boolean isMeSay, long timeMillis, int state) {
        Log.i(TAG, "Recover One Person Message");
        int ret = mImData.addMessage(person, type, msg, isMeSay, timeMillis, state);
        mPersonMessages.addMessage(person);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_RECOVER, state, ret, type.ordinal(), msg, isMeSay, person);
    }

    public void deleteMessage(Person person) {
        Log.i(TAG, "Delete Person Message");
        mImData.RemoveMessage(person);
        mPersonMessages.deleteMessage(person);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_DELETE, Table_Message.STATE_OK, -1, -1, null, true, person);
    }

    public void deleteAllMessage() {
        Log.i(TAG, "Delete All Message");
        mImData.RemoveAllMessage();
        mPersonMessages.clearAllMessages();
        mGroupMessages.clearAllMessages();
        IvyMessages.sendPersonBroadCast(IvyMessages.VALUE_PERSONTYPE_UNREAD_MESSAGECHANGE, null);
    }

    // user state---------------------------------------------------------------
    public void onResumeMyActivity() {
        if (mUserStateMonitor != null) {
            mUserStateMonitor.onResumeMyActivity();
        }
    }

    public void checkMyActive() {
        if (mUserStateMonitor != null) {
            mUserStateMonitor.checkMyActive();
        }
    }
    
    //
    public ImListener getImListener() {
        return mImListener;
    }

    public DaemonNotifaction getDaemonNotifaction() {
        return mDaemonNotifaction;
    }

    // for group message-------------------------------------------------------
    public int sendGroupMessage(boolean isBroadCast, String groupName, String message) {
        Log.i(TAG, "Send Group Message " + message);
        int ret = mImData.addGroupMessage(true, groupName, null, FileType.FileType_CommonMsg, 
                message, true, System.currentTimeMillis(), Table_Message.STATE_OK);
        mGroupMessages.addBroadCastMessage(true, groupName, null, FileType.FileType_CommonMsg, 
                message, true, System.currentTimeMillis(), Table_Message.STATE_OK, ret);

        List<Person> listPersons = mPersonManager.getPersonList();
        int nSize = listPersons.size();
        for (int i=0; i<nSize; i++) {
            if (isBroadCast) {
                if (listPersons.get(i).isOnline()) {
                    mIm.sendGroupMessage(ret, listPersons.get(i), message);
                }
            }
        }

        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_NEW, Table_Message.STATE_OK, 
                ret, FileType.FileType_CommonMsg.ordinal(), message, true, groupName, true);
        return ret;
    }

    public int sendGroupFile(boolean isBroadCast, String groupName, String msg, String filename, FileType fileType) {
        Log.i(TAG, "Send Group File " + filename);
        int ret = mImData.addGroupMessage(true, groupName, null, fileType, 
                filename, true, System.currentTimeMillis(), Table_Message.STATE_OK);
        mGroupMessages.addBroadCastMessage(true, groupName, null, fileType, 
                filename, true, System.currentTimeMillis(), Table_Message.STATE_OK, ret);
        // send to every one in the group
        List<Person> listPersons = mPersonManager.getPersonList();
        int nSize = listPersons.size();
        for (int i=0; i<nSize; i++) {
            if (isBroadCast) {
                if (listPersons.get(i).isOnline()) {
                    mIm.sendGroupFile(ret, listPersons.get(i), msg, filename, fileType);
                }
            }
        }
        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_NEW, Table_Message.STATE_OK, 
                ret, fileType.ordinal(), filename, true, groupName, true);
        return ret;
    }

    public void clearGroupUnReadMessage(boolean isBroadCast, String groupName) {
        Log.i(TAG, "clear UnRead Group Message");
        mImData.clearGroupUnReadMessage(isBroadCast, groupName);
        mGroupMessages.clearUnReadMessage(isBroadCast, groupName);
        IvyMessages.sendPersonBroadCast(IvyMessages.VALUE_GROUP_UNREAD_MESSAGECHANGE, null);
    }

    public void deleteGroupMessage(boolean isBroadCast, String groupName, int id) {
        Log.i(TAG, "Delete one Group Message");
        mImData.RemoveGroupMessage(id);
        mGroupMessages.deleteOneMessage(id);
        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_DELETE, Table_Message.STATE_OK, id, -1, null,
                isBroadCast, groupName, true);
    }

    public void recoverGroupMessage(boolean isBroadCast, String groupName, Person p, FileType type, String msg, 
    		boolean isMeSay, long timeMillis, int state) {
        Log.i(TAG, "Recover One Group Message");
        int ret = mImData.addGroupMessage(isBroadCast, groupName, p, type, 
        		msg, isMeSay, timeMillis, state);
        mGroupMessages.addBroadCastMessage(isBroadCast, groupName, p, type, msg, isMeSay, timeMillis, state, ret);
        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_RECOVER, state, 
                ret, type.ordinal(), msg, isBroadCast, groupName, isMeSay);
    }

    public void deleteGroupMessage(boolean isBroadCast, String groupName) {
        Log.i(TAG, "Delete Group Message");
        mImData.RemoveGroupMessage(isBroadCast, groupName);
        mGroupMessages.deleteMessage(isBroadCast, groupName);
        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_DELETE, Table_Message.STATE_OK, -1, -1, null,
                isBroadCast, groupName, true);
    }
}
