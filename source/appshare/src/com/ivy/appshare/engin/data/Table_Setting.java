package com.ivy.appshare.engin.data;

import android.provider.BaseColumns;

public interface Table_Setting extends BaseColumns {
    public static final String TABLE_NAME = "setting";

    public static final String KEY = "key";
    public static final String VALUE = "thevalue";

    // the following is the key.
    public static final String NICKNAME = "nickname";
    public static final String GROUPNAME = "groupname";
    public static final String SIGNATURE = "signature";
    public static final String HEADICONNAME = "headiconname";

    public static final String AUTONETWORK = "autonetwork"; // "true" is true, "false" is false.
    public static final String RING = "ring";
    public static final String VIBRATE = "vibrate";
    public static final String FIRSTTIME = "firsttime";
    public static final String TRACEACTION = "traceaction";

    // public static final String PORT = "port";
    // public static final String NETWORKCODING = "networkcoding";
}
