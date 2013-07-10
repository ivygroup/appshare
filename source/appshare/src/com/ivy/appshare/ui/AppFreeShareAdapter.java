package com.ivy.appshare.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.R;

public class AppFreeShareAdapter extends BaseAdapter {

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

	public void removeItem(int position) {
	    mlistAppInfo.remove(position);
	    notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.listitem_app, null);
			//view.setClickable(true);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}
		holder.pos = position;

		//view.setOnClickListener(this);

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
		if (appInfo.type == AppsInfo.APP_INSTALLED) {
			holder.viewInstalled.setVisibility(View.VISIBLE);
		} else {
			holder.viewInstalled.setVisibility(View.INVISIBLE);
		}
		return view;
	}

	class ViewHolder {
		public View view;
		public ImageView appIcon;
		public TextView tvAppLabel;
		public ImageView appSelected;
		public View viewInstalled;
		public int pos;

		public ViewHolder(View view) {
			this.view = view;
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			this.appSelected = (ImageView) view.findViewById(R.id.app_selected);
			this.viewInstalled = view.findViewById(R.id.layout_install);
		}
	}

	public void onClickItem(int pos) {
		int nSize = mlistAppInfo.size();
		if (pos < 0 || pos >= nSize) {
			return;
		}

		AppsInfo info = mlistAppInfo.get(pos);
		info.isSelected = !info.isSelected;
		notifyDataSetChanged();

		mListener.onSelectedChanged();
	}

	public void disSelectAll() {
		int nSize = mlistAppInfo.size();
		for (int i=0; i<nSize; i++) {
			mlistAppInfo.get(i).isSelected = false;
		}
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

	public void getSelectItems(List<AppsInfo> result) {
	    result.clear();
	    for (AppsInfo info: mlistAppInfo) {
	        if (info.isSelected) {
	            result.add(info);
	        }
	    }
	}

	public AppsInfo getMySelfAppInfo() {
	    AppsInfo resultAppsInfo = null;
	    for (AppsInfo info: mlistAppInfo) {
	        if (info.packageName.compareToIgnoreCase(MyApplication.mPackageName) == 0) {
	            if (resultAppsInfo == null) {
	                resultAppsInfo = info;
	            } else {
	                if (resultAppsInfo.versionCode < info.versionCode) {
	                    resultAppsInfo = info;
	                }
	            }
	        }
	    }
	    return resultAppsInfo;
	}
}
