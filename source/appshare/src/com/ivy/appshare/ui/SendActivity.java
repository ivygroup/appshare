package com.ivy.appshare.ui;

import java.util.List;

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
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.appshare.R;
import com.ivy.appshare.engin.connection.ConnectionState;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.utils.IvyActivityBase;

public class SendActivity extends IvyActivityBase implements OnClickListener {


    private static final int MESSAGE_NETWORK_STATE_CHANGED = 10;    //
    // arg1 = connection type,
    // arg2 = connection state
    // obj = ssid (if state = ivy wifi state)
    private static final int MESSAGE_NETWORK_SCAN_FINISH = 11;
    private static final int MESSAGE_NETWORK_CLEAR_IVYROOM= 12;
    private static final int MESSAGE_NETWORK_DISCOVERYWIFIP2P= 13;


    private SendListAdapter mAdapter = null;
    private ListView mListView = null;
    private Handler mHandler;
    private View mSwitchBar;
    private TextView mRightTextView;

    private List<String> mListSendItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);

        // get the data from intent.
        Intent intent = getIntent();
        mListSendItems = intent.getStringArrayListExtra("items");

        // set the action bar.
        View actionbar = (View) findViewById(R.id.layout_title);
        TextView textLeft = ((TextView) actionbar.findViewById(R.id.left_text_info));
        textLeft.setVisibility(View.VISIBLE);
        textLeft.setText(LocalSetting.getInstance().getMySelf().mNickName);
        TextView centerTextView = ((TextView) actionbar.findViewById(R.id.center_text_info));
        centerTextView.setVisibility(View.VISIBLE);
        centerTextView.setText(getResources().getString(R.string.sendto));
        mSwitchBar = actionbar.findViewById(R.id.switching_bar);
        mRightTextView = ((TextView) actionbar.findViewById(R.id.right_text_info));
        switchBarAndToPerson(true);

        // init listview and adapter.
        mListView = (ListView)findViewById(R.id.list);
        mAdapter = new SendListAdapter(this, mListSendItems);
        mListView.setAdapter(mAdapter);


        // handler for messages
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

    private void switchBarAndToPerson(boolean isSwitch) {
        if (isSwitch) {
            mSwitchBar.setVisibility(View.VISIBLE);
            mRightTextView.setVisibility(View.GONE);
        } else {
            mSwitchBar.setVisibility(View.GONE);
            mRightTextView.setVisibility(View.VISIBLE);
        }
    }



    private class PersonBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            int type = intent.getIntExtra(IvyMessages.PARAMETER_PERSON_TYPE, 0);
            if (mImManager != null) {
                //
                /*if (mAdapter != null) {
                    mAdapter.changeList(mImManager.getPersonListClone());
                    mAdapter.notifyDataSetChanged();
                }*/
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
