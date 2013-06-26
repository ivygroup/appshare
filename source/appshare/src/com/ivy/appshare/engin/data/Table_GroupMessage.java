package com.ivy.appshare.engin.data;

import android.provider.BaseColumns;

public interface Table_GroupMessage extends BaseColumns {
    public static final String TABLE_NAME = "groupmessage";

    public static final String GROUPTYPE = "grouptype";  // 1, is broadcast.  2, is special group.
    public static final String GROUPNAME = "groupname";  // when grouptype is 1, this field is null.
                                                         // when grouptype is 2, this field is the groupname of this message.
    public static final String USERID = "userid";   // userid and mac must matched with Table_Users fields.
    public static final String MAC = "mac";
    public static final String TYPE = "type";       // msg, file_app, file_picture,
    public static final String CONTENT = "content";
    public static final String DIRECT = "direct";       // 1 is other say, 2 is i say.
    public static final String TIME = "time";
    public static final String STATE = "state";
    public static final String UNREAD = "unread";


    public static final int GROUPTYPE_BROADCAST = 1;
    public static final int GROUPTYPE_SPECIAL = 2;
}
