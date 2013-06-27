package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_app_list);

		mLocalSetting = LocalSetting.getInstance();

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

		mSharedPersonList = (ListView) findViewById(R.id.shared_person);
		mSharedPersonList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1,getData()));
		if (getData().size() > 0) {
			mSharedPersonList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Toast.makeText(AppListActivity.this,
							"您选择了" + getData().get(arg2), Toast.LENGTH_LONG)
							.show();
				}
			});
		} else {
			mSharedPersonList.setVisibility(View.GONE);
		}

		mAPKLoader = new APKLoader();
		mAPKLoader.init(this);

		mAppGridView = (GridView) findViewById(R.id.gridview);
		mAppAdapter = new AppFreeShareAdapter(this, mAPKLoader.getAppList(),
				this);

		mAppGridView.setAdapter(mAppAdapter);
		mAPKLoader.setAdapter(mAppAdapter);
	}

    private List<String> getData(){

        List<String> data = new ArrayList<String>();
        data.add("测试数据1");
        data.add("测试数据2");

        return data;
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
							}).setNegativeButton(R.string.cancel, null).show();
			break;

		case R.id.btn_right:
		{
		    if (mAppAdapter.getSelectItemCount() > 0) {
		        Intent intent = new Intent();
		        intent.setClass(this, SendActivity.class);
		        intent.putStringArrayListExtra("items", mAppAdapter.getSelectItems());
		        startActivity(intent);
		    } else {
		        // TODO:  Toast.
		    }
		}
		break;
		}

	}
}
