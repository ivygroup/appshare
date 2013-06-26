package com.ivy.appshare.engin.data;

import com.ivy.appshare.MyApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ImSetting {
    private static final String TAG = "ImSetting";
    private static final String DATABASENAME = "ivysetting.db";
    private static final int VERSION = 1;

    private ImSettingHelper mHelper;

    public ImSetting() {
        mHelper = new ImSettingHelper(MyApplication.getInstance(), DATABASENAME, null, VERSION);
    }

    public String getNickName() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String str = getPersonalInfo(db, Table_Setting.NICKNAME, "");
        db.close();
        return str;
    }
    public String getGroupName() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String str = getPersonalInfo(db, Table_Setting.GROUPNAME, "");
        db.close();
        return str;
    }
    public String getSignature() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String str = getPersonalInfo(db, Table_Setting.SIGNATURE, "");
        db.close();
        return str;
    }
    public String getHeadIconName() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String str = getPersonalInfo(db, Table_Setting.HEADICONNAME, "");
        db.close();
        return str;
    }
    public boolean getAutoNetwork() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean b = Boolean.valueOf(getPersonalInfo(db, Table_Setting.AUTONETWORK, "false"));
        db.close();
        return b;
    }
    public boolean getRing() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean b = Boolean.valueOf(getPersonalInfo(db, Table_Setting.RING, "true"));
        db.close();
        return b;
    }
    public boolean getVibrate() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean b = Boolean.valueOf(getPersonalInfo(db, Table_Setting.VIBRATE, "true"));
        db.close();
        return b;
    }

    public void setNickName(String value) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.NICKNAME, value);
        db.close();
    }
    public void setGroupName(String value) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.GROUPNAME, value);
        db.close();
    }
    public void setSignature(String value) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.SIGNATURE, value);
        db.close();
    }
    public void setHeadIconName(String value) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.HEADICONNAME, value);
        db.close();
    }
    public void setAutoNetwork(boolean b) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.AUTONETWORK, Boolean.toString(b));
        db.close();
    }
    public void setRing(boolean b) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.RING, Boolean.toString(b));
        db.close();
    }
    public void setVibrate(boolean b) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.VIBRATE, Boolean.toString(b));
        db.close();
    }
    public boolean getFirstTime() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean b = Boolean.valueOf(getPersonalInfo(db, Table_Setting.FIRSTTIME, "true"));
        db.close();
        return b;
    }
    public void setFirstTime(boolean b) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.FIRSTTIME, String.valueOf(b));
        db.close();
    }
    public boolean getTraceAction() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean b = Boolean.valueOf(getPersonalInfo(db, Table_Setting.TRACEACTION, "true"));
        db.close();
        return b;
    }
    public void setTraceAction(boolean b) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        setPersonalInfo(db, Table_Setting.TRACEACTION, String.valueOf(b));
        db.close();
    }


    private void setPersonalInfo(SQLiteDatabase db, String key, String value) {
        if (db == null) {
            return;
        }
        if (key == null || key.length() <= 0) {
            return;
        }
        if (value == null) {
            return;
        }

        String tmpSelectString = "select count(*) from " + Table_Setting.TABLE_NAME
                + " where " + Table_Setting.KEY+ " = ?";
        Cursor cursor = db.rawQuery(tmpSelectString, new String[]{key});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table_Setting.VALUE, value);
            db.update(Table_Setting.TABLE_NAME, contentValues, "key=?", new String[]{key});
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table_Setting.KEY, key);
            contentValues.put(Table_Setting.VALUE, value);
            db.insert(Table_Setting.TABLE_NAME, null, contentValues);
        }
    }

    private String getPersonalInfo(SQLiteDatabase db, String key, String defaultValue) {
        if (db == null) {
            return defaultValue;
        }
        if (key == null || key.length() <= 0) {
            return defaultValue;
        }

        String tmpSelectString = "select " + Table_Setting.VALUE
                + " from " + Table_Setting.TABLE_NAME
                + " where " + Table_Setting.KEY+ " = ?";
        Cursor cursor = db.rawQuery(tmpSelectString, new String[]{key});
        if (cursor.getCount() != 1) {
            cursor.close();
            return defaultValue;
        }

        cursor.moveToFirst();
        String valueString = cursor.getString(0);
        cursor.close();
        return valueString;
    }
    
}
