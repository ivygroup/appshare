package com.ivyappshare.engin.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ImDataHelper extends SQLiteOpenHelper {
    private static final String TAG = "ImDataHelper";

    public ImDataHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        createAllTableAndViews(arg0);
        createGroupMessageTab(arg0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    	Log.d(TAG, "old version " + arg1 + " new version " + arg2);
        if (arg1 == 1 || arg1 == 2) {
            arg0.execSQL("drop table " + Table_Setting.TABLE_NAME);
            arg0.execSQL("drop table " + Table_Users.TABLE_NAME);
            arg0.execSQL("drop table " + Table_Message.TABLE_NAME);
            arg0.execSQL("drop table " + Table_Share.TABLE_NAME);
            arg0.execSQL("drop view " + View_UserMessage.VIEW_NAME);
            arg0.execSQL("drop view " + View_UserMessage.VIEW_UNREADNAME);
        } else if (arg1 == 3) {
            arg0.execSQL("drop table " + Table_Users.TABLE_NAME);
            arg0.execSQL("drop table " + Table_Message.TABLE_NAME);
            arg0.execSQL("drop table " + Table_Share.TABLE_NAME);
            arg0.execSQL("drop view " + View_UserMessage.VIEW_NAME);
            arg0.execSQL("drop view " + View_UserMessage.VIEW_UNREADNAME);
        }

        if (arg2 ==3 ) {
            createAllTableAndViews(arg0);
        } else if (arg2 == 4) {
            createAllTableAndViews(arg0);
            createGroupMessageTab(arg0);
        }
    }

    private void createAllTableAndViews(SQLiteDatabase arg0) {
        // create user table
        arg0.execSQL("create table " + Table_Users.TABLE_NAME + " ("
                 + Table_Users._ID + " integer primary key autoincrement, "
                 + Table_Users.PROTOCOLVERSION + " text, "
                 + Table_Users.NAME + " text, "
                 + Table_Users.HOST + " text, "
                 + Table_Users.NICKNAME + " text, "
                 + Table_Users.IMAGE + " text, "
                 + Table_Users.GROUPNAME + " text, "
                 + Table_Users.SIGNATURE + " text, "
                 + Table_Users.MAC + " text, "
                 + Table_Users.MSISDN + " text, "
                 + Table_Users.IMEI + " text);");

        // create message table
        arg0.execSQL("create table " + Table_Message.TABLE_NAME + " ("
                + Table_Message._ID + " integer primary key autoincrement, "
                + Table_Message.USERID + " integer, "
                + Table_Message.MAC + " text, "
                + Table_Message.TYPE + " integer, "
                + Table_Message.CONTENT + " text, "
                + Table_Message.DIRECT + " integer, "
                + Table_Message.TIME + " datetime, "
                + Table_Message.STATE + " integer, "
                + Table_Message.UNREAD + " integer)");

        // create share table
        arg0.execSQL("create table " + Table_Share.TABLE_NAME + " ("
                + Table_Share._ID + " integer primary key autoincrement, "
                + Table_Share.TYPE + " integer, "
                + Table_Share.CONTENT + " text, "
                + Table_Share.TIME + " datetime)");

        // create view
        arg0.execSQL("CREATE VIEW IF NOT EXISTS " + View_UserMessage.VIEW_NAME
                + " AS SELECT " + Table_Users.TABLE_NAME + ".*, count(*) as " + View_UserMessage.COUNT
                + " FROM " + Table_Message.TABLE_NAME + " JOIN " + Table_Users.TABLE_NAME + " ON "
                + Table_Message.TABLE_NAME + "." + Table_Message.MAC + "="
                + Table_Users.TABLE_NAME + "." + Table_Users.MAC
                + " GROUP BY " + Table_Message.TABLE_NAME + "." + Table_Message.MAC);

        arg0.execSQL("CREATE VIEW IF NOT EXISTS " + View_UserMessage.VIEW_UNREADNAME
                + " AS SELECT " + Table_Users.TABLE_NAME + ".*, count(*) as " + View_UserMessage.UNREADCOUNT
                + " FROM " + Table_Message.TABLE_NAME + " JOIN " + Table_Users.TABLE_NAME + " ON "
                + Table_Message.TABLE_NAME + "." + Table_Message.MAC + "="
                + Table_Users.TABLE_NAME + "." + Table_Users.MAC
                + " WHERE " + Table_Message.TABLE_NAME + "." + Table_Message.UNREAD + "=" + Table_Message.UNREAD_YES
                + " GROUP BY " + Table_Message.TABLE_NAME + "." + Table_Message.MAC);
    }

    private void createGroupMessageTab(SQLiteDatabase db) {
        db.execSQL("create table " + Table_GroupMessage.TABLE_NAME + " ("
                + Table_GroupMessage._ID + " integer primary key autoincrement, "
                + Table_GroupMessage.GROUPTYPE + " integer, "
                + Table_GroupMessage.GROUPNAME + " text, "
                + Table_GroupMessage.USERID + " integer, "
                + Table_GroupMessage.MAC + " text, "
                + Table_GroupMessage.TYPE + " integer, "
                + Table_GroupMessage.CONTENT + " text, "
                + Table_GroupMessage.DIRECT + " integer, "
                + Table_GroupMessage.TIME + " datetime, "
                + Table_GroupMessage.STATE + " integer, "
                + Table_GroupMessage.UNREAD + " integer)");
    }
}
