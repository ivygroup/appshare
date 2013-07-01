package com.ivy.appshare.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivy.appshare.R;
import com.ivy.appshare.engin.control.ImManager;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;

public class SendListAdapter extends BaseAdapter {
    private Context mContext;
    List<MyAppInfo> mListSendItems;
    ImManager mImManager;
    Person mToPerson;


    public SendListAdapter(Context context) {
        mContext = context;
        mListSendItems = new ArrayList<SendListAdapter.MyAppInfo>();
        for (AppsInfo appinfo: NeedSendAppList.getInstance().mListAppInfo) {
            MyAppInfo info = new MyAppInfo();
            info.mAppsInfo = appinfo;
            {
                File file = new File(info.mAppsInfo.sourceDir);
                DecimalFormat df= new DecimalFormat("#.##");   
                if (file.exists()) {
                    long filesize = file.length();
                    if (filesize < 1024) {
                        info.mFileSize = filesize + " byte";
                    } else if (filesize < 1024 *1024) {
                        double tmp = (double)filesize/1024;
                        info.mFileSize = df.format(tmp) + " KB";
                    } else {
                        double tmp = (double)filesize/1024/1024 ;
                        info.mFileSize = df.format(tmp) + " MB";
                    }
                }
            }

            info.mTransState = TransState.READY;
            mListSendItems.add(info);
        }
    }

    public void beginTranslate(ImManager imManager, Person toPerson) {
        mImManager = imManager;
        mToPerson = toPerson;
        synchronized (mListSendItems) {
            for (MyAppInfo info: mListSendItems) {
                info.mTransState = TransState.READY;
                int id = mImManager.sendFile(mToPerson, null, info.mAppsInfo.sourceDir, FileType.FileType_App);
                info.mFileIDForListener = id;
            }    
        }
    }

    public void changeTransState_Begin(Person person, int fileID) {
        MyAppInfo info = getItemById(fileID);
        if (info == null) {
            return;
        }
        if (info.mTransState != TransState.READY) {
            return;
        }
        info.mTransState = TransState.BEGIN;
        info.mPos = 0;
        info.mTotal = 100;
    }

    public void changeTransState_Process(Person person, int fileID, long pos, long total) {
        MyAppInfo info = getItemById(fileID);
        if (info == null) {
            return;
        }
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

    public void changeTransState_OK(Person person, int fileID) {
        MyAppInfo info = getItemById(fileID);
        if (info == null) {
            return;
        }
        info.mTransState = TransState.OK;
        info.mPos = 100;
        info.mTotal = 100;
    }

    public void changeTransState_Failed(Person person, int fileID) {
        MyAppInfo info = getItemById(fileID);
        if (info == null) {
            return;
        }
        info.mTransState = TransState.FAILED;
    }

    public void changeTransState_TimeOut(Person person, int fileID) {
        MyAppInfo info = getItemById(fileID);
        if (info == null) {
            return;
        }
        info.mTransState = TransState.TIMEOUT;
    }

    @Override
    public int getCount() {
        synchronized (mListSendItems) {
            return mListSendItems.size();
        }
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
        synchronized (mListSendItems) {
            theInfo = mListSendItems.get(position);
        }
        showItemInfos(theInfo, myClass);

        return view;
    }

    private void showItemInfos(MyAppInfo theInfo, ViewClass myClass) {
        myClass.mAppName.setText(theInfo.mAppsInfo.appLabel);
        myClass.mFileSize.setText(theInfo.mFileSize);
        myClass.mIcon.setImageDrawable(theInfo.mAppsInfo.appIcon);

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

    private MyAppInfo getItemById(int id) {
        synchronized (mListSendItems) {
            for (MyAppInfo info: mListSendItems) {
                if (info.mFileIDForListener == id) {
                    return info;
                }
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

    private enum TransState {
        READY,
        BEGIN,
        TRANSING,
        OK,
        FAILED,
        TIMEOUT,
    }

    private static class MyAppInfo {
        public AppsInfo mAppsInfo;
        // public String mDisplayName;
        // public String mFullPath;
        public String mFileSize;
        public int mFileIDForListener;

        public TransState mTransState;
        public long mPos;
        public long mTotal;
    }
}
