package com.ivy.appshare.ui;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivy.appshare.R;
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

	private SharedPreferences sp;
	private String mName;//user name

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_app_list);

		View actionbar = (View) findViewById(R.id.layout_title);
		mTextSelected = ((TextView) actionbar
				.findViewById(R.id.center_text_info));
		mTextSelected.setVisibility(View.VISIBLE);
		setSelectItemText(0);

		mTextLeft = ((TextView) actionbar.findViewById(R.id.left_text_info));
		mTextLeft.setVisibility(View.VISIBLE);

		sp = getSharedPreferences("SP", MODE_PRIVATE);
		mName = sp.getString("Name", new String(android.os.Build.MODEL));
		mTextLeft.setText(mName);
		mTextLeft.setOnClickListener(this);

		mButtonRight = ((ImageButton) actionbar.findViewById(R.id.btn_right));
		mButtonRight.setImageResource(R.drawable.ic_select_send);
		mButtonRight.setVisibility(View.VISIBLE);
		mButtonRight.setOnClickListener(this);

		mAPKLoader = new APKLoader();
		mAPKLoader.init(this);

		mAppGridView = (GridView) findViewById(R.id.gridview);
		mAppAdapter = new AppFreeShareAdapter(this, mAPKLoader.getAppList(),
				this);

		mAppGridView.setAdapter(mAppAdapter);
		mAPKLoader.setAdapter(mAppAdapter);
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
			View view = LayoutInflater.from(this).inflate(R.layout.change_user_name, null);
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
									}
									Editor editor = sp.edit();
									editor.putString("Name", mNewName);
									editor.commit();
									mTextLeft.setText(mNewName);
								}
							}).setNegativeButton(R.string.cancel, null).show();
			break;
		}

	}
}
