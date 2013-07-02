package com.ivy.appshare;


import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {
    private static MyApplication gInstance = null;
    public static final String mPackageName = "com.ivy.appshare";
    public static MyApplication getInstance() {
        return gInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gInstance = this;
    }
}
