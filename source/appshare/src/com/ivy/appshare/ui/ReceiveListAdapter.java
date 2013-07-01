package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.ivy.appshare.engin.control.ImManager;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.utils.ImageLoader;

public class ReceiveListAdapter extends BaseAdapter implements OnClickListener, ImageLoader.LoadFinishListener {
    private Context mContext;
    List<MyAppInfo> mListReceiveItems;
    ImManager mImManager;
    Person mFromPerson;
    ImageLoader mImageLoader;

    public ReceiveListAdapter(Context context) {
        mContext = context;
        mListReceiveItems = new ArrayList<ReceiveListAdapter.MyAppInfo>();

        mImageLoader = new ImageLoader(this);
    }

    public void changeTransState_Begin(Person person, int fileID, String fileFullPath) {
        MyAppInfo info = new MyAppInfo();
        info.mFileIDForListener = fileID;
        info.mFullPath = fileFullPath;
        info.mDisplayName = fileFullPath.substring(fileFullPath.lastIndexOf("/")+1);
        info.mPos = 0;
        info.mTotal = 100;
        info.mTransState = TransState.BEGIN;

        mListReceiveItems.add(info);
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
        return mListReceiveItems.size();
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

            myClass.mAppIcon = (ImageView)view.findViewById(R.id.appicon);
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
        synchronized (mListReceiveItems) {
            theInfo = mListReceiveItems.get(position);
        }
        showItemInfos(theInfo, myClass);

        myClass.mAppName.setText(mListReceiveItems.get(position).mDisplayName);
        myClass.mAppIcon.setTag(myClass);
        mImageLoader.loadImage(myClass.mAppIcon, mListReceiveItems.get(position).mFullPath, position, FileType.FileType_App);

        return view;
    }

    private void showItemInfos(MyAppInfo theInfo, ViewClass myClass) {
        myClass.mAppName.setText(theInfo.mDisplayName);
        myClass.mFileSize.setText(theInfo.mFileSize);

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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

    private static class ViewClass {
    	public ImageView mAppIcon;
        public TextView mAppName;
        public TextView mFileSize;
        public LinearLayout mProgressLinearLayout;
        public ProgressBar mProgressBar;
        public TextView mProgressText;
        public ImageView mResultImage;

    }

    private MyAppInfo getItemById(int id) {
        synchronized (mListReceiveItems) {
            for (MyAppInfo info: mListReceiveItems) {
                if (info.mFileIDForListener == id) {
                    return info;
                }
            }
        }
        return null;
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
        public String mDisplayName;
        public String mFullPath;
        public String mFileSize;
        public int mFileIDForListener;

        public TransState mTransState;
        public long mPos;
        public long mTotal;
    }

	@Override
	public boolean onAPKLoadFinished(ImageView view, Drawable drawable,
			String name, String packageName, int versionCode,
			String versionName, String path) {
		ViewClass myClass = (ViewClass)view.getTag();
		myClass.mAppName.setText(name);
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
}
