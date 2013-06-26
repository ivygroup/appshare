package com.ivy.appshare.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;

import com.ivy.appshare.R;
import com.ivy.appshare.utils.IvyActivityBase;

public class SendActivity extends IvyActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIvyConnectionManager != null) {
            mIvyConnectionManager.createHotspot();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIvyConnectionManager != null) {
            mIvyConnectionManager.disableHotspot();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        if (mIvyConnectionManager != null) {
            mIvyConnectionManager.createHotspot();
        }
    }
}
