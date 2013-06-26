package com.ivy.appshare.engin.control;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.R;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.data.ImData;
import com.ivy.appshare.engin.data.Table_Message;
import com.ivy.appshare.engin.im.Person;

public class DaemonNotifaction {
    private static final String TAG = "DaemonNotifaction";

    private Context mContext;
    private PersonManager mPersonManager;

    private ImData mImData;
    private PersonMessages mPersonMessages;
    private GroupMessages mGroupMessages;

    private MessageBroadCastReceiver mMessageReceiver = null;
    private GroupMessageBroadCastReceiver mGroupMessageReceiver = null;
    private PersonCountBroadCastReceiver mPersonCountBroadCastReceiver = null;
    private int mNotificationState = IvyMessages.NOTIFICATION_STATE_NONE;


    public DaemonNotifaction(ImData imData, PersonMessages personMessages, GroupMessages groupMessages) {
        mContext = MyApplication.getInstance();
        mPersonManager = PersonManager.getInstance();

        mImData = imData;
        mPersonMessages = personMessages;
        mGroupMessages = groupMessages;

        mMessageReceiver = new MessageBroadCastReceiver();
        IntentFilter filter = new IntentFilter(IvyMessages.INTENT_MESSAGE);
        filter.setPriority(300);
        mContext.registerReceiver(mMessageReceiver, filter);

        mGroupMessageReceiver = new GroupMessageBroadCastReceiver();
        IntentFilter filterGroup = new IntentFilter(IvyMessages.INTENT_GROUP_MESSAGE);
        filterGroup.setPriority(300);
        mContext.registerReceiver(mGroupMessageReceiver, filterGroup);

        mPersonCountBroadCastReceiver = new PersonCountBroadCastReceiver();
        IntentFilter filterPerson = new IntentFilter(IvyMessages.INTENT_PERSON);
        mContext.registerReceiver(mPersonCountBroadCastReceiver, filterPerson);

        startBackgroundNotification();
    }
    
    public void release() {
        Log.d(TAG, "release");
        mContext.unregisterReceiver(mPersonCountBroadCastReceiver);
        mContext.unregisterReceiver(mMessageReceiver);
        stopNotification();
    }

    public void addAndNotify(Intent intent) {
        int messageId = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_ID, 0);
        String key = (String)intent.getExtras().get(IvyMessages.PARAMETER_MESSAGE_PERSON);

        String prompt = null;
        Person person = null;

//        person = mPersonManager.getPerson(key);
//        if (person != null) {
//            prompt = String.format(mContext.getString(R.string.receive_one_message_format), person.mNickName);
//            addUnReadMessage(person, messageId);
//        }

//        startMessageNotification(prompt);
    }

    public void addGroupAndNotify(Intent intent) {
    	boolean isBroadCast = intent.getBooleanExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_BROADCAST, true);
    	String groupName = intent.getStringExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_GROUPNAME);
        int messageId = intent.getIntExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_ID, 0);

