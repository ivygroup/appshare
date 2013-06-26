package com.ivy.appshare.engin;

import com.ivy.appshare.engin.connection.IvyConnectionManager;
import com.ivy.appshare.engin.control.ImManager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class IvyService extends Service {
    private static final String TAG = "ImService";

    private IvyConnectionManager mIvyConnectionManager;
    private ImManager mImManager;


    // This is the object that receives interactions from clients.    See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocalBinder() {
            Log.d(TAG, "LocalBinder construct");
        }

        public IvyService getService() {
            Log.d(TAG, "get Service called.");
            return IvyService.this;
        }
    }

    @Override 
    public void onCreate() { 
        Log.d(TAG, "onCreate");
        mIvyConnectionManager = new IvyConnectionManager();
        mImManager = new ImManager();
    }

    @Override 
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mImManager.release();
        mImManager = null;

        mIvyConnectionManager.release();
        mIvyConnectionManager = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return 0;
    }

    @Override 
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder; 
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind");
        return true;
    }

    //==========================================================================
    public IvyConnectionManager getIvyConnectionManager() {
        return mIvyConnectionManager;
    }

    public ImManager getImManager() {
        return mImManager;
    }
}
