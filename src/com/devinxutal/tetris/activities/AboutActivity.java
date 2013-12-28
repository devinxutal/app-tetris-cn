package com.devinxutal.tetris.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetriscn.R;

public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration.config()
				.setSharedPreferences(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext()));
		
		net.youmi.android.YoumiAdManager.getInstance(this).init("9a569a351928555b", "779567818294d49a", false); 
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.about);
	}

	@Override
	protected void preferenceChanged() {
		// TODO Auto-generated method stub
		
	}
	
}