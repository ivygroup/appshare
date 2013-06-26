package com.ivy.appshare.ui;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppsInfo {
    public static final int APP_INSTALLED = 0;
    public static final int APP_STORAGECARD = 1;

    public String appLabel;
    public Drawable appIcon;
    public Intent intent;
    public String packageName;
    public String sourceDir;
    public int type;
    public int versionCode;
    public String versionName;
    public boolean isSelected;
}