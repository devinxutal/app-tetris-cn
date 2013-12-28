

package com.devinxutal.tetris.util;

import java.io.Serializable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.devinxutal.tetris.cfg.Constants;

public class VersionUtil {

	public static class VersionInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8387296657408645355L;

		public int solveCubeTrials = 0;
	}


	public static VersionInfo readVersionInfo(Activity activity) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		if (p == null) {
			return null;
		}
		VersionInfo info = new VersionInfo();
		info.solveCubeTrials = p
				.getInt(Constants.PREF_KEY_TRIALS_SOLVE_CUBE, 0);
		return info;
	}

	public static void writeVersionInfo(Activity activity, VersionInfo info) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		if (p == null) {
			return;
		}
		Editor editor = p.edit();
		editor.putInt(Constants.PREF_KEY_TRIALS_SOLVE_CUBE,
				info.solveCubeTrials);
		editor.commit();
	}


	//
	// public static VersionInfo readVersionInfo() {
	// File file = Environment.getExternalStorageDirectory();
	// File dir = new File(file, Constants.DATA_DIR);
	// if (!dir.exists()) {
	// dir.mkdirs();
	// }
	// File dataFile = new File(dir, Constants.VERSION_SAVING_FILE);
	//
	// if (!dataFile.exists()) {
	// return new VersionInfo();
	// }
	// try {
	// ObjectInputStream in = new ObjectInputStream(new FileInputStream(
	// dataFile));
	// VersionInfo info = (VersionInfo) in.readObject();
	// return info;
	// } catch (Exception e) {
	// return new VersionInfo();
	// }
	// }
	//
	// public static void writeVersionInfo(VersionInfo info) {
	// File file = Environment.getExternalStorageDirectory();
	// File dir = new File(file, Constants.DATA_DIR);
	// if (!dir.exists()) {
	// dir.mkdirs();
	// }
	// File dataFile = new File(dir, Constants.VERSION_SAVING_FILE);
	//
	// try {
	// if (!dataFile.exists()) {
	// dataFile.createNewFile();
	// }
	// ObjectOutputStream out = new ObjectOutputStream(
	// new FileOutputStream(dataFile));
	// out.writeObject(info);
	// out.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }


}
