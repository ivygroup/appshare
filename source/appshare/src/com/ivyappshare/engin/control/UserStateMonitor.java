package com.ivyappshare.engin.control;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ivyappshare.MyApplication;
import com.ivyappshare.engin.im.Im;
import com.ivyappshare.engin.im.Person;

public class UserStateMonitor {
    private static final String TAG = "UserStateMonitor";

    private ImService mImService;
    private String mPackageName;
    private BroadcastReceiver mScreenActionReceiver;

    public UserStateMonitor(ImService imService) {
        mImService = imService;

        mPackageName = MyApplication.getInstance().getPackageName();
        if (mPackageName == null) {
            mPackageName = "com.ivyappshare";
        }

        mScreenActionReceiver = new BroadcastReceiver() {   
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    mImService.changeUserState(Im.State_Screen_Off);
                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    mImService.changeUserState(Im.State_Idle);
                } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                    if (isTopActivity()) {
                        mImService.changeUserState(Im.State_Active);
                    } else {
                        mImService.changeUserState(Im.State_Idle);
                    }
                }
            }
        };

        registerScreenActionReceiver();
    }

    public void release() {
        unRegisterScreenActionReceiver();
    }

    private void registerScreenActionReceiver() {   
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        MyApplication.getInstance().registerReceiver(mScreenActionReceiver, filter);
    }

    private void unRegisterScreenActionReceiver() {
        if (mScreenActionReceiver != null) {
            MyApplication.getInstance().unregisterReceiver(mScreenActionReceiver);
            mScreenActionReceiver = null;
        }
    }

    public void onResumeMyActivity() {
        Person myself = LocalSetting.getInstance().getMySelf();
        if (myself.mState != Im.State_Active) {
            mImService.changeUserState(Im.State_Active);
        }
    }

    public void checkMyActive() {
        Person myself = LocalSetting.getInstance().getMySelf();
        boolean isTopActivity = isTopActivity();

        if (isTopActivity && myself.mState != Im.State_Active) {
            mImService.changeUserState(Im.State_Active);
        } else if (!isTopActivity && myself.mState != Im.State_Idle) {
            mImService.changeUserState(Im.State_Idle);
        }
    }

    //  Because this method need GET_TASKS permission, so we not use this method.
    //  <uses-permission android:name="android.permission.GET_TASKS" />
/*
     private boolean isTopActivity() {
        ActivityManager activityManager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo>  tasksInfo = activityManager.getRunningTasks(1);
        if(tasksInfo.size() > 0) {
            String topPackageName = tasksInfo.get(0).topActivity.getPackageName();
            Log.d(TAG, "top package name = " + topPackageName);
            if(mPackageName.equals(topPackageName)) {
                return true;
            }
        }
        return false;
    }*/

    private boolean isTopActivity() {
        ActivityManager activityManager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> listInfos = activityManager.getRunningAppProcesses();
        if(listInfos.size() == 0) return false;

        for(ActivityManager.RunningAppProcessInfo processInfo:listInfos) {
            /*
            if(processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Log.d(TAG, "importance = " + processInfo.importance + ", processName = " + processInfo.processName);
            }

            if (processInfo.processName.equals(mPackageName)) {
                Log.d(TAG, "importance = " + processInfo.importance + ", processName = " + processInfo.processName);
            }//*/

            if(processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && processInfo.processName.equals(mPackageName)) {
                return true;
            }
        }
        return false;
    }
}
