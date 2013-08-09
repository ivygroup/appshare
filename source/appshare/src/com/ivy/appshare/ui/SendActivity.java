package com.ivy.appshare.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import com.ivy.appshare.engin.control.TranslateFileControl;
import com.ivy.appshare.engin.data.Table_Message;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.utils.CommonUtils;
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

    private static final int MESSAGE_FILEPROCESS_STATE = 20;
    private static final int MESSAGE_FILEPROCESS_CHANGED = 21;


    private SendListAdapter mAdapter = null;
    private ListView mListView = null;
    private Handler mHandler;
    private View mSwitchBar;
    private TextView mCenterTextView;
    private TextView mRightTextView;

    private PersonBroadCastReceiver mPersonReceiver;
    private MessageBroadCastReceiver mMessageReceiver;
    private NetworkReceiver mNetworkReceiver;

    private SenderStatusManager mSenderStatusManager;

    private int mConnectionState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);

        // set the action bar.
        View actionbar = (View) findViewById(R.id.layout_title);
        TextView textLeft = ((TextView) actionbar.findViewById(R.id.left_text_info));
        textLeft.setVisibility(View.VISIBLE);
        textLeft.setText(LocalSetting.getInstance().getMySelf().mNickName);
        mCenterTextView = ((TextView) actionbar.findViewById(R.id.center_text_info));
        mCenterTextView.setVisibility(View.VISIBLE);
        mSwitchBar = actionbar.findViewById(R.id.switching_bar);
        mRightTextView = ((TextView) actionbar.findViewById(R.id.right_text_info));
        changeActionBarToWaitOrSend(true, getResources().getString(R.string.waittosend), null);

        // init listview and adapter.
        mListView = (ListView)findViewById(R.id.list);
        mAdapter = new SendListAdapter(this);
        mListView.setAdapter(mAdapter);

        mSenderStatusManager = new SenderStatusManager();

        // handler for messages
        mHandler = new Handler(this.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_SERVICE_CONNECTED:
                        if (mImManager != null) {
                            mImManager.clearAllFileTranslates();
                        }

                    	// create hotspot after service connected
                    	registerMyReceivers();
	                    if (mIvyConnectionManager != null) {
	                    	mIvyConnectionManager.createHotspot(NeedSendAppList.getInstance().mListAppInfo.size());
	                    }
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

                    case MESSAGE_FILEPROCESS_STATE:
                    {
                        Intent intent = (Intent)msg.obj;
                        int messageType = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_TYPE, 0);
                        int id = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_ID, 0);
                        int messageState = intent.getIntExtra(IvyMessages.PARAMETER_MESSGAE_STATE, 0);
                        int fileType = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_FILE_TYPE, 0);
                        String finename = intent.getStringExtra(IvyMessages.PARAMETER_MESSAGE_FILE_VALUE);
                        boolean isMeSay = intent.getBooleanExtra(IvyMessages.PARAMETER_MESSAGE_SELF, true);
                        String personKey = intent.getStringExtra(IvyMessages.PARAMETER_MESSAGE_PERSON);
                        Person person = PersonManager.getInstance().getPerson(personKey);

                        if (messageState == Table_Message.STATE_BEGIN) {
                            mAdapter.changeTransState_Begin(person, id);
                        } else if (messageState == Table_Message.STATE_OK) {
                            mAdapter.changeTransState_OK(person, id);
                        } else if (messageState == Table_Message.STATE_FAILED) {
                            mAdapter.changeTransState_Failed(person, id);
                        } else if (messageState == Table_Message.STATE_TIMEOUT) {
                            mAdapter.changeTransState_TimeOut(person, id);
                        }
                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.isCompleteTranslate()) {
                            mSenderStatusManager.setStatus(SenderStatusManager.Status.READY);
                            changeActionBarToWaitOrSend(true, getResources().getString(R.string.waittosend), null);
                        }
                    }
                        break;

                    case MESSAGE_FILEPROCESS_CHANGED:
                    {
                        FileProcessInfo info = (FileProcessInfo)msg.obj;
                        mAdapter.changeTransState_Process(info.mPerson, info.mID, info.mPos, info.mTotal);
                        mAdapter.notifyDataSetChanged();
                    }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void onDestroy() {
        if (mImManager != null) {
            mImManager.clearAllFileTranslates();
        }
        unregisterMyReceivers();
        doDownLine();
		if (mIvyConnectionManager != null) {
			mIvyConnectionManager.disableHotspot();
		}
		super.onDestroy();
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

    private void changeActionBarToWaitOrSend(boolean isWait, String centerText, String rightText) {
        mCenterTextView.setText(centerText);
        if (isWait) {
            mSwitchBar.setVisibility(View.VISIBLE);
            mRightTextView.setVisibility(View.GONE);
        } else {
            mSwitchBar.setVisibility(View.GONE);
            mRightTextView.setText(rightText);
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
            Person person = PersonManager.getInstance().getPerson(personKey);

            if (IvyMessages.VALUE_PERSONTYPE_NEW_USER == type) {
            	if (mImManager != null) {
            		if (mSenderStatusManager.isReady()) {
                		mImManager.sendMessage(person, IvyInnerMessage.getIvyInnerMessage(IvyInnerMessage.IVY_APP_IAMHOTSPOT));
            		} else {
                    	if (mConnectionState == ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED) {
                            mImManager.sendMessage(person, IvyInnerMessage.getIvyInnerMessage(IvyInnerMessage.IVY_APP_ANSWERNO));
                    	}
            		}
            	}
            } else if (IvyMessages.VALUE_PERSONTYPE_SOMEONE_EXIT == type) {
                if (mImManager != null && mSenderStatusManager.canEndCurrentWorkSession(person)) {
                    mSenderStatusManager.setStatus(SenderStatusManager.Status.READY);
                    mImManager.clearAllFileTranslates();
                    changeActionBarToWaitOrSend(true, getResources().getString(R.string.waittosend), null);
                }
            }
        }
    }

    private void askIfSendToThisPerson(Person msgPerson) {
        final Person person = msgPerson;

        Dialog alertDialog = CommonUtils.getMyAlertDialogBuilder(SendActivity.this)
                .setTitle(R.string.requestsendtitle)
                .setMessage(getResources().getString(R.string.requestsendmessage, person.mNickName))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,int which) {
	                        changeActionBarToWaitOrSend(false, getResources().getString(R.string.sendto), person.mNickName);

	                    	if (mImManager != null) {
	                    		mImManager.sendMessage(person, IvyInnerMessage.getIvyInnerMessage(IvyInnerMessage.IVY_APP_ANSWERYES));
	                    	}

	                        mAdapter.beginTranslate(mImManager, person);
	                        mSenderStatusManager.setToPersonForWorking(person);
	                        mSenderStatusManager.setStatus(SenderStatusManager.Status.WORKING);
	                    }
	                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,int which) {
	                    	if (mImManager != null) {
	                    		mImManager.sendMessage(person, IvyInnerMessage.getIvyInnerMessage(IvyInnerMessage.IVY_APP_ANSWERNO));
	                    	}
	                    	mSenderStatusManager.setStatus(SenderStatusManager.Status.READY);
	                    }
	                }).create();
        alertDialog.show();
    }

    private void processInnerMessage(Person person, int msgType) {
    	switch (msgType) {
    		case IvyInnerMessage.IVY_APP_REQUEST:
    		    if (!mSenderStatusManager.isReady()) {
    		        if (mImManager != null) {
                        mImManager.sendMessage(person, IvyInnerMessage.getIvyInnerMessage(IvyInnerMessage.IVY_APP_ANSWERNO));
                    }
    		        return;
    		    }
    		    mSenderStatusManager.setStatus(SenderStatusManager.Status.ASKDLG);
    			askIfSendToThisPerson(person);
    		break;
    	}
    }

    private class MessageBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent)
        {
            int messageType = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_TYPE, 0);
            int id = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_ID, 0);
            int messageState = intent.getIntExtra(IvyMessages.PARAMETER_MESSGAE_STATE, 0);
            int fileType = intent.getIntExtra(IvyMessages.PARAMETER_MESSAGE_FILE_TYPE, 0);
            String filename = intent.getStringExtra(IvyMessages.PARAMETER_MESSAGE_FILE_VALUE);
            boolean isMeSay = intent.getBooleanExtra(IvyMessages.PARAMETER_MESSAGE_SELF, true);
            String personKey = intent.getStringExtra(IvyMessages.PARAMETER_MESSAGE_PERSON);
            Person person = PersonManager.getInstance().getPerson(personKey);

            if (fileType == FileType.FileType_CommonMsg.ordinal()) {
            	int ret = IvyInnerMessage.parseIvyInnerMessage(filename);
            	if ( ret != -1) {
            		processInnerMessage(person, ret);
            		return;
            	}
            }

            if (fileType != FileType.FileType_App.ordinal()) {
                return;
            }

            if (messageType != IvyMessages.VALUE_MESSAGETYPE_UPDATE) {
                return;
            }
            
            if (!isMeSay) {
                return;
            }

            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FILEPROCESS_STATE, intent));
            abortBroadcast();
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

                if (state == ConnectionState.CONNECTION_STATE_HOTSPOT_ENABLED) {
                    mSenderStatusManager.setStatus(SenderStatusManager.Status.READY);
                } else if ((state == ConnectionState.CONNECTION_STATE_HOTSPOT_DISABLED) || (state == ConnectionState.CONNECTION_UNKNOWN)) {
                    if (mIvyConnectionManager != null) {
                        mIvyConnectionManager.createHotspot(NeedSendAppList.getInstance().mListAppInfo.size());
                    }
                }
                mConnectionState = state;
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

    private static class FileProcessInfo {
        public int mID;
        public Person mPerson;
        public long mPos;
        public long mTotal;
    }
    @Override
    public void onSendFileProcess(int id, Person to, String name, FileType fileType, long pos,
            long total) {
        // int progress = (int)(pos*100/total);
        FileProcessInfo info = new FileProcessInfo();
        info.mID = id;
        info.mPerson = to;
        info.mPos = pos;
        info.mTotal = total;
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FILEPROCESS_CHANGED, info));
    }

    @Override
    public void onReceiveProcess(int id, Person from, String name, FileType fileType, long pos,
            long total) {
        // int progress = (int)(pos*100/total);
        /*FileProcessInfo info = new FileProcessInfo();
        info.mID = id;
        info.mPerson = from;
        info.mPos = pos;
        info.mTotal = total;
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FILEPROCESS_CHANGED, info));*/        
    }
}
