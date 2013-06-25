package com.ivyappshare.utils;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageCache {
	public SoftReference<Drawable> mDrawable;
	public SoftReference<Bitmap> mBitmap;
	public String mPackageName;
	public String mName;
	public int mVersionCode;
	public String mVersionName;
	public int mType;
	public int mState;

	public static final int CACHE_TYPE_BITMAP = 0;
	public static final int CHAHE_TYPE_DRAWABLE = 1;

    public static final int STATE_UNINITED 	= 0;
    public static final int STATE_LOADING 	= 1;
    public static final int STATE_SUCCESS 	= 2;

    public ImageCache() {
    	mState = STATE_UNINITED;
    }

    public void setResource(int type, Object image) {
    	mType = type;
    	if (type == CACHE_TYPE_BITMAP) {
    		if (image != null) {
    			mBitmap = new SoftReference<Bitmap>((Bitmap) image);
    		} else {
    			mBitmap = null;
    		}
    	} else {
    		if (image != null) {
    			mDrawable = new SoftReference<Drawable>((Drawable) image);
    		} else {
    			mDrawable = null;
    		}
    	}
    }

    public void setAPKResource(String name, String PackageName, int versionCode, String versionName) {
		mName = name;
		mPackageName = PackageName;
		mVersionCode = versionCode;
		mVersionName = versionName;
    }

    public boolean setImageView(ImageView view) {
    	if (mType == CACHE_TYPE_BITMAP) {
    		view.setImageBitmap(mBitmap.get());
    	} else {
    		view.setImageDrawable(mDrawable.get());
    	}
    	return true;
    }

    public boolean isImageNull() {
    	if (mType == CACHE_TYPE_BITMAP) {
    		if (mBitmap.get() == null) {
    			return true;
    		}
    	} else {
    		if (mDrawable.get() == null) {
    			return true;
    		}
    	}
    	return false;
    }

    public boolean isNull() {
    	if (mType == CACHE_TYPE_BITMAP) {
    		if (mBitmap == null) {
    			return true;
    		}
    	} else {
    		if (mDrawable == null) {
    			return true;
    		}
    	}
    	return false;
    }
}