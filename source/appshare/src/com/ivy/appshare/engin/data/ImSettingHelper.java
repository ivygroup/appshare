package com.ivy.appshare.engin.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class ImSettingHelper extends SQLiteOpenHelper {
    private static final String TAG = "ImSettingHelper";

    public ImSettingHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create setting table
        db.execSQL("create table " + Table_Setting.TABLE_NAME + " ("
                + Table_Setting._ID + " integer primary key autoincrement, "
                + Table_Setting.KEY + " text, "
                + Table_Setting.VALUE + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
