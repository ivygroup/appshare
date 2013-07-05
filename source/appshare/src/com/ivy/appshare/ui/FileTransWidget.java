package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivy.appshare.R;
import com.ivy.appshare.utils.CommonUtils;

public class FileTransWidget extends BaseAdapter implements OnClickListener {
    private Context mContext;
    private List<MyAppInfo> mAppItems;
    boolean mIsReceiver;

    public FileTransWidget(Context context, boolean isReceiver) {
        mContext = context;
        mAppItems = new ArrayList<FileTransWidget.MyAppInfo>();
        mIsReceiver = isReceiver;
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


    //====================================================================

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

            // myClass.mLayout = (LinearLayout)view.findViewById(R.id.listitem);
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

        MyAppInfo theInfo = mAppItems.get(position);

        myClass.mMyAppInfo = theInfo;
        view.setOnClickListener(this);

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
            myClass.mResultImage.setVisibility(View.INVISIBLE);

        } else if (theInfo.mTransState == TransState.BEGIN) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setVisibility(View.INVISIBLE);
            int progress = 0;
            myClass.mProgressBar.setProgress(progress);
            myClass.mProgressText.setText(progress+"%");

        } else if (theInfo.mTransState == TransState.TRANSING) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setVisibility(View.INVISIBLE);
            int progress = (int)(theInfo.mPos*100/theInfo.mTotal);
            myClass.mProgressBar.setProgress(progress);
            myClass.mProgressText.setText(progress+"%");

        } else if (theInfo.mTransState == TransState.OK) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setImageResource(R.drawable.ic_send_successful);
            myClass.mResultImage.setVisibility(View.VISIBLE);
            myClass.mProgressBar.setProgress(100);
            myClass.mProgressText.setText("100%");

        } else {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setImageResource(R.drawable.ic_send_failed);
            myClass.mResultImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View arg0) {
        if (!mIsReceiver) {
            return;
        }
        MyAppInfo theInfo = ((ViewClass)arg0.getTag()).mMyAppInfo;
        CommonUtils.installApp(mContext, theInfo.mFullPath);
    }


    //====================================================================

    private static class ViewClass {
        // public LinearLayout mLayout;
        public ImageView mIcon;
        public TextView mAppName;
        public TextView mFileSize;
        public LinearLayout mProgressLinearLayout;
        public ProgressBar mProgressBar;
        public TextView mProgressText;
        public ImageView mResultImage;

        public MyAppInfo mMyAppInfo;
    }

    public static enum TransState {
        READY(false),
        BEGIN(false),
        TRANSING(false),
        OK(true),
        FAILED(true),
        TIMEOUT(true);

        private boolean mIsComplete;
        private TransState(boolean isComplete) {
            mIsComplete = isComplete;
        }

        public boolean isComplete() {
            return mIsComplete;
        }
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
