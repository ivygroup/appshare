package com.ivyappshare.engin.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.database.Cursor;

import com.ivyappshare.engin.data.ImData;
import com.ivyappshare.engin.data.Table_GroupMessage;
import com.ivyappshare.engin.data.Table_Message;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.Im.FileType;

public class GroupMessages {

	public GroupMessages(ImData imData) {
        mListMessages = new ArrayList<GroupMessage>();
        init(imData);
    }

    private List<GroupMessage> mListMessages;
    private ImData mImData;

    private void init(ImData imData) {
        mImData = imData;

        mListMessages.clear();

        Cursor cursor = mImData.getGroupMessage();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                GroupMessage msg = new GroupMessage();
                msg.mId = cursor.getInt(cursor.getColumnIndex(Table_GroupMessage._ID));
                int GroupType = cursor.getInt(cursor.getColumnIndex(Table_GroupMessage.GROUPTYPE));
                msg.mIsBroadCast = GroupType == Table_GroupMessage.GROUPTYPE_BROADCAST;
                msg.mGroupName = cursor.getString(cursor.getColumnIndex(Table_GroupMessage.GROUPNAME));
                msg.mDirect = cursor.getInt(cursor.getColumnIndex(Table_GroupMessage.DIRECT));
                if (msg.mDirect == Table_Message.DIRECT_REMOTEPERSON) {
                    String key = cursor.getString(cursor.getColumnIndex(Table_GroupMessage.MAC));
                    msg.mFromPerson = PersonManager.getInstance().getPerson(key);
                }
                msg.mType = FileType.values()[cursor.getInt(cursor.getColumnIndex(Table_GroupMessage.TYPE))];
                msg.mContent = cursor.getString(cursor.getColumnIndex(Table_GroupMessage.CONTENT));
                msg.mTime = cursor.getLong(cursor.getColumnIndex(Table_GroupMessage.TIME));
                msg.mState = cursor.getInt(cursor.getColumnIndex(Table_GroupMessage.STATE));
                msg.mUnRead = cursor.getInt(cursor.getColumnIndex(Table_GroupMessage.UNREAD));

                mListMessages.add(msg);
            }
            cursor.close();
        }
    }

    public int getUnReadMessageCount(boolean isBroadcast, String groupName) {
        int nCount = 0;
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (isBroadcast && msg.mIsBroadCast) {
                if (msg.mUnRead == Table_Message.UNREAD_YES) {
                    nCount++;
                }
            }
        }
        return nCount;
    }

    public void deleteOneMessage(int id) {
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (msg.mId == id) {
                mListMessages.remove(i);
                break;
            }
        }
    }

    public void deleteMessage(boolean isBroadcast, String groupName) {
        // delete from end to start
        int nSize = mListMessages.size();
        for (int i=nSize-1; i>=0; i--) {
            GroupMessage msg = mListMessages.get(i);
            if (isBroadcast && msg.mIsBroadCast) {
                mListMessages.remove(i);
            }
        }
    }

    public void clearUnReadMessage(boolean isBroadcast, String groupName) {
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (isBroadcast && msg.mIsBroadCast) {
                msg.mUnRead = Table_Message.UNREAD_NO;
            }
        }
    }

    public void addUnReadMessage(int id) {
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (msg.mId == id) {
                msg.mUnRead = Table_Message.UNREAD_YES;
            }
        }
    }

    public void addBroadCastMessage(boolean isBroadcast, String groupName, Person p, FileType type, 
            String msg, boolean isMeSay, long timeMillis, int state, int id) {
        GroupMessage item = new GroupMessage();
        item.mId = id;
        item.mIsBroadCast = isBroadcast;
        item.mGroupName = groupName;
        if (!isMeSay) {
            item.mFromPerson = p;
        }
        item.mType = type;
        item.mContent = msg;
        item.mDirect = (isMeSay ? Table_Message.DIRECT_LOCALUSER : Table_Message.DIRECT_REMOTEPERSON);
        item.mTime = timeMillis;
        item.mState = state;
        mListMessages.add(item);
    }

    private class ComparatorGroupMessage implements Comparator<GroupMessage> {
        public int compare(GroupMessage arg0, GroupMessage arg1) {
            return (int)(arg0.mTime - arg1.mTime);
        }
    }

    public List<GroupMessage> getGroupMessageListClone(boolean isBroadcast, String groupName) {
        List<GroupMessage> listMessages = new ArrayList<GroupMessage>();
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (isBroadcast && msg.mIsBroadCast) {
                listMessages.add(msg);
            }
        }
        Collections.sort(listMessages, new ComparatorGroupMessage());
        return listMessages;
    }

    public void updateGroupMessageState(int id, int state) {
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (msg.mId == id) {
                msg.mState = state;
            }
        }
    }
    public void updateGroupMessageTypeAndContent(int id, FileType type, String content) {
        int nSize = mListMessages.size();
        for (int i=0; i<nSize; i++) {
            GroupMessage msg = mListMessages.get(i);
            if (msg.mId == id) {
                msg.mType = type;
                msg.mContent = content;
            }
        }
    }

    public GroupMessage getLatestGroupMessage() {
        int nSize = mListMessages.size();
        if (nSize > 0) {
            return mListMessages.get(nSize-1);
        }
        return null;
    }

    public void clearAllMessages() {
        mListMessages.clear();
    }
}
