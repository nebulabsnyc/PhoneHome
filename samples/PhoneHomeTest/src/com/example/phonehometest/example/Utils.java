package com.example.phonehometest.example;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public final class Utils {
	// this points at localhost on whatever machine the meulator's running on; obviously change this to suit your needs
	static final String HTTP_BASE = "http://10.0.2.2";
	
	private Utils() {}
	
	public static class AndroidInfo {
		public final String model;
		public final int sdkVersion;
		public final int appVersion;
		
		private AndroidInfo(final String model, final int sdkVersion, final int appVersion) {
			this.model = model;
			this.sdkVersion = sdkVersion;
			this.appVersion = appVersion;
		}
	}
	
	public static AndroidInfo getAndroidInfo(Context context) {
		int versionCode = -1;
		try {
			PackageManager manager = context.getPackageManager();
			// FIXME point this at your own package!
			PackageInfo info = manager.getPackageInfo("com.example.phonehometest", 0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException nnf) {
			throw new RuntimeException("Couldn't get package versionCode!", nnf);
		}
		
		return new AndroidInfo(Build.MODEL, Build.VERSION.SDK_INT, versionCode);
	}
	
	static Header[] getAndroidHeaders(Context context) {
		// see backend example for how these are used to associate log events with users
		AndroidInfo androidInfo = getAndroidInfo(context);
		return new Header[] {
				new BasicHeader("X-Android-Model", androidInfo.model),
				new BasicHeader("X-Android-Sdk", Integer.toString(androidInfo.sdkVersion)),
				new BasicHeader("X-Android-AppVersion", Integer.toString(androidInfo.appVersion))
		};
	}
}
