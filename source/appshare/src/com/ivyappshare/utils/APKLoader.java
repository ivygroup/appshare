package com.ivyappshare.utils;

import java.io.File;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ivyappshare.MyApplication;
import com.ivyappshare.R;
import com.ivyappshare.engin.im.Im.FileType;
import com.ivyappshare.ui.AppsInfo;

public class APKLoader {
    private static final String TAG = "APKLoader";

    private List<AppsInfo> mListAppInfo;
    private Handler mMainHandler;
    private Map<String, Set<Integer>> mMapAppPackageVersion;
    private Map<String, AppsInfo> mMapPackageApp;
    private ImageLoader mImageLoader;
    private Collator mCollator;
    private SearchAsyncTask mTask;
    private Drawable mDefaultDrawable;
    private BaseAdapter mAdapter;
    private Context mContext;

    public APKLoader() {
    }

    public void init(Context context) {
    	mContext = context;
        mDefaultDrawable = mContext.getResources().getDrawable(R.drawable.ic_file_type_apk);

        mMapAppPackageVersion = new HashMap<String, Set<Integer>>();
        mMapPackageApp = new HashMap<String, AppsInfo>();
        mListAppInfo = new ArrayList<AppsInfo>();
        mCollator = Collator.getInstance(Locale.getDefault());

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                AppsInfo app = (AppsInfo)msg.obj;
                // if find a same version, dismiss it
                if (compareApkVersion(app) != 0) {
                	return;
                }
                if (app.packageName.compareToIgnoreCase(MyApplication.mPackageName) == 0) {
                    mListAppInfo.add(0, app);
                } else {
                	int nSize = mListAppInfo.size();
                	int nPos = 0;
                	for (;nPos<nSize; nPos++) {
                		AppsInfo temp = mListAppInfo.get(nPos);
                		if (temp.packageName.compareToIgnoreCase(MyApplication.mPackageName) == 0) {
                			continue;
                		}
	    				CollationKey key1 = mCollator.getCollationKey(app.appLabel);
	    				CollationKey key2 = mCollator.getCollationKey(temp.appLabel);
	    				if (key1.compareTo(key2) < 0) {
	    					break;
	    				}
                	}
                	mListAppInfo.add(nPos, app);
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        };

        mTask = new SearchAsyncTask();
        mTask.execute(0);
    }

    public void unInit() {
		if (mImageLoader != null) {
			mImageLoader.unInit();
			mImageLoader = null;
		}
		if (mTask != null) {
			mTask.quit();
			mTask.cancel(true);
			mTask = null;
		}
    }

    public void setAdapter(BaseAdapter adapter) {
    	mAdapter = adapter;
    }

    public List<AppsInfo> getAppList() {
    	return mListAppInfo;
    }

    private int compareApkVersion(AppsInfo app) {
        if (mMapAppPackageVersion.containsKey(app.packageName)) {
            Set<Integer> set = mMapAppPackageVersion.get(app.packageName);
            if (set.contains(app.versionCode)) {
                return -1;
            }
            set.add(app.versionCode);
            app.appLabel += "_" + app.versionName;

            // once find another version, update the label
            if (mMapPackageApp.containsKey(app.packageName)) {
            	AppsInfo oldApp = mMapPackageApp.get(app.packageName);
            	oldApp.appLabel += "_" + oldApp.versionName;
            	mMapPackageApp.remove(app.packageName);
            }
        } else {
            Set<Integer> set = new HashSet<Integer>();
            set.add(app.versionCode);
            mMapAppPackageVersion.put(app.packageName, set);

            mMapPackageApp.put(app.packageName, app);
        }
        return 0;
    }

    public class SearchAsyncTask extends AsyncTask<Integer, Void, Integer> 
        implements ImageLoader.LoadFinishListener{

    	private boolean mQuiting = false;
        private void readFile(final File[] files){
        	if (mQuiting) {
        		return;
        	}
            if(files!=null && files.length>0){
                for(int i=0;i<files.length;i++) {
                	if (mQuiting) {
                		return;
                	}
                    if (files[i].isDirectory()) {
                        readFile(files[i].listFiles());
                    } else {
                        String path = files[i].getPath();
                        String suffix = path.substring(path.lastIndexOf('.')+1);
                        if (suffix.compareToIgnoreCase("APK") == 0) {
                        	if (mImageLoader != null) {
                                mImageLoader.loadImage(null, path, 0, FileType.FileType_App);
                        	}
                        }
                    }
                }
            }
        }

        private void queryInstalledAppInfo() {
            PackageManager pm = MyApplication.getInstance().getPackageManager();

            List<PackageInfo> listPackages = 
                    pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

            for (PackageInfo packageinfo : listPackages) {
            	if (mQuiting) {
            		return;
            	}
                ApplicationInfo app = packageinfo.applicationInfo;
                if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    AppsInfo appInfo = new AppsInfo();
                    appInfo.appLabel = (String)app.loadLabel(pm);
                    appInfo.appIcon = app.loadIcon(pm);
                    appInfo.packageName = app.packageName;
                    appInfo.sourceDir = app.sourceDir;
                    appInfo.type = AppsInfo.APP_INSTALLED;
                    appInfo.versionCode = packageinfo.versionCode;
                    appInfo.versionName = packageinfo.versionName;
                    appInfo.isSelected = false;
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(AppsInfo.APP_INSTALLED, appInfo));
                }
            }
        }

        public void quit() {
        	mQuiting = true;
            Log.d(TAG, "Quit SearchTask");
        }
        public SearchAsyncTask() {
        	mQuiting = false;
            mImageLoader = new ImageLoader(this);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            // first, find from packet manager
        	queryInstalledAppInfo();

            // second, find from files
            readFile(Environment.getExternalStorageDirectory().listFiles());
            return 0;
        }
        
        @Override  
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Search Over");
        }

        @Override
        public boolean onAPKLoadFinished(ImageView view, Drawable drawable,
                String name, String packageName, int versionCode, String versionName, String path) {
            AppsInfo appInfo = new AppsInfo();
            if (drawable != null) {
                appInfo.appLabel = name;
                appInfo.appIcon = drawable;
                appInfo.packageName = packageName;
            } else {
                appInfo.appIcon = mDefaultDrawable;
                appInfo.appLabel = path.substring(path.lastIndexOf('/')+1);
                appInfo.packageName = "";
            }
            appInfo.sourceDir = path;
            appInfo.type = AppsInfo.APP_STORAGECARD;
            appInfo.versionCode = versionCode;
            appInfo.versionName = versionName;
            appInfo.isSelected = false;
            mMainHandler.sendMessage(mMainHandler.obtainMessage(AppsInfo.APP_STORAGECARD, appInfo));
            return true;
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
}