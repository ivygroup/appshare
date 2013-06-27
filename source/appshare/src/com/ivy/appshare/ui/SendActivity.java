package com.ivy.appshare.ui;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.control.TranslateFileControl;
import com.ivy.appshare.utils.IvyActivityBase;

public class SendActivity extends IvyActivityBase implements OnClickListener, TranslateFileControl.TransProcessListener {

    private static final int MESSAGE_SERVICE_CONNECTED = 0;


    private static final int MESSAGE_NETWORK_STATE_CHANGED = 10;    //
    // arg1 = connection type,
    // arg2 = connection state
    // obj = ssid (if state = ivy wifi state)
    private static final int MESSAGE_NETWORK_SCAN_FINISH = 11;
    private static final int MESSAGE_NETWORK_CLEAR_IVYROOM= 12;
    private static final int MESSAGE_NETWORK_DISCOVERYWIFIP2P= 13;

    private static final int MESSAGE_FILEPROCESS_CHANGED = 20;


    private SendListAdapter mAdapter = null;
    private ListView mListView = null;
    private Handler mHandler;
    private View mSwitchBar;
    private TextView mRightTextView;

    private List<String> mListSendItems;


    private PersonBroadCastReceiver mPersonReceiver;
    private MessageBroadCastReceiver mMessageReceiver;
    private NetworkReceiver mNetworkReceiver;

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
                    case MESSAGE_SERVICE_CONNECTED:
                        onResume();
                        break;

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
        registerMyReceivers();
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
        unregisterMyReceivers();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        mHandler.sendEmptyMessage(MESSAGE_SERVICE_CONNECTED);
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


    private void registerMyReceivers() {
        if (mImManager == null) {
            return; // if no immanager, can't create receivers.
        }

        if (mPersonReceiver != null) {
            return; // already create.
        }

        mPersonReceiver = new PersonBroadCastReceiver();
        {
            IntentFilter filter = new IntentFilter(IvyMessages.INTENT_PERSON);
            registerReceiver(mPersonReceiver, filter);
        }

        mMessageReceiver = new MessageBroadCastReceiver();
        {
            if (mImManager != null) {
                mImManager.getImListener().getTranslateFileControl().RegisterTransProcess(this);
            }
            IntentFilter filter = new IntentFilter(IvyMessages.INTENT_MESSAGE);
            filter.setPriority(500);
            registerReceiver(mMessageReceiver, filter);
        }

        mNetworkReceiver = new NetworkReceiver();
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(IvyMessages.INTENT_NETWORK_AIRPLANE);
            filter.addAction(IvyMessages.INTENT_NETWORK_STATECHANGE);
            filter.addAction(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM);
            filter.addAction(IvyMessages.INTENT_NETWORK_DISCOVERYWIFIP2P);
            registerReceiver(mNetworkReceiver, filter);
        }
    }

    private void unregisterMyReceivers() {
        if (mPersonReceiver != null) {
            unregisterReceiver(mPersonReceiver);
            mPersonReceiver = null;
        }
        if (mMessageReceiver != null) {
            if (mImManager != null) {
                mImManager.getImListener().getTranslateFileControl().UnRegisterTransProcess(this);
            }
            unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
    }



    private class PersonBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            if (mImManager == null) {
                return;
            }

            int type = intent.getIntExtra(IvyMessages.PARAMETER_PERSON_TYPE, 0);
            String personKey = intent.getStringExtra(IvyMessages.PARAMETER_PERSON_VALUE);
            if (IvyMessages.VALUE_PERSONTYPE_NEW_USER == type) {
                Person person = PersonManager.getInstance().getPerson(personKey);
                // TODO , ask user if send to this person.
                switchBarAndToPerson(false);
                mRightTextView.setText(person.mNickName);
                mAdapter.beginTranslate(mImManager, person);
            }
        }
    }

    private class MessageBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            
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

    @Override
    public void onSendFileProcess(int id, Person to, String name, FileType fileType, long pos,
            long total) {
        int progress = (int)(pos*100/total);
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FILEPROCESS_CHANGED, id, progress));
    }

    @Override
    public void onReceiveProcess(int id, Person from, String name, FileType fileType, long pos,
            long total) {
        int progress = (int)(pos*100/total);
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FILEPROCESS_CHANGED, id, progress));        
    }
}