//        String prompt = "";
//        if (isBroadCast) {
//            prompt = String.format(mContext.getString(R.string.receive_one_broadcast_message), "");
//        } else {
//        	String.format(mContext.getString(R.string.receive_one_group_message_format), groupName);
//        }
//        addGroupUnReadMessage(messageId);
//
//        startMessageNotification(prompt);
    }

    public void startBackgroundNotificationIfNeed() {
        if (mNotificationState != IvyMessages.NOTIFICATION_STATE_BACKGROUND) {
            return;
        }
        startBackgroundNotification();
    }

    public void startBackgroundNotification() {
//        // Add notification here
//        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification note = new Notification(R.drawable.ic_launcher_background, null, System.currentTimeMillis());
//        note.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//
//        mNotificationState = IvyMessages.NOTIFICATION_STATE_BACKGROUND;
//        Intent intent = new Intent(mContext, MainPagerActivity.class);
//        intent.setData(Uri.parse("custom://" + IvyMessages.NOTIFICATION_STATE_BACKGROUND));
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(
//                mContext,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // String notifyString = getString(R.string.running_background, LocalSetting.getInstance().getMySelf().mNickName, mPersonManager.getActivePersonCount()),
//        String notifyString =  mContext.getResources().getQuantityString(R.plurals.running_background,
//                mPersonManager.getActivePersonCount(),
//                LocalSetting.getInstance().getMySelf().mNickName, mPersonManager.getActivePersonCount());
//
//        note.setLatestEventInfo(
//                mContext,
//                mContext.getString(R.string.app_name),
//                notifyString,
//                contentIntent);
//        nm.notify(R.string.app_name, note);
    }
    
    public int startMessageNotification(String prompt) {
//        HashMap<String, Integer> mapUnRead = mPersonMessages.getUnReadMessage();;
//
//        String unReadMessage = String.format(mContext.getString(R.string.receive_some_message));
//        int nPersonCount = 0;
//        int nMessageCount = 0;
//        String personKey = null;
//        if (mapUnRead != null) {
//            for(Map.Entry<String, Integer> value: mapUnRead.entrySet()){
//                nPersonCount++;
//                nMessageCount += value.getValue();
//                personKey = value.getKey();
//            }
//        }
//
//        int nGroupMessageCount = mGroupMessages.getUnReadMessageCount(true, "");
//
//        if ((nPersonCount == 0 || nMessageCount == 0) && nGroupMessageCount == 0) {
//            return -1;
//        }
//
//        // Add notification here
//        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification note = new Notification(R.drawable.ic_launcher, prompt, System.currentTimeMillis());
//        note.flags = Notification.FLAG_NO_CLEAR;
//        note.defaults = 0;
//        if (LocalSetting.getInstance().getRing()) {
//            note.defaults |= Notification.DEFAULT_SOUND;
//        }
//        if (LocalSetting.getInstance().getVibrate()) {
//            note.defaults |= Notification.DEFAULT_VIBRATE;
//        }
//
//        Intent intent = new Intent(mContext, MainPagerActivity.class);
//
//        if (nGroupMessageCount == 0 && nMessageCount > 0) {
//            unReadMessage = String.format(mContext.getString(R.string.receive_all_message_format), 
//                    String.valueOf(nMessageCount), String.valueOf(nPersonCount));
//            if (nPersonCount == 1) {
//                mNotificationState = IvyMessages.NOTIFICATION_STATE_MESSAGE_ONE;
//                intent.setData(Uri.parse("custom://" + personKey + "/" + IvyMessages.NOTIFICATION_STATE_MESSAGE_ONE));
//            } else {
//                mNotificationState = IvyMessages.NOTIFICATION_STATE_MESSAGE_SOME;
//                intent.setData(Uri.parse("custom://" + IvyMessages.NOTIFICATION_STATE_MESSAGE_SOME));
//            }
//        } else if (nGroupMessageCount > 0 && nMessageCount == 0) {
//            unReadMessage = String.format(mContext.getString(R.string.receive_group_message_format), 
//                    String.valueOf(nGroupMessageCount));
//            mNotificationState = IvyMessages.NOTIFICATION_STATE_MESSAGE_GROUP;
//            intent.setData(Uri.parse("custom://" + IvyMessages.NOTIFICATION_STATE_MESSAGE_GROUP));
//        } else {
//            unReadMessage = String.format(mContext.getString(R.string.receive_all_message_format), 
//                    String.valueOf(nMessageCount+nGroupMessageCount), String.valueOf(nPersonCount+1));
//            mNotificationState = IvyMessages.NOTIFICATION_STATE_MESSAGE_SOME;
//            intent.setData(Uri.parse("custom://" + IvyMessages.NOTIFICATION_STATE_MESSAGE_SOME));
//        }
//
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(
//                mContext,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        note.setLatestEventInfo(
//                mContext,
//                mContext.getString(R.string.app_name),
//                unReadMessage,
//                contentIntent);
//        nm.notify(R.string.app_name, note);

        return 0;
    }
    
    public int getNotificationState() {
        return mNotificationState;
    }


    private void stopNotification() {
        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(R.string.app_name);
        
        mNotificationState = IvyMessages.NOTIFICATION_STATE_NONE;
    }

    private void addUnReadMessage(Person person, int id) {
        Log.i(TAG, "add UnRead Message " + id);
        mImData.addUnReadMessage(id);
        mPersonMessages.addUnReadMessage(person);
        IvyMessages.sendPersonBroadCast(IvyMessages.VALUE_PERSONTYPE_UNREAD_MESSAGECHANGE, null);
    }

    private void addGroupUnReadMessage(int id) {
        Log.i(TAG, "add Group UnRead Message " + id);
        mImData.addGroupUnReadMessage(id);
        mGroupMessages.addUnReadMessage(id);
        IvyMessages.sendPersonBroadCast(IvyMessages.VALUE_GROUP_UNREAD_MESSAGECHANGE, null);
    }

 // receiver----------------------------------------------------------------
    private class MessageBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            int messageState = intent.getIntExtra(IvyMessages.PARAMETER_MESSGAE_STATE, 0);
            boolean blnLocalUser = intent.getBooleanExtra(IvyMessages.PARAMETER_MESSAGE_SELF, true);
            if (!blnLocalUser && messageState == Table_Message.STATE_OK) {
                addAndNotify(intent);
            }
            // don't pass the message to next receiver
            abortBroadcast();
        }
    }

    private class GroupMessageBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            int messageState = intent.getIntExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_STATE, 0);
            boolean blnLocalUser = intent.getBooleanExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_SELF, true);
            if (!blnLocalUser && messageState == Table_Message.STATE_OK) {
                addGroupAndNotify(intent);
            }

            // don't pass the message to next receiver
            abortBroadcast();
        }
    }

    private class PersonCountBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (mNotificationState != IvyMessages.NOTIFICATION_STATE_BACKGROUND) {
                return;
            }
            int type = intent.getIntExtra(IvyMessages.PARAMETER_PERSON_TYPE, 0);
            if ((type == IvyMessages.VALUE_PERSONTYPE_NEW_USER)
                    || (type == IvyMessages.VALUE_PERSONTYPE_SOMEONE_EXIT)
                    || type == IvyMessages.VALUE_PERSONTYPE_CLEAR_ALL) {
                startBackgroundNotification();
            }
        }
    }
}
