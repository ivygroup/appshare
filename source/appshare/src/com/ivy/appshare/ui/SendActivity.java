package com.ivy.appshare.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.ivy.appshare.R;
import com.ivy.appshare.engin.connection.ConnectionState;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.utils.IvyActivityBase;

public class SendActivity extends IvyActivityBase implements OnClickListener {


    private static final int MESSAGE_NETWORK_STATE_CHANGED = 10;    //
    // arg1 = connection type,
    // arg2 = connection state
    // obj = ssid (if state = ivy wifi state)
    private static final int MESSAGE_NETWORK_SCAN_FINISH = 11;
    private static final int MESSAGE_NETWORK_CLEAR_IVYROOM= 12;
    private static final int MESSAGE_NETWORK_DISCOVERYWIFIP2P= 13;


    private SendPersonListAdapter mAdapter = null;
    private ListView mListView = null;
    private Handler mHandler;
    private Button mSendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);

        mAdapter = new SendPersonListAdapter(this, null);
        mListView = (ListView)findViewById(R.id.listPerson);
        if (mAdapter != null) {
            mListView.setAdapter(mAdapter);
        }

        mSendButton = (Button)findViewById(R.id.PushPull);
        mSendButton.setOnClickListener(this);



        mHandler = new Handler(this.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_NETWORK_STATE_CHANGED:
                        {
                            int type = msg.arg1;
                            int state = msg.arg2;

                            if (ConnectionState.isConnected(state)) {
                                doUpLine();
                            } else {
                                doDownLine();
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

    private void doUpLine() {
        if (mImManager == null) {
            return;
        }

        mImManager.upLine();
    }

    private void doDownLine() {
        if (mImManager == null) {
            return;
        }

        mImManager.downLine();
    }
    
    
    
    
    private class PersonBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            int type = intent.getIntExtra(IvyMessages.PARAMETER_PERSON_TYPE, 0);
            if (mImManager != null) {
                if (mAdapter != null) {
                    mAdapter.changeList(mImManager.getPersonListClone());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(IvyMessages.INTENT_NETWORK_STATECHANGE)) {
                int type = intent.getIntExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_TYPE, 0);
                int state = intent.getIntExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_STATE, 0);
                String ssid = intent.getStringExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_SSID);

                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_NETWORK_STATE_CHANGED, type, state, ssid));

            } else if (action.equals(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM)) {
                boolean isclear = intent.getBooleanExtra(IvyMessages.PARAMETER_NETWORK_FINISHSCANIVYROOM_ISCLEAR, false);
                if (isclear) {
                    mHandler.sendEmptyMessage(MESSAGE_NETWORK_CLEAR_IVYROOM);
                } else {
                    mHandler.sendEmptyMessage(MESSAGE_NETWORK_SCAN_FINISH);    
                }
            } else if (action.equals(IvyMessages.INTENT_NETWORK_DISCOVERYWIFIP2P)) {
                mHandler.sendEmptyMessage(MESSAGE_NETWORK_DISCOVERYWIFIP2P);
            }
        }
        
    }
}
