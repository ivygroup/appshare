package com.ivy.appshare.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.ivy.appshare.engin.IvyService;
import com.ivy.appshare.engin.connection.IvyConnectionManager;
import com.ivy.appshare.engin.control.ImManager;

public class IvyActivityBase extends Activity implements ServiceConnection {
    private static final String TAG = "IvyActivityBase";

    private IvyService mIvyService;
    protected IvyConnectionManager mIvyConnectionManager;
    protected ImManager mImManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, IvyService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mImManager != null) {
            mImManager.onResumeMyActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mImManager != null) {
            mImManager.checkMyActive();
        }
    }
/*
    @Override
    protected void onStop() {
        super.onStop();
        if (mImService != null) {
            mImService.checkMyActive();
        }
    } //*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mIvyService = ((IvyService.LocalBinder)service).getService();
        mIvyConnectionManager = mIvyService.getIvyConnectionManager();
        mImManager = mIvyService.getImManager();
        mImManager.upLine();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIvyConnectionManager = null;
        mImManager = null;
        mIvyService = null;
    }
}
