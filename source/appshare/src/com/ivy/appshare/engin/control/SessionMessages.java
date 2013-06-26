package com.ivy.appshare.engin.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ivy.appshare.engin.data.ImData;
import com.ivy.appshare.engin.data.Table_Message;
import com.ivy.appshare.engin.data.Table_Share;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;

public class SessionMessages {
	public static final String TAG = SessionMessages.class.getSimpleName();

    private ImData mImData;
    private PersonManager mPersonManager;
    private GroupMessages mGroupMessages;

    public static final int SESSION_TYPE_CHAT = 0;
    public static final int SESSION_TYPE_GROUPCHAT = 1;
    public static final int SESSION_TYPE_FREESHARE = 2;

    public class SessionMessage {
        public int mSessionType;
        public Person mPerson;
        public ChatMessage mMessage;
        public GroupMessage mGroupMessage;
        public FreeShareMessage mFreeShareMessage;
        public int mUnReadGroupCount;
        public SessionMessage(ChatMessage msg, Person person) {
        	mSessionType = SESSION_TYPE_CHAT;
            mMessage = msg;
            mPerson = person;
        }
        public SessionMessage(GroupMessage msg, int count) {
        	mSessionType = SESSION_TYPE_GROUPCHAT;
            mGroupMessage = msg;
            mUnReadGroupCount = count;
        }
        public SessionMessage(FreeShareMessage msg) {
        	mSessionType = SESSION_TYPE_FREESHARE;
        	mFreeShareMessage = msg;
        }
    }
    
    public SessionMessages(ImData imData, GroupMessages groupMessages) {
        mImData = imData;
        mGroupMessages = groupMessages;
        mPersonManager = PersonManager.getInstance();
    }

    private class ComparatorPersonMessage implements Comparator<SessionMessage> {
        public int compare(SessionMessage arg0, SessionMessage arg1) {
        	long time0 = 0, time1 = 0;
        	switch (arg0.mSessionType) {
        	case SESSION_TYPE_CHAT:
        		time0 = arg0.mMessage.mTime;
        		break;
        	case SESSION_TYPE_GROUPCHAT:
        		time0 = arg0.mGroupMessage.mTime;
        		break;
        	case SESSION_TYPE_FREESHARE:
        		time0 = arg0.mFreeShareMessage.mTime;
        		break;
        	}
        	switch (arg1.mSessionType) {
        	case SESSION_TYPE_CHAT:
        		time1 = arg1.mMessage.mTime;
        		break;
        	case SESSION_TYPE_GROUPCHAT:
        		time1 = arg1.mGroupMessage.mTime;
        		break;
        	case SESSION_TYPE_FREESHARE:
        		time1 = arg1.mFreeShareMessage.mTime;
        		break;
        	}
            return (int)(time1 - time0);
        }
    }

    private void getGroupMessage(List<SessionMessage> listPersonMessage) {
        GroupMessage groupMessage = mGroupMessages.getLatestGroupMessage();
        if (groupMessage != null) {
            listPersonMessage.add(new SessionMessage(groupMessage, mGroupMessages.getUnReadMessageCount(true, "")));
        }
    }

    private void getChatMessage(List<SessionMessage> listPersonMessage) {
        Cursor cursor = mImData.getLatestMessage();
        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }

        cursor.moveToFirst();
        do {
            String key = cursor.getString(cursor.getColumnIndex(Table_Message.MAC));
            Person person = mPersonManager.getPerson(key);
            if (person == null) {
                continue;
            }

            ChatMessage msg = new ChatMessage();
            msg.mId = cursor.getInt(cursor.getColumnIndex(Table_Message._ID));
            msg.mType = FileType.values()[cursor.getInt(cursor.getColumnIndex(Table_Message.TYPE))];
            msg.mContent = cursor.getString(cursor.getColumnIndex(Table_Message.CONTENT));
            msg.mDirect = cursor.getInt(cursor.getColumnIndex(Table_Message.DIRECT));
            msg.mTime = cursor.getLong(cursor.getColumnIndex(Table_Message.TIME));
            msg.mState = cursor.getInt(cursor.getColumnIndex(Table_Message.STATE));
            
            listPersonMessage.add(new SessionMessage(msg, person));
        } while (cursor.moveToNext());
        cursor.close();
    }

    private void getFreeShareMessage(List<SessionMessage> listPersonMessage) {
        Cursor cursor = mImData.getFreeShareHistory();
        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }

        cursor.moveToPosition(cursor.getCount()-1);

        FreeShareMessage msg = new FreeShareMessage();
        msg.mId = cursor.getInt(cursor.getColumnIndex(Table_Share._ID));
        msg.mType = FileType.values()[cursor.getInt(cursor.getColumnIndex(Table_Share.TYPE))];
        msg.mContent = cursor.getString(cursor.getColumnIndex(Table_Share.CONTENT));
        msg.mTime = cursor.getLong(cursor.getColumnIndex(Table_Share.TIME));

        listPersonMessage.add(new SessionMessage(msg));
        cursor.close();
    }

    public List<SessionMessage> getSessionMessageList() {
        List<SessionMessage> listPersonMessage = new ArrayList<SessionMessage>();

        getGroupMessage(listPersonMessage);
        getFreeShareMessage(listPersonMessage);
        getChatMessage(listPersonMessage);

        Log.d(TAG, "getSessionMessageList Count " + listPersonMessage.size());
        Collections.sort(listPersonMessage, new ComparatorPersonMessage());
        return listPersonMessage;
    }
}
