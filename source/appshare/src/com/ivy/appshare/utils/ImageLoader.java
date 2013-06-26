package com.ivy.appshare.utils;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.im.Im.FileType;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader implements Callback{
	private static final String TAG = ImageLoader.class.getSimpleName();

	private final static ConcurrentHashMap<String, ImageCache> mMapImageCache = 
			new ConcurrentHashMap<String, ImageCache>();

	private final ConcurrentHashMap<String, FileInfo> mRequests = 
			new ConcurrentHashMap<String, FileInfo>();

    private static final int MESSAGE_REQUEST_LOADING 	= 1000;
    private static final int MESSAGE_IMAGE_LOADED 		= 1002;

    private boolean mLoadingRequested = false;

    public class FileInfo {
    	public String mFilePath;
    	public int mId;
    	public FileType mFileType;
    	public ImageView mView;
    	
    	FileInfo(String path, int id, FileType type, ImageView view) {
    		mFilePath = path;
    		mId = id;
    		mFileType = type;
    		mView = view;
    	}
    }

    private LoadFinishListener mLoadListener;

    private final Handler mMainThreadHandler = new Handler(this);
    private LoaderThread mLoaderThread;

    public interface LoadFinishListener {
        public boolean onAPKLoadFinished(ImageView view, Drawable drawable, String name, 
        		String packageName, int versionCode, String versionName, String path);

        public boolean onDrawableLoadFinished(ImageView view, Drawable drawable);
        public boolean onBitmapLoadFinished(ImageView view, Bitmap bitmap);
    }

    public ImageLoader(LoadFinishListener listener) {
    	mLoadListener = listener;
		Log.d(TAG, "New ImageLoader");
    }

    public void unInit() {
    	if (mLoaderThread != null) {
    		Log.d(TAG, "Quit Loader Thread");
    		mLoaderThread.quit();
    		mLoaderThread = null;
    	}
    }

    public boolean loadImage(ImageView view, String path, int id, FileType type) {
    	Log.d(TAG, "Load Image " + path);
        int cached = loadCachedImage(view, path, type);
        if (cached == 0) {
        	mRequests.remove(path);
        } else {
        	FileInfo file = new FileInfo(path, id, type, view);
        	mRequests.put(path, file);
            requestLoading();
        }
        return cached == 0;
    }

    private int loadCachedImage(ImageView view, String path, FileType type) {
    	ImageCache cache = mMapImageCache.get(path);
        if (cache == null) {
        	cache = new ImageCache();
        	mMapImageCache.put(path, cache);
        } else if (cache.mState == ImageCache.STATE_SUCCESS) {
            if (cache.isNull()) {
            	Log.d(TAG, "Image " + path + " cache is null");
            	cache.mState = ImageCache.STATE_UNINITED;
                return 1;
            }
            if (!cache.isImageNull()) {
            	Log.d(TAG, "Image " + path + " in cache");
            	if (mLoadListener != null) {
            		boolean ret = false;
            		if (type.ordinal() == FileType.FileType_App.ordinal()) {
            			ret = mLoadListener.onAPKLoadFinished(view, 
            					cache.mDrawable.get(), cache.mName, cache.mPackageName, 
            					cache.mVersionCode, cache.mVersionName, path);
            		} else {
            			if (cache.mType == ImageCache.CHAHE_TYPE_DRAWABLE) {
                			ret = mLoadListener.onDrawableLoadFinished(view, cache.mDrawable.get());
            			} else {
            				ret = mLoadListener.onBitmapLoadFinished(view, cache.mBitmap.get());
            			}
            		}
            		if (ret) {
            			return 0;
            		}
            	}
                cache.setImageView(view);
                return 0;
            }
        }
        cache.mState = ImageCache.STATE_UNINITED;
        return -1;
    }

    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

	@Override
	public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_REQUEST_LOADING: {
            mLoadingRequested = false;
            if (mLoaderThread == null) {
                mLoaderThread = new LoaderThread();
                mLoaderThread.start();
            }

            mLoaderThread.requestLoading();
            return true;
        }

        case MESSAGE_IMAGE_LOADED: {
        	processLoadedImage();
            return true;
        }
    }
		return false;
	}

    private void processLoadedImage() {
        Iterator<String> iterator = mRequests.keySet().iterator();
        while (iterator.hasNext()) {
            String path = iterator.next();
            FileInfo file = mRequests.get(path);
            int cached = loadCachedImage(file.mView, file.mFilePath, file.mFileType);
            if (cached >= 0) {
                iterator.remove();
            }
        }
        if (!mRequests.isEmpty()) {
            requestLoading();
        }
    }

    private class LoaderThread extends HandlerThread implements Callback {
        private Handler mLoaderThreadHandler;
        public LoaderThread() {
            super("LoadThread");
        }

        public void requestLoading() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }

        public boolean handleMessage(Message msg) {
            Iterator<FileInfo> iterator = mRequests.values().iterator();

            while (iterator.hasNext()) {
            	FileInfo file = iterator.next();
                ImageCache cache = mMapImageCache.get(file.mFilePath);

                if (cache != null && cache.mState == ImageCache.STATE_UNINITED) {
                	Log.d(TAG, "Get " + file.mFilePath + " Info");
                	cache.mState = ImageCache.STATE_LOADING;

                    switch (file.mFileType) {
                        case FileType_App:
                            PackageManager pm = MyApplication.getInstance().getPackageManager();
                            PackageInfo info = pm.getPackageArchiveInfo(file.mFilePath, PackageManager.GET_ACTIVITIES);
                            if (info != null) {
                                ApplicationInfo appInfo = info.applicationInfo;
                                appInfo.sourceDir = file.mFilePath;
                                appInfo.publicSourceDir = file.mFilePath;
                                try {
                		            cache.setResource(ImageCache.CHAHE_TYPE_DRAWABLE, appInfo.loadIcon(pm));
                		            cache.setAPKResource(String.valueOf(appInfo.loadLabel(pm)), 
                		            		appInfo.packageName, info.versionCode, info.versionName);
                                    mMapImageCache.put(file.mFilePath, cache);
                                } catch (OutOfMemoryError e) {
                                    e.printStackTrace();
                                }
                            } else {
                            	Log.d(TAG, "PackageInfo is null");
                            	cache.setResource(ImageCache.CHAHE_TYPE_DRAWABLE, null);
                            }
                            break;
                        case FileType_Picture:
                        	cache.setResource(ImageCache.CACHE_TYPE_BITMAP, CommonUtils.DecodeBitmap(file.mFilePath, 256*256));
                        	break;
                    }
                	cache.mState = ImageCache.STATE_SUCCESS;
                    mMapImageCache.put(file.mFilePath, cache);
                }
            }
            mMainThreadHandler.sendEmptyMessage(MESSAGE_IMAGE_LOADED);
            return true;
        }

//        private static final int MICRO_KIND = 3;

//        private Bitmap getImageThumbnail(long id) {
//            return Images.Thumbnails.getThumbnail(mContext.getContentResolver(), id, MICRO_KIND, null);
//        }
//
//        private Bitmap getVideoThumbnail(long id) {
//            return Video.Thumbnails.getThumbnail(mContext.getContentResolver(), id, MICRO_KIND, null);
//        }
    }
}


