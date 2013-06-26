package com.ivy.appshare.engin.im;

import com.ivy.appshare.engin.im.simpleimp.SimpleIm;

public class ImFactory {
	private static Im mSimpleIm = null;
	public static Im getSimpleIm() {
		if (mSimpleIm == null) {
			mSimpleIm = new SimpleIm();
		}

		return mSimpleIm;
	}
}
