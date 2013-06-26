package com.ivy.appshare.engin.data;

import android.provider.BaseColumns;

public interface Table_Users extends BaseColumns {
    public static final String TABLE_NAME = "users";

    public static final String PROTOCOLVERSION = "protocolversion";
    public static final String NAME = "name";
    public static final String HOST = "host";
    public static final String NICKNAME = "nickname";
    public static final String IMAGE = "image";
    public static final String GROUPNAME = "groupname";
    public static final String SIGNATURE = "signature";
    public static final String MAC = "mac";
    public static final String MSISDN = "msisdn";
    public static final String IMEI = "imei";
}
