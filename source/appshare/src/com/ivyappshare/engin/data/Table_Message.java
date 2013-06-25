package com.ivyappshare.engin.data;

import android.provider.BaseColumns;

public interface Table_Message extends BaseColumns {
    public static final String TABLE_NAME = "message";

    public static final String USERID = "userid";   // userid and mac must matched with Table_Users fields.
    public static final String MAC = "mac";
    public static final String TYPE = "type";       // msg, file_app, file_picture,
    public static final String CONTENT = "content";
    public static final String DIRECT = "direct";       // 1 is other say, 2 is i say.
    public static final String TIME = "time";
    public static final String STATE = "state";
    public static final String UNREAD = "unread";

    public static final int DIRECT_REMOTEPERSON = 1;
    public static final int DIRECT_LOCALUSER = 2;

    public static final int STATE_OK = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_BEGIN = 4;
    public static final int STATE_PROCESS = 5;
    public static final int STATE_TIMEOUT = 6;

    public static final int UNREAD_YES = 1;
    public static final int UNREAD_NO = 0;
}
