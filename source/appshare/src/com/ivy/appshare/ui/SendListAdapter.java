package com.ivy.appshare.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ivy.appshare.engin.control.ImManager;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.ui.FileTransWidget.MyAppInfo;

public class SendListAdapter extends BaseAdapter {
    private ImManager mImManager;
    private Person mToPerson;
    private FileTransWidget mFileTransWidget;

    public SendListAdapter(Context context) {
        mFileTransWidget = new FileTransWidget(context, false);

        for (AppsInfo appinfo: NeedSendAppList.getInstance().mListAppInfo) {
            FileTransWidget.MyAppInfo info = new FileTransWidget.MyAppInfo();
            info.mDisplayName = appinfo.appLabel;
            info.mFullPath = appinfo.sourceDir;
            info.mAppIcon = appinfo.appIcon;

            {
                File file = new File(info.mFullPath);
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

            info.mTransState = FileTransWidget.TransState.READY;
            mFileTransWidget.addOneAppInfo(info);
        }
    }

    public void beginTranslate(ImManager imManager, Person toPerson) {
        mImManager = imManager;
        mToPerson = toPerson;
        List<MyAppInfo> listAppInfos = mFileTransWidget.getAllApps();

        for (MyAppInfo info: listAppInfos) {
            info.mTransState = FileTransWidget.TransState.READY;
            int id = mImManager.sendFile(mToPerson, null, info.mFullPath, FileType.FileType_App);
            info.mID = id;
        }
    }

    public void changeTransState_Begin(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.BEGIN, 0, 0);

    }

    public void changeTransState_Process(Person person, int fileID, long pos, long total) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.TRANSING, pos, total);
    }

    public void changeTransState_OK(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.OK, 0, 0);
    }

    public void changeTransState_Failed(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.FAILED, 0, 0);
    }

    public void changeTransState_TimeOut(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.TIMEOUT, 0, 0);
    }

    public boolean isCompleteTranslate() {
        List<MyAppInfo> listAppInfos = mFileTransWidget.getAllApps();
        for (MyAppInfo info: listAppInfos) {
            if (!info.mTransState.isComplete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCount() {
        return mFileTransWidget.getCount();
    }

    @Override
    public Object getItem(int arg0) {
        return mFileTransWidget.getItem(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return mFileTransWidget.getItemId(arg0);
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        return mFileTransWidget.getView(position, view, arg2);
    }
}
