package com.ivy.appshare;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {
    private static MyApplication gInstance = null;
    public static final String mPackageName = "com.ivy.appshare";
    
	private List<String> mFilterShareSSID = new ArrayList<String>();
    
    public static MyApplication getInstance() {
        return gInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gInstance = this;
    }
    
	public boolean findIsFlitered(String fliterData) {
		for (Iterator<String> sData = mFilterShareSSID.iterator(); sData
				.hasNext();) {
			if (fliterData.equals(sData.next()))
				return true;
		}
		return false;
	}
	
	public void addFilterData(String filterData) {
		mFilterShareSSID.add(filterData);
	}

}
