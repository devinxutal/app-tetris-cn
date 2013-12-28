package com.devinxutal.tetris.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

public class PreferenceUtil {

	public static String readPlayerName(Activity activity) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		return p.getString("player_name", "");
	}

	public static void writePlayerName(Activity activity, String name) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		if (p == null) {
			return;
		}
		Editor editor = p.edit();
		editor.putString("player_name", name);
		editor.commit();
	}

	public static String getShowUpgradeNoticeStr(Activity activity) {
		int versionCode = 0;
		try {

			versionCode = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), 0).versionCode;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "show_upgrade_notice_" + versionCode;
	}

	public static boolean canShowUpgradeNotice(Activity activity) {

		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		return p.getBoolean(getShowUpgradeNoticeStr(activity), true);
	}

	public static void setShowUpgradeNotice(Activity activity, boolean can) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		Editor editor = p.edit();
		editor.putBoolean(getShowUpgradeNoticeStr(activity), can);
		editor.commit();
	}

	//
	// public static CubeState readCubeState(Activity activity, boolean timed) {
	// SharedPreferences p = PreferenceManager
	// .getDefaultSharedPreferences(activity.getBaseContext());
	// Log.v("PreferenceUtil", "get cube state: "
	// + p.getString("saved_cube_state_" + timed, "no").length() + " "
	// + p.getString("saved_cube_state_" + timed, "no"));
	// String object = p.getString("saved_cube_state_" + timed, null);
	// return (CubeState) stringToObject(object);
	// }

	// public static void writeCubeState(Activity activity, CubeState state,
	// boolean timed) {
	// SharedPreferences p = PreferenceManager
	// .getDefaultSharedPreferences(activity.getBaseContext());
	// Log.v("PreferenceUtil","write cube state: "+objectToString(state).length());
	// if (p == null) {
	// return;
	// }
	// Editor editor = p.edit();
	// editor.putString("saved_cube_state_" + timed, objectToString(state));
	// editor.commit();
	// }

	public static String objectToString(Serializable object) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(object);
			out.close();
			return new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object stringToObject(String encodedObject) {
		try {
			return new ObjectInputStream(new ByteArrayInputStream(encodedObject
					.getBytes())).readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void resetLocale(Activity activity, Locale locale) {
		Resources res = activity.getResources();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = locale;
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
	}
}
