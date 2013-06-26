package com.ivy.appshare.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.appshare.R;

public class AppFreeShareAdapter extends BaseAdapter implements View.OnClickListener {

	private Context mContext;
	private List<AppsInfo> mlistAppInfo = null;
	private SelectChangeListener mListener;

	public interface SelectChangeListener {
		public void onSelectedChanged();
	}

	public AppFreeShareAdapter(Context c, List<AppsInfo> apps, SelectChangeListener listener) {
		mContext = c;
		mlistAppInfo = apps;
		mListener = listener;
	}

	@Override
	public int getCount() {
		return mlistAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return mlistAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.listitem_app, null);
			view.setClickable(true);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}
		holder.pos = position;

		view.setOnClickListener(this);

		AppsInfo appInfo = (AppsInfo) getItem(position);
		holder.appIcon.setImageDrawable(appInfo.appIcon);
		holder.tvAppLabel.setText(appInfo.appLabel);

		if (appInfo.isSelected) {
			holder.tvAppLabel.setTextColor(mContext.getResources().getColor(R.color.list_main));
			holder.appSelected.setVisibility(View.VISIBLE);
		} else {
			holder.tvAppLabel.setTextColor(mContext.getResources().getColor(R.color.list_secondray));
			holder.appSelected.setVisibility(View.GONE);
		}
		return view;
	}

	class ViewHolder {
		public View view;
		public ImageView appIcon;
		public TextView tvAppLabel;
		public ImageView appSelected;
		public int pos;

		public ViewHolder(View view) {
			this.view = view;
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			this.appSelected = (ImageView) view.findViewById(R.id.app_selected);
		}
	}

	@Override
	public void onClick(View v) {
		ViewHolder holder = (ViewHolder) v.getTag();
		int pos = holder.pos;
		int nSize = mlistAppInfo.size();
		if (pos < 0 || pos >= nSize) {
			return;
		}

		AppsInfo info = mlistAppInfo.get(pos);
		info.isSelected = !info.isSelected;
		notifyDataSetChanged();
		
		mListener.onSelectedChanged();
	}

	public int getSelectItemCount() {
		int nSize = mlistAppInfo.size();
		int nCount = 0;
		for (int i=0; i<nSize; i++) {
			if (mlistAppInfo.get(i).isSelected) {
				nCount++;
			}
		}
		return nCount;
	}
}
