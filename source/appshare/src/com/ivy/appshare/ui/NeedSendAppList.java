package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

public class NeedSendAppList {
    private static NeedSendAppList gInstance = null;

    public static NeedSendAppList getInstance() {
        if (gInstance == null) {
            gInstance = new NeedSendAppList();
        }
        return gInstance;
    }



    public List<AppsInfo> mListAppInfo;

    private NeedSendAppList() {
        mListAppInfo = new ArrayList<AppsInfo>();
    }
}
