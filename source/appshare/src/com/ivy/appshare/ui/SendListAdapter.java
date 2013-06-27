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
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;

public class SendListAdapter extends BaseAdapter {
    private Context mContext;
    List<MyAppInfo> mListSendItems;
    ImManager mImManager;
    Person mToPerson;


    public SendListAdapter(Context context, List<String> sendApps) {
        mContext = context;
        mListSendItems = new ArrayList<SendListAdapter.MyAppInfo>();
        for (String path: sendApps) {
            MyAppInfo info = new MyAppInfo();
            info.mFullPath = path;
            info.mDisplayName = path.substring(path.lastIndexOf("/") + 1, path.length());
            {
                File file = new File(path);
                DecimalFormat df= new DecimalFormat("#.##");   
                if (file.exists()) {
                    long filesize = file.length();
                    if (filesize < 1024) {
                        info.mFileSize = filesize + " byte";
                    } else if (filesize < 1024 *1024) {
                        double tmp = (double)filesize/1024;
                        info.mFileSize = df.format(tmp) + " k";
                    } else {
                        double tmp = (double)filesize/1024/1024 ;
                        info.mFileSize = df.format(tmp) + " m";
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
        for (MyAppInfo info: mListSendItems) {
            mImManager.sendFile(mToPerson, null, info.mFullPath, FileType.FileType_App);
        }
    }

    @Override
    public int getCount() {
        return mListSendItems.size();
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
            myClass.mProgressText = (TextView)view.findViewById(R.id.progress_text);
            myClass.mResultImage = (ImageView)view.findViewById(R.id.result);

            view.setTag(myClass);
        } else {
            myClass = (ViewClass)view.getTag();
        }

        MyAppInfo theInfo = mListSendItems.get(position);
        showItemInfos(theInfo, myClass);

        return view;
    }

    private void showItemInfos(MyAppInfo theInfo, ViewClass myClass) {
        myClass.mAppName.setText(theInfo.mDisplayName);
        myClass.mFileSize.setText(theInfo.mFileSize);

        if (theInfo.mTransState == TransState.READY) {
            myClass.mProgressLinearLayout.setVisibility(View.GONE);
            myClass.mResultImage.setVisibility(View.INVISIBLE);
        } else if (theInfo.mTransState == TransState.TRANSING) {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setVisibility(View.INVISIBLE);
        } else {
            myClass.mProgressLinearLayout.setVisibility(View.VISIBLE);
            myClass.mResultImage.setVisibility(View.VISIBLE);
        }
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
        TRANSING,
        DONE,
    }

    private static class MyAppInfo {
        public String mDisplayName;
        public String mFullPath;
        public String mFileSize;
        public TransState mTransState;
    }
}
