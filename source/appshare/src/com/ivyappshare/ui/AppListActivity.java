package com.ivyappshare.ui;

import com.ivyappshare.R;
import com.ivyappshare.utils.APKLoader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

public class AppListActivity extends Activity 
	implements AppFreeShareAdapter.SelectChangeListener, View.OnClickListener{

    private AppFreeShareAdapter mAppAdapter = null;
    private APKLoader mAPKLoader = null;
	private GridView mAppGridView = null;
	private TextView mTextSelected;
	private ImageButton mButtonRight;
	private TextView mTextLeft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_list);

        View actionbar = (View)findViewById(R.id.layout_title);
        mTextSelected = ((TextView)actionbar.findViewById(R.id.center_text_info));
        mTextSelected.setVisibility(View.VISIBLE);
        setSelectItemText(0);

        mTextLeft = ((TextView)actionbar.findViewById(R.id.left_text_info));
        mTextLeft.setVisibility(View.VISIBLE);
        mTextLeft.setText("Anna");

        mButtonRight = ((ImageButton)actionbar.findViewById(R.id.btn_right));
        mButtonRight.setImageResource(R.drawable.ic_select_send);
        mButtonRight.setVisibility(View.VISIBLE);
        mButtonRight.setOnClickListener(this);

        mAPKLoader = new APKLoader();
	    mAPKLoader.init(this);

        mAppGridView = (GridView) findViewById(R.id.gridview); 
        mAppAdapter = new AppFreeShareAdapter(this, mAPKLoader.getAppList(), this);

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
		// TODO Auto-generated method stub
		
	}
}
