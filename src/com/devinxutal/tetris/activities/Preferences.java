package com.devinxutal.tetris.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.util.PreferenceUtil;
import com.devinxutal.tetriscn.R;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Configuration.config()
				.setSharedPreferences(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext()));
		
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // (NEW)
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());

		super.onCreate(savedInstanceState);
		int resid = this.getIntent().getIntExtra("pref_res", R.xml.preferences);
		addPreferencesFromResource(resid);
		setScreenOrientation();
	}

	private void setScreenOrientation() {
		String att = Configuration.config().getScreenOrientation();
		if (att.equals("auto")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else if (att.equals("portrait")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (att.equals("landscape")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}