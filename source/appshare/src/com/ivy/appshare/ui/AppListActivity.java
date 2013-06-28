package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.appshare.R;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.utils.APKLoader;
import com.ivy.appshare.utils.CommonUtils;
import com.ivy.appshare.utils.IvyActivityBase;

public class AppListActivity extends IvyActivityBase implements
		AppFreeShareAdapter.SelectChangeListener, View.OnClickListener {

	private AppFreeShareAdapter mAppAdapter = null;
	private APKLoader mAPKLoader = null;
	private GridView mAppGridView = null;
	private TextView mTextSelected;
	private ImageButton mButtonRight;
	private TextView mTextLeft;
	private ListView mSharedPersonList;

	private LocalSetting mLocalSetting;
	private Handler mHandler;
	private int mFileSharedNum;
	private List<String> mFileShareSSID = new ArrayList<String>();
	private List<String> mFileShareName = new ArrayList<String>();
	private List<String> mShareData;
	private ArrayAdapter mAdapter;

	private static final int MESSAGE_NETWORK_SCAN_FINISH = 0;
	private static final int MESSAGE_NETWORK_CLEAR_IVYROOM = 1;
	private static final int MESSAGE_NETWORK_DISCOVERYWIFIP2P = 2;
	private static final int MESSAGE_NETWORK_STATE_CHANGED = 3;
	private NetworkReceiver mNetworkReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_app_list);

		mLocalSetting = LocalSetting.getInstance();
		mNetworkReceiver = new NetworkReceiver();

		View actionbar = (View) findViewById(R.id.layout_title);
		mTextSelected = ((TextView) actionbar
				.findViewById(R.id.center_text_info));
		mTextSelected.setVisibility(View.VISIBLE);
		setSelectItemText(0);

		mTextLeft = ((TextView) actionbar.findViewById(R.id.left_text_info));
		mTextLeft.setVisibility(View.VISIBLE);

		mTextLeft.setText(mLocalSetting.getMySelf().mNickName);
		mTextLeft.setOnClickListener(this);

		mButtonRight = ((ImageButton) actionbar.findViewById(R.id.btn_right));
		mButtonRight.setImageResource(R.drawable.ic_select_send);
		mButtonRight.setVisibility(View.VISIBLE);
		mButtonRight.setOnClickListener(this);

		mShareData = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1,mShareData);
		mSharedPersonList = (ListView) findViewById(R.id.shared_person);
		mSharedPersonList.setAdapter(mAdapter);
		mSharedPersonList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				/*CommonUtils.getMyAlertDialogBuilder(AppListActivity.this)
				.setTitle(R.string.wait_receive_dl_title)
				.setMessage(R.string.wait_receive_dl_message)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(AppListActivity.this,ReceiveActivity.class);
						intent.putExtra("ssid", mFileShareSSID.get(arg2));
						intent.putExtra("nickName", mFileShareName.get(arg2));
						startActivity(intent);
					}

				})
				.setNegativeButton(R.string.cancel, null)
				.show().setCanceledOnTouchOutside(false);*/
				Intent intent = new Intent(AppListActivity.this,ReceiveActivity.class);
				intent.putExtra("ssid", mFileShareSSID.get(arg2));
				intent.putExtra("nickName", mFileShareName.get(arg2));
				startActivity(intent);
			}
		});

		mAPKLoader = new APKLoader();
		mAPKLoader.init(this);

		mAppGridView = (GridView) findViewById(R.id.gridview);
		mAppAdapter = new AppFreeShareAdapter(this, mAPKLoader.getAppList(),
				this);

		mAppGridView.setAdapter(mAppAdapter);
		mAPKLoader.setAdapter(mAppAdapter);
		mHandler = new Handler(this.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case MESSAGE_NETWORK_STATE_CHANGED:
				case MESSAGE_NETWORK_CLEAR_IVYROOM:
				case MESSAGE_NETWORK_DISCOVERYWIFIP2P:
				case MESSAGE_NETWORK_SCAN_FINISH:
					updateList();
					break;
				}
				super.handleMessage(msg);
			}
		};

	}

	@Override
    public void onServiceConnected(ComponentName name, IBinder service) {
	    super.onServiceConnected(name, service);
	    mIvyConnectionManager.disableHotspot();
	    mIvyConnectionManager.enableWifi();
	}

	@Override
    public void onServiceDisconnected(ComponentName name) {
	    mIvyConnectionManager.disableHotspot();
	    super.onServiceDisconnected(name);
	}

	private void updateList() {
		mShareData.clear();
		mFileShareSSID.clear();
		mFileShareName.clear();
		for (int i = 0; i < mFileSharedNum; i++) {
			int count = mIvyConnectionManager.getScanResult().get(i).getShareAppCount();
			String scanSSID = mIvyConnectionManager.getScanResult().get(i).getSSID();
			String scanName = mIvyConnectionManager.getScanResult().get(i).getFriendlyName();
			String fileShareList = getResources().getString(
					R.string.share_list, scanName, count);
			mShareData.add(fileShareList);
			mFileShareSSID.add(scanSSID);
			mFileShareName.add(scanName);
		}

		mAdapter.notifyDataSetChanged();
	}

	private void setSelectItemText(int count) {
		String content = String.format(getString(R.string.choose_app), count);
		mTextSelected.setText(content);
	}

	@Override
	public void onSelectedChanged() {
		setSelectItemText(mAppAdapter.getSelectItemCount());
	}

	@Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(IvyMessages.INTENT_NETWORK_AIRPLANE);
        filter.addAction(IvyMessages.INTENT_NETWORK_STATECHANGE);
        filter.addAction(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM);
        filter.addAction(IvyMessages.INTENT_NETWORK_DISCOVERYWIFIP2P);
        registerReceiver(mNetworkReceiver, filter);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mNetworkReceiver);
	}

	private class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			mFileSharedNum = mIvyConnectionManager.getScanResult().size();

			if (action.equals(IvyMessages.INTENT_NETWORK_STATECHANGE)) {
				int type = intent.getIntExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_TYPE, 0);
				int state = intent.getIntExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_STATE, 0);
				String ssid = intent.getStringExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_SSID);

				// mNetworkState = state;
				mHandler.sendMessage(mHandler.obtainMessage(
						MESSAGE_NETWORK_STATE_CHANGED, type, state, ssid));

			} else if (action.equals(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM)) {
				boolean isclear = intent.getBooleanExtra(
								IvyMessages.PARAMETER_NETWORK_FINISHSCANIVYROOM_ISCLEAR,false);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_text_info:
			View view = LayoutInflater.from(this).inflate(R.layout.dlg_change_user_name, null);
			final EditText mNameEditText = (EditText) view.findViewById(R.id.name);
			mNameEditText.setText(mTextLeft.getText());

			CommonUtils.getMyAlertDialogBuilder(this)
					.setTitle(R.string.change_name)
					.setView(view)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									//change a new name
									String mNewName = mNameEditText.getText().toString();
									if (0 == mNewName.length()) {
										mNewName = mTextLeft.getText().toString();
										Toast.makeText(AppListActivity.this,
												R.string.name_empty, Toast.LENGTH_LONG)
												.show();
									}
									mLocalSetting.saveNickName(mNewName);
									mTextLeft.setText(mNewName);
								}
							}).setNegativeButton(R.string.cancel, null).show()
							.setCanceledOnTouchOutside(false);
			break;

		case R.id.btn_right:
		{
		    if (mAppAdapter.getSelectItemCount() > 0) {
		        mAppAdapter.getSelectItems(NeedSendAppList.getInstance().mListAppInfo);

		        Intent intent = new Intent();
		        intent.setClass(this, SendActivity.class);
		        startActivity(intent);
		    } else {
		        // TODO:  Toast.
		    }
		}
		break;
		}

	}
}
