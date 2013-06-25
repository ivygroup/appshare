package com.ivyappshare.engin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ivyappshare.MyApplication;
import com.ivyappshare.engin.control.PersonManager;
import com.ivyappshare.engin.im.Im;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.Im.FileType;

public class ImData {
    private static final String TAG = "ImData";
    private static final String DATABASENAME = "ivyshare.db";
    private static final int VERSION = 4;

    private ImDataHelper mDataHelper;
    private SQLiteDatabase mDatabase;


    public ImData() {
        init();
    }

    private void init() {
        if (mDatabase != null) {
            return;
        }
        mDataHelper = new ImDataHelper(MyApplication.getInstance(), DATABASENAME, null, VERSION);
        mDatabase = mDataHelper.getWritableDatabase();
    }

    public synchronized void release() {
        if (mDatabase != null) {
            if (mDatabase.isOpen()) {
                mDatabase.close();
            }
            mDatabase = null;
        }
    }

    // is return true, this use is added. else update or no operator. 
    public synchronized boolean updateOneUser(Person person) {
        if (mDatabase == null) {
            return false;
        }
        if (person.mMac == null) {
            return false;
        }

        String tmpSelectString = "select count(*) from " + Table_Users.TABLE_NAME
                + " where " + Table_Users.MAC + " = ?";
        Cursor cursor = mDatabase.rawQuery(tmpSelectString, new String[]{person.mMac});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            String up = "update " + Table_Users.TABLE_NAME + " set "
                    + Table_Users.PROTOCOLVERSION + " = ?, "
                    + Table_Users.NAME + " = ?, "
                    + Table_Users.HOST + " = ?, "
                    + Table_Users.NICKNAME + " = ?, "
                    + Table_Users.IMAGE + " = ?, "
                    + Table_Users.GROUPNAME + " = ?, "
                    + Table_Users.SIGNATURE + " = ?, "
                    + Table_Users.MSISDN + " = ?, "
                    + Table_Users.IMEI + " = ? "
                    + " where " + Table_Users.MAC + " = ?";
            String []args = new String[]{
                    person.mProtocolVersion,
                    person.mName,
                    person.mHost,
                    person.mNickName,
                    person.mImage,
                    person.mGroup,
                    person.mSignature,
                    person.mMsisdn,
                    person.mImei,
                    person.mMac
            };
            mDatabase.execSQL(up, args);
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(Table_Users.PROTOCOLVERSION, person.mProtocolVersion);
            values.put(Table_Users.NAME, person.mName);
            values.put(Table_Users.HOST, person.mHost);
            values.put(Table_Users.NICKNAME, person.mNickName);
            values.put(Table_Users.IMAGE, person.mImage);
            values.put(Table_Users.GROUPNAME, person.mGroup);
            values.put(Table_Users.SIGNATURE, person.mSignature);
            values.put(Table_Users.MAC, person.mMac);
            values.put(Table_Users.MSISDN, person.mMsisdn);
            values.put(Table_Users.IMEI, person.mImei);

            mDatabase.insert(Table_Users.TABLE_NAME, null, values);
            return true;
        }
    }

    public List<Person> getHistoryPersons() {
        List<Person> persons = new ArrayList<Person>();

        String tmpSelectString = "select * from " + Table_Users.TABLE_NAME;
        Cursor cursor = mDatabase.rawQuery(tmpSelectString, null);

        if (cursor.getCount() <= 0) {
            return persons;
        }

        cursor.moveToFirst();
        do {
            Person p = new Person();
            p.mProtocolVersion = cursor.getString(cursor.getColumnIndex(Table_Users.PROTOCOLVERSION));
            p.mName = cursor.getString(cursor.getColumnIndex(Table_Users.NAME));
            p.mHost = cursor.getString(cursor.getColumnIndex(Table_Users.HOST));
            p.mNickName = cursor.getString(cursor.getColumnIndex(Table_Users.NICKNAME));
            p.mImage = cursor.getString(cursor.getColumnIndex(Table_Users.IMAGE));
            p.mGroup = cursor.getString(cursor.getColumnIndex(Table_Users.GROUPNAME));
            p.mSignature = cursor.getString(cursor.getColumnIndex(Table_Users.SIGNATURE));
            p.mMac = cursor.getString(cursor.getColumnIndex(Table_Users.MAC));
            p.mMsisdn = cursor.getString(cursor.getColumnIndex(Table_Users.MSISDN));
            p.mImei = cursor.getString(cursor.getColumnIndex(Table_Users.IMEI));
            p.mState = Im.State_OffLine;
            persons.add(p);
        } while (cursor.moveToNext());
        cursor.close();

        return persons;
    }

    public HashMap<String, Integer> getUnReadMessage() {
        HashMap<String, Integer> mapUnRead = new HashMap<String, Integer>();

        String tmpSelectString = "select * from " + View_UserMessage.VIEW_UNREADNAME;
        Cursor cursor = mDatabase.rawQuery(tmpSelectString, null);
        if (cursor.getCount() <= 0) {
            return mapUnRead;
        }

        cursor.moveToFirst();
        do {
            Person p = new Person();
            p.mProtocolVersion = cursor.getString(cursor.getColumnIndex(Table_Users.PROTOCOLVERSION));
            p.mName = cursor.getString(cursor.getColumnIndex(Table_Users.NAME));
            p.mHost = cursor.getString(cursor.getColumnIndex(Table_Users.HOST));
            p.mNickName = cursor.getString(cursor.getColumnIndex(Table_Users.NICKNAME));
            p.mImage = cursor.getString(cursor.getColumnIndex(Table_Users.IMAGE));
            p.mGroup = cursor.getString(cursor.getColumnIndex(Table_Users.GROUPNAME));
            p.mSignature = cursor.getString(cursor.getColumnIndex(Table_Users.SIGNATURE));
            p.mMac = cursor.getString(cursor.getColumnIndex(Table_Users.MAC));
            p.mMsisdn = cursor.getString(cursor.getColumnIndex(Table_Users.MSISDN));
            p.mImei = cursor.getString(cursor.getColumnIndex(Table_Users.IMEI));

            int unReadCount = cursor.getInt(cursor.getColumnIndex(View_UserMessage.UNREADCOUNT));
            String key = PersonManager.getPersonKey(p);
            mapUnRead.put(key, unReadCount);
        } while (cursor.moveToNext());
        cursor.close();

        return mapUnRead;
    }

    public synchronized void ResetUnEndMessage() {
        if (mDatabase == null) {
            return;
        }
        String up = "update " + Table_Message.TABLE_NAME + " set "
                + Table_Message.STATE + " = ?, " + Table_Message.UNREAD + " = ?"
                + " where " + Table_Message.STATE + " > ?";
        String []args = new String[]{
        		String.valueOf(Table_Message.STATE_FAILED),
        		String.valueOf(Table_Message.UNREAD_NO),
        		String.valueOf(Table_Message.STATE_FAILED)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized int addMessage(Person p, FileType type, String msg, boolean isMeSay, long timeMillis, int state) {
        if (mDatabase == null) {
            return -1;
        }
        int userid = getUserID(p);

        if (-1 == userid) {
            Log.e(TAG, "Cant find this user. name = " + p.mName + ", ip = " + p.mIP.toString());
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(Table_Message.USERID, userid);
        values.put(Table_Message.MAC, p.mMac);
        values.put(Table_Message.TYPE, type.ordinal());
        values.put(Table_Message.CONTENT, msg);
        values.put(Table_Message.DIRECT, isMeSay?Table_Message.DIRECT_LOCALUSER:Table_Message.DIRECT_REMOTEPERSON);
        values.put(Table_Message.TIME, timeMillis);
        values.put(Table_Message.STATE, state);
        values.put(Table_Message.UNREAD, Table_Message.UNREAD_NO);

        mDatabase.insert(Table_Message.TABLE_NAME, null, values);

        String strSelectString = "select max(" + Table_Message._ID + ") as id from " + Table_Message.TABLE_NAME;
        Cursor cursor = mDatabase.rawQuery(strSelectString, null);
        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();

        return id;
    }

    public synchronized void RemoveMessage(int id) {
        if (mDatabase == null) {
            return;
        }
        String where = Table_Message._ID + "=?";
        String []wherearg = new String[]{String.valueOf(id)};
        mDatabase.delete(Table_Message.TABLE_NAME, where, wherearg);
    }

    public synchronized void RemoveMessage(Person person) {
        if (mDatabase == null) {
            return;
        }
        String where = Table_Message.MAC + "=?";
        String []wherearg = new String[]{person.mMac};
        mDatabase.delete(Table_Message.TABLE_NAME, where, wherearg);
    }

    public synchronized void RemoveAllMessage() {
        if (mDatabase == null) {
            return;
        }
        mDatabase.delete(Table_Message.TABLE_NAME, null, null);
        mDatabase.delete(Table_Share.TABLE_NAME, null, null);
        mDatabase.delete(Table_GroupMessage.TABLE_NAME, null, null);
    }

    public synchronized void clearUnReadMessage(Person person) {
        String up = "update " + Table_Message.TABLE_NAME + " set "
                + Table_Message.UNREAD + " = ?"
                + " where " + Table_Message.MAC + " = ?";
        String []args = new String[]{
        		String.valueOf(Table_Message.UNREAD_NO),
        		person.mMac
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void addUnReadMessage(int id) {
        String up = "update " + Table_Message.TABLE_NAME + " set "
                + Table_Message.UNREAD + " = ?"
                + " where " + Table_Message._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(Table_Message.UNREAD_YES),
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void updateMessageState(int id, int state) {
        if (mDatabase == null) {
            return;
        }
        String up = "update " + Table_Message.TABLE_NAME + " set "
                + Table_Message.STATE + " = ?"
                + " where " + Table_Message._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(state),
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void updateMessageTypeAndContent(int id, int type, String content) {
        if (mDatabase == null) {
            return;
        }
        String up = "update " + Table_Message.TABLE_NAME + " set "
                + Table_Message.TYPE + " = ?, " + Table_Message.CONTENT + " = ?"
                + " where " + Table_Message._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(type),
        		content,
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized int addGroupMessage(boolean isBroadcast, String groupName, Person p, FileType type, 
    		String msg, boolean isMeSay, long timeMillis, int state) {
        if (mDatabase == null) {
            return -1;
        }
        ContentValues values = new ContentValues();
        if (isBroadcast) {
            values.put(Table_GroupMessage.GROUPTYPE, Table_GroupMessage.GROUPTYPE_BROADCAST);
            values.put(Table_GroupMessage.GROUPNAME, "");
        } else {
            values.put(Table_GroupMessage.GROUPTYPE, Table_GroupMessage.GROUPTYPE_SPECIAL);
            values.put(Table_GroupMessage.GROUPNAME, groupName);
        }
        if (!isMeSay) {
            values.put(Table_GroupMessage.USERID, getUserID(p));
            values.put(Table_GroupMessage.MAC, p.mMac); // not save MAC if is me say
        }
        values.put(Table_GroupMessage.TYPE, type.ordinal());
        values.put(Table_GroupMessage.CONTENT, msg);
        values.put(Table_GroupMessage.DIRECT, isMeSay?Table_Message.DIRECT_LOCALUSER:Table_Message.DIRECT_REMOTEPERSON);
        values.put(Table_GroupMessage.TIME, timeMillis);
        values.put(Table_GroupMessage.STATE, state);
        values.put(Table_GroupMessage.UNREAD, Table_Message.UNREAD_NO);

        mDatabase.insert(Table_GroupMessage.TABLE_NAME, null, values);

        String strSelectString = "select max(" + Table_GroupMessage._ID + ") as id from " + Table_GroupMessage.TABLE_NAME;
        Cursor cursor = mDatabase.rawQuery(strSelectString, null);
        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();

        return id;
    }

    public synchronized void addFreeShare(FileType type, String content, long timeMillis) {
        if (mDatabase == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Table_Share.TYPE, type.ordinal());
        values.put(Table_Share.CONTENT, content);
        values.put(Table_Share.TIME, timeMillis);

        mDatabase.insert(Table_Share.TABLE_NAME, null, values);
    }

    public synchronized Cursor getFreeShareHistory() {
        if (mDatabase == null) {
            return null;
        }
        String []colums = {"*"};
        Cursor cursor = mDatabase.query(Table_Share.TABLE_NAME, colums, null, null, null , null, null);
        return cursor;
    }

    public synchronized void clearFreeShareHistory() {
        if (mDatabase == null) {
            return;
        }
        mDatabase.delete(Table_Share.TABLE_NAME, null, null);
    }

    public synchronized Cursor getMsgHistory(Person person) {
        if (mDatabase == null) {
            return null;
        }
        String []colums = {"*"};
        String where = Table_Message.MAC + "=?";
        String []wherearg = new String[]{person.mMac};
        String order = Table_Message.TIME + " asc";
        
        Cursor cursor = mDatabase.query(Table_Message.TABLE_NAME, colums, where, wherearg, null, null, order);
        return cursor;
    }

    public synchronized Cursor getLatestMessage() {
    	String sql = "select * from " + Table_Message.TABLE_NAME 
        		+ " where " + Table_Message.TIME + " in (select max(" + Table_Message.TIME 
        		+ ") from " + Table_Message.TABLE_NAME + " GROUP BY " + Table_Message.MAC
        		+ ") ORDER BY " + Table_Message.TIME + " DESC";
    	Log.d(TAG, sql);
        Cursor cursor = mDatabase.rawQuery(sql, null);
        return cursor;
    }

    public synchronized Cursor getGroupMessage() {
        String tmpSelectString = "select * from " + Table_GroupMessage.TABLE_NAME + " order by "
        		+ Table_GroupMessage.TIME + " asc";
        Cursor cursor = mDatabase.rawQuery(tmpSelectString, null);
        return cursor;
    }

    public synchronized void clearGroupUnReadMessage(boolean isBroadcast, String groupName) {
        String up = "update " + Table_GroupMessage.TABLE_NAME + " set "
                + Table_GroupMessage.UNREAD + " = ?"
                + " where " + Table_GroupMessage.GROUPTYPE + " = ?";
        String []args = new String[]{
        		String.valueOf(Table_Message.UNREAD_NO),
        		String.valueOf(Table_GroupMessage.GROUPTYPE_BROADCAST)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void addGroupUnReadMessage(int id) {
        String up = "update " + Table_GroupMessage.TABLE_NAME + " set "
                + Table_GroupMessage.UNREAD + " = ?"
                + " where " + Table_GroupMessage._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(Table_Message.UNREAD_YES),
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void updateGroupMessageState(int id, int state) {
        if (mDatabase == null) {
            return;
        }
        String up = "update " + Table_GroupMessage.TABLE_NAME + " set "
                + Table_GroupMessage.STATE + " = ?"
                + " where " + Table_GroupMessage._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(state),
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void updateGroupMessageTypeAndContent(int id, int type, String content) {
        if (mDatabase == null) {
            return;
        }
        String up = "update " + Table_GroupMessage.TABLE_NAME + " set "
                + Table_GroupMessage.TYPE + " = ?, " + Table_GroupMessage.CONTENT + " = ?"
                + " where " + Table_GroupMessage._ID + " = ?";
        String []args = new String[]{
        		String.valueOf(type),
        		content,
        		String.valueOf(id)
        };
        mDatabase.execSQL(up, args);
    }

    public synchronized void RemoveGroupMessage(int id) {
        if (mDatabase == null) {
            return;
        }
        String where = Table_GroupMessage._ID + "=?";
        String []wherearg = new String[]{String.valueOf(id)};
        mDatabase.delete(Table_GroupMessage.TABLE_NAME, where, wherearg);
    }

    public synchronized void RemoveGroupMessage(boolean isBroadcast, String groupName) {
        if (mDatabase == null) {
            return;
        }
        String where = Table_GroupMessage.GROUPTYPE + "=?";
        String []wherearg = new String[]{String.valueOf(Table_GroupMessage.GROUPTYPE_BROADCAST)};
        mDatabase.delete(Table_GroupMessage.TABLE_NAME, where, wherearg);
    }

    //private
    private int getUserID(Person person) {
        if (mDatabase == null) {
            return -1;
        }
        if (person.mMac == null) {
            return -1;
        }

        String strSelectString = "select " + Table_Users._ID        
                + " from " + Table_Users.TABLE_NAME
                + " where " + Table_Users.MAC + " = '" + person.mMac + "'";
        Cursor cursor = mDatabase.rawQuery(strSelectString, null);
        
        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();
        return id;
    }
}
