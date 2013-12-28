package com.devinxutal.tetris.cfg;

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Configuration {
	private static Configuration config = new Configuration();

	private SharedPreferences preference;

	public static Configuration config() {
		return config;
	}

	private Configuration() {

	}

	public void setSharedPreferences(SharedPreferences p) {
		this.preference = p;
	}

	public static final int ACTION_DIRECT_DOWN = 1;
	public static final int ACTION_QUICK_DOWN = 2;
	public static final int ACTION_TURN = 3;

	public int getCenterButtonAction() {
		int defaultValue = ACTION_QUICK_DOWN;
		if (preference != null) {
			return Integer.valueOf(preference.getString("center_button_action",
					ACTION_QUICK_DOWN + ""));
		}
		return defaultValue;
	}

	public String getBlockStyle() {
		String defaultValue = "classic";
		if (preference != null) {
			return preference.getString("block_style", defaultValue);
		}
		return defaultValue;
	}

	public boolean isBackgroundMusicOn() {
		if (preference != null) {
			return preference.getBoolean("background_music", true);
		}
		return true;
	}

	public void setBackgroundMusicOn(boolean on) {
		if (preference != null) {
			Editor editor = preference.edit();
			editor.putBoolean("background_music", on);
			editor.commit();
		}
	}

	public boolean isSoundEffectsOn() {
		if (preference != null) {
			return preference.getBoolean("sound_effects", true);
		}
		return true;
	}

	public void setSoundEffectsOn(boolean on) {
		if (preference != null) {
			Editor editor = preference.edit();
			editor.putBoolean("sound_effects", on);
			editor.commit();
		}
	}

	public boolean isBlockShadowOn() {
		if (preference != null) {
			return preference.getBoolean("block_shadow", true);
		}
		return true;
	}

	public void setBlockShadowOn(boolean on) {
		if (preference != null) {
			Editor editor = preference.edit();
			editor.putBoolean("block_shadow", on);
			editor.commit();
		}
	}

	public boolean isSwapRotateHold() {
		if (preference != null) {
			return preference.getBoolean("swap_rotate_hold", false);
		}
		return false;
	}

	public boolean isSwapQuickDirect() {
		if (preference != null) {
			return preference.getBoolean("swap_quick_direct", false);
		}
		return false;
	}

	public int getStartLevel() {
		int defaultValue = 1;
		if (preference != null) {
			return Integer.valueOf(preference.getString("difficulty", "1"));
		}
		return defaultValue;
	}

	public Locale getLanguage() {
		if (preference != null) {
			String locale = preference.getString("language", "default");
			if (locale.equals("zh")) {
				return Locale.SIMPLIFIED_CHINESE;
			} else if (locale.equals("en")) {
				return Locale.ENGLISH;

			} else {
				return Locale.ENGLISH;
			}
		}
		return Locale.ENGLISH;
	}

	public String getScreenOrientation() {
		String defaultValue = "auto";
		if (preference != null) {
			return preference.getString("screen_orientation", defaultValue);
		}
		return defaultValue;
	}

	public boolean isDragMode() {
		if (preference != null) {
			return preference.getBoolean("enable_drag_mode", true);
		}
		return true;
	}

}
