package com.ivy.appshare.ui;

import java.io.File;
import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ivy.appshare.engin.control.ImManager;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.utils.ImageLoader;
import com.ivy.appshare.widget.FileTransWidget;

public class ReceiveListAdapter extends BaseAdapter implements ImageLoader.LoadFinishListener {
    private static final String TAG = "ReceiveListAdapter";

    private ImManager mImManager;
    private Person mFromPerson;
    private FileTransWidget mFileTransWidget;
    private ImageLoader mImageLoader;

    public ReceiveListAdapter(Context context) {
        mFileTransWidget = new FileTransWidget(context, true);
        mImageLoader = new ImageLoader(this);
    }

    public void changeTransState_Begin(Person person, int fileID, String fileFullPath) {
        FileTransWidget.MyAppInfo info = new FileTransWidget.MyAppInfo();
        info.mID = fileID;
        info.mFullPath = fileFullPath;
        info.mDisplayName = fileFullPath.substring(fileFullPath.lastIndexOf("/")+1);
        info.mAppIcon = null;
        info.mPos = 0;
        info.mTotal = 100;
        info.mTransState = FileTransWidget.TransState.BEGIN;

        mFileTransWidget.addOneAppInfo(info);
    }

    public void changeTransState_Process(Person person, int fileID, long pos, long total) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.TRANSING, pos, total);
        FileTransWidget.MyAppInfo appInfo = mFileTransWidget.getItemById(fileID);
        if (appInfo == null) {
            // Log.e(TAG, "fileID = " + fileID);
        } else {
            appInfo.mFileSize = getFileSizeByFormat(pos);
        }
    }

    public void changeTransState_OK(Person person, int fileID) {
        FileTransWidget.MyAppInfo appInfo = mFileTransWidget.getItemById(fileID);
        if (appInfo == null) {
            Log.e(TAG, "can't find this process.");
        } else {
            mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.OK, 0, 0);
            mImageLoader.loadImage(null, appInfo.mFullPath, appInfo.mID, FileType.FileType_App);
            File file = new File(appInfo.mFullPath);
            if (file.exists()) {
                appInfo.mFileSize = getFileSizeByFormat(file.length());
            }
        }
    }

    public void changeTransState_Failed(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.FAILED, 0, 0);
    }

    public void changeTransState_TimeOut(Person person, int fileID) {
        mFileTransWidget.changeAppState(fileID, FileTransWidget.TransState.TIMEOUT, 0, 0);
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

	@Override
	public boolean onAPKLoadFinished(ImageView view, Drawable drawable,
			String name, String packageName, int versionCode,
			String versionName, String path) {
	    FileTransWidget.MyAppInfo appInfo = mFileTransWidget.getItemByPath(path);
	    appInfo.mAppIcon = drawable;
	    appInfo.mDisplayName = name;
	    notifyDataSetChanged();
		return false;
	}

	@Override
	public boolean onDrawableLoadFinished(ImageView view, Drawable drawable) {
		return false;
	}

	@Override
	public boolean onBitmapLoadFinished(ImageView view, Bitmap bitmap) {
		return false;
	}


	private String getFileSizeByFormat(long filesize) {
	    String str;
	    DecimalFormat df= new DecimalFormat("#.##");   
	    if (filesize < 1024) {
	        str = filesize + " byte";
	    } else if (filesize < 1024 *1024) {
	        double tmp = (double)filesize/1024;
	        str = df.format(tmp) + " KB";
	    } else {
	        double tmp = (double)filesize/1024/1024 ;
	        str = df.format(tmp) + " MB";
	    }
	    return str;
	}
}
