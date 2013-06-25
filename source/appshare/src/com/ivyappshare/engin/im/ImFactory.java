package com.ivyappshare.engin.im;

import com.ivyappshare.engin.im.simpleimp.SimpleIm;

public class ImFactory {
	private static Im mSimpleIm = null;
	public static Im getSimpleIm() {
		if (mSimpleIm == null) {
			mSimpleIm = new SimpleIm();
		}

		return mSimpleIm;
	}
}
