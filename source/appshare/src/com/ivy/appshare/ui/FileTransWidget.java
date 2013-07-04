package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivy.appshare.R;

public class FileTransWidget extends BaseAdapter {
    private Context mContext;
    private List<MyAppInfo> mAppItems;

    public FileTransWidget(Context context) {
        mContext = context;
        mAppItems = new ArrayList<FileTransWidget.MyAppInfo>();
    }

    public void addOneAppInfo(MyAppInfo info) {
        mAppItems.add(info);
    }

    public List<MyAppInfo> getAllApps() {
        return mAppItems;
    }

    public void changeAppState(int id, TransState newState, long pos, long total) {
        MyAppInfo info = getItemById(id);
        if (info == null) {
            return;
        }

        switch (newState) {
            case BEGIN:
            {
                if (info.mTransState != TransState.READY) {
                    return;
                }
                info.mTransState = newState;
                info.mPos = 0;
                info.mTotal = 100;
            }
                break;
                
            case TRANSING:
            {
                if (info.mTransState != TransState.TRANSING && info.mTransState != TransState.BEGIN) {
                    return;
                }
                if (pos <= info.mPos) {
                    return;
                }
                info.mTransState = TransState.TRANSING;
                info.mPos = pos;
                info.mTotal = total;
            }
                break;
                
            case OK:
            {
                info.mTransState = newState;
                info.mPos = 100;
                info.mTotal = 100;
            }
                break;
                
            case FAILED:
            {
                info.mTransState = newState;
            }
                break;
                
            case TIMEOUT:
            {
                info.mTransState = newState;
            }
                break;

            default:
                break;
        }
    }

    @Override
    public int getCount() {
        return mAppItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        if (position < 0 || position >= getCount()) {
            return null;
        }
        
        ViewClass myClass = null;
        if(view == null) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            view = factory.inflate(R.layout.listitem_send , null);
            myClass = new ViewClass();

            myClass.mIcon = (ImageView)view.findViewById(R.id.appicon);
            myClass.mAppName = (TextView)view.findViewById(R.id.name);
            myClass.mFileSize = (TextView)view.findViewById(R.id.size);
            myClass.mProgressLinearLayout = (LinearLayout)view.findViewById(R.id.progress_layout);
            myClass.mProgressBar = (ProgressBar)view.findViewById(R.id.progress);
            myClass.mProgressBar.setMax(100);
            myClass.mProgressText = (TextView)view.findViewById(R.id.progress_text);
            myClass.mResultImage = (ImageView)view.findViewById(R.id.result);

            view.setTag(myClass);
        } else {
            myClass = (ViewClass)view.getTag();
        }

        MyAppInfo theInfo;
        theInfo = mAppItems.get(position);
        showItemInfos(theInfo, myClass);

        return view;
    }

    private void showItemInfos(MyAppInfo theInfo, ViewClass myClass) {
        myClass.mAppName.setText(theInfo.mDisplayName);
        myClass.mFileSize.setText(theInfo.mFileSize);
        if (theInfo.mAppIcon != null) {
            myClass.mIcon.setImageDrawable(theInfo.mAppIcon);
        } else {
            myClass.mIcon.setImageResource(R.drawable.ic_file_type_apk);
        }

        if (theInfo.mTransState == TransState.READY) {
            myClass.mProgressLinearLayout.setVisibility(View.GONE);
            // myClass.mResultImage.setVisibility(View.INVISIBLE);
            myClass.mResultImage.setVisibility(View.GONE);

        } else if (theInfo.mTransState == TransState.BEGIN) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            // myClass.mResultImage.setVisibility(View.INVISIBLE);
            myClass.mResultImage.setVisibility(View.GONE);
            int progress = 0;
            myClass.mProgressBar.setProgress(progress);
            myClass.mProgressText.setText(progress+"%");

        } else if (theInfo.mTransState == TransState.TRANSING) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            // myClass.mResultImage.setVisibility(View.INVISIBLE);
            myClass.mResultImage.setVisibility(View.GONE);
            int progress = (int)(theInfo.mPos*100/theInfo.mTotal);
            myClass.mProgressBar.setProgress(progress);
            myClass.mProgressText.setText(progress+"%");

        } else if (theInfo.mTransState == TransState.OK) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            // myClass.mResultImage.setVisibility(View.VISIBLE);
            myClass.mProgressBar.setProgress(100);
            myClass.mProgressText.setText("100%");

        } else {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            // myClass.mResultImage.setVisibility(View.VISIBLE);

        }
    }

    public MyAppInfo getItemById(int id) {
        for (MyAppInfo info: mAppItems) {
            if (info.mID == id) {
                return info;
            }
        }
        return null;
    }

    public MyAppInfo getItemByPath(String path) {
        for (MyAppInfo info: mAppItems) {
            if (info.mFullPath.equals(path)) {
                return info;
            }
        }
        return null;
    }

    private static class ViewClass {
        public ImageView mIcon;
        public TextView mAppName;
        public TextView mFileSize;
        public LinearLayout mProgressLinearLayout;
        public ProgressBar mProgressBar;
        public TextView mProgressText;
        public ImageView mResultImage;
    }

    public enum TransState {
        READY,
        BEGIN,
        TRANSING,
        OK,
        FAILED,
        TIMEOUT,
    }

    public static class MyAppInfo {
        public String mDisplayName;
        public Drawable mAppIcon;
        public String mFullPath;

        public String mFileSize;
        public int mID;

        public TransState mTransState;
        public long mPos;
        public long mTotal;
    }
}
