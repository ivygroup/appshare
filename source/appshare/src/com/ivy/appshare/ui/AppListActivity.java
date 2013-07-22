package com.ivy.appshare.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.R;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.utils.APKLoader;
import com.ivy.appshare.utils.APKLoader.ApkLoaderDataChang;
import com.ivy.appshare.utils.CommonUtils;
import com.ivy.appshare.utils.IvyActivityBase;

public class AppListActivity extends IvyActivityBase implements
		AppFreeShareAdapter.SelectChangeListener, View.OnClickListener, View.OnLongClickListener,
		AdapterView.OnItemClickListener, ApkLoaderDataChang {
	
    private static final String TAG = "AppListActivity";
    
	private AppFreeShareAdapter mAppAdapter = null;
	private AppsInfo mMySelfAppsInfo;
	private APKLoader mAPKLoader = null;
	private GridView mAppGridView = null;
	private TextView mTextSelected;
	private ImageButton mButtonMid;
	private ImageButton mButtonRight;
	private TextView mTextLeft;
	private ListView mSharedPersonList;
	private PopupWindow mPopupWindowNfcTip;

	private LocalSetting mLocalSetting;
	private Handler mHandler;
	private int mFileSharedNum;
	private List<String> mFileShareSSID = new ArrayList<String>();
	private List<String> mFileShareName = new ArrayList<String>();
	private List<String> mShareData;
	private ArrayAdapter mAdapter;

	private ApkBroadcastReceiver mApkReceiver = new ApkBroadcastReceiver();

	private static final int MESSAGE_NETWORK_SCAN_FINISH = 0;
	private static final int MESSAGE_NETWORK_CLEAR_IVYROOM = 1;
	private static final int MESSAGE_NETWORK_DISCOVERYWIFIP2P = 2;
	private static final int MESSAGE_NETWORK_STATE_CHANGED = 3;
	private static final int MESSAGE_UI_SHOW_NFCTIP_WINDOW = 10;
	private NetworkReceiver mNetworkReceiver = null;
	
	private static final int REQUEST_RECEIVE_APP = 0;
	public static final int RECEIVE_APP_YES = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_app_list);

		mMySelfAppsInfo = null;

		mLocalSetting = LocalSetting.getInstance();
		mNetworkReceiver = new NetworkReceiver();

		View actionbar = (View) findViewById(R.id.layout_title);
		mTextSelected = ((TextView) actionbar
				.findViewById(R.id.center_text_info));
		mTextSelected.setVisibility(View.VISIBLE);

		mTextLeft = ((TextView) actionbar.findViewById(R.id.left_text_info));
		mTextLeft.setVisibility(View.VISIBLE);
		mTextLeft.setBackgroundResource(R.drawable.textbtn_selector);
		mTextLeft.setText(mLocalSetting.getMySelf().mNickName);
		mTextLeft.setOnClickListener(this);
		mTextLeft.setOnLongClickListener(this);

		mButtonMid = ((ImageButton) actionbar.findViewById(R.id.btn_mid));
		mButtonMid.setImageResource(R.drawable.unselected_pressed);
		mButtonMid.setOnClickListener(this);
		mButtonMid.setOnLongClickListener(this);

		mButtonRight = ((ImageButton) actionbar.findViewById(R.id.btn_right));
		mButtonRight.setImageResource(R.drawable.ic_menu_share);
		mButtonRight.setVisibility(View.VISIBLE);
		mButtonRight.setOnClickListener(this);
		mButtonRight.setOnLongClickListener(this);

		setSelectItemText(0);
		
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
				startActivityForResult(intent, REQUEST_RECEIVE_APP);
			}
		});

		mAPKLoader = new APKLoader();
		mAPKLoader.init(this, this);

		mAppGridView = (GridView) findViewById(R.id.gridview);
		mAppAdapter = new AppFreeShareAdapter(this, this);

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

				case MESSAGE_UI_SHOW_NFCTIP_WINDOW:
					showNfcTipWindow();
				    break;
				}
				super.handleMessage(msg);
			}
		};
		mAppGridView.setOnCreateContextMenuListener(this);
		mAppGridView.setOnItemClickListener(this);

		mApkReceiver = new ApkBroadcastReceiver();
		registerApkReceiver();

		registerNfcPushFeature();
	}

	private void registerApkReceiver() {
    	IntentFilter filter = new IntentFilter();
    	filter.addDataScheme("package");
    	filter.addAction(Intent.ACTION_PACKAGE_ADDED);
    	filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    	filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
    	registerReceiver(mApkReceiver, filter);
	}

	@SuppressLint("NewApi")
    private void registerNfcPushFeature() {
	    int current_version = Build.VERSION.SDK_INT;
	    if (current_version >= Build.VERSION_CODES.JELLY_BEAN) { // 16.
	        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	        if (nfcAdapter != null) {
	            // nfcAdapter.setNdefPushMessageCallback(new PushNfcMessage(), this);

	            // Uri uri1 = Uri.parse("file://" + mMySelfAppsInfo.sourceDir);
	            // nfcAdapter.setBeamPushUris(new Uri[]{uri1}, this);

	            nfcAdapter.setBeamPushUrisCallback(new CreateBeamUrisForMySelf(), this);
	        }
	    }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo); 

		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		if (mAppAdapter == null) {
			return;
		}
		AppsInfo appInfo = (AppsInfo)mAppAdapter.getItem(info.position);
		if (appInfo == null) {
			return;
		}

		if (appInfo.type == AppsInfo.APP_INSTALLED) {
		    MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.app_action_menu_installed, menu);
            if (appInfo.packageName.compareTo(MyApplication.mPackageName) == 0) {
                menu.getItem(1).setVisible(false);
                menu.getItem(2).setVisible(false);
            }
		} else {
		    MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.app_action_menu_uninstalled, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		if (mAppAdapter == null) {
			return false;
		}
		AppsInfo appInfo = (AppsInfo)mAppAdapter.getItem(info.position);
		if (appInfo == null) {
			return false;
		}

		switch(item.getItemId()) {
		case R.id.action_view:
		    CommonUtils.viewFile(this, FileType.FileType_App, appInfo.packageName);
		    break;
		case R.id.action_install:
			CommonUtils.installApp(this, appInfo.sourceDir);
			break;
		case R.id.action_uninstall:
			CommonUtils.unInstallAppAsync(this, appInfo.packageName, appInfo.sourceDir);
			break;
		case R.id.action_launch:
			CommonUtils.launchApp(this, appInfo.packageName);
			break;
		case R.id.action_delete:
		{
		    final int position = info.position;
		    final String sourceDir = appInfo.sourceDir;
		    CommonUtils.getMyAlertDialogBuilder(AppListActivity.this)
            .setTitle(R.string.delete_confirm)
            .setMessage(getResources().getString(R.string.areyousure_delete, appInfo.appLabel))
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAppAdapter.removeItem(position);
                    CommonUtils.deleteFile(sourceDir);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
		}
		    break;
		case R.id.action_bluetoothshare:
			CommonUtils.shareWithBluttooth(this, appInfo.sourceDir);
			break;
	     case R.id.action_property:
	        {
	            Intent intent = new Intent();
	            intent.setClass(this, QuickAppInfoActivity.class);

	            BitmapDrawable bd = (BitmapDrawable)appInfo.appIcon;
	            Bitmap bm = bd.getBitmap();
	            intent.putExtra("image", bm);
	            intent.putExtra("name", appInfo.appLabel);
	            intent.putExtra("packagename", appInfo.packageName);
	            intent.putExtra("version", appInfo.versionName);
	            intent.putExtra("sourcedir", appInfo.sourceDir);

	            startActivity(intent);
	        }
	            break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAppAdapter == null) {
			return;
		}
		mAppAdapter.onClickItem(position);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.app_list, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
            case R.id.send_by_bluetooth:
                sendMySelfByBluetooth();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
	}

	private void sendMySelfByBluetooth() {
        AppsInfo mySelfAppsInfo = mMySelfAppsInfo;
        if (mySelfAppsInfo == null || mySelfAppsInfo.sourceDir == null) {
            return;
        }

        boolean isSupportBluetooth = false;
        {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                isSupportBluetooth = false;
            } else {
                isSupportBluetooth = true;
            }
        }

        final boolean b = isSupportBluetooth;
        final String str = mySelfAppsInfo.sourceDir;
        if (isSupportBluetooth) {
            CommonUtils.getMyAlertDialogBuilder(AppListActivity.this)
            .setTitle(R.string.send_by_bluetooth_title)
            .setMessage(R.string.send_by_bluetooth_message)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendMySelfByBluetooth(str, b);
                }
            })
            .show();
        }
	}

	private void sendMySelfByBluetooth(String mySelfAppsPath, boolean isSupportBluetooth) {
        File sourceFile=new File(mySelfAppsPath);
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        if (isSupportBluetooth) {
            intent.setPackage("com.android.bluetooth");
        }
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sourceFile));
        startActivity(intent);
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
		if (0 == count) {
			mButtonMid.setVisibility(View.INVISIBLE);
			mTextSelected.setText(R.string.choose_app);
		} else {
			mButtonMid.setVisibility(View.VISIBLE);
			mTextSelected.setText(String.format(getString(R.string.selected_app), count));
		}
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
		unregisterReceiver(mApkReceiver);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);

	    if (hasFocus) {
	        initPopuptWindow();
	    }
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
		case R.id.btn_mid:
			if (mAppAdapter != null) {
				mAppAdapter.disSelectAll();
			}
			break;
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
		    	Toast.makeText(this, R.string.choose_app, Toast.LENGTH_SHORT).show();
		    }
		}
		break;
		}

	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		setSelectItemText(0);
    		mAPKLoader.reLoad();
        }
    }

    public class ApkBroadcastReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		setSelectItemText(0);
    		mAPKLoader.reLoad();
    	}
    }

	@Override
	public boolean onLongClick(View v) {
		int toastTextId = 0;
		switch (v.getId()) {
		case R.id.btn_mid:
			toastTextId = R.string.toast_unselect;
			break;
		case R.id.left_text_info:
			toastTextId = R.string.toast_rename;
			break;
		case R.id.btn_right:
			toastTextId = R.string.toast_send;
			break;
		}
		Toast.makeText(this, toastTextId, Toast.LENGTH_SHORT).show();
		return false;
	}

    @Override
    public void apkDataChanged(List<AppsInfo> data) {
        if (mAppAdapter != null) {
            mAppAdapter.setData(data);
            mAppAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void mySelfLoaded(AppsInfo info) {
        mMySelfAppsInfo = info;
    }


    @SuppressLint("NewApi")
	private class CreateBeamUrisForMySelf implements NfcAdapter.CreateBeamUrisCallback {

        @Override
        public Uri[] createBeamUris(NfcEvent arg0) {
            if (mMySelfAppsInfo == null) {
                return null;
            }
            Uri uri1 = Uri.parse("file://" + mMySelfAppsInfo.sourceDir);
            return new Uri[]{uri1};
        }
    }


    @SuppressLint("NewApi")
    private void initPopuptWindow() {
        int current_version = Build.VERSION.SDK_INT;
        if (current_version < Build.VERSION_CODES.JELLY_BEAN) { // 16.
            return;
        }

        boolean canShowNfcPopuWindow = LocalSetting.getInstance().getNfcPopupWindow();
        if (!canShowNfcPopuWindow) {
            return;
        } //*/

        if (mPopupWindowNfcTip != null) {
            return;
        }

        View popupWindow_view = getLayoutInflater().inflate(R.layout.popupwindow_nfctip, null,false);

      //获取屏幕和对话框各自高宽
        int screenWidth = AppListActivity.this.getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = AppListActivity.this.getWindowManager().getDefaultDisplay().getHeight();

        mPopupWindowNfcTip = new PopupWindow(popupWindow_view, screenWidth, screenHeight/3, true);
        mPopupWindowNfcTip.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindowNfcTip.setAnimationStyle(R.style.PopupAnimation);

        Button bt_setnfc = (Button)popupWindow_view.findViewById(R.id.bt_setnfc);
        Button bt_cancle = (Button)popupWindow_view.findViewById(R.id.bt_cancle);

        bt_setnfc.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = null;
                /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    intent = new Intent(android.provider.Settings.ACTION_NFC_SETTINGS);
                } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    intent = new Intent();
                    ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                    // intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                } else {
                    // can't run this branch.
                }  //*/
                intent = new Intent(android.provider.Settings.ACTION_NFC_SETTINGS);
                startActivity(intent);

                mPopupWindowNfcTip.dismiss();
            }
        });

        bt_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindowNfcTip.dismiss();
            }
        });

        final CheckBox cb_shownext = (CheckBox)popupWindow_view.findViewById(R.id.checkbox);
        cb_shownext.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSetting.getInstance().saveNfcPopupWindow(!isChecked);
            }
        });

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                bt_setnfc.setVisibility(View.GONE);
            } else {
                bt_setnfc.setVisibility(View.VISIBLE);
            }
        }

        // mHandler.sendEmptyMessageDelayed(MESSAGE_UI_SHOW_NFCTIP_WINDOW, 200);
        mHandler.sendEmptyMessage(MESSAGE_UI_SHOW_NFCTIP_WINDOW);
    }
    
	private void showNfcTipWindow() {
		try {
		    mPopupWindowNfcTip.showAtLocation(findViewById(R.id.layout), Gravity.BOTTOM, 0, 0);
		} catch (Exception e) {
			Log.e(TAG, "" + e);
		}
	
	}
}
