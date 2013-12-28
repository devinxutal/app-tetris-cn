package com.devinxutal.tetris.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class HelpInfoUtil {
	public static class HelpInfo {
		public boolean cubeSolverHelpInfoStep1FirstShow = true;
		public boolean cubeSolverHelpInfoStep2FirstShow = true;
		public boolean cubeSolverHelpInfoStep3FirstShow = true;
	}

	public static HelpInfo readHelpInfo(Activity activity) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		HelpInfo info = new HelpInfo();
		info.cubeSolverHelpInfoStep1FirstShow = p.getBoolean(
				"cube_solver_help_info_step_1_first_show", true);
		info.cubeSolverHelpInfoStep2FirstShow = p.getBoolean(
				"cube_solver_help_info_step_2_first_show", true);
		info.cubeSolverHelpInfoStep3FirstShow = p.getBoolean(
				"cube_solver_help_info_step_3_first_show", true);
		return info;
	}

	public static void writeHelpInfo(Activity activity, HelpInfo info) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		if (p == null) {
			return;
		}
		Editor editor = p.edit();
		editor.putBoolean("cube_solver_help_info_step_1_first_show",
				info.cubeSolverHelpInfoStep1FirstShow);

		editor.putBoolean("cube_solver_help_info_step_2_first_show",
				info.cubeSolverHelpInfoStep2FirstShow);

		editor.putBoolean("cube_solver_help_info_step_3_first_show",
				info.cubeSolverHelpInfoStep3FirstShow);
		editor.commit();
	}

}
