package com.devinxutal.tetris.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.devinxutal.tetris.cfg.Configuration;
import com.devinxutal.tetris.sound.SoundManager;
import com.devinxutal.tetris.util.BitmapUtil;
import com.devinxutal.tetris.util.PreferenceUtil;
import com.devinxutal.tetriscn.GoTetrisApplication;
import com.devinxutal.tetriscn.R;

public class SplashActivity extends BaseActivity {

	private View logo = null;
	private Handler handler = new Handler();

	private boolean initCompleted = false;
	private boolean animationCompleted = false;
	
	private  Runnable checker  =  new Runnable() {
		public void run() {
			if(initCompleted && animationCompleted){
				Log.v("SplashActivity", "checker: checked and completed");
				Intent i = new Intent(SplashActivity.this,
						MainActivity.class);
				SplashActivity.this.startActivity(i);
				SplashActivity.this.finish();
			}else{
				Log.v("SplashActivity", "checker: checked but not completed");
				handler.postDelayed(checker,500);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration.config()
				.setSharedPreferences(
						PreferenceManager
								.getDefaultSharedPreferences(getBaseContext()));
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.splash);
		this.logo = this.findViewById(R.id.perfect_games_logo);
		this.preferenceChanged();

	}

	@Override
	protected void onStart() {
		// start animation
		Animation ani = new AlphaAnimation(0f, 1f);
		ani.setDuration(3000);
		ani.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation ad) {
			}

			public void onAnimationRepeat(Animation ad) {
			}

			public void onAnimationEnd(Animation ad) {
				animationCompleted = true;
				
			}
		});
		logo.startAnimation(ani);
		// start init checker;
		
		handler.postDelayed(checker, 500);
		// init
		this.globalInit();
		this.initCompleted = true;
		super.onStart();
	}
	
	public void globalInit() {
		GoTetrisApplication.setBitmapUtil(BitmapUtil.get(this));

		SoundManager.init(this);
	}

	@Override
	protected void preferenceChanged() {
		PreferenceUtil.resetLocale(this, Configuration.config().getLanguage());
		
	}
}